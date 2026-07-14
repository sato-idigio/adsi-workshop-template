package com.example.attendance.controller;

import com.example.attendance.dto.LeaveBalanceResponse;
import com.example.attendance.dto.LeaveRequestCreate;
import com.example.attendance.dto.LeaveRequestResponse;
import com.example.attendance.entity.enums.ApprovalStatus;
import com.example.attendance.service.LeaveService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> create(
            @Valid @RequestBody LeaveRequestCreate request,
            Authentication authentication) {
        LeaveRequestResponse response = leaveService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<LeaveRequestResponse>> list(
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        Page<LeaveRequestResponse> result;
        if (isAdmin) {
            result = leaveService.findAll(status, PageRequest.of(page, size));
        } else {
            result = leaveService.findByEmployee(authentication.getName(), status, PageRequest.of(page, size));
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestResponse> approve(
            @PathVariable Long id,
            Authentication authentication) {
        LeaveRequestResponse response = leaveService.approve(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestResponse> reject(
            @PathVariable Long id,
            Authentication authentication) {
        LeaveRequestResponse response = leaveService.reject(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<LeaveBalanceResponse> getBalance(
            @RequestParam(required = false) Integer fiscalYear,
            Authentication authentication) {
        LeaveBalanceResponse response = leaveService.getBalance(authentication.getName(), fiscalYear);
        return ResponseEntity.ok(response);
    }
}
