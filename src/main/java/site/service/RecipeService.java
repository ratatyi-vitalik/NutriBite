package site.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import site.persistent.entity.recipes.Recipe;
import site.persistent.repository.RecipeRepository;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public Page<Recipe> getRecipes(int page, String query, Long categoryId, String sortBy) {
        Sort sort = Sort.unsorted();
        if ("oldest".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "date");
        } else if ("newest".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "date");
        }

        if ("rating".equals(sortBy)) {
            if (categoryId == 0) {
                return recipeRepository.findByTitleSortedByRating(query, PageRequest.of(page, 12));
            } else {
                return recipeRepository.findByTitleAndCategorySortedByRating(query, categoryId, PageRequest.of(page, 12));
            }
        } if("popularity".equals(sortBy)) {
            if (categoryId == 0) {
                return recipeRepository.findByTitleSortedByPopularity(query, PageRequest.of(page, 12));
            } else {
                return recipeRepository.findByTitleAndCategorySortedByPopularity(query, categoryId, PageRequest.of(page, 12));
            }
        } else {
            if (categoryId == 0) {
                return recipeRepository.findByTitleContainingIgnoreCase(query, PageRequest.of(page, 12, sort));
            } else {
                return recipeRepository.findByTitleContainingIgnoreCaseAndCategoryId(query, categoryId, PageRequest.of(page, 12, sort));
            }
        }
    }
}
