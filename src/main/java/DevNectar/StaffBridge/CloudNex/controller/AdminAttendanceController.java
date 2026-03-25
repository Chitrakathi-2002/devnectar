package DevNectar.StaffBridge.CloudNex.controller;

import DevNectar.StaffBridge.CloudNex.entity.Attendance;
import DevNectar.StaffBridge.CloudNex.service.AttendanceService;
import DevNectar.StaffBridge.CloudNex.service.ExcelExportService;
import DevNectar.StaffBridge.CloudNex.service.PdfExportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/admin/attendance")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAttendanceController {

    private final AttendanceService attendanceService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    public AdminAttendanceController(AttendanceService attendanceService, ExcelExportService excelExportService, PdfExportService pdfExportService) {
        this.attendanceService = attendanceService;
        this.excelExportService = excelExportService;
        this.pdfExportService = pdfExportService;
    }

    @GetMapping({"/all", "/history"})
    public String viewAllAttendance(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            Model model) {
        
        LocalDate now = LocalDate.now();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        int targetYear = (year != null) ? year : now.getYear();

        model.addAttribute("allAttendance", attendanceService.getFilteredAttendance(userId, targetMonth, targetYear));
        model.addAttribute("users", attendanceService.getAllActiveStaff());
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedMonth", targetMonth);
        model.addAttribute("selectedYear", targetYear);
        
        if (userId != null) {
            attendanceService.getUserById(userId).ifPresent(u -> model.addAttribute("filteredUserName", u.getFullName()));
        }
        
        return "admin/attendance-history";
    }

    @GetMapping("/export/excel")
    public ResponseEntity<Resource> exportToExcel(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year) {
            
        List<Attendance> list = attendanceService.getFilteredAttendance(userId, month, year);
        String userName = "Global";
        if (userId != null) {
            userName = attendanceService.getUserById(userId).map(u -> u.getFullName()).orElse("User");
        }
        
        String monthStr = (month != null) ? java.time.Month.of(month).name() : "All";
        String reportTitle = "Detailed Attendance Report - " + userName + " - " + monthStr + " " + (year != null ? year : "");
        String filename = "Attendance_Report_" + monthStr + "_" + (year != null ? year : "") + ".xlsx";
        if (userId != null) {
            filename = "Attendance_Report_" + userName.replace(" ", "_") + "_" + monthStr + ".xlsx";
        }
        
        ByteArrayInputStream in = excelExportService.exportAttendance(list, reportTitle);
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<Resource> exportToPdf(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year) {
            
        List<Attendance> list = attendanceService.getFilteredAttendance(userId, month, year);
        String userName = "Global";
        if (userId != null) {
            userName = attendanceService.getUserById(userId).map(u -> u.getFullName()).orElse("User");
        }
        
        String monthStr = (month != null) ? java.time.Month.of(month).name() : "All";
        String reportTitle = "Detailed Attendance Report - " + userName + " - " + monthStr + " " + (year != null ? year : "");
        String filename = "Attendance_Report_" + monthStr + "_" + (year != null ? year : "") + ".pdf";
        if (userId != null) {
            filename = "Attendance_Report_" + userName.replace(" ", "_") + "_" + monthStr + ".pdf";
        }
        
        ByteArrayInputStream in = pdfExportService.exportAttendance(list, reportTitle);
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }

    @GetMapping("/export/{format}")
    public ResponseEntity<Resource> exportMatrix(
            @PathVariable(value = "format") String format,
            @RequestParam(value = "month") Integer month,
            @RequestParam(value = "year") Integer year) {
            
        YearMonth ym = YearMonth.of(year, month);
        List<DevNectar.StaffBridge.CloudNex.dto.MatrixRowDTO> matrix = attendanceService.getAttendanceMatrix(month, year, null);
        String monthName = ym.getMonth().name();
        String reportTitle = "Attendance Report: " + monthName + " " + year;
        
        ByteArrayInputStream in;
        String filename;
        MediaType mediaType;

        if ("excel".equalsIgnoreCase(format)) {
            in = excelExportService.exportMatrix(matrix, reportTitle, ym.lengthOfMonth());
            filename = "Attendance_Report_" + monthName + "_" + year + ".xlsx";
            mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else if ("pdf".equalsIgnoreCase(format)) {
            in = pdfExportService.exportMatrix(matrix, reportTitle, ym.lengthOfMonth());
            filename = "Attendance_Report_" + monthName + "_" + year + ".pdf";
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            return ResponseEntity.badRequest().build();
        }

        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(mediaType)
                .body(file);
    }



    @GetMapping("/stats")
    public String showAdminAttendanceMatrix(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            Model model) {
        
        LocalDate now = LocalDate.now();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        int targetYear = (year != null) ? year : now.getYear();
        
        YearMonth ym = YearMonth.of(targetYear, targetMonth);

        model.addAttribute("isAdmin", true);
        model.addAttribute("monthName", ym.getMonth().name());
        model.addAttribute("selectedMonth", targetMonth);
        model.addAttribute("selectedYear", targetYear);
        model.addAttribute("daysInMonth", ym.lengthOfMonth());
        model.addAttribute("todayDay", (targetMonth == now.getMonthValue() && targetYear == now.getYear()) ? now.getDayOfMonth() : 0);
        model.addAttribute("matrixRows", attendanceService.getAttendanceMatrix(targetMonth, targetYear, null));
        model.addAttribute("stats", attendanceService.getAggregateAttendanceSummary(targetMonth, targetYear));
        
        return "attendance_stats";
    }
}
