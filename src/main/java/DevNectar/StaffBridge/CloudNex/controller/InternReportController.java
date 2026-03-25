package DevNectar.StaffBridge.CloudNex.controller;

import DevNectar.StaffBridge.CloudNex.entity.DailyReport;
import DevNectar.StaffBridge.CloudNex.service.DailyReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/intern/reports")
@PreAuthorize("hasRole('INTERN')")
public class InternReportController {

    private final DailyReportService dailyReportService;

    public InternReportController(DailyReportService dailyReportService) {
        this.dailyReportService = dailyReportService;
    }

    @GetMapping({"", "/", "/history"})
    public String showReportHistory(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        List<DailyReport> reports = dailyReportService.getInternReports(principal.getName());
        model.addAttribute("reports", reports);
        model.addAttribute("report", new DailyReport());
        // Return the modernized template
        return "intern/report-history";
    }

    @PostMapping("/submit")
    public String submitReport(Principal principal, 
                               @RequestParam("title") String title,
                               @RequestParam("description") String description,
                               @RequestParam(value = "hours", defaultValue = "8.0") Double hours) {
        dailyReportService.submitReport(principal.getName(), title, description, hours);
        return "redirect:/intern/reports";
    }

    @PostMapping("/update/{id}")
    public String updateReport(@PathVariable(value = "id") Long id,
                               @RequestParam("title") String title,
                               @RequestParam("description") String description,
                               @RequestParam(value = "hours", defaultValue = "8.0") Double hours) {
        dailyReportService.updateAndResubmitReport(id, title, description, hours);
        return "redirect:/intern/reports";
    }
}
