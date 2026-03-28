package DevNectar.StaffBridge.devNectar.config;

import DevNectar.StaffBridge.devNectar.entity.User;
import DevNectar.StaffBridge.devNectar.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class RegistrationApprovalInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public RegistrationApprovalInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        
        // Exclude generic paths or specifically the waiting path itself
        if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/images") 
                || uri.startsWith("/assets") || uri.startsWith("/Data-files")
                || uri.equals("/login") || uri.equals("/logout") || uri.equals("/register") 
                || uri.startsWith("/api/register") || uri.equals("/waiting-approval")) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (!user.isEnabled()) {
                    response.sendRedirect("/waiting-approval");
                    return false;
                }
            }
        }
        return true;
    }
}
