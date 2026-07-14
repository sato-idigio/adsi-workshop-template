package com.example.attendance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil(
            "test-secret-key-that-is-at-least-256-bits-long-for-HS256-algorithm-testing",
            3600000L
    );

    @Test
    @DisplayName("トークンを生成して検証できる")
    void generateAndValidateToken() {
        String token = jwtUtil.generateToken("EMP001", "ADMIN");

        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.getEmployeeNumber(token)).isEqualTo("EMP001");
        assertThat(jwtUtil.getRole(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("不正なトークンは検証失敗する")
    void invalidToken_returnsFalse() {
        assertThat(jwtUtil.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("BCryptでpassword123のハッシュを生成して表示")
    void generateBcryptHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("password123");
        System.out.println("BCrypt hash for password123: " + hash);
        assertThat(encoder.matches("password123", hash)).isTrue();
    }
}
