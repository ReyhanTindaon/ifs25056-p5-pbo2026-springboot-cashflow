package org.delcom.ifs25026springbootcashflow.framework.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Mengalihkan permintaan halaman terproteksi ke /login di sisi server.
 *
 * <p>Tanpa filter ini browser tetap menerima app shell Vaadin (ratusan KB JavaScript) lebih dulu,
 * baru kemudian dialihkan ke halaman login oleh kode klien. Pengalihan awal membuat Lighthouse
 * hanya memuat halaman login statis yang ringan.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuthRedirectFilter extends OncePerRequestFilter {

    static final String COOKIE_NAME = "cashflow_uid";

    private static final Set<String> PROTECTED_ROUTES =
            Set.of("", "dashboard", "settings", "profile", "users", "user-transactions");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (requiresAuthentication(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        // Permintaan internal Vaadin (bootstrap/UIDL) tidak boleh dialihkan.
        if (request.getParameter("v-r") != null || request.getParameter("v-loc") != null) {
            return false;
        }
        if (!PROTECTED_ROUTES.contains(normalizedPath(request))) {
            return false;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return true;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String normalizedPath(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.replaceAll("^/+|/+$", "");
    }
}
