package DevNectar.StaffBridge.CloudNex.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
public class Attendance {
    public Attendance() {}

    public Attendance(Long id, User user, LocalDate date, LocalTime checkInTime, LocalTime checkOutTime, AttendanceStatus status, boolean modifiedByAdmin, String adminReason) {
        this.id = id;
        this.user = user;
        this.date = date;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.modifiedByAdmin = modifiedByAdmin;
        this.adminReason = adminReason;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    private boolean modifiedByAdmin = false;
    private String adminReason;

    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE, HALF_DAY, PENDING_APPROVAL
    }

    // Static builder for convenience (replacing @Builder)
    public static AttendanceBuilder builder() {
        return new AttendanceBuilder();
    }

    public static class AttendanceBuilder {
        private Long id;
        private User user;
        private LocalDate date;
        private LocalTime checkInTime;
        private LocalTime checkOutTime;
        private AttendanceStatus status;
        private boolean modifiedByAdmin = false;
        private String adminReason;

        public AttendanceBuilder id(Long id) { this.id = id; return this; }
        public AttendanceBuilder user(User user) { this.user = user; return this; }
        public AttendanceBuilder date(LocalDate date) { this.date = date; return this; }
        public AttendanceBuilder checkInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; return this; }
        public AttendanceBuilder checkOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; return this; }
        public AttendanceBuilder status(AttendanceStatus status) { this.status = status; return this; }
        public AttendanceBuilder modifiedByAdmin(boolean modifiedByAdmin) { this.modifiedByAdmin = modifiedByAdmin; return this; }
        public AttendanceBuilder adminReason(String adminReason) { this.adminReason = adminReason; return this; }

        public Attendance build() {
            return new Attendance(id, user, date, checkInTime, checkOutTime, status, modifiedByAdmin, adminReason);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }
    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public boolean isModifiedByAdmin() { return modifiedByAdmin; }
    public void setModifiedByAdmin(boolean modifiedByAdmin) { this.modifiedByAdmin = modifiedByAdmin; }
    public String getAdminReason() { return adminReason; }
    public void setAdminReason(String adminReason) { this.adminReason = adminReason; return; }
}
