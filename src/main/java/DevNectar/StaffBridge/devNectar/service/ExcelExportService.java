package DevNectar.StaffBridge.devNectar.service;

import DevNectar.StaffBridge.devNectar.entity.Attendance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    public ByteArrayInputStream exportAttendance(List<Attendance> attendanceList, String reportTitle) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Detailed Attendance");

            // --- LOGO ---
            try {
                InputStream is = new ClassPathResource("static/images/devNectar_logo.png").getInputStream();
                byte[] bytes = IOUtils.toByteArray(is);
                int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                is.close();

                CreationHelper helper = workbook.getCreationHelper();
                Drawing drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                
                // Position logo in top-right
                anchor.setCol1(5); // Column F
                anchor.setRow1(0);
                anchor.setCol2(7); // Ends at column H
                anchor.setRow2(2);
                
                Picture pict = drawing.createPicture(anchor, pictureIdx);
                pict.resize(1.0, 1.0);
            } catch (Exception e) {
                // Ignore logo if fails
            }

            // --- STYLES ---
            // Title Style
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Data Style (Basic)
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Centered Data Style
            CellStyle centerDataStyle = workbook.createCellStyle();
            centerDataStyle.cloneStyleFrom(dataStyle);
            centerDataStyle.setAlignment(HorizontalAlignment.CENTER);

            // Status Styles
            CellStyle presentStyle = workbook.createCellStyle();
            presentStyle.cloneStyleFrom(centerDataStyle);
            presentStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            presentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle absentStyle = workbook.createCellStyle();
            absentStyle.cloneStyleFrom(centerDataStyle);
            absentStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
            absentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle lateStyle = workbook.createCellStyle();
            lateStyle.cloneStyleFrom(centerDataStyle);
            lateStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            lateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // --- CONTENT ---
            String[] columns = {"ID", "Full Name", "Attendance Date", "Check-In", "Check-Out", "Status", "Manual Override"};
            
            // 1. Title Row
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(35);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(reportTitle != null ? reportTitle : "Detailed Attendance History");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.length - 1));

            // 2. Headings Row
            Row headerRow = sheet.createRow(2);
            headerRow.setHeightInPoints(25);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 3. Data Rows
            int rowIdx = 3;
            for (Attendance attendance : attendanceList) {
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(20);
                
                Cell idCell = row.createCell(0);
                idCell.setCellValue(attendance.getId());
                idCell.setCellStyle(centerDataStyle);
                
                Cell nameCell = row.createCell(1);
                nameCell.setCellValue(attendance.getUser().getFullName());
                nameCell.setCellStyle(dataStyle);
                
                Cell dateCell = row.createCell(2);
                dateCell.setCellValue(attendance.getDate().toString());
                dateCell.setCellStyle(centerDataStyle);
                
                Cell inCell = row.createCell(3);
                inCell.setCellValue(attendance.getCheckInTime() != null ? attendance.getCheckInTime().toString() : "--:--");
                inCell.setCellStyle(centerDataStyle);
                
                Cell outCell = row.createCell(4);
                outCell.setCellValue(attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toString() : "--:--");
                outCell.setCellStyle(centerDataStyle);
                
                Cell statusCell = row.createCell(5);
                statusCell.setCellValue(attendance.getStatus().toString());
                if (attendance.getStatus() == Attendance.AttendanceStatus.PRESENT) {
                    statusCell.setCellStyle(presentStyle);
                } else if (attendance.getStatus() == Attendance.AttendanceStatus.ABSENT) {
                    statusCell.setCellStyle(absentStyle);
                } else if (attendance.getStatus() == Attendance.AttendanceStatus.LATE) {
                    statusCell.setCellStyle(lateStyle);
                } else {
                    statusCell.setCellStyle(centerDataStyle);
                }
                
                Cell modCell = row.createCell(6);
                modCell.setCellValue(attendance.isModifiedByAdmin() ? "MANUAL OVERRIDE" : "NO");
                modCell.setCellStyle(centerDataStyle);
            }

            // --- AUTO SIZE ---
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Fail to generate professional detailed Excel report: " + e.getMessage());
        }
    }

    public ByteArrayInputStream exportMatrix(List<DevNectar.StaffBridge.devNectar.dto.MatrixRowDTO> matrixRows, String reportTitle, int daysInMonth) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Monthly Matrix");

            // --- STYLES ---
            // Title Style
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.LEFT);

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Data Style (Base)
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);

            // Name Column Style
            CellStyle nameStyle = workbook.createCellStyle();
            nameStyle.cloneStyleFrom(dataStyle);
            nameStyle.setAlignment(HorizontalAlignment.LEFT);
            Font nameFont = workbook.createFont();
            nameFont.setBold(true);
            nameStyle.setFont(nameFont);

            // Symbol Styles
            CellStyle presentStyle = workbook.createCellStyle();
            presentStyle.cloneStyleFrom(dataStyle);
            Font presentFont = workbook.createFont();
            presentFont.setColor(IndexedColors.GREEN.getIndex());
            presentFont.setBold(true);
            presentStyle.setFont(presentFont);

            CellStyle absentStyle = workbook.createCellStyle();
            absentStyle.cloneStyleFrom(dataStyle);
            Font absentFont = workbook.createFont();
            absentFont.setColor(IndexedColors.RED.getIndex());
            absentFont.setBold(true);
            absentStyle.setFont(absentFont);

            // --- CONTENT ---
            // 1. Report Header Row
            Row headerInfoRow = sheet.createRow(0);
            Cell reportTitleCell = headerInfoRow.createCell(0);
            reportTitleCell.setCellValue(reportTitle != null ? reportTitle : "Attendance Matrix Report");
            reportTitleCell.setCellStyle(titleStyle);
            
            // 2. Headings Row
            Row headRow = sheet.createRow(2);
            int col = 0;
            Cell cName = headRow.createCell(col++);
            cName.setCellValue("Employee / Intern Name");
            cName.setCellStyle(headerStyle);

            for (int day = 1; day <= daysInMonth; day++) {
                Cell cell = headRow.createCell(col++);
                cell.setCellValue(day);
                cell.setCellStyle(headerStyle);
            }

            String[] extras = {"Present", "Absent", "Rate %"};
            for (String ex : extras) {
                Cell cell = headRow.createCell(col++);
                cell.setCellValue(ex);
                cell.setCellStyle(headerStyle);
            }

            // 3. Data Rows
            int rowIdx = 3;
            for (DevNectar.StaffBridge.devNectar.dto.MatrixRowDTO rowData : matrixRows) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;
                
                Cell nameCell = row.createCell(c++);
                nameCell.setCellValue(rowData.getFullName());
                nameCell.setCellStyle(nameStyle);

                for (int day = 1; day <= daysInMonth; day++) {
                    Cell cell = row.createCell(c++);
                    cell.setCellStyle(dataStyle);

                    if (day >= rowData.getRegistrationDay()) {
                        String status = rowData.getDailyStatus().get(day);
                        if ("PRESENT".equals(status)) {
                            cell.setCellValue("✔");
                            cell.setCellStyle(presentStyle);
                        } else if ("ABSENT".equals(status)) {
                            cell.setCellValue("✖");
                            cell.setCellStyle(absentStyle);
                        } else if ("SUNDAY".equals(status)) {
                            cell.setCellValue("H");
                        } else {
                            cell.setCellValue(".");
                        }
                    } else {
                        cell.setCellValue("-");
                    }
                }

                Cell pCell = row.createCell(c++);
                pCell.setCellValue(rowData.getPresentCount());
                pCell.setCellStyle(dataStyle);

                Cell aCell = row.createCell(c++);
                aCell.setCellValue(rowData.getAbsentCount());
                aCell.setCellStyle(dataStyle);

                Cell rCell = row.createCell(c++);
                rCell.setCellValue(String.format("%.1f%%", rowData.getAttendanceRate()));
                rCell.setCellStyle(dataStyle);
            }

            // --- AUTO SIZE ---
            for (int i = 0; i < (daysInMonth + 4); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Fail to generate professional Excel matrix: " + e.getMessage());
        }
    }
}
