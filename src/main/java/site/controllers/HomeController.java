package site.controllers;

import jakarta.servlet.http.HttpSession;
import site.persistent.entity.recipes.Category;
import site.persistent.repository.CategoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
public class HomeController {
    private final CategoryRepository categoryRepository;

    public HomeController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
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
}
