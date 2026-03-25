package DevNectar.StaffBridge.CloudNex.controller;

import DevNectar.StaffBridge.CloudNex.service.DailyReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping({"/admin/report", "/admin/reports"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final DailyReportService dailyReportService;

    public AdminReportController(DailyReportService dailyReportService) {
        this.dailyReportService = dailyReportService;
    }

    @GetMapping({"", "/", "/all"})
    public String showAllReports(
            @RequestParam(value = "manager", required = false) String managerName,
            @RequestParam(value = "userId", required = false) Long userId,
            Model model) {
        var reports = dailyReportService.getAllReports();
        
        if (userId != null) {
            reports = reports.stream()
                    .filter(r -> r.getIntern().getId().equals(userId))
                    .toList();
            
            if (!reports.isEmpty()) {
                model.addAttribute("filteredUserName", reports.get(0).getIntern().getFullName());
            }
        }
        
        if (managerName != null && !managerName.isEmpty()) {
            reports = reports.stream()
                    .filter(r -> r.getManager() != null && r.getManager().getFullName().toLowerCase().contains(managerName.toLowerCase()))
                    .toList();
        }
        
        model.addAttribute("reports", reports);
        model.addAttribute("selectedUserId", userId);
        return "admin/report-all";
    }
}
