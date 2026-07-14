package com.example.attendance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WorkTimeCalculatorTest {

    @Test
    @DisplayName("8時間丁度の勤務: 残業0分")
    void calculate_exactly8Hours_noOvertime() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 17, 0);

        var result = WorkTimeCalculator.calculate(clockIn, clockOut);

        assertThat(result.workMinutes()).isEqualTo(480);
        assertThat(result.overtimeMinutes()).isEqualTo(0);
        assertThat(result.nightMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("9時間勤務: 残業60分")
    void calculate_9Hours_60minOvertime() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 18, 0);

        var result = WorkTimeCalculator.calculate(clockIn, clockOut);

        assertThat(result.workMinutes()).isEqualTo(540);
        assertThat(result.overtimeMinutes()).isEqualTo(60);
        assertThat(result.nightMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("22時を跨ぐ勤務: 深夜時間が計算される")
    void calculate_crossing22_nightMinutesCalculated() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 20, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 23, 0);

        var result = WorkTimeCalculator.calculate(clockIn, clockOut);

        assertThat(result.workMinutes()).isEqualTo(180);
        assertThat(result.overtimeMinutes()).isEqualTo(0);
        assertThat(result.nightMinutes()).isEqualTo(60); // 22:00-23:00
    }

    @Test
    @DisplayName("深夜帯に完全に含まれる勤務")
    void calculate_entirelyNight_allNightMinutes() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 23, 0);
        var clockOut = LocalDateTime.of(2026, 7, 15, 2, 0);

        var result = WorkTimeCalculator.calculate(clockIn, clockOut);

        assertThat(result.workMinutes()).isEqualTo(180);
        assertThat(result.nightMinutes()).isEqualTo(180); // 23:00-02:00 全て深夜
    }

    @Test
    @DisplayName("5時を跨ぐ早朝勤務: 深夜時間は5時まで")
    void calculate_crossing5am_nightUntil5() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 3, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 7, 0);

        var result = WorkTimeCalculator.calculate(clockIn, clockOut);

        assertThat(result.workMinutes()).isEqualTo(240);
        assertThat(result.nightMinutes()).isEqualTo(120); // 03:00-05:00
    }

    @Test
    @DisplayName("日跨ぎで22:00-翌5:00を全てカバーする勤務")
    void calculate_overnightFullNight_7hoursNight() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 21, 0);
        var clockOut = LocalDateTime.of(2026, 7, 15, 6, 0);

        var result = WorkTimeCalculator.calculate(clockIn, clockOut);

        assertThat(result.workMinutes()).isEqualTo(540);
        assertThat(result.overtimeMinutes()).isEqualTo(60);
        assertThat(result.nightMinutes()).isEqualTo(420); // 22:00-05:00 = 7h
    }

    @Test
    @DisplayName("短時間勤務: 8時間未満で残業なし")
    void calculate_lessThan8Hours_noOvertime() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 10, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 14, 0);

        var result = WorkTimeCalculator.calculate(clockIn, clockOut);

        assertThat(result.workMinutes()).isEqualTo(240);
        assertThat(result.overtimeMinutes()).isEqualTo(0);
        assertThat(result.nightMinutes()).isEqualTo(0);
    }
}
