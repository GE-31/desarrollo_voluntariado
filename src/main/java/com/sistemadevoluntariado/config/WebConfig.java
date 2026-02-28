package com.sistemadevoluntariado.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sistemadevoluntariado.security.PermisoInterceptor;

/**
 * Configuración web MVC: registra interceptors, resource handlers, etc.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private PermisoInterceptor permisoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permisoInterceptor)
                .addPathPatterns(
                    "/voluntarios", "/voluntarios/**",
                    "/beneficiarios", "/beneficiarios/**",
                    "/actividades", "/actividades/**",
                    "/asistencias", "/asistencias/**",
                    "/inventario", "/inventario/**",
                    "/donaciones", "/donaciones/**",
                    "/salidas-donaciones", "/salidas-donaciones/**",
                    "/salidas-inventario", "/salidas-inventario/**",
                    "/calendario", "/calendario/**",
                    "/reportes", "/reportes/**",
                    "/tesoreria", "/tesoreria/**",
                    "/certificados", "/certificados/**"
                )
                .excludePathPatterns(
                    "/login", "/doLogin", "/logout",
                    "/css/**", "/js/**", "/img/**",
                    "/error"
                );
    }
}
