package com.example.attendance.repository;

import com.example.attendance.entity.Department;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    @DisplayName("最上位部署（parent_id=NULL）を取得できる")
    void findByParentIsNull_returnsTopLevelDepartments() {
        List<Department> roots = departmentRepository.findByParentIsNull();

        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getName()).isEqualTo("本社");
        assertThat(roots.get(0).getLevel()).isEqualTo(1);
    }

    @Test
    @DisplayName("親部署IDで子部署を取得できる")
    void findByParentId_returnsChildDepartments() {
        List<Department> children = departmentRepository.findByParent_Id(1L);

        assertThat(children).hasSize(2);
        assertThat(children).extracting(Department::getName)
                .containsExactlyInAnyOrder("管理部", "開発部");
    }

    @Test
    @DisplayName("レベルで部署を取得できる")
    void findByLevel_returnsDepartmentsAtLevel() {
        List<Department> level2 = departmentRepository.findByLevel(2);

        assertThat(level2).hasSize(2);
    }
}
