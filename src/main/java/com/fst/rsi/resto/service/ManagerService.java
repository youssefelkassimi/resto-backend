package com.fst.rsi.resto.service;

import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.entity.*;
import com.fst.rsi.resto.entity.enums.*;
import com.fst.rsi.resto.exception.*;
import com.fst.rsi.resto.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ManagerService {

    private final ManagerRepo managerRepository;
    private final UserRepo userRepository;
    private final CommandeRepo commandeRepository;
    private final PlatRepo platRepository;
    private final ClientRepo clientRepository;
    private final ServeurRepo serveurRepository;
    private final LivreurRepo livreurRepository;
    private final IngredientRepo ingredientRepository;
    private final RapportVenteRepo rapportVenteRepository;
    private final GestionStockRepo gestionStockRepository;
    private final PerformanceServeurRepo performanceServeurRepository;
//    private final PerformanceLivreurRepo performanceLivreurRepository;
    private final AlerteStockRepo alerteStockRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Créer un nouveau manager
     */
    public ManagerResponseDTO createManager(ManagerRequestDTO request) {
        log.info("Création d'un nouveau manager: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un compte avec cet email existe déjà");
        }

        if (userRepository.existsByTelephone(request.getTelephone())) {
            throw new BusinessException("Un compte avec ce numéro de téléphone existe déjà");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .password(passwordEncoder.encode(request.getPassword()))
                .adresse(request.getAdresse())
                .photo(request.getPhoto())
                .enabled(true)
                .emailVerified(false)
                .build();

        // Ajouter le rôle MANAGER
        user.addRole(UserRole.valueOf(request.getSpecialites()));

        User savedUser = userRepository.save(user);

        // Créer le profil manager
        Manager manager = Manager.builder()
                .user(savedUser)
                .dateEmbauche(request.getDateEmbauche() != null ? request.getDateEmbauche() : LocalDate.now())
                .actif(true)
                .salaire(request.getSalaire())
//                .niveauAcces(request.getNiveauAcces() != null ? request.getNiveauAcces() : NiveauAcces.MANAGER_STANDARD)
                .specialites(request.getSpecialites())
                .build();

        Manager savedManager = managerRepository.save(manager);

        log.info("Manager créé avec succès - ID: {}", savedManager.getIdManager());
        return convertToResponseDTO(savedManager);
    }


    @Transactional(readOnly = true)
    public ManagerResponseDTO getManagerById(Long id) {
        log.info("Récupération du manager ID: {}", id);
        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé avec l'ID: " + id));
        return convertToResponseDTO(manager);
    }

    @Transactional(readOnly = true)
    public ManagerResponseDTO getManagerByEmail(String email) {
        log.info("Récupération du manager email: {}", email);
        Manager manager = managerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé avec l'email: " + email));
        return convertToResponseDTO(manager);
    }


    @Transactional(readOnly = true)
    public Page<ManagerResponseDTO> getAllManagers(Pageable pageable) {
        log.info("Récupération de tous les managers");
        return managerRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }


    @Transactional(readOnly = true)
    public List<ManagerResponseDTO> getManagersActifs() {
        log.info("Récupération des managers actifs");
        return managerRepository.findByActifTrue()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public ManagerResponseDTO updateManager(Long id, ManagerRequestDTO request) {
        log.info("Mise à jour du manager ID: {}", id);

        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));

        User user = manager.getUser();

        if (request.getNom() != null) user.setNom(request.getNom());
        if (request.getPrenom() != null) user.setPrenom(request.getPrenom());
        if (request.getTelephone() != null) {
            if (!user.getTelephone().equals(request.getTelephone())
                    && userRepository.existsByTelephone(request.getTelephone())) {
                throw new BusinessException("Ce numéro de téléphone est déjà utilisé");
            }
            user.setTelephone(request.getTelephone());
        }
        if (request.getAdresse() != null) user.setAdresse(request.getAdresse());
        if (request.getPhoto() != null) user.setPhoto(request.getPhoto());
        if (request.getSalaire() != null) manager.setSalaire(request.getSalaire());
//        if (request.getNiveauAcces() != null) manager.setNiveauAcces(request.getNiveauAcces());
        if (request.getSpecialites() != null) manager.setSpecialites(request.getSpecialites());

        userRepository.save(user);
        Manager updatedManager = managerRepository.save(manager);

        log.info("Manager mis à jour avec succès");
        return convertToResponseDTO(updatedManager);
    }


    public ManagerResponseDTO toggleManagerStatus(Long id) {
        log.info("Changement du statut du manager ID: {}", id);

        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));

        manager.setActif(!manager.getActif());
        manager.getUser().setEnabled(manager.getActif());

        userRepository.save(manager.getUser());
        Manager updatedManager = managerRepository.save(manager);

        log.info("Statut du manager changé: {}", manager.getActif() ? "Actif" : "Inactif");
        return convertToResponseDTO(updatedManager);
    }

    // ==================== RAPPORTS DE VENTE ====================


    public Map<String, Object> genererRapportVentes(Long idManager, LocalDate dateDebut, LocalDate dateFin) {
        log.info("Génération de rapport de ventes du {} au {} par le manager ID: {}", dateDebut, dateFin, idManager);

        Manager manager = managerRepository.findById(idManager)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));

        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);

        List<Commande> commandes = commandeRepository.findByDateCommandeBetween(debut, fin);

        // Calculs des statistiques
        long totalCommandes = commandes.size();
        long commandesTerminees = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.SERVIE || c.getStatut() == StatutCommande.LIVREE)
                .count();
        long commandesAnnulees = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.ANNULEE)
                .count();

        BigDecimal chiffreAffaireTotal = commandes.stream()
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE)
                .map(Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal panierMoyen = commandesTerminees > 0
                ? chiffreAffaireTotal.divide(BigDecimal.valueOf(commandesTerminees), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Nombre de clients uniques
        long clientsUniques = commandes.stream()
                .map(c -> c.getClient().getIdClient())
                .distinct()
                .count();

        // CA par type de commande
        BigDecimal caSurPlace = commandes.stream()
                .filter(c -> c.getTypeCommande() == TypeCommande.SUR_PLACE && c.getStatut() != StatutCommande.ANNULEE)
                .map(Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal caAEmporter = commandes.stream()
                .filter(c -> c.getTypeCommande() == TypeCommande.A_EMPORTER && c.getStatut() != StatutCommande.ANNULEE)
                .map(Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal caLivraison = commandes.stream()
                .filter(c -> c.getTypeCommande() == TypeCommande.LIVRAISON && c.getStatut() != StatutCommande.ANNULEE)
                .map(Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Plats les plus vendus
        Map<String, Long> platsPlusVendus = commandes.stream()
                .flatMap(c -> c.getLignesCommande().stream())
                .collect(Collectors.groupingBy(
                        lc -> lc.getPlat().getNom(),
                        Collectors.summingLong(LigneCommande::getQuantite)
                ))
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));

        // Créer le rapport
        Map<String, Object> rapport = new HashMap<>();
        rapport.put("dateDebut", dateDebut);
        rapport.put("dateFin", dateFin);
        rapport.put("totalCommandes", totalCommandes);
        rapport.put("commandesTerminees", commandesTerminees);
        rapport.put("commandesAnnulees", commandesAnnulees);
        rapport.put("chiffreAffaireTotal", chiffreAffaireTotal);
        rapport.put("panierMoyen", panierMoyen);
        rapport.put("clientsUniques", clientsUniques);
        rapport.put("caSurPlace", caSurPlace);
        rapport.put("caAEmporter", caAEmporter);
        rapport.put("caLivraison", caLivraison);
        rapport.put("platsPlusVendus", platsPlusVendus);
        rapport.put("genereParManager", manager.getUser().getPrenom() + " " + manager.getUser().getNom());
        rapport.put("dateGeneration", LocalDateTime.now());

        log.info("Rapport de ventes généré avec succès");
        return rapport;
    }


    public RapportVenteDTO sauvegarderRapport(Long idManager, Map<String, Object> rapportData) {
        log.info("Sauvegarde du rapport de ventes par le manager ID: {}", idManager);

        Manager manager = managerRepository.findById(idManager)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));

        RapportVente rapport = RapportVente.builder()
                .manager(manager)
                .dateDebut((LocalDate) rapportData.get("dateDebut"))
                .dateFin((LocalDate) rapportData.get("dateFin"))
                .dateGeneration(LocalDateTime.now())
                .typeRapport(TypeRapport.PERSONNALISE)
                .chiffreAffaireTotal((BigDecimal) rapportData.get("chiffreAffaireTotal"))
                .nombreCommandesTotal(((Long) rapportData.get("totalCommandes")).intValue())
                .nombreClientsUniques(((Long) rapportData.get("clientsUniques")).intValue())
                .panierMoyen((BigDecimal) rapportData.get("panierMoyen"))
                .chiffreAffaireSurPlace((BigDecimal) rapportData.get("caSurPlace"))
                .chiffreAffaireAEmporter((BigDecimal) rapportData.get("caAEmporter"))
                .chiffreAffaireLivraison((BigDecimal) rapportData.get("caLivraison"))
                .build();

        RapportVente savedconvertToRapportVente= rapportVenteRepository.save(rapport);

         return convertToRapportVenteDTO(savedconvertToRapportVente);
    }


    @Transactional(readOnly = true)
    public List<RapportVenteDTO> getRapportsByManager(Long idManager) {
        log.info("Récupération des rapports du manager ID: {}", idManager);
        return rapportVenteRepository.findByManagerIdManagerOrderByDateGenerationDesc(idManager).stream()
                .map(this:: convertToRapportVenteDTO)
                .toList();
    }
// ==================== DASHBOARD ====================


    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        log.info("Récupération des statistiques du dashboard");

        Map<String, Object> stats = new HashMap<>();

        // Statistiques générales
        stats.put("totalClients", clientRepository.count());
        stats.put("clientsActifs", clientRepository.countByActifTrue());
        stats.put("totalPlats", platRepository.count());
        stats.put("platsDisponibles", platRepository.findByDisponibleTrue().size());
        stats.put("totalServeurs", serveurRepository.countActiveServeurs());
        stats.put("totalLivreurs", livreurRepository.countDisponibles());

        // Commandes du jour
        LocalDateTime debutJour = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finJour = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        List<Commande> commandesDuJour = commandeRepository.findByDateCommandeBetween(debutJour, finJour);

        stats.put("commandesDuJour", commandesDuJour.size());
        stats.put("commandesEnAttente", commandesDuJour.stream()
                .filter(c -> c.getStatut() == StatutCommande.EN_ATTENTE).count());
        stats.put("commandesEnPreparation", commandesDuJour.stream()
                .filter(c -> c.getStatut() == StatutCommande.EN_PREPARATION).count());

        // CA du jour
        BigDecimal caDuJour = commandesDuJour.stream()
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE)
                .map(Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("chiffreAffaireDuJour", caDuJour);

        // Alertes stock
        List<AlerteStock> alertesActives = alerteStockRepository
                .findByStatutIn(List.of(StatutAlerte.NON_TRAITEE, StatutAlerte.EN_COURS));
        stats.put("alertesStock", alertesActives.size());

        // Ingrédients en rupture
        long ingredientsEnRupture = ingredientRepository.countByQuantiteStockLessThanEqual(BigDecimal.ZERO);
        stats.put("ingredientsEnRupture", ingredientsEnRupture);

        log.info("Statistiques du dashboard récupérées avec succès");
        return stats;
    }

    // ==================== GESTION DU PERSONNEL ====================

    public PerformanceServeurDTO evaluerServeur(Long idManager, Long idServeur,
                                             LocalDate dateDebut, LocalDate dateFin,
                                             Map<String, Object> evaluation) {
        log.info("Évaluation du serveur ID: {} par le manager ID: {}", idServeur, idManager);

        Manager manager = managerRepository.findById(idManager)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));

        Serveur serveur = serveurRepository.findById(idServeur)
                .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé"));

        // Calculer les statistiques du serveur
        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);

        List<Commande> commandes = commandeRepository.findByDateCommandeBetween(debut, fin).stream()
                .filter(c -> c.getServeur() != null && c.getServeur().getIdServeur().equals(idServeur))
                .toList();

        int nombreCommandesServies = (int) commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.SERVIE || c.getStatut() == StatutCommande.LIVREE)
                .count();

        BigDecimal caGenere = commandes.stream()
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE)
                .map(Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal panierMoyen = nombreCommandesServies > 0
                ? caGenere.divide(BigDecimal.valueOf(nombreCommandesServies), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        PerformanceServeur performance = PerformanceServeur.builder()
                .serveur(serveur)
                .evaluePar(manager)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .nombreCommandesServies(nombreCommandesServies)
                .chiffreAffaireGenere(caGenere)
                .panierMoyen(panierMoyen)
                .noteQualiteService((BigDecimal) evaluation.get("noteQualite"))
                .noteRapidite((BigDecimal) evaluation.get("noteRapidite"))
                .noteGlobale((BigDecimal) evaluation.get("noteGlobale"))
                .commentaires((String) evaluation.get("commentaires"))
                .pointsForts((String) evaluation.get("pointsForts"))
                .axesAmelioration((String) evaluation.get("axesAmelioration"))
                .build();

        PerformanceServeur ps= performanceServeurRepository.save(performance);
         return PerformanceServeurDTO.builder()
                 .idPerformance(ps.getIdPerformance())
                 .dateDebut(ps.getDateDebut())
                 .dateFin(ps.getDateFin())
                 .nombreCommandesServies(ps.getNombreCommandesServies())
                 .chiffreAffaireGenere(ps.getChiffreAffaireGenere())
                 .panierMoyen(ps.getPanierMoyen())
                 .noteQualiteService(ps.getNoteQualiteService())
                 .noteRapidite(ps.getNoteRapidite())
                 .noteGlobale(ps.getNoteGlobale())
                 .commentaires(ps.getCommentaires())
                 .pointsForts(ps.getPointsForts())
                 .axesAmelioration(ps.getAxesAmelioration())
                 .build();
    }


//    public PerformanceLivreur evaluerLivreur(Long idManager, Long idLivreur,
//                                             LocalDate dateDebut, LocalDate dateFin,
//                                             Map<String, Object> evaluation) {
//        log.info("Évaluation du livreur ID: {} par le manager ID: {}", idLivreur, idManager);
//
//        Manager manager = managerRepository.findById(idManager)
//                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));
//
//        Livreur livreur = livreurRepository.findById(idLivreur)
//                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé"));
//
//        PerformanceLivreur performance = PerformanceLivreur.builder()
//                .livreur(livreur)
//                .evaluePar(manager)
//                .dateDebut(dateDebut)
//                .dateFin(dateFin)
//                .nombreLivraisonsEffectuees((Integer) evaluation.get("nombreLivraisons"))
//                .nombreLivraisonsAnnulees((Integer) evaluation.get("nombreAnnulees"))
//                .tempsLivraisonMoyen((BigDecimal) evaluation.get("tempsMoyen"))
//                .notePonctualite((BigDecimal) evaluation.get("notePonctualite"))
//                .noteQualite((BigDecimal) evaluation.get("noteQualite"))
//                .noteGlobale((BigDecimal) evaluation.get("noteGlobale"))
//                .commentaires((String) evaluation.get("commentaires"))
//                .build();
//
//        return performanceLivreurRepository.save(performance);
//    }

    // ==================== GESTION DES ALERTES ====================

    public AlerteStockDTO traiterAlerte(Long idManager, Long idAlerte, String actionPrise, StatutAlerte nouveauStatut) {
        log.info("Traitement de l'alerte ID: {} par le manager ID: {}", idAlerte, idManager);

        Manager manager = managerRepository.findById(idManager)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));

        AlerteStock alerte = alerteStockRepository.findById(idAlerte)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte non trouvée"));

        alerte.setStatut(nouveauStatut);
        alerte.setTraiteePar(manager);
        alerte.setDateTraitement(LocalDateTime.now());
        alerte.setActionPrise(actionPrise);

        AlerteStock alerteStockSaved= alerteStockRepository.save(alerte);

        return convertToAlerteStockeDTO(alerteStockSaved);
    }


    @Transactional(readOnly = true)
    public List<AlerteStockDTO> getAlertesNonTraitees() {
        log.info("Récupération des alertes non traitées");
        return alerteStockRepository.findByStatutIn(
                List.of(StatutAlerte.NON_TRAITEE, StatutAlerte.EN_COURS)).stream().map(this::convertToAlerteStockeDTO).toList();
    }

    // ==================== MÉTHODES UTILITAIRES ====================


    @Transactional(readOnly = true)
    public Map<String, Object> getResumePeriode(LocalDate dateDebut, LocalDate dateFin) {
        log.info("Résumé des ventes du {} au {}", dateDebut, dateFin);

        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);

        List<Commande> commandes = commandeRepository.findByDateCommandeBetween(debut, fin);

        Map<String, Object> resume = new HashMap<>();
        resume.put("totalCommandes", commandes.size());
        resume.put("chiffreAffaire", commandes.stream()
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE)
                .map(Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        resume.put("commandesMoyennesParJour",
                commandes.size() / Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin)));

        return resume;
    }


    public void changePassword(Long id, ChangePasswordDTO passwordDTO) {
        log.info("Changement de mot de passe pour le manager ID: {}", id);

        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));

        User user = manager.getUser();

        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("L'ancien mot de passe est incorrect");
        }

        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);

        log.info("Mot de passe changé avec succès");
    }

    // ==================== CONVERSION ====================

    private ManagerResponseDTO convertToResponseDTO(Manager manager) {
        User user = manager.getUser();

        return ManagerResponseDTO.builder()
                .idManager(manager.getIdManager())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .adresse(user.getAdresse())
                .photo(user.getPhoto())
                .dateEmbauche(manager.getDateEmbauche())
                .actif(manager.getActif())
                .salaire(manager.getSalaire())
//                .niveauAcces(manager.getNiveauAcces())
                .specialites(manager.getSpecialites())
                .emailVerified(user.isEmailVerified())
                .build();
    }

    private RapportVenteDTO convertToRapportVenteDTO(RapportVente rapportVente) {


        return  RapportVenteDTO.builder()
                .managerId(rapportVente.getManager().getIdManager())
                .dateDebut((LocalDate) rapportVente.getDateDebut())
                .dateFin((rapportVente.getDateFin()))
                .typeRapport(TypeRapport.PERSONNALISE)
                .chiffreAffaireTotal((BigDecimal) rapportVente.getChiffreAffaireTotal())
                .nombreCommandesTotal((rapportVente.getNombreCommandesTotal()))
                .nombreClientsUniques(rapportVente.getNombreClientsUniques())
                .panierMoyen(rapportVente.getPanierMoyen())
                .chiffreAffaireSurPlace(rapportVente.getChiffreAffaireSurPlace())
                .chiffreAffaireAEmporter(rapportVente.getChiffreAffaireAEmporter())
                .chiffreAffaireLivraison(rapportVente.getChiffreAffaireLivraison())
                .build();

    }

    private AlerteStockDTO convertToAlerteStockeDTO(AlerteStock alerteStock) {
        return  AlerteStockDTO.builder()
                .idAlerte(alerteStock.getIdAlerte())
                .ingredientId(alerteStock.getIngredient().getIdIngredient())
                .typeAlerte(alerteStock.getTypeAlerte())
                .dateAlerte(alerteStock.getDateAlerte())
                .statut(alerteStock.getStatut())
                .traiteePar(alerteStock.getTraiteePar()!=null? alerteStock.getTraiteePar().getUser().getFullName():"N/A")
                .dateTraitement(alerteStock.getDateTraitement())
                .actionPrise(alerteStock.getActionPrise())
                .message(alerteStock.getMessage())
                .build();

    }
}
