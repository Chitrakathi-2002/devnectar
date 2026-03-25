package DevNectar.StaffBridge.CloudNex.config;

import DevNectar.StaffBridge.CloudNex.entity.User;
import DevNectar.StaffBridge.CloudNex.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@Component
public class MySimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public MySimpleUrlAuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Acceptance Criteria: Redirect to /waiting-approval if isEnabled is false
            if (!user.isEnabled()) {
                response.sendRedirect("/waiting-approval");
                return;
            }
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectUrl = "/login?error=unauthorized";

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/admin/dashboard";
                break;
            } else if (role.equals("ROLE_EMPLOYEE")) {
                redirectUrl = "/employee/dashboard";
                break;
            } else if (role.equals("ROLE_INTERN")) {
                redirectUrl = "/intern/dashboard";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
