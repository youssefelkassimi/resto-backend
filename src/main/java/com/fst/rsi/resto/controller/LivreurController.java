package com.fst.rsi.resto.controller;

import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.entity.enums.*;
import com.fst.rsi.resto.service.LivreurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/livreurs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LivreurController {

    private final LivreurService livreurService;


    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LivreurResponseDTO> createLivreur(
            @Valid @RequestBody LivreurRequestDTO request) {

        LivreurResponseDTO livreur = livreurService.createLivreur(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(livreur);
    }


    @GetMapping("/{id}")
    public ResponseEntity<LivreurResponseDTO> getLivreurById(@PathVariable Long id) {
        LivreurResponseDTO livreur = livreurService.getLivreurById(id);
        return ResponseEntity.ok(livreur);
    }


    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<LivreurResponseDTO>> getAllLivreurs(Pageable pageable) {
        Page<LivreurResponseDTO> livreurs = livreurService.getAllLivreurs(pageable);
        return ResponseEntity.ok(livreurs);
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or hasRole('RESPONSABLE_CUISINE')")
    public ResponseEntity<List<LivreurResponseDTO>> getLivreursDisponibles() {
        List<LivreurResponseDTO> livreurs = livreurService.getLivreursDisponibles();
        return ResponseEntity.ok(livreurs);
    }


    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or hasRole('RESPONSABLE_CUISINE')")
    public ResponseEntity<List<LivreurResponseDTO>> getLivreursByStatut(
            @PathVariable StatutLivreur statut) {

        List<LivreurResponseDTO> livreurs = livreurService.getLivreursByStatut(statut);
        return ResponseEntity.ok(livreurs);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','LIVREUR') ")
    public ResponseEntity<LivreurResponseDTO> getLivreursByEmail(
            @PathVariable String email) {

        return ResponseEntity.ok(livreurService.getLivreurByEmail(email));
    }



    @GetMapping("/en-livraison")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<LivreurResponseDTO>> getLivreursEnLivraison() {
        List<LivreurResponseDTO> livreurs = livreurService.getLivreursEnLivraison();
        return ResponseEntity.ok(livreurs);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<LivreurResponseDTO>> searchLivreurs(
            @RequestParam String searchTerm) {

        List<LivreurResponseDTO> livreurs = livreurService.searchLivreurs(searchTerm);
        return ResponseEntity.ok(livreurs);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LivreurResponseDTO> updateLivreur(
            @PathVariable Long id,
            @Valid @RequestBody LivreurUpdateDTO updateDTO) {

        LivreurResponseDTO livreur = livreurService.updateLivreur(id, updateDTO);
        return ResponseEntity.ok(livreur);
    }


    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordDTO passwordDTO) {

        livreurService.changePassword(id, passwordDTO);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{id}/toggle-disponibilite")
    public ResponseEntity<LivreurResponseDTO> toggleDisponibilite(@PathVariable Long id) {
        LivreurResponseDTO livreur = livreurService.toggleDisponibilite(id);
        return ResponseEntity.ok(livreur);
    }


    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LivreurResponseDTO> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutLivreur nouveauStatut) {

        LivreurResponseDTO livreur = livreurService.changerStatut(id, nouveauStatut);
        return ResponseEntity.ok(livreur);
    }


    @PatchMapping("/{id}/conge")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LivreurResponseDTO> mettreEnConge(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        LivreurResponseDTO livreur = livreurService.mettreEnConge(id, dateDebut, dateFin);
        return ResponseEntity.ok(livreur);
    }


    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateLivreur(@PathVariable Long id) {
        livreurService.deactivateLivreur(id);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLivreur(@PathVariable Long id) {
        livreurService.deleteLivreur(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}/livraisons")
    public ResponseEntity<List<LivraisonResponseDTO>> getLivraisonsByLivreur(@PathVariable Long id) {
        List<LivraisonResponseDTO> livraisons = livreurService.getLivraisonsByLivreur(id);
        return ResponseEntity.ok(livraisons);
    }


    @GetMapping("/{id}/livraisons/en-cours")
    public ResponseEntity<List<LivraisonResponseDTO>> getLivraisonsEnCoursByLivreur(@PathVariable Long id) {
        List<LivraisonResponseDTO> livraisons = livreurService.getLivraisonsEnCoursByLivreur(id);
        return ResponseEntity.ok(livraisons);
    }


    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LivreurStatisticsDTO> getLivreurStatistics(@PathVariable Long id) {
        LivreurStatisticsDTO statistics = livreurService.getLivreurStatistics(id);
        return ResponseEntity.ok(statistics);
    }


    @GetMapping("/top")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<LivreurStatisticsDTO>> getTopLivreurs(
            @RequestParam(defaultValue = "10") int limit) {

        List<LivreurStatisticsDTO> topLivreurs = livreurService.getTopLivreurs(limit);
        return ResponseEntity.ok(topLivreurs);
    }





    @GetMapping("/nouveaux")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<LivreurResponseDTO>> getNouveauxLivreurs(
            @RequestParam(defaultValue = "30") int joursDepuis) {

        List<LivreurResponseDTO> livreurs = livreurService.getNouveauxLivreurs(joursDepuis);
        return ResponseEntity.ok(livreurs);
    }
}