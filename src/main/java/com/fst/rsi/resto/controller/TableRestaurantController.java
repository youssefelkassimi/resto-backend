package com.fst.rsi.resto.controller;

import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.entity.enums.StatutTable;
import com.fst.rsi.resto.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tables", description = "API de gestion des tables du restaurant")
public class TableRestaurantController {

    private final TableRestaurantService tableRestaurantService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Créer une nouvelle table")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Table créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Une table avec ce numéro existe déjà")
    })
    public ResponseEntity<TableDTO> createTable(@Valid @RequestBody TableDTO tableDTO) {
        log.info("Requête de création d'une table - Numéro: {}", tableDTO.getNumero());
        TableDTO createdTable = tableRestaurantService.createTable(tableDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTable);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Obtenir toutes les tables")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des tables récupérée avec succès")
    })
    public ResponseEntity<List<TableDTO>> getAllTables() {
        log.info("Requête de récupération de toutes les tables");
        List<TableDTO> tables = tableRestaurantService.getAllTables();
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Obtenir une table par son ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Table trouvée"),
            @ApiResponse(responseCode = "404", description = "Table non trouvée")
    })
    public ResponseEntity<TableDTO> getTableById(@PathVariable Long id) {
        log.info("Requête de récupération de la table avec l'ID: {}", id);
        TableDTO table = tableRestaurantService.getTableById(id);
        return ResponseEntity.ok(table);
    }

    @GetMapping("/numero/{numero}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Obtenir une table par son numéro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Table trouvée"),
            @ApiResponse(responseCode = "404", description = "Table non trouvée")
    })
    public ResponseEntity<TableDTO> getTableByNumero(@PathVariable Integer numero) {
        log.info("Requête de récupération de la table numéro: {}", numero);
        TableDTO table = tableRestaurantService.getTableByNumero(numero);
        return ResponseEntity.ok(table);
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Obtenir les tables par statut")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des tables filtrées par statut")
    })
    public ResponseEntity<List<TableDTO>> getTablesByStatut(@PathVariable StatutTable statut) {
        log.info("Requête de récupération des tables avec le statut: {}", statut);
        List<TableDTO> tables = tableRestaurantService.getTablesByStatut(statut);
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/capacite/{capacite}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Obtenir les tables par capacité")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des tables avec la capacité spécifiée")
    })
    public ResponseEntity<List<TableDTO>> getTablesByCapacite(@PathVariable Integer capacite) {
        log.info("Requête de récupération des tables avec une capacité de: {}", capacite);
        List<TableDTO> tables = tableRestaurantService.getTablesByCapacite(capacite);
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Obtenir toutes les tables disponibles (libres)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des tables disponibles")
    })
    public ResponseEntity<List<TableDTO>> getTablesDisponibles() {
        log.info("Requête de récupération des tables disponibles");
        List<TableDTO> tables = tableRestaurantService.getTablesDisponibles();
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/occupees")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Obtenir toutes les tables occupées")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des tables occupées")
    })
    public ResponseEntity<List<TableDTO>> getTablesOccupees() {
        log.info("Requête de récupération des tables occupées");
        List<TableDTO> tables = tableRestaurantService.getTablesOccupees();
        return ResponseEntity.ok(tables);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Mettre à jour une table")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Table mise à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Table non trouvée"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Numéro de table déjà utilisé")
    })
    public ResponseEntity<TableDTO> updateTable(
            @PathVariable Long id,
            @Valid @RequestBody TableDTO tableDTO) {
        log.info("Requête de mise à jour de la table avec l'ID: {}", id);
        TableDTO updatedTable = tableRestaurantService.updateTable(id, tableDTO);
        return ResponseEntity.ok(updatedTable);
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Changer le statut d'une table")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statut changé avec succès"),
            @ApiResponse(responseCode = "404", description = "Table non trouvée")
    })
    public ResponseEntity<TableDTO> changeStatut(
            @PathVariable Long id,
            @RequestParam StatutTable statut) {
        log.info("Requête de changement de statut de la table {} vers {}", id, statut);
        TableDTO updatedTable = tableRestaurantService.changeStatut(id, statut);
        return ResponseEntity.ok(updatedTable);
    }

    @PatchMapping("/{id}/occuper")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Marquer une table comme occupée")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Table marquée comme occupée"),
            @ApiResponse(responseCode = "404", description = "Table non trouvée")
    })
    public ResponseEntity<TableDTO> occuperTable(@PathVariable Long id) {
        log.info("Requête pour occuper la table avec l'ID: {}", id);
        TableDTO updatedTable = tableRestaurantService.occuperTable(id);
        return ResponseEntity.ok(updatedTable);
    }

    @PatchMapping("/{id}/liberer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Libérer une table (la rendre disponible)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Table libérée avec succès"),
            @ApiResponse(responseCode = "404", description = "Table non trouvée")
    })
    public ResponseEntity<TableDTO> libererTable(@PathVariable Long id) {
        log.info("Requête pour libérer la table avec l'ID: {}", id);
        TableDTO updatedTable = tableRestaurantService.libererTable(id);
        return ResponseEntity.ok(updatedTable);
    }

    @PatchMapping("/{id}/reserver")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SERVEUR')")
    @Operation(summary = "Réserver une table")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Table réservée avec succès"),
            @ApiResponse(responseCode = "404", description = "Table non trouvée")
    })
    public ResponseEntity<TableDTO> reserverTable(@PathVariable Long id) {
        log.info("Requête pour réserver la table avec l'ID: {}", id);
        TableDTO updatedTable = tableRestaurantService.reserverTable(id);
        return ResponseEntity.ok(updatedTable);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Supprimer une table")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Table supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Table non trouvée"),
            @ApiResponse(responseCode = "400", description = "Impossible de supprimer une table occupée")
    })
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        log.info("Requête de suppression de la table avec l'ID: {}", id);
        tableRestaurantService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Obtenir les statistiques des tables")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès")
    })
    public ResponseEntity<TableStatistics> getTableStatistics() {
        log.info("Requête de récupération des statistiques des tables");
        TableStatistics statistics = tableRestaurantService.getTableStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Compter les tables par statut")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nombre de tables récupéré")
    })
    public ResponseEntity<Long> countTablesByStatut(@RequestParam StatutTable statut) {
        log.info("Requête de comptage des tables avec le statut: {}", statut);
        long count = tableRestaurantService.countTablesByStatut(statut);
        return ResponseEntity.ok(count);
    }
}