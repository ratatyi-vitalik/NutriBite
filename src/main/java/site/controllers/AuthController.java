package site.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import site.persistent.entity.PasswordResetToken;
import site.persistent.repository.PasswordResetTokenRepository;
import site.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@RestController
public class AuthController {

    private final UserService userService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthController(UserService userService, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userService = userService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
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

    @PostMapping("/forgot-password")
    public RedirectView forgotPassword(@RequestParam String email, HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        userService.createAndSendToken(email, request);
        redirectAttributes.addFlashAttribute("message", "If your email exists, we've sent you a link to reset your password");
        return new RedirectView("/");
    }

    @GetMapping("/reset-password")
    public ModelAndView showResetPasswordPage(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        ModelAndView model = new ModelAndView("resetPassword");
        Optional<PasswordResetToken> passwordResetTokenOpt = passwordResetTokenRepository.findByToken(token);

        if (passwordResetTokenOpt.isEmpty() || passwordResetTokenOpt.get().isExpired()) {
            redirectAttributes.addFlashAttribute("error", "Link is invalid or has expired");
            return new ModelAndView("redirect:/");
        } else {
            model.addObject("token", token);
        }

        return model;
    }

    @PostMapping("/reset-password")
    public RedirectView resetPassword(@RequestParam("token") String token,
                                      @RequestParam("newPassword") String newPassword,
                                      RedirectAttributes redirectAttributes) {
        if (userService.resetPasswordWithToken(token, newPassword)) {
            redirectAttributes.addFlashAttribute("message", "Password was successfully changed");
            return new RedirectView("/");
        }
        redirectAttributes.addFlashAttribute("error", "Error: token is invalid");
        return new RedirectView("/");
    }

    private String getRefererUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return (referer != null) ? referer : "/";
    }
}
