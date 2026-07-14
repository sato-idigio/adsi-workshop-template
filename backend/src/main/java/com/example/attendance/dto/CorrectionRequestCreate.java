package com.example.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CorrectionRequestCreate(
    @NotNull(message = "打刻レコードIDは必須です")
    Long attendanceRecordId,

    LocalDateTime requestedClockIn,

    LocalDateTime requestedClockOut,

    @NotBlank(message = "理由は必須です")
    @Size(max = 500, message = "理由は500文字以内で入力してください")
    String reason
) {}
