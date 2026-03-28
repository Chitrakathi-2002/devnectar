package DevNectar.StaffBridge.devNectar.config;

import DevNectar.StaffBridge.devNectar.entity.Attendance;
import DevNectar.StaffBridge.devNectar.entity.DailyReport;
import DevNectar.StaffBridge.devNectar.entity.Role;
import DevNectar.StaffBridge.devNectar.entity.User;
import DevNectar.StaffBridge.devNectar.repository.AttendanceRepository;
import DevNectar.StaffBridge.devNectar.repository.DailyReportRepository;
import DevNectar.StaffBridge.devNectar.repository.RoleRepository;
import DevNectar.StaffBridge.devNectar.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DailyReportRepository reportRepository;
    private final AttendanceRepository attendanceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, RoleRepository roleRepository, 
                      DailyReportRepository reportRepository, AttendanceRepository attendanceRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.reportRepository = reportRepository;
        this.attendanceRepository = attendanceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        LocalDate nowDate = LocalDate.now();
        // 1. Create default Roles
        Role adminRole = createRoleIfNotFound("ROLE_ADMIN");
        Role employeeRole = createRoleIfNotFound("ROLE_EMPLOYEE");
        Role internRole = createRoleIfNotFound("ROLE_INTERN");

        // 2. Create Admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@devNectar.com");
            admin.setFullName("System Admin");
            admin.setTechStack("Full Stack Developer, Java, Spring Boot, PostgreSQL"); // Added tech stack
            admin.setRoles(Collections.singleton(adminRole));
            admin.setEnabled(true);
            admin.setRegistrationStatus("APPROVED");
            admin.setRegistrationDate(nowDate.withMonth(1).withDayOfMonth(1));
            userRepository.save(admin);
        } else {
            User admin = userRepository.findByUsername("admin").get();
            if (admin.getTechStack() == null || admin.getTechStack().isEmpty()) {
                admin.setTechStack("Full Stack Developer, Java, Spring Boot, PostgreSQL");
                userRepository.save(admin);
            }
        }

        // 3. Create Manager (Employee)
        User manager;
        if (userRepository.findByUsername("manager").isEmpty()) {
            manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("admin123"));
            manager.setEmail("manager@devNectar.com");
            manager.setFullName("David Manager");
            manager.setTechStack("Project Management, Backend Development"); // Added tech stack
            manager.setRoles(Collections.singleton(employeeRole));
            manager.setEnabled(true);
            manager.setRegistrationStatus("APPROVED");
            manager.setRegistrationDate(nowDate.withMonth(2).withDayOfMonth(1));
            manager = userRepository.save(manager);
        } else {
            manager = userRepository.findByUsername("manager").get();
            if (manager.getTechStack() == null || manager.getTechStack().isEmpty()) {
                manager.setTechStack("Project Management, Backend Development");
                userRepository.save(manager);
            }
        }

        // 4. Create Intern
        User intern;
        if (userRepository.findByUsername("intern").isEmpty()) {
            intern = new User();
            intern.setUsername("intern");
            intern.setPassword(passwordEncoder.encode("admin123"));
            intern.setEmail("alice@devNectar.com");
            intern.setFullName("Alice Intern");
            intern.setRoles(Collections.singleton(internRole));
            intern.setManager(manager);
            intern.setEnabled(true);
            intern.setRegistrationStatus("APPROVED");
            intern.setRegistrationDate(nowDate.isBefore(nowDate.withMonth(3).withDayOfMonth(10)) ? nowDate : nowDate.withMonth(3).withDayOfMonth(10));
            intern.setTechStack("Java, Spring Boot");
            intern = userRepository.save(intern);
        } else {
            intern = userRepository.findByUsername("intern").get();
        }

        // 5. Create Sample Reports
        if (reportRepository.count() == 0 && intern != null) {
            DailyReport r1 = DailyReport.builder()
                .intern(intern)
                .manager(manager)
                .title("Initial Backend Setup")
                .description("Configured Spring Security and implemented the User entity with manual boilerplate.")
                .hours(8.0)
                .submissionDate(LocalDate.now().minusDays(1))
                .status(DailyReport.ReportStatus.SUBMITTED)
                .build();
            reportRepository.save(r1);

            DailyReport r2 = DailyReport.builder()
                .intern(intern)
                .manager(manager)
                .title("Frontend Integration")
                .description("Connected the dashboard with the REST API using Axios.")
                .hours(6.5)
                .submissionDate(LocalDate.now())
                .status(DailyReport.ReportStatus.SUBMITTED)
                .build();
            reportRepository.save(r2);
        }

        // 6. Create Sample Attendance
        if (attendanceRepository.count() == 0 && intern != null) {
            Attendance a1 = Attendance.builder()
                .user(intern)
                .date(LocalDate.now().minusDays(1))
                .checkInTime(LocalTime.of(9, 0))
                .checkOutTime(LocalTime.of(18, 0))
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();
            attendanceRepository.save(a1);
        }
        
        userRepository.findAll().stream()
            .filter(u -> u.getRegistrationDate() == null)
            .forEach(u -> {
                u.setRegistrationDate(LocalDate.now());
                userRepository.save(u);
            });
    }

    private Role createRoleIfNotFound(String roleName) {
        return roleRepository.findAll().stream()
                .filter(r -> r.getName().equals(roleName))
                .findFirst()
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(roleName);
                    return roleRepository.save(r);
                });
    }
}
