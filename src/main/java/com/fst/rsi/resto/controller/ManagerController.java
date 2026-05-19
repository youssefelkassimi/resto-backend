// ManagerController.java
package com.fst.rsi.resto.controller;

import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.entity.*;
import com.fst.rsi.resto.entity.enums.*;
import com.fst.rsi.resto.service.ManagerService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/managers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ManagerController {

    private final ManagerService managerService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ManagerResponseDTO> createManager(
            @Valid @RequestBody ManagerRequestDTO request) {

        ManagerResponseDTO manager = managerService.createManager(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(manager);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ManagerResponseDTO> getManagerById(@PathVariable Long id) {
        ManagerResponseDTO manager = managerService.getManagerById(id);
        return ResponseEntity.ok(manager);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ManagerResponseDTO> getManagerByEmail(@PathVariable String email) {
        ManagerResponseDTO manager = managerService.getManagerByEmail(email);
        return ResponseEntity.ok(manager);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Page<ManagerResponseDTO>> getAllManagers(Pageable pageable) {
        Page<ManagerResponseDTO> managers = managerService.getAllManagers(pageable);
        return ResponseEntity.ok(managers);
    }


    @GetMapping("/actifs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<ManagerResponseDTO>> getManagersActifs() {
        List<ManagerResponseDTO> managers = managerService.getManagersActifs();
        return ResponseEntity.ok(managers);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ManagerResponseDTO> updateManager(
            @PathVariable Long id,
            @Valid @RequestBody ManagerRequestDTO request) {

        ManagerResponseDTO manager = managerService.updateManager(id, request);
        return ResponseEntity.ok(manager);
    }


    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ManagerResponseDTO> toggleManagerStatus(@PathVariable Long id) {
        ManagerResponseDTO manager = managerService.toggleManagerStatus(id);
        return ResponseEntity.ok(manager);
    }


    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordDTO passwordDTO) {

        managerService.changePassword(id, passwordDTO);
        return ResponseEntity.ok().build();
    }

    // ==================== RAPPORTS DE VENTE ====================


    @PostMapping("/{idManager}/rapports/generer")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> genererRapportVentes(
            @PathVariable Long idManager,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        Map<String, Object> rapport = managerService.genererRapportVentes(idManager, dateDebut, dateFin);
        return ResponseEntity.ok(rapport);
    }


    @PostMapping("/{idManager}/rapports/sauvegarder")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<RapportVenteDTO> sauvegarderRapport(
            @PathVariable Long idManager,
            @RequestBody Map<String, Object> rapportData) {

        RapportVenteDTO rapport = managerService.sauvegarderRapport(idManager, rapportData);
        return ResponseEntity.status(HttpStatus.CREATED).body(rapport);
    }


    @GetMapping("/{idManager}/rapports")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<RapportVenteDTO>> getRapportsByManager(@PathVariable Long idManager) {
        List<RapportVenteDTO> rapports = managerService.getRapportsByManager(idManager);
        return ResponseEntity.ok(rapports);
    }


    @PostMapping("/{idManager}/rapports/generer-et-sauvegarder")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<RapportVenteDTO> genererEtSauvegarderRapport(
            @PathVariable Long idManager,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        Map<String, Object> rapport = managerService.genererRapportVentes(idManager, dateDebut, dateFin);
        RapportVenteDTO rapportSauvegarde = managerService.sauvegarderRapport(idManager, rapport);

        return ResponseEntity.status(HttpStatus.CREATED).body(rapportSauvegarde);
    }

    // ==================== DASHBOARD ====================


    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = managerService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }


    @GetMapping("/dashboard/resume")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getResumePeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        Map<String, Object> resume = managerService.getResumePeriode(dateDebut, dateFin);
        return ResponseEntity.ok(resume);
    }

    // ==================== GESTION DU PERSONNEL ====================

    @PostMapping("/{idManager}/evaluer-serveur/{idServeur}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PerformanceServeurDTO> evaluerServeur(
            @PathVariable Long idManager,
            @PathVariable Long idServeur,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestBody Map<String, Object> evaluation) {

        PerformanceServeurDTO performance = managerService.evaluerServeur(
                idManager, idServeur, dateDebut, dateFin, evaluation);

        return ResponseEntity.status(HttpStatus.CREATED).body(performance);
    }



    // ==================== GESTION DES ALERTES ====================


    @GetMapping("/alertes/non-traitees")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<AlerteStockDTO>> getAlertesNonTraitees() {
        List<AlerteStockDTO> alertes = managerService.getAlertesNonTraitees();
        return ResponseEntity.ok(alertes);
    }


    @PatchMapping("/{idManager}/alertes/{idAlerte}/traiter")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AlerteStockDTO> traiterAlerte(
            @PathVariable Long idManager,
            @PathVariable Long idAlerte,
            @RequestParam String actionPrise,
            @RequestParam StatutAlerte nouveauStatut) {

        AlerteStockDTO alerte = managerService.traiterAlerte(idManager, idAlerte, actionPrise, nouveauStatut);
        return ResponseEntity.ok(alerte);
    }

    // ==================== RAPPORTS SPÉCIFIQUES ====================


    @GetMapping("/{idManager}/rapports/journalier")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rapportJournalier(
            @PathVariable Long idManager,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate dateRapport = date != null ? date : LocalDate.now();
        Map<String, Object> rapport = managerService.genererRapportVentes(
                idManager, dateRapport, dateRapport);

        return ResponseEntity.ok(rapport);
    }


    @GetMapping("/{idManager}/rapports/hebdomadaire")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rapportHebdomadaire(@PathVariable Long idManager) {
        LocalDate dateFin = LocalDate.now();
        LocalDate dateDebut = dateFin.minusDays(7);

        Map<String, Object> rapport = managerService.genererRapportVentes(
                idManager, dateDebut, dateFin);

        return ResponseEntity.ok(rapport);
    }


    @GetMapping("/{idManager}/rapports/mensuel")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rapportMensuel(
            @PathVariable Long idManager,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee) {

        LocalDate maintenant = LocalDate.now();
        int moisRapport = mois != null ? mois : maintenant.getMonthValue();
        int anneeRapport = annee != null ? annee : maintenant.getYear();

        LocalDate dateDebut = LocalDate.of(anneeRapport, moisRapport, 1);
        LocalDate dateFin = dateDebut.withDayOfMonth(dateDebut.lengthOfMonth());

        Map<String, Object> rapport = managerService.genererRapportVentes(
                idManager, dateDebut, dateFin);

        return ResponseEntity.ok(rapport);
    }


    @GetMapping("/{idManager}/rapports/annuel")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rapportAnnuel(
            @PathVariable Long idManager,
            @RequestParam(required = false) Integer annee) {

        int anneeRapport = annee != null ? annee : LocalDate.now().getYear();

        LocalDate dateDebut = LocalDate.of(anneeRapport, 1, 1);
        LocalDate dateFin = LocalDate.of(anneeRapport, 12, 31);

        Map<String, Object> rapport = managerService.genererRapportVentes(
                idManager, dateDebut, dateFin);

        return ResponseEntity.ok(rapport);
    }

    // ==================== STATISTIQUES AVANCÉES ====================


    @GetMapping("/statistiques/performance-globale")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPerformanceGlobale(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        // Obtenir le résumé de base
        Map<String, Object> resume = managerService.getResumePeriode(dateDebut, dateFin);
        Map<String, Object> stats = new java.util.HashMap<>(resume);

        // Ajouter les statistiques du dashboard
        Map<String, Object> dashboardStats = managerService.getDashboardStats();
        stats.put("statsActuelles", dashboardStats);

        return ResponseEntity.ok(stats);
    }


    @GetMapping("/statistiques/comparaison")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> comparerPeriodes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut1,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin1,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut2,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin2) {

        Map<String, Object> comparaison = new java.util.HashMap<>();

        Map<String, Object> periode1 = managerService.getResumePeriode(debut1, fin1);
        Map<String, Object> periode2 = managerService.getResumePeriode(debut2, fin2);

        comparaison.put("periode1", periode1);
        comparaison.put("periode2", periode2);

        // Calculer les variations
        Map<String, Object> variations = new java.util.HashMap<>();
        variations.put("variationCommandes",
                ((Number) periode2.get("totalCommandes")).longValue() -
                        ((Number) periode1.get("totalCommandes")).longValue());

        comparaison.put("variations", variations);

        return ResponseEntity.ok(comparaison);
    }

    // ==================== EXPORTS ====================


    @GetMapping("/{idManager}/rapports/{idRapport}/export/pdf")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<String> exportRapportPDF(
            @PathVariable Long idManager,
            @PathVariable Long idRapport) {

        // TODO: 3la hesab wa9ete
        return ResponseEntity.ok("Export PDF non encore implémenté");
    }





    @GetMapping("/statistiques/export/csv")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportStatisticsCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        Map<String, Object> resume = managerService.getResumePeriode(dateDebut, dateFin);

        StringBuilder csv = new StringBuilder();
        csv.append("Période,Début,Fin\n");
        csv.append("Données,").append(dateDebut).append(",").append(dateFin).append("\n\n");
        csv.append("Métrique,Valeur\n");

        resume.forEach((key, value) -> {
            csv.append(key).append(",").append(value).append("\n");
        });

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=statistiques.csv")
                .header("Content-Type", "text/csv")
                .body(csv.toString().getBytes());
    }

    // ==================== ACTIONS RAPIDES ====================


    @GetMapping("/{idManager}/actions-rapides")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getActionsRapides(@PathVariable Long idManager) {
        Map<String, Object> actions = new java.util.HashMap<>();

        Map<String, Object> stats = managerService.getDashboardStats();
        actions.put("statistiques", stats);

        List<AlerteStockDTO> alertes = managerService.getAlertesNonTraitees();
        actions.put("alertesEnAttente", alertes.size());
        actions.put("alertes", alertes);

        List<RapportVenteDTO> rapportsRecents = managerService.getRapportsByManager(idManager);
        actions.put("derniersRapports", rapportsRecents.stream().limit(5).collect(java.util.stream.Collectors.toList()));

        return ResponseEntity.ok(actions);
    }


    @GetMapping("/statut-restaurant")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatutRestaurant() {
        Map<String, Object> statut = new java.util.HashMap<>();

        Map<String, Object> dashboard = managerService.getDashboardStats();
        List<AlerteStockDTO> alertes = managerService.getAlertesNonTraitees();

        statut.put("ouvert", true);
        statut.put("dashboard", dashboard);
        statut.put("alertesCritiques", alertes.stream()
                .filter(a -> a.getTypeAlerte() == TypeAlerte.STOCK_EPUISE ||
                        a.getTypeAlerte() == TypeAlerte.PERIME)
                .count());

        // Statut général basé sur les alertes
        long alertesCritiques = (long) statut.get("alertesCritiques");
        if (alertesCritiques > 5) {
            statut.put("statutGeneral", "CRITIQUE");
        } else if (alertesCritiques > 2) {
            statut.put("statutGeneral", "ATTENTION");
        } else {
            statut.put("statutGeneral", "NORMAL");
        }

        return ResponseEntity.ok(statut);
    }

    @GetMapping("/{idManager}/rapports/{idRapport}/export/excel")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<String> exportRapportExcel(
            @PathVariable Long idManager,
            @PathVariable Long idRapport) {

        // TODO: 3la hesab wa9ete
        return ResponseEntity.ok("Export Excel non encore implémenté");
    }

    //
//    @PostMapping("/{idManager}/evaluer-livreur/{idLivreur}")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
//    public ResponseEntity<PerformanceLivreur> evaluerLivreur(
//            @PathVariable Long idManager,
//            @PathVariable Long idLivreur,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
//            @RequestBody Map<String, Object> evaluation) {
//
//        PerformanceLivreur performance = managerService.evaluerLivreur(
//                idManager, idLivreur, dateDebut, dateFin, evaluation);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(performance);
//    }
}