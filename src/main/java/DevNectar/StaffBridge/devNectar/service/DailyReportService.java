package DevNectar.StaffBridge.devNectar.service;

import DevNectar.StaffBridge.devNectar.entity.DailyReport;
import DevNectar.StaffBridge.devNectar.entity.ReportHistory;
import DevNectar.StaffBridge.devNectar.entity.User;
import DevNectar.StaffBridge.devNectar.repository.DailyReportRepository;
import DevNectar.StaffBridge.devNectar.repository.ReportHistoryRepository;
import DevNectar.StaffBridge.devNectar.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DailyReportService {
    private static final Logger logger = LoggerFactory.getLogger(DailyReportService.class);

    private final DailyReportRepository dailyReportRepository;
    private final UserRepository userRepository;
    private final ReportHistoryRepository reportHistoryRepository;

    public DailyReportService(DailyReportRepository dailyReportRepository, UserRepository userRepository, ReportHistoryRepository reportHistoryRepository) {
        this.dailyReportRepository = dailyReportRepository;
        this.userRepository = userRepository;
        this.reportHistoryRepository = reportHistoryRepository;
    }

    @Transactional
    public void submitReport(String username, String title, String description, Double hours) {
        User intern = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Intern not found"));
        
        DailyReport report = DailyReport.builder()
                .intern(intern)
                .manager(intern.getManager())
                .title(title)
                .description(description)
                .hours(hours)
                .submissionDate(LocalDate.now())
                .status(DailyReport.ReportStatus.SUBMITTED)
                .build();
        
        DailyReport savedReport = dailyReportRepository.save(report);
        logHistory(savedReport, null, DailyReport.ReportStatus.SUBMITTED, intern, "Initial Submission");
    }

    @Transactional
    public void updateAndResubmitReport(Long reportId, String title, String description, Double hours) {
        DailyReport report = dailyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        if (report.getStatus() != DailyReport.ReportStatus.REVISION_REQUESTED) {
            throw new RuntimeException("Only reports marked for revision can be edited and resubmitted.");
        }

        DailyReport.ReportStatus oldStatus = report.getStatus();
        report.setTitle(title);
        report.setDescription(description);
        report.setHours(hours);
        report.setStatus(DailyReport.ReportStatus.SUBMITTED);
        
        dailyReportRepository.save(report);
        logHistory(report, oldStatus, DailyReport.ReportStatus.SUBMITTED, report.getIntern(), "Resubmitted after revision");
    }

    public List<DailyReport> getInternReports(String username) {
        User intern = userRepository.findByUsername(username).orElseThrow();
        return dailyReportRepository.findByInternOrderBySubmissionDateDesc(intern);
    }

    public List<DailyReport> getPendingReportsForManager(String username) {
        User manager = userRepository.findByUsername(username).orElseThrow();
        return dailyReportRepository.findByManagerAndStatus(manager.getId(), DailyReport.ReportStatus.SUBMITTED);
    }

    public List<DailyReport> getApprovedReportsForManager(String username) {
        User manager = userRepository.findByUsername(username).orElseThrow();
        return dailyReportRepository.findByManagerAndStatus(manager.getId(), DailyReport.ReportStatus.APPROVED);
    }

    public List<DailyReport> getAllReports() {
        return dailyReportRepository.findAll();
    }

    @Transactional
    public void processManagerAction(Long reportId, String managerUsername, String action, String feedback) {
        DailyReport report = dailyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        User manager = userRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        DailyReport.ReportStatus oldStatus = report.getStatus();
        DailyReport.ReportStatus newStatus;

        switch (action.toUpperCase()) {
            case "APPROVE":
                newStatus = DailyReport.ReportStatus.APPROVED;
                break;
            case "REVISION":
                newStatus = DailyReport.ReportStatus.REVISION_REQUESTED;
                report.setManagerFeedback(feedback);
                break;
            case "CANCEL":
                newStatus = DailyReport.ReportStatus.CANCELLED;
                report.setManagerFeedback(feedback);
                break;
            case "REVIEW":
                newStatus = DailyReport.ReportStatus.UNDER_REVIEW;
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }

        report.setStatus(newStatus);
        dailyReportRepository.save(report);
        logHistory(report, oldStatus, newStatus, manager, feedback);
        
        logger.info("Report [ID: {}] changed from {} to {} by {}", reportId, oldStatus, newStatus, managerUsername);
    }

    private void logHistory(DailyReport report, DailyReport.ReportStatus from, DailyReport.ReportStatus to, User user, String comment) {
        ReportHistory history = new ReportHistory(report, from, to, user, comment);
        reportHistoryRepository.save(history);
    }

    public List<ReportHistory> getReportHistory(Long reportId) {
        return reportHistoryRepository.findByReportIdOrderByTimestampDesc(reportId);
    }
}
