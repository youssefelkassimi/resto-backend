package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.Commande;
import com.fst.rsi.resto.entity.Facture;
import com.fst.rsi.resto.entity.enums.ModePaiement;
import com.fst.rsi.resto.entity.enums.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FactureRepo extends JpaRepository<Facture, Long> {
    Optional<Facture> findByCommande(Commande commande);
    List<Facture> findByDateFactureBetween(LocalDateTime startDate, LocalDateTime endDate);
    Optional<Facture> findByCommandeIdCommande(Long idCommande);

    boolean existsByCommandeIdCommande(Long idCommande);

    List<Facture> findByCommandeClientIdClient(Long idClient);

    List<Facture> findByStatutPaiement(StatutPaiement statut);

    List<Facture> findByModePaiement(ModePaiement mode);

}
