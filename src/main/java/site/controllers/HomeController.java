package site.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import site.persistent.entity.Subscriber;
import site.persistent.entity.recipes.Category;
import site.persistent.repository.CategoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import site.persistent.repository.SubscriberRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class HomeController {
    private final CategoryRepository categoryRepository;
    private final SubscriberRepository subscriberRepository;

    public HomeController(CategoryRepository categoryRepository, SubscriberRepository subscriberRepository) {
        this.categoryRepository = categoryRepository;
        this.subscriberRepository = subscriberRepository;
    }

    @GetMapping("/")
    public ModelAndView home(HttpSession session) {
        ModelAndView model = new ModelAndView("index"); // указываем имя HTML-шаблона

        Object user = session.getAttribute("user");
        model.addObject("user", user);

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
}
