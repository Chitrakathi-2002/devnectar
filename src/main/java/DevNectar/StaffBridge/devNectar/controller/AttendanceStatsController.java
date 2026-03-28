package DevNectar.StaffBridge.devNectar.controller;

import DevNectar.StaffBridge.devNectar.service.AttendanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/attendance")
public class AttendanceStatsController {

    private final AttendanceService attendanceService;

    public AttendanceStatsController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/stats")
    public String showAttendanceStats(@RequestParam(value = "month", required = false) Integer month,
                                     @RequestParam(value = "year", required = false) Integer year,
                                     Principal principal, Authentication authentication, Model model) {
        String username = principal.getName();
        LocalDate now = LocalDate.now();
        
        // Use provided month/year or default to current
        int selectedMonth = (month != null) ? month : now.getMonthValue();
        int selectedYear = (year != null) ? year : now.getYear();
        
        YearMonth ym = YearMonth.of(selectedYear, selectedMonth);

        Map<LocalDate, Integer> dailyCounts = attendanceService.getDailyLoginCounts(username, selectedMonth, selectedYear);
        
        model.addAttribute("monthName", java.time.Month.of(selectedMonth).name());
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("daysInMonth", ym.lengthOfMonth());
        
        // Only highlight today if viewing the current month/year
        model.addAttribute("todayDay", (selectedMonth == now.getMonthValue() && selectedYear == now.getYear()) ? now.getDayOfMonth() : 0);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        if (isAdmin) {
            model.addAttribute("stats", attendanceService.getAggregateAttendanceSummary(selectedMonth, selectedYear));
            model.addAttribute("allAttendance", attendanceService.getAllUsersMonthlyAttendance(selectedMonth, selectedYear));
            model.addAttribute("matrixRows", attendanceService.getAttendanceMatrix(selectedMonth, selectedYear, null));
        } else {
            model.addAttribute("stats", attendanceService.getMonthlyStats(username, selectedMonth, selectedYear));
            model.addAttribute("allAttendance", new ArrayList<>());
            model.addAttribute("matrixRows", new ArrayList<>());
        }

        model.addAttribute("chartLabels", dailyCounts.keySet().stream()
                .map(LocalDate::toString)
                .collect(Collectors.toList()));
        model.addAttribute("chartData", dailyCounts.values());

        return "attendance_stats";
    }
}
