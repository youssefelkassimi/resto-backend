package com.fst.rsi.resto.controller;

import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.entity.enums.StatutClient;
import com.fst.rsi.resto.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Client Management", description = "APIs pour la gestion des clients")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @Operation(summary = "Créer un nouveau client", description = "Enregistre un nouveau client dans le système")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Email ou téléphone déjà existant")
    })
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO request) {
        log.info("REST request to create client: {}", request.getEmail());
        ClientResponseDTO response = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CLIENT')")
    @Operation(summary = "Obtenir un client par ID", description = "Récupère les détails d'un client par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client trouvé"),
            @ApiResponse(responseCode = "404", description = "Client non trouvé")
    })
    public ResponseEntity<ClientResponseDTO> getClientById(
            @Parameter(description = "ID du client") @PathVariable Long id) {
        log.info("REST request to get client by id: {}", id);
        ClientResponseDTO response = clientService.getClientById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER','CLIENT')")
    @Operation(summary = "Obtenir un client par email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client trouvé"),
            @ApiResponse(responseCode = "404", description = "Client non trouvé")
    })
    public ResponseEntity<ClientResponseDTO> getClientByEmail(
            @Parameter(description = "Email du client") @PathVariable String email) {
        log.info("REST request to get client by email: {}", email);
        ClientResponseDTO response = clientService.getClientByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/telephone/{telephone}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Obtenir un client par téléphone")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client trouvé"),
            @ApiResponse(responseCode = "404", description = "Client non trouvé")
    })
    public ResponseEntity<ClientResponseDTO> getClientByTelephone(
            @Parameter(description = "Numéro de téléphone") @PathVariable String telephone) {
        log.info("REST request to get client by telephone: {}", telephone);
        ClientResponseDTO response = clientService.getClientByTelephone(telephone);
        return ResponseEntity.ok(response);
    }

    @GetMapping
      @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Obtenir tous les clients", description = "Récupère la liste paginée de tous les clients")
    @ApiResponse(responseCode = "200", description = "Liste des clients récupérée")
    public ResponseEntity<Page<ClientResponseDTO>> getAllClients(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("REST request to get all clients - page: {}", pageable.getPageNumber());
        Page<ClientResponseDTO> response = clientService.getAllClients(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/actifs")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Obtenir les clients actifs")
    @ApiResponse(responseCode = "200", description = "Liste des clients actifs")
    public ResponseEntity<List<ClientResponseDTO>> getClientsActifs() {
        log.info("REST request to get active clients");
        List<ClientResponseDTO> response = clientService.getClientsActifs();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statut/{statut}")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Obtenir les clients par statut de fidélité")
    @ApiResponse(responseCode = "200", description = "Liste des clients par statut")
    public ResponseEntity<List<ClientResponseDTO>> getClientsByStatut(
            @Parameter(description = "Statut de fidélité") @PathVariable StatutClient statut) {
        log.info("REST request to get clients by statut: {}", statut);
        List<ClientResponseDTO> response = clientService.getClientsByStatut(statut);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
      @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Rechercher des clients", description = "Recherche des clients par nom ou prénom")
    @ApiResponse(responseCode = "200", description = "Résultats de la recherche")
    public ResponseEntity<List<ClientResponseDTO>> searchClients(
            @Parameter(description = "Terme de recherche") @RequestParam String searchTerm) {
        log.info("REST request to search clients: {}", searchTerm);
        List<ClientResponseDTO> response = clientService.searchClients(searchTerm);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vip")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Obtenir les clients VIP", description = "Récupère les clients Gold et Platinum")
    @ApiResponse(responseCode = "200", description = "Liste des clients VIP")
    public ResponseEntity<List<ClientResponseDTO>> getClientsVIP() {
        log.info("REST request to get VIP clients");
        List<ClientResponseDTO> response = clientService.getClientsVIP();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nouveaux")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Obtenir les nouveaux clients", description = "Récupère les clients inscrits dans les 30 derniers jours")
    @ApiResponse(responseCode = "200", description = "Liste des nouveaux clients")
    public ResponseEntity<List<ClientResponseDTO>> getNouveauxClients() {
        log.info("REST request to get new clients");
        List<ClientResponseDTO> response = clientService.getNouveauxClients();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inactifs")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Obtenir les clients inactifs")
    @ApiResponse(responseCode = "200", description = "Liste des clients inactifs")
    public ResponseEntity<List<ClientResponseDTO>> getClientsInactifs(
            @Parameter(description = "Nombre de jours d'inactivité")
            @RequestParam(defaultValue = "90") int joursInactivite) {
        log.info("REST request to get inactive clients - days: {}", joursInactivite);
        List<ClientResponseDTO> response = clientService.getClientsInactifs(joursInactivite);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or (#id == authentication.principal.id)")
    @Operation(summary = "Mettre à jour un client")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client mis à jour"),
            @ApiResponse(responseCode = "404", description = "Client non trouvé"),
            @ApiResponse(responseCode = "409", description = "Téléphone déjà utilisé")
    })
    public ResponseEntity<ClientResponseDTO> updateClient(
            @Parameter(description = "ID du client") @PathVariable Long id,
            @Valid @RequestBody ClientUpdateDTO updateDTO) {
        log.info("REST request to update client: {}", id);
        ClientResponseDTO response = clientService.updateClient(id, updateDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAnyRole('ADMIN') or (#id == authentication.principal.id)")
    @Operation(summary = "Changer le mot de passe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mot de passe changé"),
            @ApiResponse(responseCode = "400", description = "Ancien mot de passe incorrect")
    })
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "ID du client") @PathVariable Long id,
            @Valid @RequestBody ChangePasswordDTO passwordDTO) {
        log.info("REST request to change password for client: {}", id);
        clientService.changePassword(id, passwordDTO);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/toggle-status")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Activer/Désactiver un client")
    @ApiResponse(responseCode = "200", description = "Statut modifié")
    public ResponseEntity<ClientResponseDTO> toggleClientStatus(
            @Parameter(description = "ID du client") @PathVariable Long id) {
        log.info("REST request to toggle client status: {}", id);
        ClientResponseDTO response = clientService.toggleClientStatus(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Désactiver un client")
    @ApiResponse(responseCode = "200", description = "Client désactivé")
    public ResponseEntity<Void> deactivateClient(
            @Parameter(description = "ID du client") @PathVariable Long id) {
        log.info("REST request to deactivate client: {}", id);
        clientService.deactivateClient(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un client", description = "Suppression logique du client")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client supprimé"),
            @ApiResponse(responseCode = "400", description = "Client avec commandes actives")
    })
    public ResponseEntity<Void> deleteClient(
            @Parameter(description = "ID du client") @PathVariable Long id) {
        log.info("REST request to delete client: {}", id);
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/points/add")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Ajouter des points de fidélité")
    @ApiResponse(responseCode = "200", description = "Points ajoutés")
    public ResponseEntity<ClientResponseDTO> ajouterPointsFidelite(
            @Parameter(description = "ID du client") @PathVariable Long id,
            @Parameter(description = "Nombre de points") @RequestParam Integer points) {
        log.info("REST request to add loyalty points: {} for client: {}", points, id);
        ClientResponseDTO response = clientService.ajouterPointsFidelite(id, points);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/points/use")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR', 'CLIENT')")
    @Operation(summary = "Utiliser des points de fidélité")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Points utilisés"),
            @ApiResponse(responseCode = "400", description = "Points insuffisants")
    })
    public ResponseEntity<ClientResponseDTO> utiliserPointsFidelite(
            @Parameter(description = "ID du client") @PathVariable Long id,
            @Parameter(description = "Nombre de points") @RequestParam Integer points) {
        log.info("REST request to use loyalty points: {} for client: {}", points, id);
        ClientResponseDTO response = clientService.utiliserPointsFidelite(id, points);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/commandes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR') or (#id == authentication.principal.id)")
    @Operation(summary = "Obtenir l'historique des commandes")
    @ApiResponse(responseCode = "200", description = "Historique récupéré")
    public ResponseEntity<List<CommandeHistoriqueDTO>> getHistoriqueCommandes(
            @Parameter(description = "ID du client") @PathVariable Long id) {
        log.info("REST request to get order history for client: {}", id);
        List<CommandeHistoriqueDTO> response = clientService.getHistoriqueCommandes(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or (#id == authentication.principal.id)")
    @Operation(summary = "Obtenir les statistiques du client")
    @ApiResponse(responseCode = "200", description = "Statistiques récupérées")
    public ResponseEntity<ClientStatisticsDTO> getClientStatistics(
            @Parameter(description = "ID du client") @PathVariable Long id) {
        log.info("REST request to get statistics for client: {}", id);
        ClientStatisticsDTO response = clientService.getClientStatistics(id);
        return ResponseEntity.ok(response);
    }
}