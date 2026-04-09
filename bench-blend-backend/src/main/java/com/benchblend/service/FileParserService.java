package com.benchblend.service;

import com.benchblend.model.ExamSchedule;
import com.benchblend.model.ExamSession;
import com.benchblend.model.RoomBlock;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FileParserService {

    public List<ExamSchedule> parseExamSchedule(MultipartFile file,
                                                  ExamSession session,
                                                  LocalDate examDate) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.endsWith(".csv")) {
            return parseExamScheduleCsv(file, session, examDate);
        } else {
            return parseExamScheduleExcel(file, session, examDate);
        }
    }

    public List<RoomBlock> parseRoomSkeleton(MultipartFile file,
                                              ExamSession session,
                                              LocalDate examDate) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.endsWith(".csv")) {
            return parseRoomSkeletonCsv(file, session, examDate);
        } else {
            return parseRoomSkeletonExcel(file, session, examDate);
        }
    }

    // ===== EXAM SCHEDULE CSV =====
    private List<ExamSchedule> parseExamScheduleCsv(MultipartFile file,
                                                      ExamSession session,
                                                      LocalDate examDate) throws Exception {
        List<ExamSchedule> schedules = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        boolean firstLine = true;

        while ((line = reader.readLine()) != null) {
            if (firstLine) { firstLine = false; continue; }
            String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (cols.length < 7) continue;

            schedules.add(ExamSchedule.builder()
                .session(session)
                .examDate(examDate)
                .className(clean(cols[0]))
                .timeSlot(clean(cols[1]))
                .subjectCode(clean(cols[2]))
                .semester(clean(cols[3]))
                .subjectName(clean(cols[4]))
                .studentCount(Integer.parseInt(clean(cols[5])))
                .version(clean(cols[6]))
                .build());
        }
        return schedules;
    }

    // ===== EXAM SCHEDULE EXCEL =====
    private List<ExamSchedule> parseExamScheduleExcel(MultipartFile file,
                                                        ExamSession session,
                                                        LocalDate examDate) throws Exception {
        List<ExamSchedule> schedules = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        boolean firstRow = true;

        for (Row row : sheet) {
            if (firstRow) { firstRow = false; continue; }
            if (isRowEmpty(row)) continue;

            schedules.add(ExamSchedule.builder()
                .session(session)
                .examDate(examDate)
                .className(getCellValue(row, 0))
                .timeSlot(getCellValue(row, 1))
                .subjectCode(getCellValue(row, 2))
                .semester(getCellValue(row, 3))
                .subjectName(getCellValue(row, 4))
                .studentCount((int) row.getCell(5).getNumericCellValue())
                .version(getCellValue(row, 6))
                .build());
        }
        workbook.close();
        return schedules;
    }

    // ===== ROOM SKELETON CSV =====
    private List<RoomBlock> parseRoomSkeletonCsv(MultipartFile file,
                                                ExamSession session,
                                                LocalDate examDate) throws Exception {
        List<RoomBlock> blocks = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        int lineCount = 0;

        while ((line = reader.readLine()) != null) {
            lineCount++;
            // Skip first 7 lines (6 header rows + 1 column header row)
            if (lineCount <= 7) continue;

            String[] cols = line.split(",", -1);
            if (cols.length < 8) continue;

            String strengthStr = clean(cols[5]);
            String blockNoStr = clean(cols[6]);
            String roomNo = clean(cols[7]);

            if (strengthStr.isEmpty() || blockNoStr.isEmpty()) continue;

            try {
                blocks.add(RoomBlock.builder()
                        .session(session)
                        .examDate(examDate)
                        .strength(Integer.parseInt(strengthStr))
                        .blockNo(Integer.parseInt(blockNoStr))
                        .roomNo(roomNo)
                        .build());
            } catch (NumberFormatException e) {
                continue;
            }
        }
        return blocks;
    }
    // ===== ROOM SKELETON EXCEL =====
    private List<RoomBlock> parseRoomSkeletonExcel(MultipartFile file,
                                                    ExamSession session,
                                                    LocalDate examDate) throws Exception {
        List<RoomBlock> blocks = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        boolean firstRow = true;

        for (Row row : sheet) {
            if (firstRow) { firstRow = false; continue; }
            if (isRowEmpty(row)) continue;

            Cell strengthCell = row.getCell(0);
            Cell blockCell = row.getCell(1);
            Cell roomCell = row.getCell(2);

            if (strengthCell == null || blockCell == null) continue;

            blocks.add(RoomBlock.builder()
                    .session(session)
                    .examDate(examDate)
                    .strength((int) strengthCell.getNumericCellValue())
                    .blockNo((int) blockCell.getNumericCellValue())
                    .roomNo(roomCell != null ? getCellValue(roomCell) : "")
                    .build());
        }
        workbook.close();
        return blocks;
    }

    private String clean(String val) {
        return val == null ? "" : val.trim().replace("\"", "");
    }

    private String getCellValue(Row row, int col) {
        Cell cell = row.getCell(col);
        return getCellValue(cell);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}