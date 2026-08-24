package site.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import site.persistent.entity.Subscriber;
import site.persistent.repository.SubscriberRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class SendMailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public SendMailService(JavaMailSender mailSender,
                           TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void SendNewsMails(List<Subscriber> subscribers, String title, String subject, String content, String ctaText, String ctaUrl) {
        boolean hasCta = ctaText != null && ctaUrl != null && !ctaText.trim().isEmpty() && !ctaUrl.trim().isEmpty();

        for (Subscriber subscriber : subscribers) {
            try {
                Context context = new Context();
                context.setVariable("categoryName", title);
                context.setVariable("subject", subject);
                context.setVariable("content", content);

                if (hasCta) {
                    context.setVariable("ctaText", ctaText);
                    context.setVariable("ctaUrl", ctaUrl);
                }

                String unsubscribeUrl = "http://localhost:8080/unsubscribe-from-mail?token=" + subscriber.getToken();
                context.setVariable("unsubscribeUrl", unsubscribeUrl);

                String htmlContent = templateEngine.process("Mails/newsletter", context);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(
                        message,
                        MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                        StandardCharsets.UTF_8.name()
                );

                helper.setTo(subscriber.getEmail());
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                helper.setFrom("noreply@tastebite.com");

                helper.getMimeMessage().addHeader("List-Unsubscribe", "<" + unsubscribeUrl + ">");
                helper.getMimeMessage().addHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");

                mailSender.send(message);
            } catch (Exception e) {
                System.out.println("Failed to send email to " + subscriber.getEmail() + ": " + e.getMessage());
            }
        }
    }
}
