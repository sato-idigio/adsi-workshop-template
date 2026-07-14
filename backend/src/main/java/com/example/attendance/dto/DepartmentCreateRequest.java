package com.example.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentCreateRequest(
        @NotBlank(message = "部署名は必須です") @Size(max = 100) String name,
        Long parentId
) {
}
