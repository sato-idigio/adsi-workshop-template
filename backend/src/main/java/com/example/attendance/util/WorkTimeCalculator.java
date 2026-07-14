package com.example.attendance.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public final class WorkTimeCalculator {

    private static final int STANDARD_WORK_MINUTES = 480;
    private static final LocalTime NIGHT_START = LocalTime.of(22, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(5, 0);

    private WorkTimeCalculator() {}

    public record Result(int workMinutes, int overtimeMinutes, int nightMinutes) {}

    public static Result calculate(LocalDateTime clockIn, LocalDateTime clockOut) {
        int workMinutes = (int) ChronoUnit.MINUTES.between(clockIn, clockOut);
        int overtimeMinutes = Math.max(0, workMinutes - STANDARD_WORK_MINUTES);
        int nightMinutes = calculateNightMinutes(clockIn, clockOut);

        return new Result(workMinutes, overtimeMinutes, nightMinutes);
    }

    private static int calculateNightMinutes(LocalDateTime start, LocalDateTime end) {
        int totalNightMinutes = 0;
        LocalDateTime cursor = start;

        while (cursor.isBefore(end)) {
            LocalDateTime dayNightStart = cursor.toLocalDate().atTime(NIGHT_START);
            LocalDateTime dayNightEnd = cursor.toLocalDate().plusDays(1).atTime(NIGHT_END);

            LocalTime cursorTime = cursor.toLocalTime();

            if (cursorTime.isBefore(NIGHT_END)) {
                // Currently in early-morning night period (00:00-05:00)
                LocalDateTime periodEnd = cursor.toLocalDate().atTime(NIGHT_END);
                LocalDateTime overlapEnd = end.isBefore(periodEnd) ? end : periodEnd;
                totalNightMinutes += (int) ChronoUnit.MINUTES.between(cursor, overlapEnd);
                cursor = overlapEnd;
            } else if (cursorTime.isBefore(NIGHT_START)) {
                // Currently in daytime (05:00-22:00), skip to 22:00
                LocalDateTime nextNight = dayNightStart;
                cursor = end.isBefore(nextNight) ? end : nextNight;
            } else {
                // Currently in evening night period (22:00-23:59)
                LocalDateTime overlapEnd = end.isBefore(dayNightEnd) ? end : dayNightEnd;
                totalNightMinutes += (int) ChronoUnit.MINUTES.between(cursor, overlapEnd);
                cursor = overlapEnd;
            }
        }

        return totalNightMinutes;
    }
}
