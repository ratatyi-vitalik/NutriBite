package site.persistent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.persistent.entity.ProposedRecipe;

public interface ProposedRecipeRepository extends JpaRepository<ProposedRecipe, Long> {
}
