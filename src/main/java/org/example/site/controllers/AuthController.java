package org.example.site.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.site.persistent.entity.User;
import org.example.site.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public RedirectView registration(@RequestParam String name, @RequestParam String email,
                                     @RequestParam String password, HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        boolean registrationSuccess = userService.registerUser(name, email, password);

        if (registrationSuccess) {
            redirectAttributes.addFlashAttribute("regSuccess", "Вы успешно зарегистрировались! Теперь воооооооооидите.");
        } else {
            redirectAttributes.addFlashAttribute("regError", "Пользователь с таким Email уже существует.");
        }

        return new RedirectView(getRefererUrl(request));
    }

    @PostMapping("/login")
    public RedirectView login(@RequestParam("email") String email, @RequestParam("password") String password,
                              HttpSession httpSession, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Optional<User> user = userService.authenticate(email, password);

        if (user.isPresent()) {
            httpSession.setAttribute("user", user.get());
        } else {
            redirectAttributes.addFlashAttribute("loginError", "Неверный Email или пароль.");
        }

        return new RedirectView(getRefererUrl(request));
    }

    @PostMapping("/logout")
    public RedirectView handleLogout(HttpSession session, HttpServletRequest request) {
        session.invalidate();
        return new RedirectView(getRefererUrl(request));
    }

    private String getRefererUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return (referer != null) ? referer : "/";
    }
}
