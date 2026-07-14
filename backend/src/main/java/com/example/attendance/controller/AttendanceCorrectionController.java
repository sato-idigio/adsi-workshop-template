package com.example.attendance.controller;

import com.example.attendance.dto.CorrectionRequestCreate;
import com.example.attendance.dto.CorrectionRequestResponse;
import com.example.attendance.service.AttendanceCorrectionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/attendance-corrections")
public class AttendanceCorrectionController {

    private final AttendanceCorrectionService correctionService;

    public AttendanceCorrectionController(AttendanceCorrectionService correctionService) {
        this.correctionService = correctionService;
    }

    @PostMapping
    public ResponseEntity<CorrectionRequestResponse> create(
            Principal principal,
            @Valid @RequestBody CorrectionRequestCreate request) {
        var result = correctionService.create(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public ResponseEntity<Page<CorrectionRequestResponse>> list(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var role = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "ADMIN" : "EMPLOYEE";
        var result = correctionService.list(authentication.getName(), role, status, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<CorrectionRequestResponse> approve(
            Principal principal,
            @PathVariable Long id) {
        var result = correctionService.approve(principal.getName(), id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<CorrectionRequestResponse> reject(
            Principal principal,
            @PathVariable Long id) {
        var result = correctionService.reject(principal.getName(), id);
        return ResponseEntity.ok(result);
    }
}
