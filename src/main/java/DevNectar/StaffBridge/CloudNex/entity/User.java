package DevNectar.StaffBridge.CloudNex.entity;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    public User() {}

    public User(Long id, String username, String password, String email, String fullName, User manager, Set<Role> roles, boolean isEnabled, String registrationStatus, String employeeExternalId, String techStack, String portfolioUrl, String profileImagePath, String resumeFilePath, LocalDate registrationDate) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.manager = manager;
        this.roles = roles != null ? roles : new HashSet<>();
        this.isEnabled = isEnabled;
        this.registrationStatus = registrationStatus;
        this.employeeExternalId = employeeExternalId;
        this.techStack = techStack;
        this.portfolioUrl = portfolioUrl;
        this.profileImagePath = profileImagePath;
        this.resumeFilePath = resumeFilePath;
        this.registrationDate = registrationDate;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    private boolean isEnabled = false;

    @Column(nullable = false)
    private String registrationStatus = "PENDING";

    @Column(name = "employee_ext_id")
    private String employeeExternalId;
    
    @Column(length = 500)
    private String techStack;
    
    private String portfolioUrl;

    private String profileImagePath;
    private String resumeFilePath;
    
    @Column(nullable = false)
    private boolean isDeleted = false;

    private LocalDate registrationDate;
    private LocalDateTime deactivatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
    public String getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
    public String getEmployeeExternalId() { return employeeExternalId; }
    public void setEmployeeExternalId(String employeeExternalId) { this.employeeExternalId = employeeExternalId; }
    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }
    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }
    public String getResumeFilePath() { return resumeFilePath; }
    public void setResumeFilePath(String resumeFilePath) { this.resumeFilePath = resumeFilePath; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public LocalDateTime getDeactivatedAt() { return deactivatedAt; }
    public void setDeactivatedAt(LocalDateTime deactivatedAt) { this.deactivatedAt = deactivatedAt; }
}
