package site.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import site.persistent.entity.PasswordResetToken;
import site.persistent.entity.User;
import site.persistent.repository.PasswordResetTokenRepository;
import site.persistent.repository.UserRepository;
import site.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;
import java.util.UUID;

@RestController
public class AuthController {

    private final UserService userService;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthController(UserService userService, JavaMailSender mailSender, UserRepository userRepository,
                          PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userService = userService;
        this.mailSender = mailSender;
        this.userRepository = userRepository;
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

    @PostMapping("/forgot-password")
    public RedirectView forgotPassword(@RequestParam String email, HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();

            passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);

            PasswordResetToken resetToken = new PasswordResetToken(token, user, 15);
            passwordResetTokenRepository.save(resetToken);

            String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String resetLink = appUrl + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Сброс пароля");
            message.setText("Для сброса пароля перейдите по ссылке:\n" + resetLink + "\n\nСсылка действительна 15 минут.");
            mailSender.send(message);
        }

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
        Optional<PasswordResetToken> passwordResetTokenOpt = passwordResetTokenRepository.findByToken(token);
        User user;
        if (passwordResetTokenOpt.isPresent() && !passwordResetTokenOpt.get().isExpired()) {
            user = passwordResetTokenOpt.get().getUser();
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            userRepository.save(user);
            passwordResetTokenRepository.delete(passwordResetTokenOpt.get());
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
