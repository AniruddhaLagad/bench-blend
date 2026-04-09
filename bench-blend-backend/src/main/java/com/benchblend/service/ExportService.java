package com.benchblend.service;

import com.benchblend.model.SeatingArrangement;
import com.benchblend.repository.SeatingArrangementRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final SeatingArrangementRepository seatingArrangementRepository;

    // ===================== CSV EXPORT =====================
    public byte[] exportAsCsv(Long sessionId, LocalDate examDate) {
        List<SeatingArrangement> arrangements =
                seatingArrangementRepository.findBySessionIdAndExamDateOrderByBlockNo(sessionId, examDate);

        if (arrangements.isEmpty()) {
            throw new RuntimeException("No seating arrangement found for this date");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Progressive Education Society's\n");
        sb.append("Modern College of Arts Science and Commerce Ganeshkhind Pune - 16\n");
        sb.append("Autonomous\n");
        sb.append("March-April 2026\n");
        sb.append("Morning Session\n");
        sb.append("Date: ").append(examDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("\n");
        sb.append("\n");
        sb.append("Class,Time,Sub Code,Sem,Subject,Strength,Block No,Room No,Benches Used,Bench Range,Mode\n");

        Map<String, Integer> roomSideCounter = new HashMap<>();

        for (SeatingArrangement a : arrangements) {
            sb.append(csvCell(a.getClassName())).append(",");
            sb.append(csvCell(a.getTimeSlot())).append(",");
            sb.append(csvCell(a.getSubjectCode())).append(",");
            sb.append(csvCell(a.getSemester())).append(",");
            sb.append(csvCell(a.getSubjectName())).append(",");
            sb.append(a.getStrength()).append(",");
            sb.append(a.getBlockNo()).append(",");
            sb.append(csvCell(a.getRoomNo())).append(",");
            sb.append(a.getBenchesUsed()).append(",");
            sb.append(csvCell(buildBenchRange(a, roomSideCounter))).append(",");
            sb.append(csvCell(a.getSeatingMode())).append("\n");
        }

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    // ===================== EXCEL EXPORT =====================
    public byte[] exportAsExcel(Long sessionId, LocalDate examDate) throws IOException {
        List<SeatingArrangement> arrangements =
                seatingArrangementRepository.findBySessionIdAndExamDateOrderByBlockNo(sessionId, examDate);

        if (arrangements.isEmpty()) {
            throw new RuntimeException("No seating arrangement found for this date");
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Seating Arrangement");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle boldStyle = createBoldStyle(workbook);

            int rowNum = 0;

            rowNum = addTitleRow(sheet, rowNum, "Progressive Education Society's", titleStyle, 11);
            rowNum = addTitleRow(sheet, rowNum, "Modern College of Arts, Science and Commerce, Ganeshkhind, Pune - 16", titleStyle, 11);
            rowNum = addTitleRow(sheet, rowNum, "Autonomous", titleStyle, 11);
            rowNum = addTitleRow(sheet, rowNum, "March-April 2026", titleStyle, 11);
            rowNum = addTitleRow(sheet, rowNum, "Morning Session", titleStyle, 11);
            rowNum = addTitleRow(sheet, rowNum,
                    "Date: " + examDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                    boldStyle, 11);

            sheet.createRow(rowNum++);

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Class", "Time", "Sub Code", "Sem", "Subject",
                    "Strength", "Block No", "Room No", "Benches Used", "Bench Range", "Mode"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Map<String, Integer> roomSideCounter = new HashMap<>();

            for (SeatingArrangement a : arrangements) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, a.getClassName(), dataStyle);
                createCell(row, 1, a.getTimeSlot(), dataStyle);
                createCell(row, 2, a.getSubjectCode(), dataStyle);
                createCell(row, 3, a.getSemester(), dataStyle);
                createCell(row, 4, a.getSubjectName(), dataStyle);
                createNumericCell(row, 5, a.getStrength(), dataStyle);
                createNumericCell(row, 6, a.getBlockNo(), dataStyle);
                createCell(row, 7, a.getRoomNo(), dataStyle);
                createNumericCell(row, 8, a.getBenchesUsed(), dataStyle);
                createCell(row, 9, buildBenchRange(a, roomSideCounter), dataStyle);
                createCell(row, 10, a.getSeatingMode(), dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ===================== HELPERS =====================
    private String buildBenchRange(SeatingArrangement a, Map<String, Integer> roomSideCounter) {
        String roomCode = a.getRoomNo().replace("-", "");
        String side = a.getSide() != null ? a.getSide() : "L";
        String key = roomCode + "-" + side;

        int startBench = roomSideCounter.getOrDefault(key, 0) + 1;
        int endBench = startBench + a.getBenchesUsed() - 1;

        roomSideCounter.put(key, endBench);

        return String.format("%s%s%02d to %s%s%02d",
                roomCode, side, startBench, roomCode, side, endBench);
    }

    private String csvCell(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private int addTitleRow(Sheet sheet, int rowNum, String text, CellStyle style, int colspan) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, colspan - 1));
        return rowNum + 1;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createNumericCell(Row row, int col, int value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createBoldStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}