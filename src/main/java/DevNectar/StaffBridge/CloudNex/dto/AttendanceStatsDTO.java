package DevNectar.StaffBridge.CloudNex.dto;

public class AttendanceStatsDTO {
    private int totalDays;
    private int presentCount;
    private int absentCount;
    private double attendanceRate;

    public AttendanceStatsDTO(int totalDays, int presentCount, int absentCount, double attendanceRate) {
        this.totalDays = totalDays;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.attendanceRate = attendanceRate;
    }

    // Getters and Setters
    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }
    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }
    public int getAbsentCount() { return absentCount; }
    public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }
    public double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
}
