package com.example.attendance_tracker;

public class AttendanceRecord {
    public String recordId, username, date, type, timeDisplay, location, status, remarks;
    public double paidHours, overtime, grossPay;

    public AttendanceRecord() {}

    public AttendanceRecord(String recordId, String username, String date, String type,
                            String timeDisplay, double paidHours, double overtime,
                            double grossPay, String status, String location, String remarks) {
        this.recordId = recordId;
        this.username = username;
        this.date = date;
        this.type = type;
        this.timeDisplay = timeDisplay;
        this.paidHours = paidHours;
        this.overtime = overtime;
        this.grossPay = grossPay;
        this.status = status;
        this.location = location;
        this.remarks = remarks;
    }
}
