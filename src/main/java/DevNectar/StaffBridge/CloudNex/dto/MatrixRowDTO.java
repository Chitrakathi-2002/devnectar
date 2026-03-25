package DevNectar.StaffBridge.CloudNex.dto;

import java.util.Map;

public class MatrixRowDTO {
    private String fullName;
    private Map<Integer, String> dailyStatus; // DayOfMonth -> Status (PRESENT, ABSENT, PENDING)
    private int presentCount;
    private int absentCount;
    private double attendanceRate;
    private boolean presentToday;
    private int registrationDay; // The first day of the month the user is eligible for attendance

    public MatrixRowDTO() {}

    public MatrixRowDTO(String fullName, Map<Integer, String> dailyStatus) {
        this.fullName = fullName;
        this.dailyStatus = dailyStatus;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Map<Integer, String> getDailyStatus() {
        return dailyStatus;
    }

    public void setDailyStatus(Map<Integer, String> dailyStatus) {
        this.dailyStatus = dailyStatus;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public double getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(double attendanceRate) {
        this.attendanceRate = attendanceRate;
    }

    public boolean isPresentToday() { return presentToday; }
    public void setPresentToday(boolean presentToday) { this.presentToday = presentToday; }

    public int getRegistrationDay() { return registrationDay; }
    public void setRegistrationDay(int registrationDay) { this.registrationDay = registrationDay; }
}
