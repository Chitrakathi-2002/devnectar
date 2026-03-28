package DevNectar.StaffBridge.devNectar.service;

import DevNectar.StaffBridge.devNectar.entity.User;
import DevNectar.StaffBridge.devNectar.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileUploadService fileUploadService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileUploadService = fileUploadService;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void updateUserProfile(String username, User updateData, org.springframework.web.multipart.MultipartFile profileImage, org.springframework.web.multipart.MultipartFile resumeFile) {
        User user = findByUsername(username);
        user.setFullName(updateData.getFullName());
        user.setEmail(updateData.getEmail());
        user.setTechStack(updateData.getTechStack());
        user.setPortfolioUrl(updateData.getPortfolioUrl());
        
        if (profileImage != null && !profileImage.isEmpty()) {
            String imagePath = fileUploadService.saveProfileImage(profileImage);
            if (imagePath != null) {
                user.setProfileImagePath(imagePath);
            }
        }
        
        if (resumeFile != null && !resumeFile.isEmpty()) {
            String resumePath = fileUploadService.saveResumeFile(resumeFile);
            if (resumePath != null) {
                user.setResumeFilePath(resumePath);
            }
        }

        userRepository.save(user);
    }

    @Transactional
    public boolean softDeleteUser(String username, String password) {
        User user = findByUsername(username);
        if (passwordEncoder.matches(password, user.getPassword())) {
            userRepository.softDeleteUserById(user.getId());
            
            // Professional Feedback
            boolean isIntern = user.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_INTERN"));
            if (isIntern && user.getManager() != null) {
                System.out.println("User [" + user.getFullName() + "] has deactivated their account.");
            }
            
            return true;
        }
        return false;
    }
}
