package org.delcom.ifs25026springbootcashflow.framework.view;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Berkas build Vaadin memakai nama ber-hash sehingga isinya tidak pernah
 * berubah untuk URL yang sama. Vaadin hanya memberi max-age 1 jam, jadi header
 * cache diperpanjang menjadi satu tahun agar kunjungan berikutnya tidak
 * mengunduh ulang bundle JavaScript.
 */
@Component
public class StaticAssetCacheFilter extends OncePerRequestFilter {

    private static final String PREFIX = "/VAADIN/build/";
    private static final String CACHE_CONTROL = "public, max-age=31536000, immutable";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!path.startsWith(PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request, new ImmutableCacheResponse(response));
    }

    private static final class ImmutableCacheResponse extends HttpServletResponseWrapper {

        private ImmutableCacheResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, overrideCacheControl(name, value));
        }

        @Override
        public void addHeader(String name, String value) {
            super.setHeader(name, overrideCacheControl(name, value));
        }

        private static String overrideCacheControl(String name, String value) {
            return "Cache-Control".equalsIgnoreCase(name) ? CACHE_CONTROL : value;
        }
    }
}
