package site.persistent.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.persistent.entity.recipes.Category;
import site.persistent.entity.recipes.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByTitleContainingIgnoreCase(String title);

    Page<Recipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query(value = """
        SELECT r.* 
        FROM recipes r
        LEFT JOIN recipe_views rv ON rv.recipe_id = r.id AND rv.viewed_at >= :since
        GROUP BY r.id, r.title, r.author_id, r.date, r.description, r.image_path,\s
                     r.prep_time, r.cook_time, r.servings, r.video_path, r.nutrition_id
        ORDER BY COUNT(rv.id) DESC, r.id ASC
        LIMIT 8
    """, nativeQuery = true)
    List<Recipe> findTop8PopularOrFirstById(@Param("since") LocalDateTime since);

    List<Recipe> findTop8ByPrepTimeLessThanEqualOrderByPrepTimeAsc(Integer maxPrepTime);

    boolean existsById(long id);
}
