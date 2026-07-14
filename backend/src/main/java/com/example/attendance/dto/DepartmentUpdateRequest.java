package com.example.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepartmentUpdateRequest(
        @NotBlank(message = "部署名は必須です") @Size(max = 100) String name,
        Long parentId,
        @NotNull(message = "バージョンは必須です") Long version
) {
}
