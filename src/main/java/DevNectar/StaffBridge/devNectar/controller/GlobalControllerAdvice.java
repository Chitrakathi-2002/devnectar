package DevNectar.StaffBridge.devNectar.controller;

import DevNectar.StaffBridge.devNectar.entity.User;
import DevNectar.StaffBridge.devNectar.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    public GlobalControllerAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("currentUser")
    public User addCurrentUserToModel() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    @ModelAttribute("isEmployee")
    public boolean isEmployee() {
        return hasRole("ROLE_EMPLOYEE");
    }

    @ModelAttribute("isIntern")
    public boolean isIntern() {
        return hasRole("ROLE_INTERN");
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
