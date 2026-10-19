package com.nexfit.auth;

import com.nexfit.auth.AuthService.LoginResult;
import com.nexfit.auth.AuthService.PasswordChangeResult;
import com.nexfit.shared.SessionUserFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rutas de /api/auth: login (pública), logout y cambio de contraseña (con sesión). El prefijo /api es el context-path. */
@RestController
@RequestMapping("/auth")
class AuthController {

    record LoginRequest(String email, String password) { }

    record ChangePasswordRequest(String currentPassword, String newPassword) { }

    private final AuthService auth;

    AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest body, HttpServletRequest request) {
        Optional<LoginResult> result = auth.login(body.email(), body.password(), request.getRemoteAddr());
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciales inválidas"));
        }
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionUserFilter.USER_ID_ATTRIBUTE, result.get().userId());
        // Permite a Spring Session encontrar todas las sesiones de un usuario (para revocarlas al cambiar la contraseña).
        session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
            String.valueOf(result.get().userId()));
        return ResponseEntity.ok(Map.of("userId", result.get().userId(),
            "mustChangePassword", result.get().mustChangePassword()));
    }

    /** Cambio de contraseña (temporal o voluntario) de quien tiene la sesión abierta. */
    @PostMapping("/password")
    ResponseEntity<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequest body,
                                                        HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute(SessionUserFilter.USER_ID_ATTRIBUTE);
        if (!(userId instanceof Long id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PasswordChangeResult result = auth.changePassword(id, body.currentPassword(), body.newPassword(),
            session.getId());
        return switch (result) {
            case CHANGED -> ResponseEntity.noContent().build();
            case TOO_SHORT -> badRequest("La contraseña debe tener al menos "
                + AuthService.MIN_PASSWORD_LENGTH + " caracteres");
            case WRONG_CURRENT -> badRequest("La contraseña actual no es correcta");
            case SAME_AS_CURRENT -> badRequest("La contraseña nueva debe ser distinta de la actual");
        };
    }

    private static ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
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