package com.fst.rsi.resto.controller;

import com.fst.rsi.resto.dto.FactureDTO;
import com.fst.rsi.resto.entity.enums.ModePaiement;
import com.fst.rsi.resto.entity.enums.StatutPaiement;
import com.fst.rsi.resto.service.FactureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FactureController {

    private final FactureService factureService;

    // ==================== CRUD FACTURES ====================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<FactureDTO> createFacture(
            @RequestParam Long idCommande,
            @RequestParam ModePaiement modePaiement) {
        log.info("REST - Création de facture pour la commande ID: {}", idCommande);
        FactureDTO facture = factureService.createFacture(idCommande, modePaiement);
        return ResponseEntity.status(HttpStatus.CREATED).body(facture);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CLIENT')")
    public ResponseEntity<FactureDTO> getFactureById(@PathVariable Long id) {
        log.info("REST - Récupération de la facture ID: {}", id);
        FactureDTO facture = factureService.convertToFactureDTO(factureService.getFactureById(id));
        return ResponseEntity.ok(facture);
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<FactureDTO>> getAllFactures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateFacture") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("REST - Récupération de toutes les factures");

        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<FactureDTO> factures = factureService.getAllFactures(pageable);

        return ResponseEntity.ok(factures);
    }


    @GetMapping("/commande/{idCommande}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CLIENT')")
    public ResponseEntity<FactureDTO> getFactureByCommande(@PathVariable Long idCommande) {
        log.info("REST - Récupération de la facture pour la commande ID: {}", idCommande);
        FactureDTO facture = factureService.getFactureByCommande(idCommande);
        return ResponseEntity.ok(facture);
    }


    @GetMapping("/client/{idClient}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CLIENT')")
    public ResponseEntity<List<FactureDTO>> getFacturesByClient(@PathVariable Long idClient) {
        log.info("REST - Récupération des factures du client ID: {}", idClient);
        List<FactureDTO> factures = factureService.getFacturesByClient(idClient);
        return ResponseEntity.ok(factures);
    }


    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<FactureDTO>> getFacturesByStatut(@PathVariable StatutPaiement statut) {
        log.info("REST - Récupération des factures avec statut: {}", statut);
        List<FactureDTO> factures = factureService.getFacturesByStatut(statut);
        return ResponseEntity.ok(factures);
    }


    @GetMapping("/mode-paiement/{mode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<FactureDTO>> getFacturesByModePaiement(@PathVariable ModePaiement mode) {
        log.info("REST - Récupération des factures par mode de paiement: {}", mode);
        List<FactureDTO> factures = factureService.getFacturesByModePaiement(mode);
        return ResponseEntity.ok(factures);
    }


    @GetMapping("/periode")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<FactureDTO>> getFacturesByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.info("REST - Récupération des factures entre {} et {}", debut, fin);
        List<FactureDTO> factures = factureService.getFacturesByPeriode(debut, fin);
        return ResponseEntity.ok(factures);
    }


    @GetMapping("/impayees")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<FactureDTO>> getFacturesImpayees() {
        log.info("REST - Récupération des factures impayées");
        List<FactureDTO> factures = factureService.getFacturesImpayees();
        return ResponseEntity.ok(factures);
    }


    @GetMapping("/aujourd-hui")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<FactureDTO>> getFacturesDuJour() {
        log.info("REST - Récupération des factures du jour");
        List<FactureDTO> factures = factureService.getFacturesDuJour();
        return ResponseEntity.ok(factures);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFacture(@PathVariable Long id) {
        log.info("REST - Suppression de la facture ID: {}", id);
        factureService.deleteFacture(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== GESTION DES PAIEMENTS ====================


    @PutMapping("/{id}/payer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<FactureDTO> marquerCommePaye(
            @PathVariable Long id,
            @RequestParam ModePaiement modePaiement) {
        log.info("REST - Marquage de la facture ID: {} comme payée", id);
        FactureDTO facture = factureService.marquerCommePaye(id, modePaiement);
        return ResponseEntity.ok(facture);
    }


    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<FactureDTO> annulerFacture(@PathVariable Long id) {
        log.info("REST - Annulation de la facture ID: {}", id);
        FactureDTO facture = factureService.annulerFacture(id);
        return ResponseEntity.ok(facture);
    }


    @PutMapping("/{id}/mode-paiement")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<FactureDTO> changerModePaiement(
            @PathVariable Long id,
            @RequestParam ModePaiement nouveauMode) {
        log.info("REST - Changement du mode de paiement pour la facture ID: {}", id);
        FactureDTO facture = factureService.changerModePaiement(id, nouveauMode);
        return ResponseEntity.ok(facture);
    }

    // ==================== STATISTIQUES ====================


    @GetMapping("/statistiques")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getStatistiques(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.info("REST - Récupération des statistiques entre {} et {}", debut, fin);
        Map<String, Object> stats = factureService.getStatistiques(debut, fin);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/chiffre-affaire")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getChiffreAffaire(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.info("REST - Calcul du chiffre d'affaires entre {} et {}", debut, fin);
        BigDecimal montant = factureService.getChiffreAffaire(debut, fin);

        Map<String, Object> response = Map.of(
                "debut", debut,
                "fin", fin,
                "chiffreAffaire", montant
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/chiffre-affaire/aujourd-hui")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getChiffreAffaireDuJour() {
        log.info("REST - Calcul du chiffre d'affaires du jour");
        BigDecimal montant = factureService.getChiffreAffaireDuJour();

        Map<String, Object> response = Map.of(
                "date", LocalDateTime.now(),
                "chiffreAffaire", montant
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/top-clients")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getTopClientsByMontant(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("REST - Récupération du top {} clients par montant", limit);
        List<Map<String, Object>> topClients = factureService.getTopClientsByMontant(limit);
        return ResponseEntity.ok(topClients);
    }


    @GetMapping("/rapport-tva")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getRapportTVA(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.info("REST - Génération du rapport de TVA entre {} et {}", debut, fin);
        Map<String, Object> rapport = factureService.getRapportTVA(debut, fin);
        return ResponseEntity.ok(rapport);
    }

    // ==================== GÉNÉRATION DE DOCUMENTS ====================


    @GetMapping("/{id}/contenu")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CLIENT')")
    public ResponseEntity<Map<String, String>> genererContenuFacture(@PathVariable Long id) {
        log.info("REST - Génération du contenu de la facture ID: {}", id);
        String contenu = factureService.genererContenuFacture(id);

        Map<String, String> response = Map.of(
                "idFacture", id.toString(),
                "contenu", contenu
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}/telecharger")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CLIENT')")
    public ResponseEntity<String> telechargerFacture(@PathVariable Long id) {
        log.info("REST - Téléchargement de la facture ID: {}", id);
        String contenu = factureService.genererContenuFacture(id);

        return ResponseEntity.ok()
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Content-Disposition", "attachment; filename=facture_" + id + ".txt")
                .body(contenu);
    }
}