package com.nexfit.auth;

import com.nexfit.shared.SessionUserFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rutas públicas: /api/auth/login y /api/auth/logout (el prefijo /api es el context-path). */
@RestController
@RequestMapping("/auth")
class AuthController {

    record LoginRequest(String email, String password) { }

    private final AuthService auth;

    AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest body, HttpServletRequest request) {
        Optional<Long> userId = auth.login(body.email(), body.password(), request.getRemoteAddr());
        if (userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciales inválidas"));
        }
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        request.getSession(true).setAttribute(SessionUserFilter.USER_ID_ATTRIBUTE, userId.get());
        return ResponseEntity.ok(Map.of("userId", userId.get()));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute(SessionUserFilter.USER_ID_ATTRIBUTE);
        if (!(userId instanceof Long id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        auth.logout(id, request.getRemoteAddr());
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
