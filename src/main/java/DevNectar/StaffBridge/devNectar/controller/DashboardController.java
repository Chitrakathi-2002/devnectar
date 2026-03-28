package DevNectar.StaffBridge.devNectar.controller;

import DevNectar.StaffBridge.devNectar.entity.User;
import DevNectar.StaffBridge.devNectar.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final DevNectar.StaffBridge.devNectar.service.AttendanceService attendanceService;
    private final DevNectar.StaffBridge.devNectar.service.DailyReportService dailyReportService;

    public DashboardController(UserRepository userRepository, 
                               DevNectar.StaffBridge.devNectar.service.AttendanceService attendanceService,
                               DevNectar.StaffBridge.devNectar.service.DailyReportService dailyReportService) {
        this.userRepository = userRepository;
        this.attendanceService = attendanceService;
        this.dailyReportService = dailyReportService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboardRouter(Authentication authentication) {
        if (authentication == null) return "redirect:/login";
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN")) return "redirect:/admin/dashboard";
            if (role.equals("ROLE_EMPLOYEE")) return "redirect:/employee/dashboard";
            if (role.equals("ROLE_INTERN")) return "redirect:/intern/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        List<User> pending = userRepository.findByRegistrationStatus("PENDING");
        List<User> deletionRequests = userRepository.findByRegistrationStatus("DELETION_PENDING");
        long employeeCount = userRepository.countAllActiveEmployees();
        long internCount = userRepository.countAllActiveInterns();
        
        // Dynamic Counts for "Present Today"
        java.time.LocalDate today = java.time.LocalDate.now();
        long internsPresent = attendanceService.getAllActiveStaff().stream()
                .filter(u -> attendanceService.getMyAttendance(u.getUsername()).stream()
                .anyMatch(a -> a.getDate().equals(today))).count();
        long reportsToday = dailyReportService.getAllReports().stream()
                .filter(r -> r.getSubmissionDate().equals(today)).count();
        
        model.addAttribute("pendingUsers", pending);
        model.addAttribute("deletionRequests", deletionRequests);
        model.addAttribute("employeeCount", employeeCount);
        model.addAttribute("internCount", internCount);
        model.addAttribute("internsPresent", internsPresent);
        model.addAttribute("reportsToday", reportsToday);
        return "admin/dashboard";
    }

    @PostMapping("/admin/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveUser(@PathVariable(value = "id") Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEnabled(true);
        user.setRegistrationStatus("APPROVED");
        userRepository.save(user);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectUser(@PathVariable(value = "id") Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRegistrationStatus("REJECTED");
        userRepository.save(user);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/terminate/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveTermination(@PathVariable(value = "id") Long id) {
        userRepository.softDeleteUserById(id);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/terminate/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectTermination(@PathVariable(value = "id") Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEnabled(true);
        user.setRegistrationStatus("APPROVED");
        user.setDeactivatedAt(null);
        userRepository.save(user);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/employee/dashboard")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String employeeDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();
        attendanceService.checkAndAutoPunchIn(username);

        long teamCount = userRepository.countByManagerAndIsDeletedFalse(currentUser);
        long pendingApprovals = dailyReportService.getPendingReportsForManager(username).size();
        
        // Precise team attendance rate calculation
        int monthlyRate = 100;
        if (teamCount > 0) {
            LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
            long totalPresent = attendanceService.getTeamAttendance(currentUser.getId()).stream()
                    .filter(a -> !a.getDate().isBefore(firstOfMonth))
                    .count();
            int daysPassed = LocalDate.now().getDayOfMonth();
            monthlyRate = (int) Math.min(100, (totalPresent * 100.0) / (teamCount * daysPassed));
        }

        java.util.List<DevNectar.StaffBridge.devNectar.entity.Attendance> myHistory = attendanceService.getMyAttendance(username);
        LocalDate today = LocalDate.now();
        DevNectar.StaffBridge.devNectar.entity.Attendance todayRecord = myHistory.stream()
                .filter(a -> a.getDate().equals(today))
                .findFirst()
                .orElse(null);

        model.addAttribute("teamCount", teamCount);
        model.addAttribute("pendingApprovals", pendingApprovals);
        model.addAttribute("monthlyRate", monthlyRate);
        model.addAttribute("todayAttendance", todayRecord);
        
        return "employee/dashboard";
    }

    @GetMapping("/intern/dashboard")
    @PreAuthorize("hasRole('INTERN')")
    public String internDashboard(Authentication authentication, Model model) {
        attendanceService.checkAndAutoPunchIn(authentication.getName());
        String username = authentication.getName();
        List<DevNectar.StaffBridge.devNectar.entity.DailyReport> reports = dailyReportService.getInternReports(username);
        List<DevNectar.StaffBridge.devNectar.entity.Attendance> attendanceRecords = attendanceService.getMyAttendance(username);
        
        long completedDays = reports.stream().filter(r -> r.getStatus() == DevNectar.StaffBridge.devNectar.entity.DailyReport.ReportStatus.APPROVED).count();
        long pendingReportsCount = reports.stream().filter(r -> r.getStatus() == DevNectar.StaffBridge.devNectar.entity.DailyReport.ReportStatus.SUBMITTED || r.getStatus() == DevNectar.StaffBridge.devNectar.entity.DailyReport.ReportStatus.REVISION_REQUESTED).count();
        
        LocalDate today = LocalDate.now();
        DevNectar.StaffBridge.devNectar.entity.Attendance todayRecord = attendanceRecords.stream()
                .filter(a -> a.getDate().equals(today))
                .findFirst()
                .orElse(null);

        model.addAttribute("reports", reports.stream().limit(5).collect(java.util.stream.Collectors.toList()));
        model.addAttribute("daysCompleted", completedDays);
        model.addAttribute("pendingReports", pendingReportsCount);
        model.addAttribute("todayAttendance", todayRecord);
        
        return "intern/dashboard";
    }

    @GetMapping("/waiting-approval")
    public String waitingApproval() {
        return "waiting_approval";
    }

    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }
}
