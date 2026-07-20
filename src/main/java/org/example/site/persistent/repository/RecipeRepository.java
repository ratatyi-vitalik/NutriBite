package org.example.site.persistent.repository;

import org.example.site.persistent.entity.recipes.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByTitleContainingIgnoreCase(String title);

    Page<Recipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
