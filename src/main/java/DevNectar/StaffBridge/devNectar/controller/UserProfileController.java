package DevNectar.StaffBridge.devNectar.controller;

import DevNectar.StaffBridge.devNectar.entity.User;
import DevNectar.StaffBridge.devNectar.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class UserProfileController {
    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(Principal principal, Model model) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User user, 
                               @RequestParam(value = "profileImage", required = false) org.springframework.web.multipart.MultipartFile profileImage,
                               @RequestParam(value = "resumeFile", required = false) org.springframework.web.multipart.MultipartFile resumeFile,
                               Principal principal, RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        try {
            userService.updateUserProfile(username, user, profileImage, resumeFile);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(@RequestParam("password") String password, Principal principal, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        if (userService.softDeleteUser(username, password)) {
            // Clear context and session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return "redirect:/login?deleted=true";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid password. Account deletion failed.");
            return "redirect:/profile";
        }
    }
}
