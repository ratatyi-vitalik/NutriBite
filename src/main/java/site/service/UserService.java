package site.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import site.persistent.entity.PasswordResetToken;
import site.persistent.entity.User;
import site.persistent.repository.PasswordResetTokenRepository;
import site.persistent.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public UserService(UserRepository userRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    public boolean registerUser(String name, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            return false;
        }

        String hashedPassword = passwordEncoder.encode(password);

        User newUser = new User(name, email, hashedPassword);
        userRepository.save(newUser);
        return true;
    }

    @Transactional
    public void createAndSendToken(String email, HttpServletRequest request) {
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
    }

    @Transactional
    public boolean resetPasswordWithToken(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);

        if (tokenOpt.isPresent() && !tokenOpt.get().isExpired()) {
            User user = tokenOpt.get().getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            passwordResetTokenRepository.delete(tokenOpt.get());
            return true;
        }
        return false;
    }

    @Transactional
    public String changeUserPassword(User user, String oldPassword, String newPassword, String confirmPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return "Old password doesn't match";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "New passwords don't match";
        }
        if (oldPassword.equals(newPassword)) {
            return "New and old passwords must not match!";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        reloadUserInSecurityContext(user);
        return null;
    }

    public void reloadUserInSecurityContext(User user) {
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                user,
                user.getPassword(),
                user.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}