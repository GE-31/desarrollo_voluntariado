package com.sistemadevoluntariado.service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.repository.UsuarioRepository;

/**
 * Carga el usuario desde la BD para Spring Security.
 * Usa inyección de dependencias con @Autowired.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = Logger.getLogger(CustomUserDetailsService.class.getName());

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("► Spring Security cargando usuario: " + username);

        Optional<Usuario> opt = usuarioRepository.findByUsername(username);
        Usuario u = opt.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        boolean activo = !"INACTIVO".equalsIgnoreCase(u.getEstado());

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPasswordHash(),
                activo,  // enabled
                true,    // accountNonExpired
                true,    // credentialsNonExpired
                activo,  // accountNonLocked
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    /** Carga el objeto Usuario completo (para guardar en sesión HTTP). */
    @Transactional
    public Usuario loadUsuarioCompleto(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }
}
