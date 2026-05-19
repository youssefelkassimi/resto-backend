package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.CompositionPlat;
import com.fst.rsi.resto.entity.CompositionPlatId;
import com.fst.rsi.resto.entity.Ingredient;
import com.fst.rsi.resto.entity.Plat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompositionPlatRepo extends JpaRepository<CompositionPlat, CompositionPlatId> {
    List<CompositionPlat> findByPlat(Plat plat);
    List<CompositionPlat> findByIngredient(Ingredient ingredient);

    boolean existsByPlatIdPlatAndIngredientIdIngredient(Long idPlat, Long idIngredient);

    @Query("SELECT c FROM CompositionPlat c WHERE c.plat.idPlat = :idPlat")
    List<CompositionPlat> findByPlatId(@Param("idPlat") Long idPlat);

//    @Query("SELECT c FROM CompositionPlat c WHERE c.plat.idPlat = :idPlat and c.ingredient.id_ingredient=:idIngredient")
    Optional<CompositionPlat> findByPlatIdPlatAndIngredientIdIngredient( Long idPlat ,  Long idIngredient);

    @Query("SELECT c FROM CompositionPlat c WHERE c.ingredient.idIngredient = :idIngredient")
    List<CompositionPlat> findByIngredientId(@Param("idIngredient") Long idIngredient);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM CompositionPlat c WHERE c.plat.idPlat = :idPlat " +
            "AND c.ingredient.idIngredient = :idIngredient")
    boolean existsByPlatAndIngredient(@Param("idPlat") Long idPlat,
                                      @Param("idIngredient") Long idIngredient);
}