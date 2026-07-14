package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceRecordResponse> clockIn(Principal principal) {
        var result = attendanceService.clockIn(principal.getName());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceRecordResponse> clockOut(Principal principal) {
        var result = attendanceService.clockOut(principal.getName());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/today")
    public ResponseEntity<AttendanceRecordResponse> getToday(Principal principal) {
        var result = attendanceService.getToday(principal.getName());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<AttendanceRecordResponse>> getMonthly(
            Principal principal,
            @RequestParam String yearMonth) {
        var result = attendanceService.getMonthly(principal.getName(), yearMonth);
        return ResponseEntity.ok(result);
    }
}
