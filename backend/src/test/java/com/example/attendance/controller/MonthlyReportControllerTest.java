package com.example.attendance.controller;

import com.example.attendance.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MonthlyReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;
    private String employeeToken;

    @BeforeEach
    void setUp() {
        adminToken = jwtUtil.generateToken("EMP001", "ADMIN");
        employeeToken = jwtUtil.generateToken("EMP002", "EMPLOYEE");
    }

    @Test
    @DisplayName("GET /api/reports/monthly - 管理者は200で結果を取得できる")
    void getMonthlyReport_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("yearMonth", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/reports/monthly - 一般社員は403")
    void getMonthlyReport_employee_returns403() throws Exception {
        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + employeeToken)
                        .param("yearMonth", "2026-07"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/reports/monthly - 未認証は401")
    void getMonthlyReport_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/reports/monthly")
                        .param("yearMonth", "2026-07"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/reports/monthly - yearMonth未指定は400")
    void getMonthlyReport_missingYearMonth_returns400() throws Exception {
        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/reports/monthly - departmentIdフィルターで絞り込み")
    void getMonthlyReport_withDepartmentFilter_returns200() throws Exception {
        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("yearMonth", "2026-07")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("POST /api/reports/monthly/generate - 管理者は集計を生成できる")
    void generateReport_admin_returns200() throws Exception {
        mockMvc.perform(post("/api/reports/monthly/generate")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("yearMonth", "2026-07"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/reports/monthly/generate - 一般社員は403")
    void generateReport_employee_returns403() throws Exception {
        mockMvc.perform(post("/api/reports/monthly/generate")
                        .header("Authorization", "Bearer " + employeeToken)
                        .param("yearMonth", "2026-07"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/reports/monthly/generate - 未認証は401")
    void generateReport_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/reports/monthly/generate")
                        .param("yearMonth", "2026-07"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/reports/monthly/generate - yearMonth未指定は400")
    void generateReport_missingYearMonth_returns400() throws Exception {
        mockMvc.perform(post("/api/reports/monthly/generate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reports/monthly/generate - 生成後にGETで結果が取得できる")
    void generateAndGet_returnsGeneratedData() throws Exception {
        mockMvc.perform(post("/api/reports/monthly/generate")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("yearMonth", "2026-06"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reports/monthly")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("yearMonth", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].totalWorkMinutes").value(0))
                .andExpect(jsonPath("$.content[0].workingDays").value(0));
    }
}
