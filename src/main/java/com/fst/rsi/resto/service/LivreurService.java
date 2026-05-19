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
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LivreurService {

    private final LivreurRepo livreurRepository;
    private final UserRepo userRepository;
    private final LivraisonRepo livraisonRepository;
//    private final PerformanceLivreurRe performanceLivreurRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Créer un nouveau livreur
     */
    public LivreurResponseDTO createLivreur(LivreurRequestDTO request) {
        log.info("Création d'un nouveau livreur: {}", request.getEmail());

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

        user.addRole(UserRole.LIVREUR);

        User savedUser = userRepository.save(user);

        Livreur livreur = Livreur.builder()
                .user(savedUser)
                .vehicule(request.getVehicule())
                .disponible(true)
                .dateEmbauche((request.getDateEmbauche() != null ? request.getDateEmbauche() : LocalDate.now()).atStartOfDay())
                .salaireBase(request.getSalaireBase())
                .primeParLivraison(request.getPrimeParLivraison())
                .statut(StatutLivreur.ACTIF)
                .build();

        Livreur savedLivreur = livreurRepository.save(livreur);

        log.info("Livreur créé avec succès - ID: {}", savedLivreur.getIdLivreur());
        return convertToResponseDTO(savedLivreur);
    }



    @Transactional(readOnly = true)
    public LivreurResponseDTO getLivreurById(Long id) {
        log.info("Récupération du livreur ID: {}", id);
        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec l'ID: " + id));
        return convertToResponseDTO(livreur);
    }



    @Transactional(readOnly = true)
    public LivreurResponseDTO getLivreurByEmail(String email) {
        log.info("Récupération du livreur par email: {}", email);
        Livreur livreur = livreurRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec l'email: " + email));
        return convertToResponseDTO(livreur);
    }


    @Transactional(readOnly = true)
    public Page<LivreurResponseDTO> getAllLivreurs(Pageable pageable) {
        log.info("Récupération de tous les livreurs - Page: {}", pageable.getPageNumber());
        return livreurRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }


    @Transactional(readOnly = true)
    public List<LivreurResponseDTO> getLivreursDisponibles() {
        log.info("Récupération des livreurs disponibles");
        return getLivreursByStatut(StatutLivreur.ACTIF);
    }




    @Transactional(readOnly = true)
    public List<LivreurResponseDTO> getLivreursByStatut(StatutLivreur statut) {
        log.info("Récupération des livreurs avec le statut: {}", statut);
        return livreurRepository.findByStatut(statut)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<LivreurResponseDTO> getLivreursEnLivraison() {
        log.info("Récupération des livreurs en livraison");
        return getLivreursByStatut(StatutLivreur.EN_LIVRAISON);
    }


    @Transactional(readOnly = true)
    public List<LivreurResponseDTO> searchLivreurs(String searchTerm) {
        log.info("Recherche de livreurs avec le terme: {}", searchTerm);
        return livreurRepository.searchByNomOrPrenom(searchTerm)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    public LivreurResponseDTO updateLivreur(Long id, LivreurUpdateDTO updateDTO) {
        log.info("Mise à jour du livreur ID: {}", id);

        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec l'ID: " + id));

        User user = livreur.getUser();

        // Mettre à jour les informations utilisateur
        if (updateDTO.getNom() != null) {
            user.setNom(updateDTO.getNom());
        }
        if (updateDTO.getPrenom() != null) {
            user.setPrenom(updateDTO.getPrenom());
        }
        if (updateDTO.getTelephone() != null) {
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

        if (updateDTO.getVehicule() != null) {
            livreur.setVehicule(updateDTO.getVehicule());
        }

        if (updateDTO.getSalaireBase() != null) {
            livreur.setSalaireBase(updateDTO.getSalaireBase());
        }
        if (updateDTO.getPrimeParLivraison() != null) {
            livreur.setPrimeParLivraison(updateDTO.getPrimeParLivraison());
        }

        userRepository.save(user);
        Livreur updatedLivreur = livreurRepository.save(livreur);

        log.info("Livreur mis à jour avec succès");
        return convertToResponseDTO(updatedLivreur);
    }

    public void changePassword(Long id, ChangePasswordDTO passwordDTO) {
        log.info("Changement de mot de passe pour le livreur ID: {}", id);

        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec l'ID: " + id));

        User user = livreur.getUser();

        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("L'ancien mot de passe est incorrect");
        }

        if (passwordEncoder.matches(passwordDTO.getNewPassword(), user.getPassword())) {
            throw new BusinessException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);

        log.info("Mot de passe changé avec succès pour le livreur ID: {}", id);
    }


    public LivreurResponseDTO toggleDisponibilite(Long id) {
        log.info("Changement de disponibilité du livreur ID: {}", id);

        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec l'ID: " + id));

        livreur.setDisponible(!livreur.getDisponible());

        if (!livreur.getDisponible() && livreur.getStatut() != StatutLivreur.EN_LIVRAISON) {
            livreur.setStatut(StatutLivreur.EN_PAUSE);
        } else if (livreur.getDisponible() && livreur.getStatut() == StatutLivreur.EN_PAUSE) {
            livreur.setStatut(StatutLivreur.ACTIF);
        }

        Livreur updatedLivreur = livreurRepository.save(livreur);

        log.info("Disponibilité changée: {}", livreur.getDisponible() ? "Disponible" : "Non disponible");
        return convertToResponseDTO(updatedLivreur);
    }


    public LivreurResponseDTO changerStatut(Long id, StatutLivreur nouveauStatut) {
        log.info("Changement du statut du livreur ID: {} vers {}", id, nouveauStatut);

        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec l'ID: " + id));

        if (livreur.getStatut() == StatutLivreur.EN_LIVRAISON && nouveauStatut != StatutLivreur.ACTIF) {
            throw new BusinessException("Un livreur en livraison ne peut passer qu'au statut ACTIF");
        }

        livreur.setStatut(nouveauStatut);

        if (nouveauStatut == StatutLivreur.HORS_SERVICE || nouveauStatut == StatutLivreur.CONGE) {
            livreur.setDisponible(false);
        }

        Livreur updatedLivreur = livreurRepository.save(livreur);

        log.info("Statut du livreur changé avec succès");
        return convertToResponseDTO(updatedLivreur);
    }


    public LivreurResponseDTO mettreEnConge(Long id, LocalDate dateDebut, LocalDate dateFin) {
        log.info("Mise en congé du livreur ID: {} du {} au {}", id, dateDebut, dateFin);

        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé"));

        if (livreur.getStatut() == StatutLivreur.EN_LIVRAISON) {
            throw new BusinessException("Impossible de mettre en congé un livreur en cours de livraison");
        }

        livreur.setStatut(StatutLivreur.CONGE);
        livreur.setDisponible(false);

        Livreur updatedLivreur = livreurRepository.save(livreur);

        log.info("Livreur mis en congé avec succès");
        return convertToResponseDTO(updatedLivreur);
    }


    public void deactivateLivreur(Long id) {
        log.info("Désactivation du livreur ID: {}", id);

        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec l'ID: " + id));

        List<Livraison> livraisonsEnCours = livraisonRepository
                .findByLivreurIdLivreurAndStatutLivraisonIn(
                        id,
                        List.of(StatutLivraison.ASSIGNEE, StatutLivraison.EN_COURS)
                );

        if (!livraisonsEnCours.isEmpty()) {
            throw new BusinessException(
                    MessageFormat.format("Impossible de désactiver ce livreur. Il a {0} livraison(s) en cours", livraisonsEnCours.size())
            );
        }

        livreur.setStatut(StatutLivreur.HORS_SERVICE);
        livreur.setDisponible(false);
        livreur.getUser().setEnabled(false);

        userRepository.save(livreur.getUser());
        livreurRepository.save(livreur);

        log.info("Livreur désactivé avec succès");
    }


    public void deleteLivreur(Long id) {
        log.info("Suppression du livreur ID: {}", id);

        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec l'ID: " + id));

        List<Livraison> livraisons = livraisonRepository.findByLivreurIdLivreur(id);

        if (!livraisons.isEmpty()) {
            throw new BusinessException(
                    MessageFormat.format("Impossible de supprimer ce livreur. Il a effectué {0} livraison(s)", livraisons.size())
            );
        }

        livreur.setStatut(StatutLivreur.HORS_SERVICE);
        livreur.setDisponible(false);
        livreur.getUser().setEnabled(false);
        livreurRepository.save(livreur);

        log.info("Livreur supprimé (désactivé) avec succès");
    }


    @Transactional(readOnly = true)
    public List<LivraisonResponseDTO> getLivraisonsByLivreur(Long idLivreur) {
        log.info("Récupération des livraisons du livreur ID: {}", idLivreur);

        List<Livraison> livraisons = livraisonRepository.findByLivreurIdLivreur(idLivreur);
        return livraisons.stream()
                .map(this::convertLivraisonToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<LivraisonResponseDTO> getLivraisonsEnCoursByLivreur(Long idLivreur) {
        log.info("Récupération des livraisons en cours du livreur ID: {}", idLivreur);

        List<Livraison> livraisons = livraisonRepository.findByLivreurIdLivreurAndStatutLivraisonIn(
                idLivreur,
                List.of(StatutLivraison.ASSIGNEE, StatutLivraison.EN_COURS)
        );

        return livraisons.stream()
                .map(this::convertLivraisonToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public LivreurStatisticsDTO getLivreurStatistics(Long id) {
        log.info("Calcul des statistiques pour le livreur ID: {}", id);

        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec l'ID: %d".formatted(id)));

        List<Livraison> livraisons = livraisonRepository.findByLivreurIdLivreur(id);

        long totalLivraisons = livraisons.size();

        long livraisonsEffectuees = livraisons.stream()
                .filter(l -> l.getStatutLivraison() == StatutLivraison.LIVREE)
                .count();

        long livraisonsEnCours = livraisons.stream()
                .filter(l -> l.getStatutLivraison() == StatutLivraison.ASSIGNEE
                        || l.getStatutLivraison() == StatutLivraison.EN_COURS)
                .count();

        long livraisonsAnnulees = livraisons.stream()
                .filter(l -> l.getStatutLivraison() == StatutLivraison.ANNULEE)
                .count();

        BigDecimal tempsLivraisonMoyen = livraisons.stream()
                .filter(l -> l.getHeureDepart() != null && l.getHeureArrivee() != null)
                .map(l -> {
                    long minutes = java.time.Duration.between(
                            l.getHeureDepart(),
                            l.getHeureArrivee()
                    ).toMinutes();
                    return BigDecimal.valueOf(minutes);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(
                        BigDecimal.valueOf(Math.max(livraisonsEffectuees, 1)),
                        2,
                        BigDecimal.ROUND_HALF_UP
                );

        BigDecimal gainsLivraisons = BigDecimal.valueOf(livraisonsEffectuees)
                .multiply(livreur.getPrimeParLivraison() != null
                        ? livreur.getPrimeParLivraison()
                        : BigDecimal.ZERO);

        Livraison derniereLivraison = livraisons.stream()
                .max((l1, l2) -> l1.getCommande().getDateCommande()
                        .compareTo(l2.getCommande().getDateCommande()))
                .orElse(null);

        return LivreurStatisticsDTO.builder()
                .idLivreur(id)
                .nomComplet("%s %s".formatted(livreur.getUser().getPrenom(), livreur.getUser().getNom()))
                .totalLivraisons(totalLivraisons)
                .livraisonsEffectuees(livraisonsEffectuees)
                .livraisonsEnCours(livraisonsEnCours)
                .livraisonsAnnulees(livraisonsAnnulees)
                .tempsLivraisonMoyen(tempsLivraisonMoyen)
                .gainsLivraisons(gainsLivraisons)
                .vehicule(livreur.getVehicule())
                .statut(livreur.getStatut())
                .disponible(livreur.getDisponible())
                .dateEmbauche(LocalDate.from(livreur.getDateEmbauche()))
                .dateDerniereLivraison(derniereLivraison != null
                        ? derniereLivraison.getCommande().getDateCommande()
                        : null)
                .build();
    }


    @Transactional(readOnly = true)
    public List<LivreurStatisticsDTO> getTopLivreurs(int limit) {
        log.info("Récupération du top {} livreurs", limit);

        List<Livreur> livreurs = livreurRepository.findByStatut(StatutLivreur.ACTIF);

        return livreurs.stream()
                .map(livreur -> getLivreurStatistics(livreur.getIdLivreur()))
                .sorted((s1, s2) -> Long.compare(s2.getLivraisonsEffectuees(), s1.getLivraisonsEffectuees()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les performances d'un l
     **/
//    @Transactional(readOnly = true)
//    public List<PerformanceLivreurDTO> getPerformances(Long idLivreur,
//                                                       LocalDate dateDebut,
//                                                       LocalDate dateFin) {
//        log.info("Récupération des performances du livreur ID: {} entre {} et {}",
//                idLivreur, dateDebut, dateFin);
//
//        return performanceLivreurRepository.findByLivreurIdLivreurAndDateDebutBetween(
//                        idLivreur, dateDebut, dateFin)
//                .stream()
//                .map(this::convertPerformanceToDTO)
//                .collect(Collectors.toList());
//    }

//
//    @Transactional(readOnly = true)
//    public List<LivreurResponseDTO> getLivreursPermisExpire() {
//        log.info("Récupération des livreurs avec permis expiré ou proche de l'expiration");
//
//        LocalDate dateLimit = LocalDate.now().plusDays(30);
//
//        return livreurRepository.findAll().stream()
//                .filter(l -> l.getDateValiditePermis() != null
//                        && l.getDateValiditePermis().isBefore(dateLimit))
//                .map(this::convertToResponseDTO)
//                .collect(Collectors.toList());
//    }



    @Transactional(readOnly = true)
    public List<LivreurResponseDTO> getNouveauxLivreurs(int joursDepuis) {
        log.info("Récupération des livreurs embauchés depuis {} jours", joursDepuis);

        LocalDate dateLimit = LocalDate.now().minusDays(joursDepuis);
        return livreurRepository.findByDateEmbaucheAfter(dateLimit)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== Méthodes Privées ====================

    private LivreurResponseDTO convertToResponseDTO(Livreur livreur) {
        User user = livreur.getUser();

        return LivreurResponseDTO.builder()
                .idLivreur(livreur.getIdLivreur())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .adresse(user.getAdresse())
                .photo(user.getPhoto())
                .vehicule(livreur.getVehicule())
                .disponible(livreur.getDisponible())
                .dateEmbauche(LocalDate.from(livreur.getDateEmbauche()))
                .salaireBase(livreur.getSalaireBase())
                .primeParLivraison(livreur.getPrimeParLivraison())
                .statut(livreur.getStatut())
                .emailVerified(user.isEmailVerified())
                .build();
    }


    private LivraisonResponseDTO convertLivraisonToResponseDTO(Livraison livraison) {
        return LivraisonResponseDTO.builder()
                .idLivraison(livraison.getIdLivraison())
                .idCommande(livraison.getCommande().getIdCommande())
                .adresseLivraison(livraison.getAdresseLivraison())
                .statutLivraison(livraison.getStatutLivraison())
                .heureDepart(livraison.getHeureDepart())
                .heureArrivee(livraison.getHeureArrivee())
                .fraisLivraison(livraison.getFraisLivraison())
                .build();
    }


}

