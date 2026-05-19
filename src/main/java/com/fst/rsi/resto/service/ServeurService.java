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
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServeurService {

    private final ServeurRepo serveurRepository;
    private final UserRepo userRepository;
    private final CommandeRepo commandeRepository;
    private final TableRestaurantRepo tableRestaurantRepository;
    private final PerformanceServeurRepo performanceServeurRepository;
    private final PasswordEncoder passwordEncoder;


    public ServeurResponseDTO createServeur(ServeurRequestDTO request) {
        log.info("Création d'un nouveau serveur: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un compte avec cet email existe déjà");
        }

        if (userRepository.existsByTelephone(request.getTelephone())) {
            throw new BusinessException("Un compte avec ce numéro de téléphone existe déjà");
        }

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

        user.addRole(UserRole.SERVEUR);

        User savedUser = userRepository.save(user);

        Serveur serveur = Serveur.builder()
                .user(savedUser)
                .dateEmbauche(request.getDateEmbauche() != null ? request.getDateEmbauche() : LocalDate.now())
                .actif(true)
                .salaire(request.getSalaire())
                .nombreHeuresSemaine(request.getNombreHeuresSemaine())
                .build();

        Serveur savedServeur = serveurRepository.save(serveur);

        log.info("Serveur créé avec succès - ID: {}", savedServeur.getIdServeur());
        return convertToResponseDTO(savedServeur);
    }


    @Transactional(readOnly = true)
    public ServeurResponseDTO getServeurById(Long id) {
        log.info("Récupération du serveur ID: {}", id);
        Serveur serveur = serveurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé avec l'ID: " + id));
        return convertToResponseDTO(serveur);
    }


    @Transactional(readOnly = true)
    public ServeurResponseDTO getServeurByEmail(String email) {
        log.info("Récupération du serveur par email: {}", email);
        Serveur serveur = serveurRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé avec l'email: " + email));
        return convertToResponseDTO(serveur);
    }

    @Transactional(readOnly = true)
    public ServeurResponseDTO getServeurByTelephone(String telephone) {
        log.info("Récupération du serveur par téléphone: {}", telephone);
        Serveur serveur = serveurRepository.findByUserTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé avec le téléphone: " + telephone));
        return convertToResponseDTO(serveur);
    }


    @Transactional(readOnly = true)
    public Page<ServeurResponseDTO> getAllServeurs(Pageable pageable) {
        log.info("Récupération de tous les serveurs - Page: {}", pageable.getPageNumber());
        return serveurRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }


    @Transactional(readOnly = true)
    public List<ServeurResponseDTO> getServeursActifs() {
        log.info("Récupération des serveurs actifs");
        return serveurRepository.findByActifTrue()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    /**
     * Récupérer les serveurs disponibles (actifs et sans commandes en cours)
     */
    @Transactional(readOnly = true)
    public List<ServeurResponseDTO> getServeursDisponibles() {
        log.info("Récupération des serveurs disponibles");

        List<Serveur> serveursActifs = serveurRepository.findByActifTrue();

        return serveursActifs.stream()
                .filter(serveur -> {
                    // Vérifier s'il n'a pas de commandes en cours
                    List<Commande> commandesEnCours = commandeRepository
                            .findByServeurIdServeurAndStatutIn(
                                    serveur.getIdServeur(),
                                    List.of(StatutCommande.EN_ATTENTE, StatutCommande.EN_PREPARATION)
                            );
                    return commandesEnCours.isEmpty();
                })
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher des serveurs par nom ou prénom
     */
    @Transactional(readOnly = true)
    public List<ServeurResponseDTO> searchServeurs(String searchTerm) {
        log.info("Recherche de serveurs avec le terme: {}", searchTerm);
        return serveurRepository.searchByNomOrPrenom(searchTerm)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    public ServeurResponseDTO updateServeur(Long id, ServeurUpdateDTO updateDTO) {
        log.info("Mise à jour du serveur ID: {}", id);

        Serveur serveur = serveurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé avec l'ID: " + id));

        User user = serveur.getUser();

        // Mettre à jour les informations utilisateur
        if (updateDTO.getNom() != null) {
            user.setNom(updateDTO.getNom());
        }
        if (updateDTO.getPrenom() != null) {
            user.setPrenom(updateDTO.getPrenom());
        }
        if (updateDTO.getTelephone() != null) {
            // Vérifier si le téléphone n'est pas déjà utilisé
            if (!user.getTelephone().equals(updateDTO.getTelephone())
                    && userRepository.existsByTelephone(updateDTO.getTelephone())) {
                throw new BusinessException("Ce numéro de téléphone est déjà utilisé");
            }
            user.setTelephone(updateDTO.getTelephone());
        }
        if (updateDTO.getAdresse() != null) {
            user.setAdresse(updateDTO.getAdresse());
        }
        if (updateDTO.getPhoto() != null) {
            user.setPhoto(updateDTO.getPhoto());
        }

        // Mettre à jour les informations du serveur
        if (updateDTO.getSalaire() != null) {
            serveur.setSalaire(updateDTO.getSalaire());
        }

        if (updateDTO.getNombreHeuresSemaine() != null) {
            serveur.setNombreHeuresSemaine(updateDTO.getNombreHeuresSemaine());
        }

        userRepository.save(user);
        Serveur updatedServeur = serveurRepository.save(serveur);

        log.info("Serveur mis à jour avec succès");
        return convertToResponseDTO(updatedServeur);
    }


    public void changePassword(Long id, ChangePasswordDTO passwordDTO) {
        log.info("Changement de mot de passe pour le serveur ID: {}", id);

        Serveur serveur = serveurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé avec l'ID: " + id));

        User user = serveur.getUser();

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("L'ancien mot de passe est incorrect");
        }

        // Vérifier que le nouveau mot de passe est différent
        if (passwordEncoder.matches(passwordDTO.getNewPassword(), user.getPassword())) {
            throw new BusinessException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        // Changer le mot de passe
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);

        log.info("Mot de passe changé avec succès pour le serveur ID: {}", id);
    }


    public ServeurResponseDTO toggleServeurStatus(Long id) {
        log.info("Changement du statut du serveur ID: {}", id);

        Serveur serveur = serveurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé avec l'ID: " + id));

        serveur.setActif(!serveur.getActif());
        serveur.getUser().setEnabled(serveur.getActif());

        userRepository.save(serveur.getUser());
        Serveur updatedServeur = serveurRepository.save(serveur);

        log.info("Statut du serveur changé: {}", serveur.getActif() ? "Actif" : "Inactif");
        return convertToResponseDTO(updatedServeur);
    }

    public void deactivateServeur(Long id) {
        log.info("Désactivation du serveur ID: {}", id);

        Serveur serveur = serveurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé avec l'ID: " + id));

        // Vérifier s'il a des commandes en cours
        List<Commande> commandesEnCours = commandeRepository
                .findByServeurIdServeurAndStatutIn(
                        id,
                        List.of(StatutCommande.EN_ATTENTE, StatutCommande.EN_PREPARATION, StatutCommande.PRETE)
                );

        if (!commandesEnCours.isEmpty()) {
            throw new BusinessException(
                    "Impossible de désactiver ce serveur. Il a " +
                            commandesEnCours.size() + " commande(s) en cours"
            );
        }

        serveur.setActif(false);
        serveur.getUser().setEnabled(false);

        userRepository.save(serveur.getUser());
        serveurRepository.save(serveur);

        log.info("Serveur désactivé avec succès");
    }


    public void deleteServeur(Long id) {
        log.info("Suppression du serveur ID: {}", id);

        Serveur serveur = serveurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé avec l'ID: " + id));

        // Vérifier s'il a des commandes
        List<Commande> commandes = commandeRepository.findByServeurIdServeur(id);

        if (!commandes.isEmpty()) {
            throw new BusinessException(
                    "Impossible de supprimer ce serveur. Il a géré " +
                            commandes.size() + " commande(s)"
            );
        }

        // Désactiver au lieu de supprimer
        serveur.setActif(false);
        serveur.getUser().setEnabled(false);
        serveurRepository.save(serveur);

        log.info("Serveur supprimé (désactivé) avec succès");
    }


    public CommandeResponseDTO assignerServeurACommande(Long idServeur, Long idCommande) {
        log.info("Assignation du serveur ID: {} à la commande ID: {}", idServeur, idCommande);

        Serveur serveur = serveurRepository.findById(idServeur)
                .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé"));

        if (!serveur.getActif()) {
            throw new BusinessException("Ce serveur n'est pas actif");
        }

        Commande commande = commandeRepository.findById(idCommande)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée"));

        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new BusinessException("Seules les commandes en attente peuvent être assignées");
        }

        commande.setServeur(serveur);
        Commande updatedCommande = commandeRepository.save(commande);

        log.info("Serveur assigné à la commande avec succès");
        return convertCommandeToResponseDTO(updatedCommande);
    }


    @Transactional(readOnly = true)
    public List<CommandeResponseDTO> getCommandesByServeur(Long idServeur) {
        log.info("Récupération des commandes du serveur ID: {}", idServeur);

        List<Commande> commandes = commandeRepository.findByServeurIdServeur(idServeur);
        return commandes.stream()
                .map(this::convertCommandeToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<CommandeResponseDTO> getCommandesEnCoursByServeur(Long idServeur) {
        log.info("Récupération des commandes en cours du serveur ID: {}", idServeur);

        List<Commande> commandes = commandeRepository.findByServeurIdServeurAndStatutIn(
                idServeur,
                List.of(StatutCommande.EN_ATTENTE, StatutCommande.EN_PREPARATION, StatutCommande.PRETE)
        );

        return commandes.stream()
                .map(this::convertCommandeToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<TableDTO> getTablesAssignees(Long idServeur) {
        log.info("Récupération des tables assignées au serveur ID: {}", idServeur);

        Serveur serveur = serveurRepository.findById(idServeur)
                .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé"));

        // Récupérer les tables avec des commandes actives du serveur
        List<Commande> commandesActives = commandeRepository.findByServeurIdServeurAndStatutIn(
                idServeur,
                List.of(StatutCommande.EN_ATTENTE, StatutCommande.EN_PREPARATION, StatutCommande.PRETE)
        );

        return commandesActives.stream()
                .map(Commande::getTable)
                .filter(table -> table != null)
                .distinct()
                .map(this::convertTableToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les statistiques d'un serveur
     */
    @Transactional(readOnly = true)
    public ServeurStatisticsDTO getServeurStatistics(Long id) {
        log.info("Calcul des statistiques pour le serveur ID: {}", id);

        Serveur serveur = serveurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé avec l'ID: " + id));

        List<Commande> commandes = commandeRepository.findByServeurIdServeur(id);

        long totalCommandes = commandes.size();

        long commandesServies = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.SERVIE || c.getStatut() == StatutCommande.LIVREE)
                .count();

        long commandesEnCours = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.EN_ATTENTE
                        || c.getStatut() == StatutCommande.EN_PREPARATION
                        || c.getStatut() == StatutCommande.PRETE)
                .count();

        BigDecimal chiffreAffaireGenere = commandes.stream()
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE)
                .map(Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal panierMoyen = commandesServies > 0
                ? chiffreAffaireGenere.divide(
                BigDecimal.valueOf(commandesServies),
                2,
                BigDecimal.ROUND_HALF_UP
        )
                : BigDecimal.ZERO;

        // Commande la plus récente
        Commande derniereCommande = commandes.stream()
                .max((c1, c2) -> c1.getDateCommande().compareTo(c2.getDateCommande()))
                .orElse(null);

        long nombreTablesServies = commandes.stream()
                .filter(c -> c.getTable() != null)
                .map(c -> c.getTable().getIdTable())
                .distinct()
                .count();

        return ServeurStatisticsDTO.builder()
                .idServeur(id)
                .nomComplet(serveur.getUser().getPrenom() + " " + serveur.getUser().getNom())
                .totalCommandes(totalCommandes)
                .commandesServies(commandesServies)
                .commandesEnCours(commandesEnCours)
                .chiffreAffaireGenere(chiffreAffaireGenere)
                .panierMoyen(panierMoyen)
                .nombreTablesServies(nombreTablesServies)
                .dateEmbauche(serveur.getDateEmbauche())
                .dateDerniereCommande(derniereCommande != null ? derniereCommande.getDateCommande() : null)
                .actif(serveur.getActif())
                .build();
    }


    @Transactional(readOnly = true)
    public List<ServeurStatisticsDTO> getTopServeurs(int limit) {
        log.info("Récupération du top {} serveurs", limit);

        List<Serveur> serveurs = serveurRepository.findByActifTrue();

        return serveurs.stream()
                .map(serveur -> getServeurStatistics(serveur.getIdServeur()))
                .sorted((s1, s2) -> s2.getChiffreAffaireGenere().compareTo(s1.getChiffreAffaireGenere()))
                .limit(limit)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PerformanceServeurDTO> getPerformances(Long idServeur,
                                                       LocalDate dateDebut,
                                                       LocalDate dateFin) {
        log.info("Récupération des performances du serveur ID: {} entre {} et {}",
                idServeur, dateDebut, dateFin);

        return performanceServeurRepository.findByServeurIdServeurAndDateDebutBetween(
                        idServeur, dateDebut, dateFin)
                .stream()
                .map(this::convertPerformanceToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ServeurResponseDTO> getServeursByPerformance(BigDecimal noteMinimum) {
        log.info("Récupération des serveurs avec note >= {}", noteMinimum);

        List<Serveur> serveurs = serveurRepository.findByActifTrue();

        return serveurs.stream()
                .filter(serveur -> {
                    List<PerformanceServeur> performances = serveur.getPerformances();
                    if (performances == null || performances.isEmpty()) {
                        return false;
                    }

                    // Calculer la note moyenne
                    BigDecimal noteMoyenne = performances.stream()
                            .map(PerformanceServeur::getNoteGlobale)
                            .filter(note -> note != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(performances.size()), 2, BigDecimal.ROUND_HALF_UP);

                    return noteMoyenne.compareTo(noteMinimum) >= 0;
                })
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ServeurResponseDTO> getNouveauxServeurs(int joursDepuis) {
        log.info("Récupération des serveurs embauchés depuis {} jours", joursDepuis);

        LocalDate dateLimit = LocalDate.now().minusDays(joursDepuis);
        return serveurRepository.findByDateEmbaucheAfter(dateLimit)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== Méthodes Privées ====================

    private ServeurResponseDTO convertToResponseDTO(Serveur serveur) {
        User user = serveur.getUser();

        return ServeurResponseDTO.builder()
                .idServeur(serveur.getIdServeur())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .adresse(user.getAdresse())
                .photo(user.getPhoto())
                .dateEmbauche(serveur.getDateEmbauche())
                .actif(serveur.getActif())
                .salaire(serveur.getSalaire())
                .nombreHeuresSemaine(serveur.getNombreHeuresSemaine())
                .emailVerified(user.isEmailVerified())
                .build();
    }


    private CommandeResponseDTO convertCommandeToResponseDTO(Commande commande) {
        return CommandeResponseDTO.builder()
                .idCommande(commande.getIdCommande())
                .client(convertClientToDTO(commande.getClient()))
                .table(convertTableToResponseDTO(commande.getTable()))
                .dateCommande(commande.getDateCommande())
                .statut(commande.getStatut())
                .typeCommande(commande.getTypeCommande())
                .montantTotal(commande.getMontantTotal())
                .remise(commande.getRemise())
                .instructionsSpeciales(commande.getInstructionsSpeciales())
                .lignesCommande(convertLigneCommandeToDTO(commande.getLignesCommande()))
                .tempsEstimePreparation(commande.getTempsEstimePreparation())
                .build();
    }

    private TableDTO convertTableToResponseDTO(TableRestaurant table) {
        return TableDTO.builder()
                .idTable(table.getIdTable())
                .numero(table.getNumero())
                .capacite(table.getCapacite())
                .statut(table.getStatut())
                .build();
    }


    private PerformanceServeurDTO convertPerformanceToDTO(PerformanceServeur performance) {
        return PerformanceServeurDTO.builder()
                .idPerformance(performance.getIdPerformance())
                .dateDebut(performance.getDateDebut())
                .dateFin(performance.getDateFin())
                .nombreCommandesServies(performance.getNombreCommandesServies())
                .chiffreAffaireGenere(performance.getChiffreAffaireGenere())
                .panierMoyen(performance.getPanierMoyen())
                .noteQualiteService(performance.getNoteQualiteService())
                .noteRapidite(performance.getNoteRapidite())
                .noteGlobale(performance.getNoteGlobale())
                .commentaires(performance.getCommentaires())
                .pointsForts(performance.getPointsForts())
                .axesAmelioration(performance.getAxesAmelioration())
                .build();
    }

    private ClientDTO convertClientToDTO(Client client) {
        if (client == null)
            return null;
        else
            return ClientDTO.builder()
                    .idClient(client.getIdClient())
                    .nom(client.getUser().getNom())
                    .prenom(client.getUser().getPrenom())
                    .email(client.getUser().getEmail())
                    .telephone(client.getUser().getTelephone())
                    .build();
    }

    private List<LigneCommandeDTO> convertLigneCommandeToDTO(List<LigneCommande>ligneCommande) {
        return ligneCommande.stream()
                .map(this::convertLigneCommandeToDTO)
                .toList();
    }

    private LigneCommandeDTO convertLigneCommandeToDTO(LigneCommande ligneCommande) {

        return LigneCommandeDTO.builder()
                .idLigne(ligneCommande.getIdLigne())
                .platNom(ligneCommande.getPlat().getNom())
                .prixUnitaire(ligneCommande.getPrixUnitaire())
                .quantite(ligneCommande.getQuantite())
                .sousTotal(ligneCommande.getSousTotal())
                .build();
    }

}

