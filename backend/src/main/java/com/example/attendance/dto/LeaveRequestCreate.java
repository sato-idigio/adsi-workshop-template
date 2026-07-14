package com.example.attendance.dto;

import com.example.attendance.entity.enums.LeaveType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LeaveRequestCreate(
        @NotNull(message = "休暇種別は必須です")
        LeaveType leaveType,

        @NotNull(message = "開始日は必須です")
        LocalDate startDate,

        @NotNull(message = "終了日は必須です")
        LocalDate endDate,

        @NotNull(message = "理由は必須です")
        @Size(min = 1, max = 500, message = "理由は1〜500文字で入力してください")
        String reason
) {}
