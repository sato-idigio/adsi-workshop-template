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
    private String employeeToken;

    @BeforeEach
    void setUp() {
        adminToken = jwtUtil.generateToken("EMP001", "ADMIN");
        employeeToken = jwtUtil.generateToken("EMP002", "EMPLOYEE");
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
    @DisplayName("POST /api/leaves - バリデーションエラーで400（理由が空）")
    void create_emptyReason_returns400() throws Exception {
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
    @DisplayName("POST /api/leaves - 終了日が開始日より前で400")
    void create_endDateBeforeStartDate_returns400() throws Exception {
        mockMvc.perform(post("/api/leaves")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "leaveType": "PAID",
                                    "startDate": "2026-08-05",
                                    "endDate": "2026-08-01",
                                    "reason": "テスト休暇"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/leaves - 未認証で401")
    void create_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "leaveType": "SPECIAL",
                                    "startDate": "2026-08-03",
                                    "endDate": "2026-08-03",
                                    "reason": "テスト"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/leaves - ADMIN が全件取得")
    void list_admin_returnsAll() throws Exception {
        mockMvc.perform(get("/api/leaves")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/leaves - EMPLOYEE が一覧取得（社員未登録時は404）")
    void list_employee_notRegistered_returns404() throws Exception {
        mockMvc.perform(get("/api/leaves")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/leaves - ステータスフィルタ付き")
    void list_withStatusFilter_returnsFiltered() throws Exception {
        mockMvc.perform(get("/api/leaves")
                        .param("status", "PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/leaves - ページネーションパラメータ")
    void list_withPagination_returnsPage() throws Exception {
        mockMvc.perform(get("/api/leaves")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5));
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
        mockMvc.perform(put("/api/leaves/1/approve")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/leaves/{id}/reject - 未認証で401")
    void reject_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/leaves/1/reject"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/leaves/{id}/reject - EMPLOYEE権限で403")
    void reject_employeeRole_returns403() throws Exception {
        mockMvc.perform(put("/api/leaves/1/reject")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }
}
