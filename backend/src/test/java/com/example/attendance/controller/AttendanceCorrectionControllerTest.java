package com.example.attendance.controller;

import com.example.attendance.util.JwtUtil;
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
class AttendanceCorrectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken() {
        return jwtUtil.generateToken("EMP001", "ADMIN");
    }

    @Test
    @DisplayName("POST /api/attendance-corrections: 打刻後に修正申請を作成できる")
    void create_afterClockIn_returns201() throws Exception {
        var token = adminToken();

        // まず出勤打刻
        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", "Bearer " + token));

        // 修正申請（出勤打刻レコードID=1を想定）
        mockMvc.perform(post("/api/attendance-corrections")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "attendanceRecordId": 1,
                                  "requestedClockIn": "2026-07-14T08:30:00",
                                  "requestedClockOut": "2026-07-14T18:00:00",
                                  "reason": "出勤時刻修正"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.reason").value("出勤時刻修正"));
    }

    @Test
    @DisplayName("POST /api/attendance-corrections: 存在しないレコードで404")
    void create_recordNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/attendance-corrections")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "attendanceRecordId": 9999,
                                  "reason": "修正理由"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/attendance-corrections: 申請一覧を取得できる")
    void list_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/attendance-corrections")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("POST /api/attendance-corrections: 未認証で401")
    void create_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/attendance-corrections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"attendanceRecordId": 1, "reason": "修正"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/attendance-corrections/{id}/approve: 存在しない申請で404")
    void approve_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/attendance-corrections/9999/approve")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isNotFound());
    }
}
