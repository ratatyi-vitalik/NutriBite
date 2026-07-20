package org.example.site.controllers;

import org.example.site.persistent.entity.recipes.Category;
import org.example.site.persistent.entity.recipes.Recipe;
import org.example.site.persistent.repository.CategoryRepository;
import org.example.site.persistent.repository.RecipeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;

    public SearchController(RecipeRepository recipeRepository, CategoryRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/search-results")
    public ModelAndView searchResultsPage() {
        return new ModelAndView("searchResults");
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false, defaultValue = "") String query) {
        if (query.isBlank()) {
            return ResponseEntity.ok(Map.of("categories", List.of(), "recipes", List.of()));
        }
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(query);
        List<Map<String, Object>> categoryMaps = new ArrayList<>();
        for (Category cat : categories) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("id", cat.getId());
            categoryMap.put("title", cat.getName());
            categoryMap.put("imagePath", cat.getImagePath());
            categoryMaps.add(categoryMap);
        }
        List<Recipe> recipes = recipeRepository.findByTitleContainingIgnoreCase(query);
        List<Map<String, Object>> recipeMaps = new ArrayList<>();
        for (Recipe recipe : recipes) {
            Map<String, Object> recipeMap = new HashMap<>();
            recipeMap.put("id", recipe.getId());
            recipeMap.put("title", recipe.getTitle());
            recipeMap.put("imagePath", recipe.getImagePath());
            recipeMaps.add(recipeMap);
        }
        return ResponseEntity.ok(Map.of("categories", categoryMaps, "recipes", recipeMaps));
    }

    @GetMapping("/search-page")
    public ResponseEntity<?> searchPage(@RequestParam(required = false, defaultValue = "") String query,
                                    @RequestParam(required = false, defaultValue = "0") int page) {
        if (query.isBlank()) {
            return ResponseEntity.ok(Map.of("totalRecipes", 0, "totalPages", 0, "recipes", List.of()));
        }
        Page<Recipe> recipePage = recipeRepository.findByTitleContainingIgnoreCase(query, PageRequest.of(page, 20));

        List<Map<String, Object>> recipes = new ArrayList<>();
        for (Recipe recipe : recipePage) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", recipe.getId());
            map.put("title", recipe.getTitle());
            map.put("imagePath", recipe.getImagePath());
            recipes.add(map);
        }

        return ResponseEntity.ok(Map.of(
                "totalRecipes", recipePage.getTotalElements(),
                "totalPages", recipePage.getTotalPages(),
                "recipes", recipes
        ));
    }
}