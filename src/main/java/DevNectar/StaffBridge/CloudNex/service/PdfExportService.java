package DevNectar.StaffBridge.CloudNex.service;

import DevNectar.StaffBridge.CloudNex.entity.Attendance;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Service
public class PdfExportService {

    // --- SHARED COLORS & FONTS ---
    private static final BaseColor CLOUDNEX_BRAND = new BaseColor(46, 204, 113); // Brand Green
    private static final BaseColor HEADER_BG = new BaseColor(44, 62, 80); // Professional Navy
    private static final BaseColor ROW_EVEN_BG = new BaseColor(248, 249, 250); // Light Gray
    private static final BaseColor STATUS_PRESENT_BG = new BaseColor(232, 245, 233);
    private static final BaseColor STATUS_ABSENT_BG = new BaseColor(255, 235, 238);

    private static final Font BRAND_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, CLOUDNEX_BRAND);
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
    private static final Font META_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, BaseColor.GRAY);
    private static final Font HEAD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
    private static final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
    private static final Font DATA_FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);

    /**
     * Professional Detailed Attendance PDF Report
     */
    public ByteArrayInputStream exportAttendance(List<Attendance> attendanceList, String reportTitle) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Branding Header
            Paragraph brand = new Paragraph("CloudNex", BRAND_FONT);
            brand.setAlignment(Element.ALIGN_RIGHT);
            document.add(brand);

            // 2. Report Header
            Paragraph title = new Paragraph(reportTitle != null ? reportTitle : "Detailed Attendance History", TITLE_FONT);
            title.setSpacingBefore(10);
            document.add(title);

            // 3. Metadata (Timestamp & System info)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Paragraph meta = new Paragraph("Generated On: " + timestamp + " | Authorized: Administrator", META_FONT);
            meta.setSpacingAfter(25);
            document.add(meta);

            // 4. Table Construction
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1.2f, 4.0f, 3.2f, 2.4f, 2.4f, 2.4f, 2.8f });

            // Table Headers
            Stream.of("ID", "Staff Name", "Date", "Check-In", "Check-Out", "Status", "Override")
                    .forEach(h -> {
                        PdfPCell cell = new PdfPCell(new Phrase(h, HEAD_FONT));
                        cell.setBackgroundColor(HEADER_BG);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setPadding(10);
                        cell.setBorderColor(BaseColor.WHITE);
                        table.addCell(cell);
                    });

            // Data Rows
            int rowCount = 0;
            for (Attendance attendance : attendanceList) {
                BaseColor bg = (rowCount % 2 == 1) ? ROW_EVEN_BG : BaseColor.WHITE;

                table.addCell(createStyledCell(String.valueOf(attendance.getId()), DATA_FONT, Element.ALIGN_CENTER, bg));
                table.addCell(createStyledCell(attendance.getUser().getFullName(), DATA_FONT, Element.ALIGN_LEFT, bg));
                table.addCell(createStyledCell(attendance.getDate().toString(), DATA_FONT, Element.ALIGN_CENTER, bg));
                
                String in = attendance.getCheckInTime() != null ? attendance.getCheckInTime().toString() : "--:--";
                table.addCell(createStyledCell(in, DATA_FONT, Element.ALIGN_CENTER, bg));
                
                String outTime = attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toString() : "--:--";
                table.addCell(createStyledCell(outTime, DATA_FONT, Element.ALIGN_CENTER, bg));
                
                // Status Cell with specialized styling
                PdfPCell statusCell = createStyledCell(attendance.getStatus().toString(), DATA_FONT, Element.ALIGN_CENTER, bg);
                if (attendance.getStatus() == Attendance.AttendanceStatus.PRESENT) statusCell.setBackgroundColor(STATUS_PRESENT_BG);
                else if (attendance.getStatus() == Attendance.AttendanceStatus.ABSENT) statusCell.setBackgroundColor(STATUS_ABSENT_BG);
                table.addCell(statusCell);

                String override = attendance.isModifiedByAdmin() ? "YES" : "NO";
                table.addCell(createStyledCell(override, DATA_FONT, Element.ALIGN_CENTER, bg));

                rowCount++;
            }

            document.add(table);
            
            // Footer Branding (Centered)
            Paragraph footer = new Paragraph("\n\nCloudNex Attendance System - Secure Audit Trail", META_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("PDF Generation Error: " + e.getMessage());
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Professional Attendance Matrix PDF Report (Landscape)
     */
    public ByteArrayInputStream exportMatrix(List<DevNectar.StaffBridge.CloudNex.dto.MatrixRowDTO> matrixRows, String reportTitle, int daysInMonth) {
        Document document = new Document(PageSize.A4.rotate()); // Landscape
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Branding Header
            Paragraph brand = new Paragraph("CloudNex", BRAND_FONT);
            brand.setAlignment(Element.ALIGN_RIGHT);
            document.add(brand);

            // 2. Report Header
            Paragraph title = new Paragraph(reportTitle != null ? reportTitle : "Monthly Workforce Matrix", TITLE_FONT);
            title.setSpacingBefore(5);
            document.add(title);

            // 3. Metadata
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Paragraph meta = new Paragraph("Monthly Analysis | Generated On: " + timestamp, META_FONT);
            meta.setSpacingAfter(15);
            document.add(meta);

            // 4. Width Calculations
            float[] columnWidths = new float[daysInMonth + 4];
            columnWidths[0] = 5.5f; // Staff Name
            for (int i = 1; i <= daysInMonth; i++) columnWidths[i] = 1.0f; // Days
            columnWidths[daysInMonth + 1] = 1.6f; // P (Present)
            columnWidths[daysInMonth + 2] = 1.6f; // A (Absent)
            columnWidths[daysInMonth + 3] = 2.2f; // % (Rate)

            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);

            // Table Headers
            PdfPCell nameHead = new PdfPCell(new Phrase("Staff / Day", HEAD_FONT));
            nameHead.setBackgroundColor(HEADER_BG);
            nameHead.setPadding(6);
            nameHead.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(nameHead);

            for (int d = 1; d <= daysInMonth; d++) {
                PdfPCell dCell = new PdfPCell(new Phrase(String.valueOf(d), HEAD_FONT));
                dCell.setBackgroundColor(HEADER_BG);
                dCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                dCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(dCell);
            }

            for (String h : new String[]{"P", "A", "%"}) {
                PdfPCell tCell = new PdfPCell(new Phrase(h, HEAD_FONT));
                tCell.setBackgroundColor(HEADER_BG);
                tCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(tCell);
            }

            // Data Rows
            int rowIdx = 0;
            for (DevNectar.StaffBridge.CloudNex.dto.MatrixRowDTO rowData : matrixRows) {
                BaseColor bg = (rowIdx % 2 == 1) ? ROW_EVEN_BG : BaseColor.WHITE;

                // Name Cell
                table.addCell(createStyledCell(rowData.getFullName(), DATA_FONT_SMALL, Element.ALIGN_LEFT, bg));

                // Day Cells
                for (int day = 1; day <= daysInMonth; day++) {
                    String statusStr = "-";
                    BaseColor cellColor = bg;
                    
                    if (day >= rowData.getRegistrationDay()) {
                        String s = rowData.getDailyStatus().get(day);
                        if ("PRESENT".equals(s)) { statusStr = "V"; cellColor = STATUS_PRESENT_BG; }
                        else if ("ABSENT".equals(s)) { statusStr = "X"; cellColor = STATUS_ABSENT_BG; }
                        else if ("SUNDAY".equals(s)) { statusStr = "H"; cellColor = new BaseColor(237, 231, 246); }
                        else if ("HOLIDAY".equals(s)) { statusStr = "H"; cellColor = new BaseColor(255, 243, 224); }
                        else statusStr = ".";
                    }

                    PdfPCell sCell = createStyledCell(statusStr, DATA_FONT_SMALL, Element.ALIGN_CENTER, bg);
                    if (cellColor != bg) sCell.setBackgroundColor(cellColor);
                    table.addCell(sCell);
                }

                // Totals
                table.addCell(createStyledCell(String.valueOf(rowData.getPresentCount()), DATA_FONT_SMALL, Element.ALIGN_CENTER, bg));
                table.addCell(createStyledCell(String.valueOf(rowData.getAbsentCount()), DATA_FONT_SMALL, Element.ALIGN_CENTER, bg));
                table.addCell(createStyledCell(String.format("%.1f%%", rowData.getAttendanceRate()), DATA_FONT_SMALL, Element.ALIGN_CENTER, bg));

                rowIdx++;
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Matrix PDF Generation Error: " + e.getMessage());
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private PdfPCell createStyledCell(String text, Font font, int alignment, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setBackgroundColor(bg);
        cell.setBorderColor(new BaseColor(224, 224, 224)); // Soft Gray Border
        return cell;
    }
}
