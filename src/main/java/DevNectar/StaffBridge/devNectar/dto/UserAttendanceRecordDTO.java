package DevNectar.StaffBridge.devNectar.dto;

import DevNectar.StaffBridge.devNectar.entity.Attendance;
import java.util.Map;

public class UserAttendanceRecordDTO {
    private String fullName;
    private String username;
    private String managerName;
    private Map<Integer, Attendance.AttendanceStatus> dailyStatus; // DayOfMonth -> Status
    private int presentCount;
    private int totalDays;
    private double attendanceRate;

    public UserAttendanceRecordDTO() {}

    public UserAttendanceRecordDTO(String fullName, String username, String managerName, Map<Integer, Attendance.AttendanceStatus> dailyStatus, int presentCount, int totalDays, double attendanceRate) {
        this.fullName = fullName;
        this.username = username;
        this.managerName = managerName;
        this.dailyStatus = dailyStatus;
        this.presentCount = presentCount;
        this.totalDays = totalDays;
        this.attendanceRate = attendanceRate;
    }

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public Map<Integer, Attendance.AttendanceStatus> getDailyStatus() { return dailyStatus; }
    public void setDailyStatus(Map<Integer, Attendance.AttendanceStatus> dailyStatus) { this.dailyStatus = dailyStatus; }
    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }
    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }
    public double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
}
