package site.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import site.persistent.entity.Notification;
import site.persistent.entity.Subscriber;
import site.persistent.entity.User;
import site.persistent.entity.recipes.Category;
import site.persistent.repository.CategoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import site.persistent.repository.NotificationRepository;
import site.persistent.repository.SubscriberRepository;
import site.persistent.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class HomeController {
    private final CategoryRepository categoryRepository;
    private final SubscriberRepository subscriberRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public HomeController(CategoryRepository categoryRepository,
                          SubscriberRepository subscriberRepository,
                          NotificationRepository notificationRepository,
                          UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.subscriberRepository = subscriberRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public ModelAndView home(Authentication authentication) {
        ModelAndView model = new ModelAndView("index");

        boolean isAuthenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        if (isAuthenticated) {
            User user = (User) authentication.getPrincipal();
            model.addObject("user", user);
            model.addObject("unreadNotificationsCount", notificationRepository.countByUserIdAndIsReadFalse(user.getId()));

            List<Notification> notifications = notificationRepository.findTop5ByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
            if (notifications.size() < 5) {
                List<Notification> nots = notificationRepository.findTop5ByUserIdAndIsReadTrueOrderByCreatedAtDesc(user.getId());
                notifications.addAll(nots.stream().limit(5 - notifications.size()).toList());
            }
            model.addObject("notifications", notifications);
        }

        List<Category> categories = categoryRepository.findTop6ByOrderByIdAsc();
        model.addObject("categories", categories);
        return model;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(@RequestParam String email) {
        if (subscriberRepository.existsByEmail(email.trim().toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("error", "This email is already subscribed"));
        }
        Subscriber subscriber = new Subscriber(email.trim().toLowerCase());
        subscriberRepository.save(subscriber);
        return ResponseEntity.ok(Map.of("message", "You subscribed to newsletter"));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestParam String email) {
        Optional<Subscriber> subscriber = subscriberRepository.findByEmail(email.trim().toLowerCase());
        if (subscriber.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "This email is not subscribed"));
        }
        subscriberRepository.delete(subscriber.get());
        return ResponseEntity.ok(Map.of("message", "You unsubscribed"));
    }

    @GetMapping("/check-subscription")
    public boolean checkSubscription(@RequestParam String email) {
        return subscriberRepository.existsByEmail(email.trim().toLowerCase());
    }

    @PostMapping("/unsubscribe-from-mail")
    public ResponseEntity<String> showUnsubscribePage(@RequestParam String token) {
        if (subscriberRepository.existsByToken(token)) {
            subscriberRepository.deleteByToken(token);
        }
        return ResponseEntity.ok("Unsubscribed successfully");
    }

    @GetMapping("/ban-status")
    public ResponseEntity<?> getBanStatus(@AuthenticationPrincipal User currentUser) {
        boolean banned = currentUser.isBanned();
        String remainingTime = banned ? currentUser.getRemainingBanTimeFormatted() : "";

        return ResponseEntity.ok(Map.of(
                "isBanned", banned,
                "remainingTime", remainingTime
        ));
    }

    @GetMapping("/notifications/mark-all-read")
    @Transactional
    public RedirectView markAllNotificationsRead(@AuthenticationPrincipal User user,
                                                 HttpServletRequest request) {
        if (user != null) {
            notificationRepository.markAllAsReadByUserId(user.getId());
        }
        return new RedirectView(getRefererUrl(request));
    }

    private String getRefererUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return (referer != null) ? referer : "/";
    }
}
