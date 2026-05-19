package com.fst.rsi.resto.controller;

import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.entity.enums.*;
import com.fst.rsi.resto.service.CommandeService;
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
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommandeController {

    private final CommandeService commandeService;

    @PostMapping
    public ResponseEntity<CommandeResponseDTO> createCommande(
            @Valid @RequestBody CommandeRequestDTO request) {

        CommandeResponseDTO commande = commandeService.createCommande(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(commande);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponseDTO> getCommandeById(@PathVariable Long id) {
        CommandeResponseDTO commande = commandeService.getCommandeById(id);
        return ResponseEntity.ok(commande);
    }


    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<CommandeResponseDTO>> getAllCommandes(Pageable pageable) {
        Page<CommandeResponseDTO> commandes = commandeService.getAllCommandes(pageable);
        return ResponseEntity.ok(commandes);
    }



    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CommandeResponseDTO>> getCommandesByClient(
            @PathVariable Long clientId) {

        List<CommandeResponseDTO> commandes = commandeService.getCommandesByClient(clientId);
        return ResponseEntity.ok(commandes);
    }


    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('SERVEUR') or hasRole('ADMIN')")
    public ResponseEntity<List<CommandeResponseDTO>> getCommandesByStatut(
            @PathVariable StatutCommande statut) {

        List<CommandeResponseDTO> commandes = commandeService.getCommandesByStatut(statut);
        return ResponseEntity.ok(commandes);
    }


    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('SERVEUR') or hasRole('ADMIN')")
    public ResponseEntity<List<CommandeResponseDTO>> getCommandesByType(
            @PathVariable TypeCommande type) {

        List<CommandeResponseDTO> commandes = commandeService.getCommandesByType(type);
        return ResponseEntity.ok(commandes);
    }


    @GetMapping("/aujourd-hui")
    @PreAuthorize("hasRole('MANAGER') or hasRole('SERVEUR') or hasRole('ADMIN')")
    public ResponseEntity<List<CommandeResponseDTO>> getCommandesDuJour() {
        List<CommandeResponseDTO> commandes = commandeService.getCommandesDuJour();
        return ResponseEntity.ok(commandes);
    }


    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('MANAGER') or hasRole('SERVEUR') or hasRole('ADMIN')")
    public ResponseEntity<List<CommandeResponseDTO>> getCommandesEnAttente() {
        List<CommandeResponseDTO> commandes = commandeService.getCommandesEnAttente();
        return ResponseEntity.ok(commandes);
    }


    @GetMapping("/periode")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<CommandeResponseDTO>> getCommandesByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        List<CommandeResponseDTO> commandes = commandeService.getCommandesByPeriode(debut, fin);
        return ResponseEntity.ok(commandes);
    }


    @PutMapping("/{id}/statut")
    @PreAuthorize("hasRole('MANAGER') or hasRole('SERVEUR') or hasRole('ADMIN')")
    public ResponseEntity<CommandeResponseDTO> updateStatut(
            @PathVariable Long id,
            @RequestParam StatutCommande nouveauStatut) {

        CommandeResponseDTO commande = commandeService.updateStatut(id, nouveauStatut);
        return ResponseEntity.ok(commande);
    }


    @PutMapping("/{id}")
                  @PreAuthorize("hasRole('MANAGER') or hasRole('SERVEUR') or hasRole('CLIENT')")
    public ResponseEntity<CommandeResponseDTO> updateCommande(
            @PathVariable Long id,
            @Valid @RequestBody CommandeRequestDTO request) {

        CommandeResponseDTO commande = commandeService.updateCommande(id, request);
        return ResponseEntity.ok(commande);
    }


    @PutMapping("/{id}/annuler")
    public ResponseEntity<CommandeResponseDTO> annulerCommande(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Annulation demandée par le client") String motif) {

        CommandeResponseDTO commande = commandeService.annulerCommande(id, motif);
        return ResponseEntity.ok(commande);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCommande(@PathVariable Long id) {
        commandeService.deleteCommande(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/statistics")
           @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<CommandeStatisticsDTO> getStatistiques(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        CommandeStatisticsDTO statistics = commandeService.getStatistiques(debut, fin);
        return ResponseEntity.ok(statistics);
    }


    @PostMapping("/verifier-disponibilite")
    public ResponseEntity<VerificationDisponibiliteDTO> verifierDisponibilite(
            @Valid @RequestBody CommandeRequestDTO request) {

        boolean disponible = commandeService.verifierDisponibiliteCommande(request);

        VerificationDisponibiliteDTO response = VerificationDisponibiliteDTO.builder()
                .disponible(disponible)
                .message(disponible ? "Tous les plats sont disponibles" : "Certains plats ne sont pas disponibles")
                .build();

        return ResponseEntity.ok(response);
    }


    @PostMapping("/calculer-montant")
    public ResponseEntity<MontantCommandeDTO> calculerMontant(
            @Valid @RequestBody CommandeRequestDTO request) {

        MontantCommandeDTO montant = commandeService.calculerMontantCommande(request);
        return ResponseEntity.ok(montant);
    }





    @GetMapping("/export/csv")
    //       //       @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportCommandesCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        byte[] csvData = commandeService.exportCommandesCSV(debut, fin);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=commandes.csv")
                .header("Content-Type", "text/csv")
                .body(csvData);
    }

    /**
     * Obtenir les commandes récentes d'un client
     */
    @GetMapping("/client/{clientId}/recentes")
    public ResponseEntity<List<CommandeResponseDTO>> getCommandesRecentesClient(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "5") int limit) {

//        TODO: a faire chababe
//        List<CommandeResponseDTO> commandes = commandeService.getCommandesRecentesClient(clientId, limit);
        return ResponseEntity.ok(null);
    }


}