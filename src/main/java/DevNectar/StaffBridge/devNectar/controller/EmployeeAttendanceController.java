package DevNectar.StaffBridge.devNectar.controller;

import DevNectar.StaffBridge.devNectar.entity.Attendance;
import DevNectar.StaffBridge.devNectar.entity.User;
import DevNectar.StaffBridge.devNectar.repository.UserRepository;
import DevNectar.StaffBridge.devNectar.service.AttendanceService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/employee/attendance")
public class EmployeeAttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    public EmployeeAttendanceController(AttendanceService attendanceService, UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showSelfAttendance(Authentication authentication, Model model) {
        String username = authentication.getName();
        attendanceService.checkAndAutoPunchIn(username);
        List<Attendance> history = attendanceService.getMyAttendance(username);
        model.addAttribute("history", history);
        model.addAttribute("attendanceState", attendanceService.getAttendanceUIState(username));
        return "employee/attendance";
    }

    @GetMapping("/team")
    public String showTeamAttendance(Authentication authentication, Model model) {
        String username = authentication.getName();
        User employee = userRepository.findByUsername(username).orElseThrow();
        List<Attendance> teamAttendance = attendanceService.getTeamAttendance(employee.getId());
        model.addAttribute("teamAttendance", teamAttendance);
        return "employee/team-attendance";
    }

    @PostMapping("/punch-in")
    public String punchIn(Authentication authentication) {
        attendanceService.punchIn(authentication.getName());
        return "redirect:/employee/attendance";
    }

    @PostMapping("/punch-out")
    public String punchOut(Authentication authentication) {
        attendanceService.punchOut(authentication.getName());
        return "redirect:/employee/attendance";
    }

    @PostMapping("/request-late")
    public String requestLate(Authentication authentication) {
        attendanceService.requestLateArrival(authentication.getName());
        return "redirect:/employee/attendance";
    }
}
