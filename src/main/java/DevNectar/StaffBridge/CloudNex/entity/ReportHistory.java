package DevNectar.StaffBridge.CloudNex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_history")
public class ReportHistory {
    public ReportHistory() {}

    public ReportHistory(DailyReport report, DailyReport.ReportStatus fromStatus, DailyReport.ReportStatus toStatus, User changedBy, String comment) {
        this.report = report;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.comment = comment;
        this.timestamp = LocalDateTime.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private DailyReport report;

    @Enumerated(EnumType.STRING)
    private DailyReport.ReportStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private DailyReport.ReportStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id")
    private User changedBy;

    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String comment;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DailyReport getReport() { return report; }
    public void setReport(DailyReport report) { this.report = report; }
    public DailyReport.ReportStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(DailyReport.ReportStatus fromStatus) { this.fromStatus = fromStatus; }
    public DailyReport.ReportStatus getToStatus() { return toStatus; }
    public void setToStatus(DailyReport.ReportStatus toStatus) { this.toStatus = toStatus; }
    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
