package com.fst.rsi.resto.controller;

import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.entity.enums.StatutLivraison;
import com.fst.rsi.resto.service.LivraisonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/livraisons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LivraisonController {

    private final LivraisonService livraisonService;


    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or hasRole('RESPONSABLE_CUISINE')")
    public ResponseEntity<LivraisonResponseDTO> createLivraison(
            @Valid @RequestBody LivraisonRequestDTO request) {

        LivraisonResponseDTO livraison = livraisonService.createLivraison(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(livraison);
    }


    @GetMapping("/{id}")
    public ResponseEntity<LivraisonResponseDTO> getLivraisonById(@PathVariable Long id) {
        LivraisonResponseDTO livraison = livraisonService.getLivraisonById(id);
        return ResponseEntity.ok(livraison);
    }


    @GetMapping("/commande/{idCommande}")
    public ResponseEntity<LivraisonResponseDTO> getLivraisonByCommande(@PathVariable Long idCommande) {
        LivraisonResponseDTO livraison = livraisonService.getLivraisonByCommande(idCommande);
        return ResponseEntity.ok(livraison);
    }


    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<LivraisonResponseDTO>> getAllLivraisons(Pageable pageable) {
        Page<LivraisonResponseDTO> livraisons = livraisonService.getAllLivraisons(pageable);
        return ResponseEntity.ok(livraisons);
    }


    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('LIVREUR') or hasRole('ADMIN') or hasRole('RESPONSABLE_CUISINE')")
    public ResponseEntity<List<LivraisonResponseDTO>> getLivraisonsByStatut(
            @PathVariable StatutLivraison statut) {

        List<LivraisonResponseDTO> livraisons = livraisonService.getLivraisonsByStatut(statut);
        return ResponseEntity.ok(livraisons);
    }


    @GetMapping("/en-cours")
    @PreAuthorize("hasRole('MANAGER') or hasRole('LIVREUR') or hasRole('ADMIN')")
    public ResponseEntity<List<LivraisonResponseDTO>> getLivraisonsEnCours() {
        List<LivraisonResponseDTO> livraisons = livraisonService.getLivraisonsEnCours();
        return ResponseEntity.ok(livraisons);
    }


    @GetMapping("/non-assignees")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or hasRole('RESPONSABLE_CUISINE')")
    public ResponseEntity<List<LivraisonResponseDTO>> getLivraisonsNonAssignees() {
        List<LivraisonResponseDTO> livraisons = livraisonService.getLivraisonsNonAssignees();
        return ResponseEntity.ok(livraisons);
    }


    @PatchMapping("/{idLivraison}/assigner-livreur/{idLivreur}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or hasRole('RESPONSABLE_CUISINE')")
    public ResponseEntity<LivraisonResponseDTO> assignerLivreur(
            @PathVariable Long idLivraison,
            @PathVariable Long idLivreur) {

        LivraisonResponseDTO livraison = livraisonService.assignerLivreur(idLivraison, idLivreur);
        return ResponseEntity.ok(livraison);
    }


    @PatchMapping("/{id}/demarrer")
    @PreAuthorize("hasRole('LIVREUR') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LivraisonResponseDTO> demarrerLivraison(@PathVariable Long id) {
        LivraisonResponseDTO livraison = livraisonService.demarrerLivraison(id);
        return ResponseEntity.ok(livraison);
    }


    @PatchMapping("/{id}/terminer")
    @PreAuthorize("hasRole('LIVREUR') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LivraisonResponseDTO> terminerLivraison(@PathVariable Long id) {
        LivraisonResponseDTO livraison = livraisonService.terminerLivraison(id);
        return ResponseEntity.ok(livraison);
    }


    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LivraisonResponseDTO> annulerLivraison(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Annulation demandée") String motif) {

        LivraisonResponseDTO livraison = livraisonService.annulerLivraison(id, motif);
        return ResponseEntity.ok(livraison);
    }


    @PatchMapping("/{id}/adresse")
    public ResponseEntity<LivraisonResponseDTO> updateAdresseLivraison(
            @PathVariable Long id,
            @RequestParam String nouvelleAdresse) {

        LivraisonResponseDTO livraison = livraisonService.updateAdresseLivraison(id, nouvelleAdresse);
        return ResponseEntity.ok(livraison);
    }


    @PatchMapping("/{idLivraison}/reassigner/{idNouveauLivreur}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LivraisonResponseDTO> reassignerLivreur(
            @PathVariable Long idLivraison,
            @PathVariable Long idNouveauLivreur) {

        LivraisonResponseDTO livraison = livraisonService.reassignerLivreur(idLivraison, idNouveauLivreur);
        return ResponseEntity.ok(livraison);
    }


    @PostMapping("/{id}/signaler-retard")
    @PreAuthorize("hasRole('LIVREUR') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> signalerRetard(
            @PathVariable Long id,
            @RequestParam int minutesRetard) {

        livraisonService.signalerRetard(id, minutesRetard);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/statistics")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LivraisonStatisticsDTO> getStatistiques(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        LivraisonStatisticsDTO statistics = livraisonService.getStatistiques(debut, fin);
        return ResponseEntity.ok(statistics);
    }


    @GetMapping("/temps-estime")
    public ResponseEntity<Integer> calculerTempsEstime(@RequestParam String adresse) {
        int tempsEstime = livraisonService.calculerTempsEstime(adresse);
        return ResponseEntity.ok(tempsEstime);
    }


    @GetMapping("/aujourd-hui")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<LivraisonResponseDTO>> getLivraisonsDuJour() {
        List<LivraisonResponseDTO> livraisons = livraisonService.getLivraisonsDuJour();
        return ResponseEntity.ok(livraisons);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLivraison(@PathVariable Long id) {
        livraisonService.deleteLivraison(id);
        return ResponseEntity.noContent().build();
    }
}