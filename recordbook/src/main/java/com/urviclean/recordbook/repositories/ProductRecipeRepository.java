package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.ProductRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRecipeRepository extends JpaRepository<ProductRecipe, Long> {
}

