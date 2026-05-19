package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.Client;
import com.fst.rsi.resto.entity.Commande;
import com.fst.rsi.resto.entity.Serveur;
import com.fst.rsi.resto.entity.enums.StatutCommande;
import com.fst.rsi.resto.entity.enums.TypeCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommandeRepo extends JpaRepository<Commande, Long> {
    List<Commande> findByClient(Client client);
    List<Commande> findByServeur(Serveur serveur);
    List<Commande> findByStatut(StatutCommande statut);
    List<Commande> findByDateCommandeBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Commande> findByClientIdClientOrderByDateCommandeDesc(Long clientId);
    List<Commande> findByTypeCommande(TypeCommande typeCommande);

    @Query("SELECT c FROM Commande c WHERE c.serveur.idServeur = :serveurId ORDER BY c.dateCommande DESC")
    List<Commande> findByServeur(@Param("serveurId") Long serveurId);

    @Query("SELECT c FROM Commande c WHERE c.table.idTable = :tableId AND c.statut != 'SERVIE' AND c.statut != 'ANNULEE'")
    List<Commande> findActiveCommandesByTable(@Param("tableId") Long tableId);


    @Query("SELECT c FROM Commande c WHERE c.serveur.idServeur = :serveurId ORDER BY c.dateCommande DESC")
    List<Commande> findByServeurIdServeur(@Param("serveurId") Long serveurId);

    @Query("SELECT c FROM Commande c WHERE c.serveur.idServeur = :serveurId AND c.statut IN :statuts")
    List<Commande> findByServeurIdServeurAndStatutIn(
            @Param("serveurId") Long serveurId,
            @Param("statuts") List<StatutCommande> statuts);

}
