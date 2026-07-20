package org.example.site.persistent.repository;

import org.example.site.persistent.entity.recipes.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findTop6ByOrderByIdAsc();

    List<Category> findByNameContainingIgnoreCase(String name);

}
