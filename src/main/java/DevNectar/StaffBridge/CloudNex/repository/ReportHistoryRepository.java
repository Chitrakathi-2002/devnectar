package DevNectar.StaffBridge.CloudNex.repository;

import DevNectar.StaffBridge.CloudNex.entity.ReportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportHistoryRepository extends JpaRepository<ReportHistory, Long> {
    List<ReportHistory> findByReportIdOrderByTimestampDesc(Long reportId);
}
