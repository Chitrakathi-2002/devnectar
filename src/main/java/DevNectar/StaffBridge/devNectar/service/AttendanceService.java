package DevNectar.StaffBridge.devNectar.service;

import DevNectar.StaffBridge.devNectar.entity.Attendance;
import DevNectar.StaffBridge.devNectar.entity.User;
import DevNectar.StaffBridge.devNectar.repository.AttendanceRepository;
import DevNectar.StaffBridge.devNectar.repository.UserRepository;
import DevNectar.StaffBridge.devNectar.dto.AttendanceStatsDTO;
import DevNectar.StaffBridge.devNectar.dto.UserAttendanceRecordDTO;
import DevNectar.StaffBridge.devNectar.dto.AttendanceSummaryDTO;
import DevNectar.StaffBridge.devNectar.dto.MatrixRowDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private static final LocalTime AUTO_CHECKIN_START = LocalTime.of(11, 0);
    private static final LocalTime AUTO_CHECKIN_END = LocalTime.of(11, 30);
    private static final LocalTime CHECKOUT_CUTOFF = LocalTime.of(18, 30);

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final DailyReportService dailyReportService;

    public AttendanceService(AttendanceRepository attendanceRepository, UserRepository userRepository, DailyReportService dailyReportService) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.dailyReportService = dailyReportService;
    }

    @Transactional
    public void punchOutWithReport(String username, String title, String description, Double hours) {
        // Submit the report
        dailyReportService.submitReport(username, title, description, hours);
        
        // Then perform the punch-out
        punchOut(username);
    }

    @Transactional
    public void checkAndAutoPunchIn(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Check if already has a record for today
        if (attendanceRepository.findByUserAndDate(user, today).isPresent()) {
            return;
        }

        // Auto Check-In: If login is after 11:00 AM
        if (now.isAfter(AUTO_CHECKIN_START)) {
            Attendance attendance = Attendance.builder()
                    .user(user)
                    .date(today)
                    .checkInTime(now)
                    .status(Attendance.AttendanceStatus.PRESENT)
                    .build();
            attendanceRepository.save(attendance);
        }
    }

    @Transactional
    public void requestLateArrival(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (attendanceRepository.findByUserAndDate(user, today).isPresent()) {
            throw new RuntimeException("Attendance record already exists for today");
        }

        Attendance attendance = Attendance.builder()
                .user(user)
                .date(today)
                .checkInTime(now)
                .status(Attendance.AttendanceStatus.PENDING_APPROVAL)
                .build();
        attendanceRepository.save(attendance);
    }

    @Scheduled(cron = "0 0 0 * * *") // Run at midnight
    @Transactional
    public void processEODHalfDayPenalties() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Attendance> incompleteSessions = attendanceRepository.findByDateAndCheckOutTimeIsNull(yesterday);
        
        for (Attendance attendance : incompleteSessions) {
            if (attendance.getStatus() != Attendance.AttendanceStatus.ABSENT) {
                attendance.setStatus(Attendance.AttendanceStatus.HALF_DAY);
                attendanceRepository.save(attendance);
            }
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAttendanceUIState(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        Optional<Attendance> attendanceOpt = attendanceRepository.findByUserAndDate(user, today);
        
        Map<String, Object> state = new HashMap<>();
        state.put("hasCheckedIn", attendanceOpt.isPresent());
        state.put("hasCheckedOut", attendanceOpt.isPresent() && attendanceOpt.get().getCheckOutTime() != null);
        state.put("status", attendanceOpt.map(Attendance::getStatus).orElse(null));
        
        // Time-based restrictions
        state.put("isAutoCheckInWindow", now.isAfter(AUTO_CHECKIN_START) && now.isBefore(AUTO_CHECKIN_END));
        state.put("isLateArrival", now.isAfter(AUTO_CHECKIN_END));
        state.put("isAfterCheckoutCutoff", now.isAfter(CHECKOUT_CUTOFF));
        state.put("checkInTime", attendanceOpt.map(a -> a.getCheckInTime() != null ? a.getCheckInTime().toString() : null).orElse(null));
        state.put("checkOutTime", attendanceOpt.map(a -> a.getCheckOutTime() != null ? a.getCheckOutTime().toString() : null).orElse(null));
        
        return state;
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public AttendanceStatsDTO getMonthlyStats(String username, int month, int year) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return new AttendanceStatsDTO(0, 0, 0, 0);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Attendance> records = attendanceRepository.findByUserAndDateBetween(userOpt.get(), start, end);
        
        int totalDays = yearMonth.lengthOfMonth();
        int present = (int) records.stream().filter(r -> r.getStatus() == Attendance.AttendanceStatus.PRESENT).count();
        int absent = (int) records.stream().filter(r -> r.getStatus() == Attendance.AttendanceStatus.ABSENT).count();
        double rate = totalDays > 0 ? (present * 100.0 / totalDays) : 0;

        return new AttendanceStatsDTO(totalDays, present, absent, rate);
    }

    public Map<LocalDate, Integer> getDailyLoginCounts(String username, int month, int year) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return Collections.emptyMap();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Attendance> records = attendanceRepository.findByUserAndDateBetween(userOpt.get(), start, end);
        java.util.Map<LocalDate, Integer> dailyCounts = new java.util.TreeMap<>();
        
        for (int i = 1; i <= yearMonth.lengthOfMonth(); i++) {
            dailyCounts.put(yearMonth.atDay(i), 0);
        }

        for (Attendance record : records) {
            dailyCounts.put(record.getDate(), 1);
        }

        return dailyCounts;
    }

    @Transactional(readOnly = true)
    public List<UserAttendanceRecordDTO> getAllUsersMonthlyAttendance(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        int totalDaysInMonth = yearMonth.lengthOfMonth();

        List<User> targetUsers = userRepository.findAll().stream()
                .filter(u -> !u.isDeleted())
                .filter(u -> !u.getUsername().equalsIgnoreCase("admin")) // Show all users except the main admin
                .collect(Collectors.toList());

        List<Attendance> allRecords = attendanceRepository.findByDateBetween(start, end);
        Map<Long, List<Attendance>> recordsByUser = allRecords.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        return targetUsers.stream().map(user -> {
            List<Attendance> userRecords = recordsByUser.getOrDefault(user.getId(), Collections.emptyList());
            Map<Integer, Attendance.AttendanceStatus> dailyStatus = new HashMap<>();
            
            int presentCount = 0;
            for (Attendance a : userRecords) {
                dailyStatus.put(a.getDate().getDayOfMonth(), a.getStatus());
                if (a.getStatus() == Attendance.AttendanceStatus.PRESENT) {
                    presentCount++;
                }
            }

            UserAttendanceRecordDTO dto = new UserAttendanceRecordDTO();
            dto.setFullName(user.getFullName());
            dto.setDailyStatus(dailyStatus);
            dto.setPresentCount(presentCount);
            dto.setTotalDays(totalDaysInMonth);
            dto.setAttendanceRate(totalDaysInMonth > 0 ? (presentCount * 100.0 / totalDaysInMonth) : 0.0);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatrixRowDTO> getAttendanceMatrix(int month, int year, Long userId) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate now = LocalDate.now();

        List<User> activeUsers = userRepository.findAll().stream()
                .filter(u -> u.isEnabled() && !u.isDeleted())
                .filter(u -> !u.getUsername().equalsIgnoreCase("admin"))
                .filter(u -> userId == null || u.getId().equals(userId))
                .collect(Collectors.toList());

        List<Attendance> allRecords = attendanceRepository.findByDateBetween(start, end);
        Map<Long, List<Attendance>> recordsByUser = allRecords.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        return activeUsers.stream().map(user -> {
            Map<Integer, String> dailyStatus = new HashMap<>();
            List<Attendance> userRecords = recordsByUser.getOrDefault(user.getId(), Collections.emptyList());
            Map<Integer, Attendance.AttendanceStatus> recordMap = userRecords.stream()
                    .collect(Collectors.toMap(a -> a.getDate().getDayOfMonth(), Attendance::getStatus, (s1, s2) -> s1));

            int presentCount = 0;
            int absentCount = 0;
            int pastDaysCount = 0;

            LocalDate regDate = user.getRegistrationDate();
            int registrationDay = 1;
            if (regDate != null) {
                if (regDate.getYear() > yearMonth.getYear() || 
                   (regDate.getYear() == yearMonth.getYear() && regDate.getMonthValue() > yearMonth.getMonthValue())) {
                    registrationDay = daysInMonth + 1; // Future registration
                } else if (regDate.getYear() == yearMonth.getYear() && regDate.getMonthValue() == yearMonth.getMonthValue()) {
                    registrationDay = regDate.getDayOfMonth();
                }
            }

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = yearMonth.atDay(day);
                Attendance.AttendanceStatus status = recordMap.get(day);
                
                // If the user hasn't registered yet by this date, skip it (leave blank)
                if (regDate != null && date.isBefore(regDate)) {
                    dailyStatus.put(day, "NOT_JOINED");
                    continue;
                }

                if (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                    dailyStatus.put(day, "SUNDAY");
                } else if (status != null) {
                    dailyStatus.put(day, status.name());
                    if (status == Attendance.AttendanceStatus.PRESENT) {
                        presentCount++;
                    } else if (status == Attendance.AttendanceStatus.ABSENT) {
                        absentCount++;
                    }
                } else {
                    if (date.isBefore(now) || date.isEqual(now)) {
                        dailyStatus.put(day, "ABSENT");
                        absentCount++;
                    } else {
                        dailyStatus.put(day, "PENDING");
                    }
                }
                
                if (!date.isAfter(now) && date.getDayOfWeek() != java.time.DayOfWeek.SUNDAY && (regDate == null || !date.isBefore(regDate))) {
                    pastDaysCount++;
                }
            }

            MatrixRowDTO row = new MatrixRowDTO();
            row.setFullName(user.getFullName());
            row.setDailyStatus(dailyStatus);
            row.setPresentCount(presentCount);
            row.setAbsentCount(absentCount);
            row.setRegistrationDay(registrationDay);
            
            double rate = pastDaysCount > 0 ? (double) presentCount / pastDaysCount * 100 : 0;
            row.setAttendanceRate(rate);
            
            row.setPresentToday(recordMap.get(now.getDayOfMonth()) == Attendance.AttendanceStatus.PRESENT && yearMonth.getMonth() == now.getMonth() && yearMonth.getYear() == now.getYear());
            return row;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryDTO getAggregateAttendanceSummary(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        
        List<Attendance> allAttendance = attendanceRepository.findByDateBetween(start, end);
        
        long present = allAttendance.stream().filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT).count();
        long absent = allAttendance.stream().filter(a -> a.getStatus() == Attendance.AttendanceStatus.ABSENT).count();

        AttendanceSummaryDTO summary = new AttendanceSummaryDTO();
        summary.setTotalDays(yearMonth.lengthOfMonth());
        summary.setPresentCount((int)present);
        summary.setAbsentCount((int)absent);
        
        long totalActiveUsers = userRepository.findAll().stream()
                .filter(u -> !u.isDeleted())
                .filter(u -> u.getRoles().stream().anyMatch(r -> {
                    String name = r.getName().toUpperCase();
                    return name.contains("EMPLOYEE") || name.contains("INTERN");
                }))
                .count();
        
        double totalPossibleRecords = (double)yearMonth.lengthOfMonth() * totalActiveUsers;
        summary.setAttendanceRate(totalPossibleRecords > 0 ? (present * 100.0 / totalPossibleRecords) : 0.0);
        
        return summary;
    }

    @Transactional(readOnly = true)
    public List<Attendance> getFilteredAttendance(Long userId, Integer month, Integer year) {
        LocalDate start = null;
        LocalDate end = null;
        
        if (month != null && year != null) {
            YearMonth ym = YearMonth.of(year, month);
            start = ym.atDay(1);
            end = ym.atEndOfMonth();
        }
        
        return attendanceRepository.getFilteredAttendance(userId, start, end).stream()
                .filter(a -> !a.getUser().getUsername().equalsIgnoreCase("admin"))
                .sorted(Comparator.comparing(Attendance::getDate).reversed()
                        .thenComparing(a -> a.getCheckInTime() != null ? a.getCheckInTime() : LocalTime.MIN, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<User> getAllActiveStaff() {
        return userRepository.findAll().stream()
                .filter(u -> !u.isDeleted())
                .filter(u -> u.getRoles().stream().anyMatch(r -> {
                    String name = r.getName().toUpperCase();
                    return name.contains("EMPLOYEE") || name.contains("INTERN");
                }))
                .sorted(Comparator.comparing(User::getFullName))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll().stream()
                .filter(a -> !a.getUser().getUsername().equalsIgnoreCase("admin"))
                .sorted(Comparator.comparing(Attendance::getDate).reversed()
                        .thenComparing(a -> a.getCheckInTime() != null ? a.getCheckInTime() : LocalTime.MIN, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void adminUpdateAttendance(Long id, String statusStr, String reason) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        
        Attendance.AttendanceStatus status = Attendance.AttendanceStatus.valueOf(statusStr.toUpperCase());
        attendance.setStatus(status);
        attendance.setAdminReason(reason);
        attendance.setModifiedByAdmin(true);
        attendanceRepository.save(attendance);
    }

    @Transactional
    public void punchIn(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate today = LocalDate.now();
        if (attendanceRepository.findByUserAndDate(user, today).isPresent()) {
            throw new RuntimeException("Already punched in for today");
        }
        
        Attendance attendance = Attendance.builder()
                .user(user)
                .date(today)
                .checkInTime(LocalTime.now())
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();
        attendanceRepository.save(attendance);
    }

    @Transactional
    public void punchOut(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByUserAndDate(user, today)
                .orElseThrow(() -> new RuntimeException("No punch-in record for today"));
        
        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Already punched out for today");
        }

        attendance.setCheckOutTime(LocalTime.now());
        attendanceRepository.save(attendance);
    }

    @Transactional(readOnly = true)
    public List<Attendance> getMyAttendance(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return attendanceRepository.findByUserOrderByDateDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Attendance> getTeamAttendance(Long managerId) {
        return attendanceRepository.findByManagerId(managerId);
    }
}
