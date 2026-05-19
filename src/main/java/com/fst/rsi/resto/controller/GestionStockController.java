package com.fst.rsi.resto.controller;

import com.fst.rsi.resto.dto.GestionStockDTO;
import com.fst.rsi.resto.dto.IngredientDTO;
import com.fst.rsi.resto.entity.*;
import com.fst.rsi.resto.entity.enums.*;
import com.fst.rsi.resto.service.GestionStockService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GestionStockController {

    private final GestionStockService gestionStockService;


    @PostMapping("/ingredients")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<IngredientDTO> createIngredient(
            @RequestParam String nom,
            @RequestParam BigDecimal quantiteStock,
            @RequestParam String unite,
            @RequestParam(required = false) BigDecimal seuilAlerte,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datePeremption,
            @RequestParam(required = false) Long idFournisseur) {

        IngredientDTO ingredient = gestionStockService.createIngredient(
                nom, quantiteStock, unite, seuilAlerte, datePeremption, idFournisseur);

        return ResponseEntity.status(HttpStatus.CREATED).body(ingredient);
    }


    @GetMapping("/ingredients/{id}")
    public ResponseEntity<Ingredient> getIngredientById(@PathVariable Long id) {
        Ingredient ingredient = gestionStockService.getIngredientById(id);
        return ResponseEntity.ok(ingredient);
    }


    @GetMapping("/ingredients")
    public ResponseEntity<Page<IngredientDTO>> getAllIngredients(Pageable pageable) {
        Page<IngredientDTO> ingredients = gestionStockService.getAllIngredients(pageable);
        return ResponseEntity.ok(ingredients);
    }


    @GetMapping("/ingredients/search")
    public ResponseEntity<List<IngredientDTO>> searchIngredients(@RequestParam String searchTerm) {
        List<IngredientDTO> ingredients = gestionStockService.searchIngredients(searchTerm);
        return ResponseEntity.ok(ingredients);
    }

    @GetMapping("/ingredients/rupture")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<IngredientDTO>> getIngredientsEnRupture() {
        List<IngredientDTO> ingredients = gestionStockService.getIngredientsEnRupture();
        return ResponseEntity.ok(ingredients);
    }


    @GetMapping("/ingredients/alerte")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<IngredientDTO>> getIngredientsSousSeuilAlerte() {
        List<IngredientDTO> ingredients = gestionStockService.getIngredientsSousSeuilAlerte();
        return ResponseEntity.ok(ingredients);
    }


    @GetMapping("/ingredients/proche-peremption")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<IngredientDTO>> getIngredientsProchesPeremption(
            @RequestParam(defaultValue = "7") int joursAvant) {

        List<IngredientDTO> ingredients = gestionStockService.getIngredientsProchesPeremption(joursAvant);
        return ResponseEntity.ok(ingredients);
    }


    @GetMapping("/ingredients/perimes")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<IngredientDTO>> getIngredientsPerimes() {
        List<IngredientDTO> ingredients = gestionStockService.getIngredientsPerimes();
        return ResponseEntity.ok(ingredients);
    }


    @PutMapping("/ingredients/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<IngredientDTO> updateIngredient(
            @PathVariable Long id,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String unite,
            @RequestParam(required = false) BigDecimal seuilAlerte,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datePeremption,
            @RequestParam(required = false) Long idFournisseur) {

        IngredientDTO ingredient = gestionStockService.updateIngredient(
                id, nom, unite, seuilAlerte, datePeremption, idFournisseur);

        return ResponseEntity.ok(ingredient);
    }


    @DeleteMapping("/ingredients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        gestionStockService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== GESTION DES STOCKS ====================


    @PostMapping("/ingredients/{idIngredient}/ajouter")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GestionStockDTO> ajouterStock(
            @PathVariable Long idIngredient,
            @RequestParam Long idManager,
            @RequestParam BigDecimal quantite,
            @RequestParam(required = false) TypeOperationStock typeOperation,
            @RequestParam(required = false) String motif,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datePeremption) {

        GestionStockDTO gestion = gestionStockService.ajouterStock(
                idIngredient, idManager, quantite, typeOperation, motif, datePeremption);

        return ResponseEntity.status(HttpStatus.CREATED).body(gestion);
    }


    @PostMapping("/ingredients/{idIngredient}/retirer")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GestionStockDTO> retirerStock(
            @PathVariable Long idIngredient,
            @RequestParam Long idManager,
            @RequestParam BigDecimal quantite,
            @RequestParam(required = false) TypeOperationStock typeOperation,
            @RequestParam String motif) {

        GestionStockDTO gestion = gestionStockService.retirerStock(
                idIngredient, idManager, quantite, typeOperation, motif);

        return ResponseEntity.status(HttpStatus.CREATED).body(gestion);
    }


    @PostMapping("/ingredients/{idIngredient}/ajuster")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GestionStockDTO> ajusterStock(
            @PathVariable Long idIngredient,
            @RequestParam Long idManager,
            @RequestParam BigDecimal nouvelleQuantite,
            @RequestParam String motif) {

        GestionStockDTO gestion = gestionStockService.ajusterStock(
                idIngredient, idManager, nouvelleQuantite, motif);

        return ResponseEntity.status(HttpStatus.CREATED).body(gestion);
    }


    @PostMapping("/ingredients/{idIngredient}/perte")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GestionStockDTO> declarerPerte(
            @PathVariable Long idIngredient,
            @RequestParam Long idManager,
            @RequestParam BigDecimal quantite,
            @RequestParam String motif,
            @RequestParam(required = false) String typeRaison) {

        GestionStockDTO gestion = gestionStockService.declarerPerte(
                idIngredient, idManager, quantite, motif, typeRaison);

        return ResponseEntity.status(HttpStatus.CREATED).body(gestion);
    }


    @GetMapping("/ingredients/{idIngredient}/historique")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<GestionStockDTO>> getHistoriqueStock(@PathVariable Long idIngredient) {
        List<GestionStockDTO> historique = gestionStockService.getHistoriqueStock(idIngredient);
        return ResponseEntity.ok(historique);
    }

    @GetMapping("/historique/type/{type}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<GestionStockDTO>> getHistoriqueByType(@PathVariable TypeOperationStock type) {
        List<GestionStockDTO> historique = gestionStockService.getHistoriqueByType(type);
        return ResponseEntity.ok(historique);
    }


    @GetMapping("/historique/periode")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<GestionStockDTO>> getHistoriqueByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        List<GestionStockDTO> historique = gestionStockService.getHistoriqueByPeriode(debut, fin).stream()
                .map(gestionStockService::convertToGestionStockDTO)
                .toList();
        return ResponseEntity.ok(historique);
    }


    @GetMapping("/historique/manager/{idManager}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<GestionStockDTO>> getHistoriqueByManager(@PathVariable Long idManager) {
        List<GestionStockDTO> historique = gestionStockService.getHistoriqueByManager(idManager);
        return ResponseEntity.ok(historique);
    }

    // ==================== GESTION DES ALERTES ====================


    @PostMapping("/alertes")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AlerteStock> creerAlerte(
            @RequestParam Long idIngredient,
            @RequestParam TypeAlerte typeAlerte,
            @RequestParam(required = false) String message) {

        AlerteStock alerte = gestionStockService.creerAlerte(idIngredient, typeAlerte, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(alerte);
    }


    @PatchMapping("/alertes/{idAlerte}/traiter")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AlerteStock> traiterAlerte(
            @PathVariable Long idAlerte,
            @RequestParam Long idManager,
            @RequestParam StatutAlerte nouveauStatut,
            @RequestParam String actionPrise) {

        AlerteStock alerte = gestionStockService.traiterAlerte(
                idAlerte, idManager, nouveauStatut, actionPrise);

        return ResponseEntity.ok(alerte);
    }


    @GetMapping("/alertes/actives")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<AlerteStock>> getAlertesActives() {
        List<AlerteStock> alertes = gestionStockService.getAlertesActives();
        return ResponseEntity.ok(alertes);
    }


    @GetMapping("/alertes/type/{typeAlerte}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<AlerteStock>> getAlertesByType(@PathVariable TypeAlerte typeAlerte) {
        List<AlerteStock> alertes = gestionStockService.getAlertesByType(typeAlerte);
        return ResponseEntity.ok(alertes);
    }


    @GetMapping("/alertes/ingredient/{idIngredient}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<AlerteStock>> getAlertesByIngredient(@PathVariable Long idIngredient) {
        List<AlerteStock> alertes = gestionStockService.getAlertesByIngredient(idIngredient);
        return ResponseEntity.ok(alertes);
    }

    // ==================== STATISTIQUES ====================


    @GetMapping("/statistiques")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStockStatistics() {
        Map<String, Object> stats = gestionStockService.getStockStatistics();
        return ResponseEntity.ok(stats);
    }



    @GetMapping("/rapports/mouvements")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRapportMouvements(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        Map<String, Object> rapport = gestionStockService.getRapportMouvements(debut, fin);
        return ResponseEntity.ok(rapport);
    }

    // ==================== RAPPORTS SPÉCIFIQUES ====================



    @GetMapping("/rapports/journalier")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rapportJournalier(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate dateRapport = date != null ? date : LocalDate.now();
        LocalDateTime debut = dateRapport.atStartOfDay();
        LocalDateTime fin = dateRapport.atTime(23, 59, 59);

        Map<String, Object> rapport = gestionStockService.getRapportMouvements(debut, fin);
        return ResponseEntity.ok(rapport);
    }



    @GetMapping("/rapports/hebdomadaire")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rapportHebdomadaire() {
        LocalDateTime fin = LocalDateTime.now();
        LocalDateTime debut = fin.minusDays(7);

        Map<String, Object> rapport = gestionStockService.getRapportMouvements(debut, fin);
        return ResponseEntity.ok(rapport);
    }



    @GetMapping("/rapports/mensuel")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rapportMensuel(
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee) {

        LocalDate maintenant = LocalDate.now();
        int moisRapport = mois != null ? mois : maintenant.getMonthValue();
        int anneeRapport = annee != null ? annee : maintenant.getYear();

        LocalDate dateDebut = LocalDate.of(anneeRapport, moisRapport, 1);
        LocalDate dateFin = dateDebut.withDayOfMonth(dateDebut.lengthOfMonth());

        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);

        Map<String, Object> rapport = gestionStockService.getRapportMouvements(debut, fin);
        return ResponseEntity.ok(rapport);
    }

    // ==================== ACTIONS RAPIDES ====================


    @GetMapping("/vue-ensemble")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getVueEnsemble() {
        Map<String, Object> vue = new java.util.HashMap<>();

        // Statistiques
        Map<String, Object> stats = gestionStockService.getStockStatistics();
        vue.put("statistiques", stats);

        // Alertes actives
        List<AlerteStock> alertes = gestionStockService.getAlertesActives();
        vue.put("alertes", alertes);

        // Ingrédients critiques
        List<IngredientDTO> rupture = gestionStockService.getIngredientsEnRupture();
        List<IngredientDTO> sousAlerte = gestionStockService.getIngredientsSousSeuilAlerte();
        List<IngredientDTO> perimes = gestionStockService.getIngredientsPerimes();

        vue.put("ingredientsEnRupture", rupture);
        vue.put("ingredientsSousAlerte", sousAlerte);
        vue.put("ingredientsPerimes", perimes);

        return ResponseEntity.ok(vue);
    }



    @GetMapping("/actions-urgentes")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getActionsUrgentes() {
        Map<String, Object> actions = new java.util.HashMap<>();

        // Ingrédients à commander en urgence
        List<IngredientDTO> aCommander = new java.util.ArrayList<>();
        aCommander.addAll(gestionStockService.getIngredientsEnRupture());
        aCommander.addAll(gestionStockService.getIngredientsSousSeuilAlerte());

        // Ingrédients à retirer (périmés)
        List<IngredientDTO> aRetirer = gestionStockService.getIngredientsPerimes();

        // Alertes non traitées
        List<AlerteStock> alertesNonTraitees = gestionStockService.getAlertesActives();

        actions.put("ingredientsACommander", aCommander.stream().distinct().collect(java.util.stream.Collectors.toList()));
        actions.put("ingredientsARetirer", aRetirer);
        actions.put("alertesNonTraitees", alertesNonTraitees);
        actions.put("totalActionsUrgentes",
                aCommander.size() + aRetirer.size() + alertesNonTraitees.size());

        return ResponseEntity.ok(actions);
    }


    @GetMapping("/inventaire")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getInventaire() {
        List<IngredientDTO> ingredients = gestionStockService.getAllIngredients(Pageable.unpaged()).getContent();

        List<Map<String, Object>> inventaire = ingredients.stream()
                .map(ingredient -> {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("id", ingredient.getId());
                    item.put("nom", ingredient.getNom());
                    item.put("quantite", ingredient.getQuantiteStock());
                    item.put("unite", ingredient.getUnite());
                    item.put("seuilAlerte", ingredient.getSeuilAlerte());
                    item.put("datePeremption", ingredient.getDatePeremption());

                    // Statut
                    if (ingredient.getQuantiteStock().compareTo(BigDecimal.ZERO) <= 0) {
                        item.put("statut", "RUPTURE");
                    } else if (ingredient.getSeuilAlerte() != null
                            && ingredient.getQuantiteStock().compareTo(ingredient.getSeuilAlerte()) <= 0) {
                        item.put("statut", "ALERTE");
                    } else {
                        item.put("statut", "OK");
                    }

                    return item;
                })
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(inventaire);
    }

    // ==================== EXPORT ====================


    @GetMapping("/export/inventaire/csv")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportInventaireCSV() {
        List<IngredientDTO> ingredients = gestionStockService.getAllIngredients(Pageable.unpaged()).getContent();

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Nom,Quantité,Unité,Seuil Alerte,Date Péremption,Fournisseur,Statut\n");

        for (IngredientDTO ingredient : ingredients) {
            csv.append(ingredient.getId()).append(",");
            csv.append(ingredient.getNom()).append(",");
            csv.append(ingredient.getQuantiteStock()).append(",");
            csv.append(ingredient.getUnite()).append(",");
            csv.append(ingredient.getSeuilAlerte() != null ? ingredient.getSeuilAlerte() : "N/A").append(",");
            csv.append(ingredient.getDatePeremption() != null ? ingredient.getDatePeremption() : "N/A").append(",");
            csv.append(ingredient.getFournisseurEmail() != null ? ingredient.getFournisseurEmail() : "N/A").append(",");

            // Statut
            if (ingredient.getQuantiteStock().compareTo(BigDecimal.ZERO) <= 0) {
                csv.append("RUPTURE");
            } else if (ingredient.getSeuilAlerte() != null
                    && ingredient.getQuantiteStock().compareTo(ingredient.getSeuilAlerte()) <= 0) {
                csv.append("ALERTE");
            } else {
                csv.append("OK");
            }
            csv.append("\n");
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=inventaire.csv")
                .header("Content-Type", "text/csv")
                .body(csv.toString().getBytes());
    }


    @GetMapping("/export/mouvements/csv")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportMouvementsCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        List<GestionStock> mouvements = gestionStockService.getHistoriqueByPeriode(debut, fin);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Ingrédient,Type Opération,Quantité,Qté Avant,Qté Après,Manager,Motif\n");

        for (GestionStock mvt : mouvements) {
            csv.append(mvt.getDateOperation()).append(",");
            csv.append(mvt.getIngredient().getNom()).append(",");
            csv.append(mvt.getTypeOperation()).append(",");
            csv.append(mvt.getQuantite()).append(" ").append(mvt.getIngredient().getUnite()).append(",");
            csv.append(mvt.getQuantiteAvant()).append(",");
            csv.append(mvt.getQuantiteApres()).append(",");
            csv.append(mvt.getManager().getUser().getNom()).append(" ")
                    .append(mvt.getManager().getUser().getPrenom()).append(",");
            csv.append(mvt.getMotif() != null ? mvt.getMotif() : "N/A").append("\n");
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=mouvements_stock.csv")
                .header("Content-Type", "text/csv")
                .body(csv.toString().getBytes());
    }
}