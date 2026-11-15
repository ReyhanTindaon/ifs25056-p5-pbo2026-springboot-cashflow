package org.delcom.ifs25026springbootcashflow.framework.view;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.delcom.ifs25026springbootcashflow.domain.entity.User;
import org.delcom.ifs25026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25026springbootcashflow.usecase.AuthUseCase;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Gerbang login berupa halaman HTML statis tanpa JavaScript Vaadin.
 * Halaman ini hanya mengirim form ke server, lalu aplikasi Vaadin dipakai
 * setelah cookie sesi terbentuk.
 */
@Controller
public class LoginPageController {

    private static final String COOKIE_NAME = "cashflow_uid";
    private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 7;

    private final AuthUseCase authUseCase;

    public LoginPageController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "forward:/login.html";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "password", required = false) String password,
            HttpServletResponse response
    ) {
        try {
            User user = authUseCase.login(username, password);
            writeUserCookie(response, user.getId());
            return "redirect:/";
        } catch (ValidationException ex) {
            return "redirect:/login#login-error";
        }
    }

    private void writeUserCookie(HttpServletResponse response, int userId) {
        Cookie cookie = new Cookie(COOKIE_NAME, String.valueOf(userId));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        response.addCookie(cookie);
    }
}
