package site.persistent.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.persistent.entity.recipes.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByNameContainingIgnoreCase(String name);

    @Query(value = """
        SELECT c.* 
        FROM categories c
        LEFT JOIN recipes r ON r.category_id = c.id
        LEFT JOIN recipe_views rv ON rv.recipe_id = r.id AND rv.viewed_at >= :since
        GROUP BY c.id, c.name, c.image_path
        ORDER BY COUNT(rv.id) DESC, c.id ASC
        LIMIT 6
    """, nativeQuery = true)
    List<Category> findTop6PopularOrFirstById(@Param("since") LocalDateTime since);
}
