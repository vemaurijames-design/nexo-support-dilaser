package co.dilaser.nexo.config;

import co.dilaser.nexo.usuario.Usuario;
import co.dilaser.nexo.usuario.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtService jwtService;
    private final UsuarioRepository usuarios;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarios) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            if (!token.isEmpty() && !"undefined".equals(token) && !"null".equals(token)) {
                try {
                    Claims claims = jwtService.parse(token);
                    if (!"refresh".equals(claims.get("typ"))) {
                        UUID id = UUID.fromString(claims.getSubject());
                        Usuario u = usuarios.findById(id).orElse(null);
                        if (u != null && u.isActivo()) {
                            String role = "ROLE_" + u.getRol().name();
                            var auth = new UsernamePasswordAuthenticationToken(
                                    u, null, List.of(new SimpleGrantedAuthority(role)));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                } catch (Exception e) {
                    log.warn("JWT inválido en {}: {}", request.getRequestURI(), e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            }
        }
        chain.doFilter(request, response);
    }
}
