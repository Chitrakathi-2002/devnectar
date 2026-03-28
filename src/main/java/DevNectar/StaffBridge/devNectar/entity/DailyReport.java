package DevNectar.StaffBridge.devNectar.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_reports")
public class DailyReport {
    public DailyReport() {}

    public DailyReport(Long id, User intern, User manager, String title, String description, Double hours, LocalDate submissionDate, ReportStatus status, String managerFeedback) {
        this.id = id;
        this.intern = intern;
        this.manager = manager;
        this.title = title;
        this.description = description;
        this.hours = hours;
        this.submissionDate = submissionDate;
        this.status = status;
        this.managerFeedback = managerFeedback;
        this.history = new ArrayList<>(); // Ensure initialization
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "intern_id", nullable = false)
    private User intern;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private Double hours;

    @Column(nullable = false)
    private LocalDate submissionDate;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.SUBMITTED;

    @Column(columnDefinition = "TEXT")
    private String managerFeedback;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportHistory> history = new ArrayList<>();

    public enum ReportStatus {
        SUBMITTED, PENDING, APPROVED, REVISION_REQUESTED, CANCELLED, UNDER_REVIEW
    }

    // Static builder (manual)
    public static DailyReportBuilder builder() {
        return new DailyReportBuilder();
    }

    public static class DailyReportBuilder {
        private Long id;
        private User intern;
        private User manager;
        private String title;
        private String description;
        private Double hours;
        private LocalDate submissionDate;
        private ReportStatus status = ReportStatus.SUBMITTED;
        private String managerFeedback;

        public DailyReportBuilder id(Long id) { this.id = id; return this; }
        public DailyReportBuilder intern(User intern) { this.intern = intern; return this; }
        public DailyReportBuilder manager(User manager) { this.manager = manager; return this; }
        public DailyReportBuilder title(String title) { this.title = title; return this; }
        public DailyReportBuilder description(String description) { this.description = description; return this; }
        public DailyReportBuilder hours(Double hours) { this.hours = hours; return this; }
        public DailyReportBuilder submissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; return this; }
        public DailyReportBuilder status(ReportStatus status) { this.status = status; return this; }
        public DailyReportBuilder managerFeedback(String managerFeedback) { this.managerFeedback = managerFeedback; return this; }

        public DailyReport build() {
            return new DailyReport(id, intern, manager, title, description, hours, submissionDate, status, managerFeedback);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getIntern() { return intern; }
    public void setIntern(User intern) { this.intern = intern; }
    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getHours() { return hours; }
    public void setHours(Double hours) { this.hours = hours; }
    public LocalDate getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public String getManagerFeedback() { return managerFeedback; }
    public void setManagerFeedback(String managerFeedback) { this.managerFeedback = managerFeedback; }
    public List<ReportHistory> getHistory() { return history; }
    public void setHistory(List<ReportHistory> history) { this.history = history; }
}
