package com.fst.rsi.resto.controller;

import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.service.ServeurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/serveurs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ServeurController {

    private final ServeurService serveurService;



    @PostMapping
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ServeurResponseDTO> createServeur(
            @Valid @RequestBody ServeurRequestDTO request) {

        ServeurResponseDTO serveur = serveurService.createServeur(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(serveur);
    }



    @GetMapping("/{id}")
    public ResponseEntity<ServeurResponseDTO> getServeurById(@PathVariable Long id) {
        ServeurResponseDTO serveur = serveurService.getServeurById(id);
        return ResponseEntity.ok(serveur);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ServeurResponseDTO> getServeurByEmail(@PathVariable String email) {
        ServeurResponseDTO serveur = serveurService.getServeurByEmail(email);
        return ResponseEntity.ok(serveur);
    }



    @GetMapping
    public ResponseEntity<Page<ServeurResponseDTO>> getAllServeurs(Pageable pageable) {
        Page<ServeurResponseDTO> serveurs = serveurService.getAllServeurs(pageable);
        return ResponseEntity.ok(serveurs);
    }



    @GetMapping("/actifs")
    public ResponseEntity<List<ServeurResponseDTO>> getServeursActifs() {
        List<ServeurResponseDTO> serveurs = serveurService.getServeursActifs();
        return ResponseEntity.ok(serveurs);
    }



    @GetMapping("/disponibles")
    public ResponseEntity<List<ServeurResponseDTO>> getServeursDisponibles() {
        List<ServeurResponseDTO> serveurs = serveurService.getServeursDisponibles();
        return ResponseEntity.ok(serveurs);
    }



    @GetMapping("/search")
    public ResponseEntity<List<ServeurResponseDTO>> searchServeurs(
            @RequestParam String searchTerm) {

        List<ServeurResponseDTO> serveurs = serveurService.searchServeurs(searchTerm);
        return ResponseEntity.ok(serveurs);
    }


    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ServeurResponseDTO> updateServeur(
            @PathVariable Long id,
            @Valid @RequestBody ServeurUpdateDTO updateDTO) {

        ServeurResponseDTO serveur = serveurService.updateServeur(id, updateDTO);
        return ResponseEntity.ok(serveur);
    }



    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordDTO passwordDTO) {

        serveurService.changePassword(id, passwordDTO);
        return ResponseEntity.ok().build();
    }



    @PatchMapping("/{id}/toggle-status")
    //@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ServeurResponseDTO> toggleServeurStatus(@PathVariable Long id) {
        ServeurResponseDTO serveur = serveurService.toggleServeurStatus(id);
        return ResponseEntity.ok(serveur);
    }



    @PatchMapping("/{id}/deactivate")
    //@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateServeur(@PathVariable Long id) {
        serveurService.deactivateServeur(id);
        return ResponseEntity.ok().build();
    }



    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteServeur(@PathVariable Long id) {
        serveurService.deleteServeur(id);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/{idServeur}/commandes/{idCommande}")
    //@PreAuthorize("hasRole('MANAGER') or hasRole('SERVEUR')")
    public ResponseEntity<CommandeResponseDTO> assignerServeurACommande(
            @PathVariable Long idServeur,
            @PathVariable Long idCommande) {

        CommandeResponseDTO commande = serveurService.assignerServeurACommande(idServeur, idCommande);
        return ResponseEntity.ok(commande);
    }



    @GetMapping("/{id}/commandes")
    public ResponseEntity<List<CommandeResponseDTO>> getCommandesByServeur(@PathVariable Long id) {
        List<CommandeResponseDTO> commandes = serveurService.getCommandesByServeur(id);
        return ResponseEntity.ok(commandes);
    }



    @GetMapping("/{id}/commandes/en-cours")
    public ResponseEntity<List<CommandeResponseDTO>> getCommandesEnCoursByServeur(@PathVariable Long id) {
        List<CommandeResponseDTO> commandes = serveurService.getCommandesEnCoursByServeur(id);
        return ResponseEntity.ok(commandes);
    }



    @GetMapping("/{id}/tables")
    public ResponseEntity<List<TableDTO>> getTablesAssignees(@PathVariable Long id) {
        List<TableDTO> tables = serveurService.getTablesAssignees(id);
        return ResponseEntity.ok(tables);
    }



    @GetMapping("/{id}/statistics")
    public ResponseEntity<ServeurStatisticsDTO> getServeurStatistics(@PathVariable Long id) {
        ServeurStatisticsDTO statistics = serveurService.getServeurStatistics(id);
        return ResponseEntity.ok(statistics);
    }



    @GetMapping("/top")
    //@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<ServeurStatisticsDTO>> getTopServeurs(
            @RequestParam(defaultValue = "10") int limit) {

        List<ServeurStatisticsDTO> topServeurs = serveurService.getTopServeurs(limit);
        return ResponseEntity.ok(topServeurs);
    }



    @GetMapping("/{id}/performances")
    //@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<PerformanceServeurDTO>> getPerformances(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        List<PerformanceServeurDTO> performances = serveurService.getPerformances(id, dateDebut, dateFin);
        return ResponseEntity.ok(performances);
    }



    @GetMapping("/performance/minimum")
    //@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<ServeurResponseDTO>> getServeursByPerformance(
            @RequestParam BigDecimal noteMinimum) {

        List<ServeurResponseDTO> serveurs = serveurService.getServeursByPerformance(noteMinimum);
        return ResponseEntity.ok(serveurs);
    }


    @GetMapping("/nouveaux")
    //@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<ServeurResponseDTO>> getNouveauxServeurs(
            @RequestParam(defaultValue = "30") int joursDepuis) {

        List<ServeurResponseDTO> serveurs = serveurService.getNouveauxServeurs(joursDepuis);
        return ResponseEntity.ok(serveurs);
    }
}