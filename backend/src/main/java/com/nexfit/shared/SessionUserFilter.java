package com.nexfit.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * La sesión guarda solo el id del usuario. En cada solicitud se consulta en BD si sigue activo
 * y sin bloqueo (si no, 401) y se arman sus autoridades vigentes: "PLATFORM_ADMIN" y "rol:orgId"
 * solo para afiliaciones y gimnasios activos. Nada de esto se guarda en la sesión.
 *
 * <p>Si el usuario tiene una contraseña temporal sin cambiar (US-02), solo puede cambiarla o cerrar
 * sesión: cualquier otra ruta recibe 403 con código PASSWORD_CHANGE_REQUIRED y no se le dan
 * autoridades. Una temporal con la vigencia vencida deja de servir (401).
 */
public class SessionUserFilter extends OncePerRequestFilter {

    public static final String USER_ID_ATTRIBUTE = "NEXFIT_USER_ID";

    /** Rutas permitidas mientras la contraseña temporal sigue pendiente de cambio. */
    private static final Set<String> ALLOWED_WHILE_PASSWORD_PENDING =
        Set.of("/auth/login", "/auth/logout", "/auth/password");

    private record Account(boolean platformAdmin, boolean mustChangePassword) { }

    private final JdbcClient jdbc;

    public SessionUserFilter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object id = session == null ? null : session.getAttribute(USER_ID_ATTRIBUTE);
        if (id instanceof Long userId) {
            var account = jdbc.sql("""
                    SELECT is_platform_admin, must_change_password FROM users
                    WHERE id = :id AND is_active AND (locked_until IS NULL OR locked_until <= now())
                      AND NOT (must_change_password
                               AND temp_password_expires_at IS NOT NULL
                               AND temp_password_expires_at <= now())
                    """)
                .param("id", userId)
                .query((rs, n) -> new Account(rs.getBoolean("is_platform_admin"), rs.getBoolean("must_change_password")))
                .optional();
            if (account.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (account.get().mustChangePassword()) {
                if (!ALLOWED_WHILE_PASSWORD_PENDING.contains(pathWithinApp(request))) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":\"PASSWORD_CHANGE_REQUIRED\","
                        + "\"message\":\"Debes cambiar tu contraseña temporal antes de continuar\"}");
                    return;
                }
                SecurityContext pending = SecurityContextHolder.createEmptyContext();
                pending.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(userId, null, authorities));
                SecurityContextHolder.setContext(pending);
                chain.doFilter(request, response);
                return;
            }
            if (account.get().platformAdmin()) {
                authorities.add(new SimpleGrantedAuthority("PLATFORM_ADMIN"));
            }
            jdbc.sql("""
                    SELECT mr.role::text || ':' || om.organization_id
                    FROM organization_members om
                    JOIN organizations o ON o.id = om.organization_id AND o.is_active
                    JOIN member_roles mr ON mr.member_id = om.id
                    WHERE om.user_id = :id AND om.is_active
                    """)
                .param("id", userId).query(String.class).list()
                .forEach(a -> authorities.add(new SimpleGrantedAuthority(a)));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(userId, null, authorities));
            SecurityContextHolder.setContext(context);
        }
        chain.doFilter(request, response);
    }

    /** Ruta sin el context-path (/api en producción), p. ej. "/auth/password". */
    private static String pathWithinApp(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return contextPath.isEmpty() ? uri : uri.substring(contextPath.length());
    }
}