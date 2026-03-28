package DevNectar.StaffBridge.devNectar.controller;

import DevNectar.StaffBridge.devNectar.entity.Attendance;
import DevNectar.StaffBridge.devNectar.service.AttendanceService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/intern/attendance")
public class InternAttendanceController {

    private final AttendanceService attendanceService;

    public InternAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public String showAttendance(Authentication authentication, Model model) {
        String username = authentication.getName();
        attendanceService.checkAndAutoPunchIn(username);
        List<Attendance> history = attendanceService.getMyAttendance(username);
        
        java.time.LocalDate today = java.time.LocalDate.now();
        Attendance todayRecord = history.stream()
                .filter(a -> a.getDate().equals(today))
                .findFirst()
                .orElse(null);
        
        model.addAttribute("history", history);
        model.addAttribute("todayAttendance", todayRecord);
        model.addAttribute("attendanceState", attendanceService.getAttendanceUIState(username));
        return "intern/attendance";
    }

    @PostMapping("/punch-in")
    public String punchIn(Authentication authentication) {
        attendanceService.punchIn(authentication.getName());
        return "redirect:/intern/dashboard";
    }

    @PostMapping("/punch-out")
    public String punchOut(Authentication authentication) {
        attendanceService.punchOut(authentication.getName());
        return "redirect:/intern/attendance";
    }

    @PostMapping("/punch-out-with-report")
    public String punchOutWithReport(Authentication authentication,
                                     @RequestParam("title") String title,
                                     @RequestParam("description") String description,
                                     @RequestParam("hours") Double hours) {
        attendanceService.punchOutWithReport(authentication.getName(), title, description, hours);
        return "redirect:/intern/attendance";
    }

    @PostMapping("/request-late")
    public String requestLate(Authentication authentication) {
        attendanceService.requestLateArrival(authentication.getName());
        return "redirect:/intern/attendance";
    }
}
