package com.nexfit.auth;

import com.nexfit.audit.AuditEventType;
import com.nexfit.audit.AuditService;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Inicio y cierre de sesión (US-01). No lanza excepciones por credenciales inválidas: así la auditoría se confirma. */
@Service
public class AuthService {

    private final JdbcClient jdbc;
    private final PasswordEncoder encoder;
    private final AuditService audit;
    private final String dummyHash;

    AuthService(JdbcClient jdbc, PasswordEncoder encoder, AuditService audit) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.audit = audit;
        this.dummyHash = encoder.encode("nexfit-dummy");
    }

    private record Candidate(long id, String passwordHash, boolean usable) { }

    /** @return id del usuario si las credenciales son válidas; vacío en cualquier otro caso. */
    @Transactional
    public Optional<Long> login(String email, String password, String ip) {
        Optional<Candidate> found = email == null ? Optional.empty() : jdbc.sql("""
                SELECT id, password_hash,
                       is_active AND (locked_until IS NULL OR locked_until <= now()) AS usable
                FROM users WHERE lower(email) = lower(:email)
                """)
            .param("email", email.trim())
            .query((rs, n) -> new Candidate(rs.getLong("id"), rs.getString("password_hash"), rs.getBoolean("usable")))
            .optional();
        // Se compara siempre contra un hash para que el tiempo no delate si el correo existe.
        boolean matches = encoder.matches(password == null ? "" : password,
            found.map(Candidate::passwordHash).orElse(dummyHash));
        if (found.isPresent() && matches && found.get().usable()) {
            long id = found.get().id();
            audit.record(AuditEventType.LOGIN, id, null, "user", id, ip, null);
            return Optional.of(id);
        }
        Long id = found.map(Candidate::id).orElse(null);
        audit.record(AuditEventType.LOGIN_FAILED, id, null, id == null ? null : "user", id, ip, null);
        return Optional.empty();
    }

    @Transactional
    public void logout(long userId, String ip) {
        audit.record(AuditEventType.LOGOUT, userId, null, "user", userId, ip, null);
    }
}
