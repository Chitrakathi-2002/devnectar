package DevNectar.StaffBridge.devNectar.controller;

import DevNectar.StaffBridge.devNectar.entity.User;
import DevNectar.StaffBridge.devNectar.service.RegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import DevNectar.StaffBridge.devNectar.repository.UserRepository;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Controller
@RequestMapping("/register")
public class PublicRegistrationController {

    private final RegistrationService registrationService;
    private final UserRepository userRepository;

    public PublicRegistrationController(RegistrationService registrationService, UserRepository userRepository) {
        this.registrationService = registrationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        // Fetch all employees for the intern's manager dropdown
        List<User> employees = userRepository.findByRoles_Name("ROLE_EMPLOYEE");
        model.addAttribute("employees", employees);
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping
    public String registerUser(@ModelAttribute User user,
                               @RequestParam("roleName") String roleName,
                               @RequestParam(value = "managerId", required = false) Long managerId,
                               @RequestParam("profileImage") MultipartFile profileImage,
                               @RequestParam("resumeFile") MultipartFile resumeFile) {
        
        registrationService.registerUser(user, roleName, managerId, profileImage, resumeFile);
        return "redirect:/registration-success";
    }
}
