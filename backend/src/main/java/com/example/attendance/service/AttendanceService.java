package com.example.attendance.service;

import com.example.attendance.dto.AttendanceRecordResponse;

import java.util.List;

public interface AttendanceService {

    AttendanceRecordResponse clockIn(String employeeNumber);

    AttendanceRecordResponse clockOut(String employeeNumber);

    AttendanceRecordResponse getToday(String employeeNumber);

    List<AttendanceRecordResponse> getMonthly(String employeeNumber, String yearMonth);
}
