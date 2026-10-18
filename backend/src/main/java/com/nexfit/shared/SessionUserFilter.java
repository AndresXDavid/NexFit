package com.nexfit.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
 */
public class SessionUserFilter extends OncePerRequestFilter {

    public static final String USER_ID_ATTRIBUTE = "NEXFIT_USER_ID";

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
            var platformAdmin = jdbc.sql("""
                    SELECT is_platform_admin FROM users
                    WHERE id = :id AND is_active AND (locked_until IS NULL OR locked_until <= now())
                    """)
                .param("id", userId).query(Boolean.class).optional();
            if (platformAdmin.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (platformAdmin.get()) {
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
}
