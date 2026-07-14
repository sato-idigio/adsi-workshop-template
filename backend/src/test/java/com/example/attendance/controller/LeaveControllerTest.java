package com.example.attendance.controller;

import com.example.attendance.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;

    @BeforeEach
    void setUp() {
        adminToken = jwtUtil.generateToken("EMP001", "ADMIN");
    }

    @Test
    @DisplayName("POST /api/leaves - 休暇申請が作成される")
    void create_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/api/leaves")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "leaveType": "SPECIAL",
                                    "startDate": "2026-08-03",
                                    "endDate": "2026-08-03",
                                    "reason": "慶弔休暇"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leaveType").value("SPECIAL"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.days").value(1.0));
    }

    @Test
    @DisplayName("POST /api/leaves - バリデーションエラーで400")
    void create_invalidRequest_returns400() throws Exception {
        mockMvc.perform(post("/api/leaves")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "leaveType": "PAID",
                                    "startDate": "2026-08-01",
                                    "endDate": "2026-08-01",
                                    "reason": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/leaves - 認証済みユーザーが一覧取得")
    void list_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/leaves")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/leaves/balance - 残日数取得")
    void getBalance_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/leaves/balance")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("fiscalYear", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fiscalYear").value(2026))
                .andExpect(jsonPath("$.totalDays").value(20.0));
    }

    @Test
    @DisplayName("PUT /api/leaves/{id}/approve - 未認証で401")
    void approve_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/leaves/1/approve"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/leaves/{id}/approve - EMPLOYEE権限で403")
    void approve_employeeRole_returns403() throws Exception {
        String employeeToken = jwtUtil.generateToken("EMP002", "EMPLOYEE");

        mockMvc.perform(put("/api/leaves/1/approve")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }
}
