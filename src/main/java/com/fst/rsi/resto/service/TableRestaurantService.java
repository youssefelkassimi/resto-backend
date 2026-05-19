package com.fst.rsi.resto.service;

import com.fst.rsi.resto.dto.TableDTO;
import com.fst.rsi.resto.dto.TableStatistics;
import com.fst.rsi.resto.entity.TableRestaurant;
import com.fst.rsi.resto.entity.enums.StatutTable;
import com.fst.rsi.resto.exception.BusinessException;
import com.fst.rsi.resto.exception.ResourceNotFoundException;
import com.fst.rsi.resto.repository.TableRestaurantRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TableRestaurantService {

    private final TableRestaurantRepo tableRestaurantRepo;

    /**
     * Créer une nouvelle table
     */
    public TableDTO createTable(TableDTO tableDTO) {
        log.info("Création d'une nouvelle table - Numéro: {}", tableDTO.getNumero());

        // Vérifier si le numéro de table existe déjà
        if (tableRestaurantRepo.findByNumero(tableDTO.getNumero()).isPresent()) {
            throw new BusinessException("Une table avec le numéro " + tableDTO.getNumero() + " existe déjà");
        }

        TableRestaurant table = TableRestaurant.builder()
                .numero(tableDTO.getNumero())
                .capacite(tableDTO.getCapacite())
                .statut(tableDTO.getStatut() != null ? tableDTO.getStatut() : StatutTable.LIBRE)
                .build();

        TableRestaurant savedTable = tableRestaurantRepo.save(table);
        log.info("Table créée avec succès - ID: {}", savedTable.getIdTable());

        return mapToDTO(savedTable);
    }


    @Transactional(readOnly = true)
    public List<TableDTO> getAllTables() {
        log.info("Récupération de toutes les tables");
        return tableRestaurantRepo.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public TableDTO getTableById(Long id) {
        log.info("Récupération de la table avec l'ID: {}", id);
        TableRestaurant table = tableRestaurantRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table non trouvée avec l'ID: " + id));
        return mapToDTO(table);
    }

    @Transactional(readOnly = true)
    public TableDTO getTableByNumero(Integer numero) {
        log.info("Récupération de la table numéro: {}", numero);
        TableRestaurant table = tableRestaurantRepo.findByNumero(numero)
                .orElseThrow(() -> new ResourceNotFoundException("Table non trouvée avec le numéro: " + numero));
        return mapToDTO(table);
    }


    @Transactional(readOnly = true)
    public List<TableDTO> getTablesByStatut(StatutTable statut) {
        log.info("Récupération des tables avec le statut: {}", statut);
        return tableRestaurantRepo.findByStatut(statut)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TableDTO> getTablesByCapacite(Integer capacite) {
        log.info("Récupération des tables avec une capacité de: {}", capacite);
        return tableRestaurantRepo.findByCapacite(capacite)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<TableDTO> getTablesDisponibles() {
        log.info("Récupération des tables disponibles");
        return getTablesByStatut(StatutTable.LIBRE);
    }


    @Transactional(readOnly = true)
    public List<TableDTO> getTablesOccupees() {
        log.info("Récupération des tables occupées");
        return getTablesByStatut(StatutTable.OCCUPEE);
    }


    public TableDTO updateTable(Long id, TableDTO tableDTO) {
        log.info("Mise à jour de la table avec l'ID: {}", id);

        TableRestaurant existingTable = tableRestaurantRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table non trouvée avec l'ID: " + id));

        // Vérifier si le nouveau numéro est déjà utilisé par une autre table
        if (tableDTO.getNumero() != null && !tableDTO.getNumero().equals(existingTable.getNumero())) {
            tableRestaurantRepo.findByNumero(tableDTO.getNumero()).ifPresent(table -> {
                if (!table.getIdTable().equals(id)) {
                    throw new BusinessException("Une table avec le numéro " + tableDTO.getNumero() + " existe déjà");
                }
            });
            existingTable.setNumero(tableDTO.getNumero());
        }

        if (tableDTO.getCapacite() != null) {
            existingTable.setCapacite(tableDTO.getCapacite());
        }

        if (tableDTO.getStatut() != null) {
            existingTable.setStatut(tableDTO.getStatut());
        }

        TableRestaurant updatedTable = tableRestaurantRepo.save(existingTable);
        log.info("Table mise à jour avec succès - ID: {}", updatedTable.getIdTable());

        return mapToDTO(updatedTable);
    }


    public TableDTO changeStatut(Long id, StatutTable nouveauStatut) {
        log.info("Changement du statut de la table {} vers {}", id, nouveauStatut);

        TableRestaurant table = tableRestaurantRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table non trouvée avec l'ID: " + id));

        table.setStatut(nouveauStatut);
        TableRestaurant updatedTable = tableRestaurantRepo.save(table);

        log.info("Statut de la table {} changé avec succès vers {}", id, nouveauStatut);
        return mapToDTO(updatedTable);
    }


    public TableDTO occuperTable(Long id) {
        log.info("Occupation de la table avec l'ID: {}", id);
        return changeStatut(id, StatutTable.OCCUPEE);
    }


    public TableDTO libererTable(Long id) {
        log.info("Libération de la table avec l'ID: {}", id);
        return changeStatut(id, StatutTable.LIBRE);
    }


    public TableDTO reserverTable(Long id) {
        log.info("Réservation de la table avec l'ID: {}", id);
        return changeStatut(id, StatutTable.RESERVEE);
    }


    public void deleteTable(Long id) {
        log.info("Suppression de la table avec l'ID: {}", id);

        TableRestaurant table = tableRestaurantRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table non trouvée avec l'ID: " + id));

        if (table.getStatut() == StatutTable.OCCUPEE) {
            throw new IllegalStateException("Impossible de supprimer une table occupée");
        }

        tableRestaurantRepo.deleteById(id);
        log.info("Table supprimée avec succès - ID: {}", id);
    }


    @Transactional(readOnly = true)
    public long countTablesByStatut(StatutTable statut) {
        return tableRestaurantRepo.findByStatut(statut).size();
    }


    @Transactional(readOnly = true)
    public TableStatistics getTableStatistics() {
        List<TableRestaurant> allTables = tableRestaurantRepo.findAll();

        long totalTables = allTables.size();
        long tablesLibres = allTables.stream().filter(t -> t.getStatut() == StatutTable.LIBRE).count();
        long tablesOccupees = allTables.stream().filter(t -> t.getStatut() == StatutTable.OCCUPEE).count();
        long tablesReservees = allTables.stream().filter(t -> t.getStatut() == StatutTable.RESERVEE).count();
        long tablesHorsService = allTables.stream().filter(t -> t.getStatut() == StatutTable.HORS_SERVICE).count();

        return TableStatistics.builder()
                .totalTables(totalTables)
                .tablesLibres(tablesLibres)
                .tablesOccupees(tablesOccupees)
                .tablesReservees(tablesReservees)
                .tablesHorsService(tablesHorsService)
                .build();
    }


    private TableDTO mapToDTO(TableRestaurant table) {
        return TableDTO.builder()
                .idTable(table.getIdTable())
                .numero(table.getNumero())
                .capacite(table.getCapacite())
                .statut(table.getStatut())
                .build();
    }


}