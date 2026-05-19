package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.Livraison;
import com.fst.rsi.resto.entity.Livreur;
import com.fst.rsi.resto.entity.enums.StatutLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LivraisonRepo extends JpaRepository<Livraison, Long> {
    List<Livraison> findByLivreur(Livreur livreur);
    List<Livraison> findByStatutLivraison(StatutLivraison statut);

    Optional<Livraison> findByCommandeIdCommande(Long idCommande);

    boolean existsByCommandeIdCommande(Long idCommande);


    List<Livraison> findByStatutLivraisonIn(List<StatutLivraison> statuts);

    List<Livraison> findByLivreurIdLivreur(Long idLivreur);

    List<Livraison> findByLivreurIdLivreurAndStatutLivraisonIn(Long idLivreur, List<StatutLivraison> statuts);

    List<Livraison> findByLivreurIsNullAndStatutLivraison(StatutLivraison statut);

    @Query("SELECT l FROM Livraison l WHERE l.commande.dateCommande BETWEEN :debut AND :fin")
    List<Livraison> findByCommandeDateCommandeBetween(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);
}
