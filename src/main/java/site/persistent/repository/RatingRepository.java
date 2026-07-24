package site.persistent.repository;

import site.persistent.entity.recipes.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByRecipeIdAndUserId(Long recipeId, Long userId);
}