package com.fst.rsi.resto.service;

import com.fst.rsi.resto.dto.FactureDTO;
import com.fst.rsi.resto.dto.GestionStockDTO;
import com.fst.rsi.resto.dto.IngredientDTO;
import com.fst.rsi.resto.entity.*;
import com.fst.rsi.resto.entity.enums.*;
import com.fst.rsi.resto.exception.*;
import com.fst.rsi.resto.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GestionStockService {

    private final IngredientRepo ingredientRepository;
    private final GestionStockRepo gestionStockRepository;
    private final AlerteStockRepo alerteStockRepository;
    private final ManagerRepo managerRepository;
    private final FournisseurRepo fournisseurRepository;
    private final CommandeFournisseurRepo commandeFournisseurRepository;


    public IngredientDTO createIngredient(String nom, BigDecimal quantiteStock, String unite,
                                       BigDecimal seuilAlerte, LocalDate datePeremption, Long idFournisseur) {
        log.info("Création d'un nouvel ingrédient: {}", nom);

        if (ingredientRepository.existsByNom(nom)) {
            throw new BusinessException("Un ingrédient avec ce nom existe déjà");
        }

        Fournisseur fournisseur = null;
        if (idFournisseur != null) {
            fournisseur = fournisseurRepository.findById(idFournisseur)
                    .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé"));
        }

        Ingredient ingredient = Ingredient.builder()
                .nom(nom)
                .quantiteStock(quantiteStock)
                .unite(unite)
                .seuilAlerte(seuilAlerte)
                .datePeremption(datePeremption)
                .fournisseur(fournisseur)
                .build();

        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        verifierEtCreerAlerte(savedIngredient);

        log.info("Ingrédient créé avec succès - ID: {}", savedIngredient.getIdIngredient());
        return convertToIngredientDTO(savedIngredient);
    }


    @Transactional(readOnly = true)
    public Ingredient getIngredientById(Long id) {
        log.info("Récupération de l'ingrédient ID: {}", id);
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrédient non trouvé avec l'ID: %d".formatted(id)));
    }


    @Transactional(readOnly = true)
    public Page<IngredientDTO> getAllIngredients(Pageable pageable) {
        log.info("Récupération de tous les ingrédients");
        Page<Ingredient> ingredientsPage = ingredientRepository.findAll(pageable);

        List<IngredientDTO> dtoList = ingredientsPage.getContent().stream()
                .map(this::convertToIngredientDTO)
                .toList();

        return new PageImpl<>(dtoList, pageable, ingredientsPage.getTotalElements());
    }


    @Transactional(readOnly = true)
    public List<IngredientDTO> searchIngredients(String searchTerm) {
        log.info("Recherche d'ingrédients avec le terme: {}", searchTerm);
        return ingredientRepository.findByNomContainingIgnoreCase(searchTerm).stream()
                .map(this::convertToIngredientDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<IngredientDTO> getIngredientsEnRupture() {
        log.info("Récupération des ingrédients en rupture de stock");
        return ingredientRepository.findByQuantiteStockLessThanEqual(BigDecimal.ZERO).stream()
                .map(this::convertToIngredientDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<IngredientDTO> getIngredientsSousSeuilAlerte() {
        log.info("Récupération des ingrédients sous le seuil d'alerte");
        return ingredientRepository.findByQuantiteStockLessThanEqualSeuilAlerte().stream()
                .map(this::convertToIngredientDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<IngredientDTO> getIngredientsProchesPeremption(int joursAvant) {
        log.info("Récupération des ingrédients proches de la péremption (dans {} jours)", joursAvant);
        LocalDate dateLimit = LocalDate.now().plusDays(joursAvant);
        return ingredientRepository.findByDatePeremptionBefore(dateLimit)
                .stream()
                .filter(i -> i.getDatePeremption().isAfter(LocalDate.now()))
                .map(this::convertToIngredientDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<IngredientDTO> getIngredientsPerimes() {
        log.info("Récupération des ingrédients périmés");
        return ingredientRepository.findByDatePeremptionBefore(LocalDate.now()).stream()
                .map(this::convertToIngredientDTO)
                .toList();
    }


    public IngredientDTO updateIngredient(Long id, String nom, String unite, BigDecimal seuilAlerte,
                                       LocalDate datePeremption, Long idFournisseur) {
        log.info("Mise à jour de l'ingrédient ID: {}", id);

        Ingredient ingredient = getIngredientById(id);

        if (nom != null && !ingredient.getNom().equals(nom) && ingredientRepository.existsByNom(nom)) {
            throw new BusinessException("Un ingrédient avec ce nom existe déjà");
        }

        if (nom != null) ingredient.setNom(nom);
        if (unite != null) ingredient.setUnite(unite);
        if (seuilAlerte != null) ingredient.setSeuilAlerte(seuilAlerte);
        if (datePeremption != null) ingredient.setDatePeremption(datePeremption);

        if (idFournisseur != null) {
            Fournisseur fournisseur = fournisseurRepository.findById(idFournisseur)
                    .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé"));
            ingredient.setFournisseur(fournisseur);
        }

        Ingredient updatedIngredient = ingredientRepository.save(ingredient);
        verifierEtCreerAlerte(updatedIngredient);

        log.info("Ingrédient mis à jour avec succès");
        return convertToIngredientDTO(updatedIngredient);
    }


    public void deleteIngredient(Long id) {
        log.info("Suppression de l'ingrédient ID: {}", id);

        Ingredient ingredient = getIngredientById(id);

        if (ingredient.getCompositions() != null && !ingredient.getCompositions().isEmpty()) {
            throw new BusinessException(
                    "Impossible de supprimer cet ingrédient. Il est utilisé dans %d plat(s)".formatted(ingredient.getCompositions().size())
            );
        }

        ingredientRepository.delete(ingredient);
        log.info("Ingrédient supprimé avec succès");
    }

    // ==================== GESTION DES STOCKS ====================


    public GestionStockDTO ajouterStock(Long idIngredient, Long idManager, BigDecimal quantite,
                                     TypeOperationStock typeOperation, String motif, LocalDate datePeremption) {
        log.info("Ajout de stock pour l'ingrédient ID: {}", idIngredient);

        Ingredient ingredient = getIngredientById(idIngredient);
        Manager manager = managerRepository.findById(idManager)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));

        if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La quantité doit être positive");
        }

        BigDecimal quantiteAvant = ingredient.getQuantiteStock();
        BigDecimal quantiteApres = quantiteAvant.add(quantite);

        ingredient.setQuantiteStock(quantiteApres);
        if (datePeremption != null) {
            ingredient.setDatePeremption(datePeremption);
        }
        ingredientRepository.save(ingredient);

        GestionStock gestionStock = GestionStock.builder()
                .manager(manager)
                .ingredient(ingredient)
                .typeOperation(typeOperation != null ? typeOperation : TypeOperationStock.AJOUT)
                .quantite(quantite)
                .dateOperation(LocalDateTime.now())
                .motif(motif)
                .quantiteAvant(quantiteAvant)
                .quantiteApres(quantiteApres)
                .build();

        GestionStock savedGestion = gestionStockRepository.save(gestionStock);
        traiterAlertesStock(ingredient);

        log.info("Stock ajouté avec succès. Nouveau stock: {} {}", quantiteApres, ingredient.getUnite());
        return convertToGestionStockDTO(savedGestion);
    }


    @Transactional(rollbackFor = {BusinessException.class, InsufficientStockException.class})
    public GestionStockDTO retirerStock(Long idIngredient, Long idManager, BigDecimal quantite,
                                     TypeOperationStock typeOperation, String motif) {
        log.info("Retrait de stock pour l'ingrédient ID: {}", idIngredient);

        Ingredient ingredient = getIngredientById(idIngredient);
        Manager manager = managerRepository.findById(idManager)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));

        if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La quantité doit être positive");
        }

        BigDecimal quantiteAvant = ingredient.getQuantiteStock();

        if (quantiteAvant.compareTo(quantite) < 0) {
            throw new InsufficientStockException(
                    "Stock insuffisant pour %s. Disponible: %s %s, Demandé: %s %s"
                            .formatted(ingredient.getNom(), quantiteAvant,
                                    ingredient.getUnite(), quantite, ingredient.getUnite())
            );
        }

        BigDecimal quantiteApres = quantiteAvant.subtract(quantite);
        ingredient.setQuantiteStock(quantiteApres);
        ingredientRepository.save(ingredient);

        GestionStock gestionStock = GestionStock.builder()
                .manager(manager)
                .ingredient(ingredient)
                .typeOperation(typeOperation != null ? typeOperation : TypeOperationStock.RETRAIT)
                .quantite(quantite)
                .dateOperation(LocalDateTime.now())
                .motif(motif)
                .quantiteAvant(quantiteAvant)
                .quantiteApres(quantiteApres)
                .build();

        GestionStock savedGestion = gestionStockRepository.save(gestionStock);
        verifierEtCreerAlerte(ingredient);

        log.info("Stock retiré avec succès. Nouveau stock: {} {}", quantiteApres, ingredient.getUnite());
        return convertToGestionStockDTO(savedGestion);
    }

    /**
     * Ajuster le stock (Inventaire)
     */
    public GestionStockDTO ajusterStock(Long idIngredient, Long idManager, BigDecimal nouvelleQuantite, String motif) {
        log.info("Ajustement de stock pour l'ingrédient ID: {}", idIngredient);

        Ingredient ingredient = getIngredientById(idIngredient);
        Manager manager = managerRepository.findById(idManager)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));

        if (nouvelleQuantite.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("La quantité ne peut pas être négative");
        }

        BigDecimal quantiteAvant = ingredient.getQuantiteStock();
        BigDecimal difference = nouvelleQuantite.subtract(quantiteAvant);

        ingredient.setQuantiteStock(nouvelleQuantite);
        ingredientRepository.save(ingredient);

        GestionStock gestionStock = GestionStock.builder()
                .manager(manager)
                .ingredient(ingredient)
                .typeOperation(TypeOperationStock.AJUSTEMENT)
                .quantite(difference.abs())
                .dateOperation(LocalDateTime.now())
                .motif(motif + " | Inventaire")
                .quantiteAvant(quantiteAvant)
                .quantiteApres(nouvelleQuantite)
                .build();

        GestionStock savedGestion = gestionStockRepository.save(gestionStock);

        if (nouvelleQuantite.compareTo(quantiteAvant) < 0) {
            verifierEtCreerAlerte(ingredient);
        } else {
            traiterAlertesStock(ingredient);
        }

        log.info("Stock ajusté avec succès. Ancien: {} {}, Nouveau: {} {}",
                quantiteAvant, ingredient.getUnite(), nouvelleQuantite, ingredient.getUnite());

        return convertToGestionStockDTO(savedGestion);
    }



    public GestionStockDTO declarerPerte(Long idIngredient, Long idManager, BigDecimal quantite,
                                      String motif, String typeRaison) {
        log.info("Déclaration de perte pour l'ingrédient ID: {}", idIngredient);

        String motifComplet = "PERTE - %s%s".formatted(motif, typeRaison != null ? " (" + typeRaison + ")" : "");

        return retirerStock(idIngredient, idManager, quantite, TypeOperationStock.PERTE, motifComplet);
    }


    @Transactional(readOnly = true)
    public List<GestionStockDTO> getHistoriqueStock(Long idIngredient) {
        log.info("Récupération de l'historique de stock pour l'ingrédient ID: {}", idIngredient);
        return gestionStockRepository.findByIngredientIdIngredientOrderByDateOperationDesc(idIngredient).stream()
                .map(this::convertToGestionStockDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<GestionStockDTO> getHistoriqueByType(TypeOperationStock type) {
        log.info("Récupération de l'historique des opérations de type: {}", type);
        return gestionStockRepository.findByTypeOperation(type).stream()
                .map(this::convertToGestionStockDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<GestionStock> getHistoriqueByPeriode(LocalDateTime debut, LocalDateTime fin) {
        log.info("Récupération de l'historique entre {} et {}", debut, fin);
        return gestionStockRepository.findByDateOperationBetween(debut, fin);
    }


    @Transactional(readOnly = true)
    public List<GestionStockDTO> getHistoriqueByManager(Long idManager) {
        log.info("Récupération de l'historique du manager ID: {}", idManager);
        return gestionStockRepository.findByManagerIdManagerOrderByDateOperationDesc(idManager).stream()
                .map(this::convertToGestionStockDTO)
                .toList();
    }

    // ==================== GESTION DES ALERTES ====================


    public AlerteStock creerAlerte(Long idIngredient, TypeAlerte typeAlerte, String message) {
        log.info("Création d'alerte pour l'ingrédient ID: {} - Type: {}", idIngredient, typeAlerte);

        Ingredient ingredient = getIngredientById(idIngredient);

        // Vérifier s'il n'existe pas déjà une alerte non traitée
        List<AlerteStock> alertesExistantes = alerteStockRepository
                .findByIngredientIdIngredientAndStatutIn(
                        idIngredient,
                        List.of(StatutAlerte.NON_TRAITEE, StatutAlerte.EN_COURS)
                );

        if (!alertesExistantes.isEmpty()) {
            log.warn("Une alerte non traitée existe déjà pour cet ingrédient");
            return alertesExistantes.get(0);
        }

        AlerteStock alerte = AlerteStock.builder()
                .ingredient(ingredient)
                .typeAlerte(typeAlerte)
                .dateAlerte(LocalDateTime.now())
                .statut(StatutAlerte.NON_TRAITEE)
                .message(message != null ? message : genererMessageAlerte(ingredient, typeAlerte))
                .build();

        AlerteStock savedAlerte = alerteStockRepository.save(alerte);

        log.info("Alerte créée avec succès - ID: {}", savedAlerte.getIdAlerte());
        return savedAlerte;
    }


    public AlerteStock traiterAlerte(Long idAlerte, Long idManager, StatutAlerte nouveauStatut, String actionPrise) {
        log.info("Traitement de l'alerte ID: {} par le manager ID: {}", idAlerte, idManager);

        AlerteStock alerte = alerteStockRepository.findById(idAlerte)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte non trouvée"));

        Manager manager = managerRepository.findById(idManager)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));

        if (alerte.getStatut() == StatutAlerte.TRAITEE || alerte.getStatut() == StatutAlerte.IGNOREE) {
            throw new BusinessException("Cette alerte a déjà été traitée");
        }

        alerte.setStatut(nouveauStatut);
        alerte.setTraiteePar(manager);
        alerte.setDateTraitement(LocalDateTime.now());
        alerte.setActionPrise(actionPrise);

        AlerteStock updatedAlerte = alerteStockRepository.save(alerte);

        log.info("Alerte traitée avec succès");
        return updatedAlerte;
    }


    @Transactional(readOnly = true)
    public List<AlerteStock> getAlertesActives() {
        log.info("Récupération des alertes actives");
        return alerteStockRepository.findByStatutIn(
                List.of(StatutAlerte.NON_TRAITEE, StatutAlerte.EN_COURS)
        );
    }



    @Transactional(readOnly = true)
    public List<AlerteStock> getAlertesByType(TypeAlerte typeAlerte) {
        log.info("Récupération des alertes de type: {}", typeAlerte);
        return alerteStockRepository.findByTypeAlerte(typeAlerte);
    }



    @Transactional(readOnly = true)
    public List<AlerteStock> getAlertesByIngredient(Long idIngredient) {
        log.info("Récupération des alertes pour l'ingrédient ID: {}", idIngredient);
        return alerteStockRepository.findByIngredientIdIngredient(idIngredient);
    }

    // ==================== STATISTIQUES ====================


    @Transactional(readOnly = true)
    public Map<String, Object> getStockStatistics() {
        log.info("Calcul des statistiques de stock");

        Map<String, Object> stats = new HashMap<>();

        long totalIngredients = ingredientRepository.count();
        long ingredientsEnRupture = ingredientRepository.countByQuantiteStockLessThanEqual(BigDecimal.ZERO);
        long ingredientsSousSeuilAlerte = ingredientRepository.findByQuantiteStockLessThanEqualSeuilAlerte().size();

        LocalDate dateLimit = LocalDate.now().plusDays(7);
        long ingredientsProchesPeremption = ingredientRepository.findByDatePeremptionBefore(dateLimit)
                .stream()
                .filter(i -> i.getDatePeremption().isAfter(LocalDate.now()))
                .count();

        long ingredientsPerimes = ingredientRepository.findByDatePeremptionBefore(LocalDate.now()).size();

        long alertesActives = alerteStockRepository.countByStatutIn(
                List.of(StatutAlerte.NON_TRAITEE, StatutAlerte.EN_COURS)
        );

        stats.put("totalIngredients", totalIngredients);
        stats.put("ingredientsEnRupture", ingredientsEnRupture);
        stats.put("ingredientsSousSeuilAlerte", ingredientsSousSeuilAlerte);
        stats.put("ingredientsProchesPeremption", ingredientsProchesPeremption);
        stats.put("ingredientsPerimes", ingredientsPerimes);
        stats.put("alertesActives", alertesActives);
        stats.put("dateCalcul", LocalDateTime.now());

        return stats;
    }

    /**
     * Rapport de mouvements de stock
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRapportMouvements(LocalDateTime debut, LocalDateTime fin) {
        log.info("Génération du rapport de mouvements entre {} et {}", debut, fin);

        List<GestionStock> mouvements = gestionStockRepository.findByDateOperationBetween(debut, fin);

        Map<String, Object> rapport = new HashMap<>();
        rapport.put("dateDebut", debut);
        rapport.put("dateFin", fin);
        rapport.put("totalMouvements", mouvements.size());

        // Statistiques par type d'opération
        Map<TypeOperationStock, Long> parType = mouvements.stream()
                .collect(Collectors.groupingBy(GestionStock::getTypeOperation, Collectors.counting()));
        rapport.put("mouvementsParType", parType);

        // Ingrédients les plus mouvementés
        Map<String, Long> plusMouvementes = mouvements.stream()
                .collect(Collectors.groupingBy(
                        gs -> gs.getIngredient().getNom(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        rapport.put("ingredientsPlusMouvementes", plusMouvementes);

        return rapport;
    }

    // ==================== TÂCHE PLANIFIÉE ====================



    @Scheduled(cron = " 0 0 8 *  * * ")
    public void verificationQuotidienneStocks() {
        log.info("Début de la vérification quotidienne des stocks");

        int batchSize = 100;
        List<Ingredient> allIngredients = ingredientRepository.findAll();

        for (int i = 0; i < allIngredients.size(); i += batchSize) {
            List<Ingredient> batch = allIngredients.subList(i,
                    Math.min(i + batchSize, allIngredients.size()));
            batch.parallelStream().forEach(this::verifierEtCreerAlerte);
        }

        log.info("Vérification quotidienne des stocks terminée");
    }
    // ==================== MÉTHODES PRIVÉES ====================


    private void verifierEtCreerAlerte(Ingredient ingredient) {
        List<AlerteStock> alertesExistantes = alerteStockRepository
                .findByIngredientIdIngredientAndStatutIn(
                        ingredient.getIdIngredient(),
                        List.of(StatutAlerte.NON_TRAITEE, StatutAlerte.EN_COURS)
                );

        if (!alertesExistantes.isEmpty()) {
            return;
        }

        // Vérifier stock épuisé
        if (ingredient.getQuantiteStock().compareTo(BigDecimal.ZERO) <= 0) {
            creerAlerte(ingredient.getIdIngredient(), TypeAlerte.STOCK_EPUISE, null);
            return;
        }

        // Vérifier stock bas
        if (ingredient.getSeuilAlerte() != null
                && ingredient.getQuantiteStock().compareTo(ingredient.getSeuilAlerte()) <= 0) {
            System.out.println("ingredient  "+ingredient);
            creerAlerte(ingredient.getIdIngredient(), TypeAlerte.STOCK_BAS, null);
            return;
        }

        // Vérifier péremption
        if (ingredient.getDatePeremption() != null) {
            LocalDate maintenant = LocalDate.now();

            if (ingredient.getDatePeremption().isBefore(maintenant)) {
                creerAlerte(ingredient.getIdIngredient(), TypeAlerte.PERIME, null);
            } else if (ingredient.getDatePeremption().isBefore(maintenant.plusDays(7))) {
                creerAlerte(ingredient.getIdIngredient(), TypeAlerte.DATE_PEREMPTION, null);
            }
        }
    }


    private void traiterAlertesStock(Ingredient ingredient) {
        List<AlerteStock> alertesActives = alerteStockRepository
                .findByIngredientIdIngredientAndStatutIn(
                        ingredient.getIdIngredient(),
                        List.of(StatutAlerte.NON_TRAITEE, StatutAlerte.EN_COURS)
                );

        for (AlerteStock alerte : alertesActives) {
            if ((alerte.getTypeAlerte() == TypeAlerte.STOCK_BAS ||
                    alerte.getTypeAlerte() == TypeAlerte.STOCK_EPUISE)
                    && ingredient.getQuantiteStock().compareTo(ingredient.getSeuilAlerte()) > 0) {

                alerte.setStatut(StatutAlerte.TRAITEE);
                alerte.setDateTraitement(LocalDateTime.now());
                alerte.setActionPrise("Stock réapprovisionné automatiquement");
                alerteStockRepository.save(alerte);
            }
        }
    }


    private String genererMessageAlerte(Ingredient ingredient, TypeAlerte typeAlerte) {
        return switch (typeAlerte) {
            case STOCK_EPUISE -> String.format(
                    "URGENT: L'ingrédient '%s' est en rupture de stock (0 %s)",
                    ingredient.getNom(), ingredient.getUnite()
            );
            case STOCK_BAS -> String.format(
                    "ATTENTION: L'ingrédient '%s' est sous le seuil d'alerte. " +
                            "Stock actuel: %s %s, Seuil: %s %s",
                    ingredient.getNom(),
                    ingredient.getQuantiteStock(),
                    ingredient.getUnite(),
                    ingredient.getSeuilAlerte(),
                    ingredient.getUnite()
            );
            case DATE_PEREMPTION -> String.format(
                    "ATTENTION: L'ingrédient '%s' arrive à péremption le %s (%s %s disponibles)",
                    ingredient.getNom(),
                    ingredient.getDatePeremption(),
                    ingredient.getQuantiteStock(),
                    ingredient.getUnite()
            );
            case PERIME -> String.format(
                    "URGENT: L'ingrédient '%s' est périmé depuis le %s. À retirer immédiatement!",
                    ingredient.getNom(),
                    ingredient.getDatePeremption()
            );
        };
    }

    private IngredientDTO convertToIngredientDTO(Ingredient ingredient) {
        if (ingredient == null)
            return null;
        return IngredientDTO.builder()
                .id(ingredient.getIdIngredient())
                .nom(ingredient.getNom())
                .quantiteStock(ingredient.getQuantiteStock())
                .unite(ingredient.getUnite())
                .datePeremption(ingredient.getDatePeremption())
                .fournisseurEmail(ingredient.getFournisseur().getEmail())
                .build();

    }


    public GestionStockDTO convertToGestionStockDTO(GestionStock gestionStock) {
        if (gestionStock == null) {
            return null;
        }

        return GestionStockDTO.builder()
                .idGestionStock(gestionStock.getIdGestionStock())
                .managerId(gestionStock.getManager() != null ?
                        gestionStock.getManager().getIdManager() : null)
                .ingredientId(gestionStock.getIngredient() != null ?
                        gestionStock.getIngredient().getIdIngredient() : null)
                .typeOperation(gestionStock.getTypeOperation())
                .quantite(gestionStock.getQuantite())
                .dateOperation(gestionStock.getDateOperation())
                .motif(gestionStock.getMotif())
                .quantiteAvant(gestionStock.getQuantiteAvant())
                .quantiteApres(gestionStock.getQuantiteApres())
                .build();
    }
}