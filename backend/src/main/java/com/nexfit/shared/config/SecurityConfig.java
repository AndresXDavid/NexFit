package com.nexfit.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Configuración mínima de seguridad del esqueleto (TT-03).
 *
 * - /actuator/health es público: lo usa Cloud Run y no toca la base de datos.
 * - Todo lo demás exige sesión; sin sesión responde 401 (no redirige a un formulario).
 * - El inicio de sesión real con correo y contraseña llega en US-01 (NEX-20).
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .csrf(Customizer.withDefaults());
        return http.build();
    }
}
