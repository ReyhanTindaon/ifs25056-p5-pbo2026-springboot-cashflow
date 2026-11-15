package org.delcom.ifs25026springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Melengkapi index.html yang dihasilkan Vaadin dengan atribut bahasa dan
 * metadata SEO yang tersedia sejak HTML pertama (bukan setelah JavaScript jalan).
 */
@Component
public class AppShellSeoInitListener implements VaadinServiceInitListener {

    static final String APP_NAME = "Cashflow";
    static final String DESCRIPTION = "Aplikasi pencatatan cashflow: kelola pemasukan, "
            + "pengeluaran, saldo, dan bukti transaksi dalam satu dasbor.";
    private static final String HTML_LANG = "id";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(indexHtmlResponse -> {
            Document document = indexHtmlResponse.getDocument();
            document.getElementsByTag("html").attr("lang", HTML_LANG);

            ensureMeta(document, "name", "description", DESCRIPTION);
            ensureMeta(document, "name", "theme-color", "#F3F4F6");
            ensureMeta(document, "property", "og:type", "website");
            ensureMeta(document, "property", "og:site_name", APP_NAME);
            ensureMeta(document, "property", "og:title", APP_NAME);
            ensureMeta(document, "property", "og:description", DESCRIPTION);

            if (document.title().isBlank()) {
                document.title(APP_NAME);
            }

            String origin = requestOrigin(indexHtmlResponse.getVaadinRequest());
            if (origin != null) {
                String path = requestPath(indexHtmlResponse.getVaadinRequest());
                ensureMeta(document, "property", "og:url", origin + path);
                ensureMeta(document, "property", "og:image", origin + "/icons/icon.png");
                ensureLink(document, "canonical", origin + path);
            }
        });
    }

    private static void ensureMeta(Document document, String attribute, String value, String content) {
        Element meta = document.head().selectFirst("meta[" + attribute + "=\"" + value + "\"]");
        if (meta == null) {
            meta = document.head().appendElement("meta");
            meta.attr(attribute, value);
        }
        meta.attr("content", content);
    }

    private static void ensureLink(Document document, String rel, String href) {
        Element link = document.head().selectFirst("link[rel=\"" + rel + "\"]");
        if (link == null) {
            link = document.head().appendElement("link");
            link.attr("rel", rel);
        }
        link.attr("href", href);
    }

    private static String requestPath(VaadinRequest request) {
        String path = request.getPathInfo();
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    static String requestOrigin(VaadinRequest request) {
        if (!(request instanceof VaadinServletRequest servletRequest)) {
            return null;
        }
        HttpServletRequest httpRequest = servletRequest.getHttpServletRequest();
        String host = httpRequest.getHeader("Host");
        if (host == null || host.isBlank()) {
            int port = httpRequest.getServerPort();
            boolean defaultPort = ("http".equals(httpRequest.getScheme()) && port == 80)
                    || ("https".equals(httpRequest.getScheme()) && port == 443);
            host = httpRequest.getServerName() + (defaultPort ? "" : ":" + port);
        }
        String scheme = httpRequest.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isBlank()) {
            scheme = httpRequest.getScheme();
        }
        return scheme + "://" + host;
    }
}
