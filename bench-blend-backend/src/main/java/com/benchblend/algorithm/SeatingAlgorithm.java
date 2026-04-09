package com.benchblend.algorithm;

import com.benchblend.model.*;
import com.benchblend.repository.ClassBlockHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatingAlgorithm {

    private final ClassBlockHistoryRepository classBlockHistoryRepository;

    public List<SeatingArrangement> generate(
            List<ExamSchedule> schedules,
            List<RoomBlock> blocks,
            ExamSession session,
            LocalDate examDate,
            String seatingMode) {

        boolean isDouble = "DOUBLE".equalsIgnoreCase(seatingMode);
        List<ExamSchedule> sorted = sortSubjects(schedules);
        Map<String, Set<Integer>> usedBlocksPerClass = getUsedBlocksPerClass(session.getId(), sorted);
        List<BlockSlot> blockSlots = blocks.stream()
                .map(b -> new BlockSlot(b, b.getStrength()))
                .collect(Collectors.toList());

        if (isDouble) {
            return generateDouble(sorted, blockSlots, session, examDate, usedBlocksPerClass);
        } else {
            return generateSingle(sorted, blockSlots, session, examDate, usedBlocksPerClass);
        }
    }

    // ===================== SINGLE MODE =====================
    private List<SeatingArrangement> generateSingle(
            List<ExamSchedule> schedules,
            List<BlockSlot> blockSlots,
            ExamSession session,
            LocalDate examDate,
            Map<String, Set<Integer>> usedBlocksPerClass) {

        List<SeatingArrangement> result = new ArrayList<>();
        int blockIndex = 0;

        for (ExamSchedule schedule : schedules) {
            int remaining = schedule.getStudentCount();

            while (remaining > 0 && blockIndex < blockSlots.size()) {
                BlockSlot slot = blockSlots.get(blockIndex);

                if (wasBlockUsedForClass(usedBlocksPerClass, schedule.getClassName(), slot.block.getBlockNo())) {
                    blockIndex++;
                    continue;
                }

                int canFit = slot.remaining;
                int toPlace = Math.min(remaining, canFit);

                result.add(buildArrangement(session, examDate, slot.block,
                        schedule, toPlace, "SINGLE", null));

                slot.remaining -= toPlace;
                remaining -= toPlace;

                if (slot.remaining == 0) blockIndex++;
            }

            if (remaining > 0) {
                log.warn("Could not seat all students for subject: {} - {} students unseated",
                        schedule.getSubjectCode(), remaining);
            }
        }

        return result;
    }

    // ===================== DOUBLE MODE =====================
    private List<SeatingArrangement> generateDouble(
            List<ExamSchedule> schedules,
            List<BlockSlot> blockSlots,
            ExamSession session,
            LocalDate examDate,
            Map<String, Set<Integer>> usedBlocksPerClass) {

        List<SeatingArrangement> result = new ArrayList<>();

        // Track what subject is assigned to each side of each block
        // Key: "blockNo-L" or "blockNo-R", Value: subjectCode
        Map<String, String> blockSideAssignment = new HashMap<>();

        // Remaining capacity per side per block
        // Order: 1-L, 1-R, 2-L, 2-R ... so blocks are filled together
        Map<String, Integer> sideCapacity = new LinkedHashMap<>();
        Map<String, RoomBlock> sideBlockMap = new LinkedHashMap<>();

        for (BlockSlot bs : blockSlots) {
            String lKey = bs.block.getBlockNo() + "-L";
            String rKey = bs.block.getBlockNo() + "-R";
            sideCapacity.put(lKey, bs.block.getStrength());
            sideCapacity.put(rKey, bs.block.getStrength());
            sideBlockMap.put(lKey, bs.block);
            sideBlockMap.put(rKey, bs.block);
        }

        List<String> allSideKeys = new ArrayList<>(sideCapacity.keySet());

        for (ExamSchedule schedule : schedules) {
            int remaining = schedule.getStudentCount();

            for (int i = 0; i < allSideKeys.size() && remaining > 0; i++) {
                String key = allSideKeys.get(i);
                RoomBlock block = sideBlockMap.get(key);
                String side = key.endsWith("-L") ? "L" : "R";
                String otherSideKey = block.getBlockNo() + "-" + (side.equals("L") ? "R" : "L");

                // Skip if no capacity left on this side
                int capacity = sideCapacity.get(key);
                if (capacity == 0) continue;

                // Skip if block was already used for this class in this session
                if (wasBlockUsedForClass(usedBlocksPerClass, schedule.getClassName(), block.getBlockNo())) {
                    continue;
                }

                // Skip if this side is already assigned to a DIFFERENT subject
                String assignedSubject = blockSideAssignment.get(key);
                if (assignedSubject != null && !assignedSubject.equals(schedule.getSubjectCode())) {
                    continue;
                }

                // Skip if OTHER side of same block already has the SAME subject
                String otherSideSubject = blockSideAssignment.get(otherSideKey);
                if (schedule.getSubjectCode().equals(otherSideSubject)) {
                    continue;
                }

                int toPlace = Math.min(remaining, capacity);

                result.add(buildArrangement(session, examDate, block,
                        schedule, toPlace, "DOUBLE", side));

                blockSideAssignment.put(key, schedule.getSubjectCode());
                sideCapacity.put(key, capacity - toPlace);
                remaining -= toPlace;
            }

            if (remaining > 0) {
                log.warn("Could not seat all students for subject: {} - {} students unseated",
                        schedule.getSubjectCode(), remaining);
            }
        }

        return result;
    }

    // ===================== SORTING =====================
    private List<ExamSchedule> sortSubjects(List<ExamSchedule> schedules) {
        Map<String, List<ExamSchedule>> byTimeSlot = schedules.stream()
                .collect(Collectors.groupingBy(ExamSchedule::getTimeSlot));

        List<ExamSchedule> sorted = new ArrayList<>();
        byTimeSlot.entrySet().stream()
                .sorted(Map.Entry.<String, List<ExamSchedule>>comparingByValue(
                        Comparator.comparingInt(list -> -list.stream()
                                .mapToInt(ExamSchedule::getStudentCount).sum())))
                .forEach(entry -> {
                    entry.getValue().sort(Comparator.comparingInt(ExamSchedule::getStudentCount).reversed());
                    sorted.addAll(entry.getValue());
                });

        return sorted;
    }

    // ===================== HISTORY / ROTATION =====================
    private Map<String, Set<Integer>> getUsedBlocksPerClass(Long sessionId, List<ExamSchedule> schedules) {
        Map<String, Set<Integer>> result = new HashMap<>();
        Set<String> classNames = schedules.stream()
                .map(ExamSchedule::getClassName)
                .collect(Collectors.toSet());

        for (String className : classNames) {
            List<ClassBlockHistory> history = classBlockHistoryRepository
                    .findBySessionIdAndClassName(sessionId, className);
            Set<Integer> usedBlocks = history.stream()
                    .map(ClassBlockHistory::getBlockNo)
                    .collect(Collectors.toSet());
            result.put(className, usedBlocks);
        }

        return result;
    }

    private boolean wasBlockUsedForClass(Map<String, Set<Integer>> usedBlocksPerClass,
                                          String className, Integer blockNo) {
        Set<Integer> used = usedBlocksPerClass.getOrDefault(className, Collections.emptySet());
        return used.contains(blockNo);
    }

    // ===================== BUILDERS =====================
    private SeatingArrangement buildArrangement(ExamSession session, LocalDate examDate,
                                                 RoomBlock block, ExamSchedule schedule,
                                                 int benchesUsed, String mode, String side) {
        return SeatingArrangement.builder()
                .session(session)
                .examDate(examDate)
                .blockNo(block.getBlockNo())
                .roomNo(block.getRoomNo())
                .strength(block.getStrength())
                .className(schedule.getClassName())
                .timeSlot(schedule.getTimeSlot())
                .subjectCode(schedule.getSubjectCode())
                .semester(schedule.getSemester())
                .subjectName(schedule.getSubjectName())
                .benchesUsed(benchesUsed)
                .seatingMode(mode)
                .side(side)
                .build();
    }

    // ===================== INNER CLASSES =====================
    static class BlockSlot {
        RoomBlock block;
        int remaining;

        BlockSlot(RoomBlock block, int remaining) {
            this.block = block;
            this.remaining = remaining;
        }
    }
}