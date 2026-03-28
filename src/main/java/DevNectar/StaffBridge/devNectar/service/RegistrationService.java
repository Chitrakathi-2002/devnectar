package DevNectar.StaffBridge.devNectar.service;

import DevNectar.StaffBridge.devNectar.entity.Role;
import DevNectar.StaffBridge.devNectar.entity.User;
import DevNectar.StaffBridge.devNectar.repository.RoleRepository;
import DevNectar.StaffBridge.devNectar.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public RegistrationService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(User user, String roleName, Long managerId, MultipartFile profileImage, MultipartFile resumeFile) {
        try {
            // Role Management
            Role role = roleRepository.findAll().stream()
                    .filter(r -> r.getName().equals(roleName))
                    .findFirst().orElseThrow(() -> new RuntimeException(roleName + " not found"));
            user.setRoles(Collections.singleton(role));

            // Manager link for Interns
            if (roleName.equals("ROLE_INTERN") && managerId != null) {
                User manager = userRepository.findById(managerId)
                        .orElseThrow(() -> new RuntimeException("Manager not found"));
                user.setManager(manager);
            }

            // File Handling: Profile Image
            if (profileImage != null && !profileImage.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + profileImage.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + "profiles/" + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, profileImage.getBytes());
                user.setProfileImagePath("/uploads/profiles/" + fileName);
            }

            // File Handling: Resume
            if (resumeFile != null && !resumeFile.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + resumeFile.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + "resumes/" + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, resumeFile.getBytes());
                user.setResumeFilePath("/uploads/resumes/" + fileName);
            }

            // Default security state
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setEnabled(false);
            user.setRegistrationStatus("PENDING");

            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Could not store files. Error: " + e.getMessage());
        }
    }

    @Transactional
    public void approveEmployee(Long employeeId) {
        User employee = userRepository.findById(employeeId).orElseThrow();
        employee.setEnabled(true);
        employee.setRegistrationStatus("APPROVED");
        userRepository.save(employee);
    }

    @Transactional
    public void approveIntern(Long internId, Long approverId) {
        User intern = userRepository.findById(internId).orElseThrow();
        User approver = userRepository.findById(approverId).orElseThrow();

        boolean isAdmin = approver.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        boolean isManager = intern.getManager() != null && intern.getManager().getId().equals(approverId);

        if (isAdmin || isManager) {
            intern.setEnabled(true);
            intern.setRegistrationStatus("APPROVED");
            userRepository.save(intern);
        } else {
            throw new SecurityException("Approver is neither Admin nor assigned Manager");
        }
    }
}
