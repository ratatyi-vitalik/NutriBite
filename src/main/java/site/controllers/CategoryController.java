package site.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import site.persistent.entity.User;
import site.persistent.entity.recipes.Category;
import site.persistent.repository.CategoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@RestController
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/categories")
    public ModelAndView categoriesPage(@AuthenticationPrincipal User user) {
        ModelAndView model = new ModelAndView("categories");
        model.addObject("categories", categoryRepository.findAll());
        model.addObject("user", user);
        return model;
    }

    @GetMapping("/category")
    public ModelAndView category(Long categoryId) {
        ModelAndView model = new ModelAndView("category");

        Optional<Category> category = categoryRepository.findById(categoryId);
        if (category.isPresent()) {
            model.addObject("category", category.get());
        }
        else {
            return new ModelAndView("redirect:/");
        }

        return model;
    }
}
