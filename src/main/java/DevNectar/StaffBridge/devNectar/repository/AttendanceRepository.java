package DevNectar.StaffBridge.devNectar.repository;

import DevNectar.StaffBridge.devNectar.entity.Attendance;
import DevNectar.StaffBridge.devNectar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserAndDate(User user, LocalDate date);
    List<Attendance> findByDateAndCheckOutTimeIsNull(LocalDate date);
    long countByDateAndStatus(LocalDate date, Attendance.AttendanceStatus status);
    
    List<Attendance> findByUserOrderByDateDescCheckInTimeDesc(User user);
    List<Attendance> findByUserOrderByDateDesc(User user);

    @Query("SELECT a FROM Attendance a JOIN a.user u WHERE u.manager.id = :managerId AND a.date = :date")
    List<Attendance> findByManagerIdAndDate(@Param("managerId") Long managerId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a JOIN a.user u WHERE u.manager.id = :managerId ORDER BY a.date DESC, a.checkInTime DESC")
    List<Attendance> findByManagerId(@Param("managerId") Long managerId);

    List<Attendance> findByDateBetween(LocalDate start, LocalDate end);
    
    List<Attendance> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    @Query("SELECT a FROM Attendance a WHERE " +
           "(CAST(:userId AS long) IS NULL OR a.user.id = :userId) AND " +
           "(CAST(:startDate AS LocalDate) IS NULL OR a.date >= :startDate) AND " +
           "(CAST(:endDate AS LocalDate) IS NULL OR a.date <= :endDate) " +
           "ORDER BY a.date DESC, a.checkInTime DESC")
    List<Attendance> getFilteredAttendance(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
