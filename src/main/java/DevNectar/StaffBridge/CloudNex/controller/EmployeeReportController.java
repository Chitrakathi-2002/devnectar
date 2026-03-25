package DevNectar.StaffBridge.CloudNex.controller;

import DevNectar.StaffBridge.CloudNex.entity.DailyReport;
import DevNectar.StaffBridge.CloudNex.service.DailyReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/employee/report")
@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
public class EmployeeReportController {

    private final DailyReportService dailyReportService;

    public EmployeeReportController(DailyReportService dailyReportService) {
        this.dailyReportService = dailyReportService;
    }

    @GetMapping("/approvals")
    public String showPendingApprovals(Authentication authentication, Model model) {
        List<DailyReport> pendingReports = dailyReportService.getPendingReportsForManager(authentication.getName());
        model.addAttribute("pendingReports", pendingReports);
        return "employee/report-approvals";
    }

    @PostMapping("/action/{id}")
    public String processReportAction(@PathVariable(value = "id") Long id, 
                                      @RequestParam("action") String action,
                                      @RequestParam(value = "feedback", required = false) String feedback,
                                      Authentication authentication) {
        dailyReportService.processManagerAction(id, authentication.getName(), action, feedback);
        return "redirect:/employee/report/approvals";
    }

    @GetMapping("/history")
    public String showApprovedHistory(Authentication authentication, Model model) {
        List<DailyReport> approvedReports = dailyReportService.getApprovedReportsForManager(authentication.getName());
        model.addAttribute("approvedReports", approvedReports);
        return "employee/report-history";
    }
}
