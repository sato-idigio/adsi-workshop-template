package com.example.attendance.repository;

import com.example.attendance.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("社員番号で社員を検索できる")
    void findByEmployeeNumber_existingNumber_returnsEmployee() {
        Optional<Employee> result = employeeRepository.findByEmployeeNumber("EMP001");

        assertThat(result).isPresent();
        assertThat(result.get().getLastName()).isEqualTo("管理者");
        assertThat(result.get().getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("存在しない社員番号で検索すると空を返す")
    void findByEmployeeNumber_nonExisting_returnsEmpty() {
        Optional<Employee> result = employeeRepository.findByEmployeeNumber("NONEXIST");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("社員番号の重複チェックができる")
    void existsByEmployeeNumber_existing_returnsTrue() {
        assertThat(employeeRepository.existsByEmployeeNumber("EMP001")).isTrue();
        assertThat(employeeRepository.existsByEmployeeNumber("EMP999")).isFalse();
    }

    @Test
    @DisplayName("メールアドレスの重複チェックができる")
    void existsByEmail_existing_returnsTrue() {
        assertThat(employeeRepository.existsByEmail("admin@example.com")).isTrue();
        assertThat(employeeRepository.existsByEmail("nobody@example.com")).isFalse();
    }
}
