package site.persistent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.persistent.entity.recipes.RecipeView;

import java.time.LocalDateTime;

public interface RecipeViewRepository extends JpaRepository<RecipeView, Long> {
    boolean existsByRecipeIdAndUserIdAndViewedAtAfter(long recipeId, long userId, LocalDateTime time);

    boolean existsByRecipeIdAndIpAddressAndViewedAtAfter(long recipeId, String ipAddress, LocalDateTime time);
}
