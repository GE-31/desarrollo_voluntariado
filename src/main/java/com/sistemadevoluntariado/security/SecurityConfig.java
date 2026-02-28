package com.sistemadevoluntariado.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.sistemadevoluntariado.service.CustomUserDetailsService;

import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private LoginSuccessHandler loginSuccessHandler;

    // 🔐 Encoder compatible con BCrypt ($2a$, $2b$)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔐 Proveedor de autenticación
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // 🔐 AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 🔐 Configuración principal de seguridad
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ❌ CSRF desactivado (formularios simples / AJAX)
            .csrf(csrf -> csrf.disable())

            // Permitir iframes del mismo origen (para vista previa de PDF)
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )

            // 🔓 Autorizaciones
            .authorizeHttpRequests(auth -> auth
                // Permitir FORWARD/INCLUDE/ERROR internos (JSP, HTML forwards)
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR).permitAll()
                .requestMatchers(
                    "/login",
                    "/doLogin",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/error"
                ).permitAll()
                .anyRequest().authenticated()
            )

            // 🔐 Login con HTML
            .formLogin(form -> form
                .loginPage("/login")            // GET → login.html
                .loginProcessingUrl("/doLogin") // POST del formulario
                .usernameParameter("usuario")   // name="usuario"
                .passwordParameter("clave")     // name="clave"
                .successHandler(loginSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // 🔓 Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // 🔐 Provider
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
