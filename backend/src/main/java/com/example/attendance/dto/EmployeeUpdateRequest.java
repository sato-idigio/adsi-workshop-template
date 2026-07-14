package com.example.attendance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeeUpdateRequest(
        @NotBlank(message = "姓は必須です") @Size(max = 50) String lastName,
        @NotBlank(message = "名は必須です") @Size(max = 50) String firstName,
        @NotBlank(message = "メールは必須です") @Email String email,
        @NotNull(message = "部署IDは必須です") Long departmentId,
        @Size(max = 100) String position,
        @NotBlank(message = "ロールは必須です") String role,
        @NotNull(message = "入社日は必須です") LocalDate hireDate,
        @NotNull(message = "バージョンは必須です") Long version
) {
}
