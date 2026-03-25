package DevNectar.StaffBridge.CloudNex.repository;

import DevNectar.StaffBridge.CloudNex.entity.DailyReport;
import DevNectar.StaffBridge.CloudNex.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {
    List<DailyReport> findByInternOrderBySubmissionDateDesc(User intern);
    long countBySubmissionDate(LocalDate date);

    @Query("SELECT r FROM DailyReport r WHERE r.manager.id = :managerId AND r.status = :status")
    List<DailyReport> findByManagerAndStatus(@Param("managerId") Long managerId, @Param("status") DailyReport.ReportStatus status);

    @Query("SELECT r FROM DailyReport r WHERE r.manager.id = :managerId")
    List<DailyReport> findByManagerId(@Param("managerId") Long managerId);
}
