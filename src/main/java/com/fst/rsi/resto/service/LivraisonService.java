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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LivraisonService {

    private final LivraisonRepo livraisonRepository;
    private final CommandeRepo commandeRepository;
    private final LivreurRepo livreurRepository;
    private final NotificationService notificationService;


    public LivraisonResponseDTO createLivraison(LivraisonRequestDTO request) {
        log.info("Création d'une nouvelle livraison pour la commande ID: {}", request.getIdCommande());

        Commande commande = commandeRepository.findById(request.getIdCommande())
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée"));

        // Vérifier que la commande est de type livraison
        if (commande.getTypeCommande() != TypeCommande.LIVRAISON) {
            throw new BusinessException("Cette commande n'est pas une commande de livraison");
        }
// Vérifier qu'une livraison n'existe pas déjà pour cette commande
        if (livraisonRepository.existsByCommandeIdCommande(request.getIdCommande())) {
            throw new BusinessException("Une livraison existe déjà pour cette commande");
        }

        // Créer la livraison
        Livraison livraison = Livraison.builder()
                .commande(commande)
                .adresseLivraison(request.getAdresseLivraison())
                .statutLivraison(StatutLivraison.ASSIGNEE)
                .fraisLivraison(request.getFraisLivraison() != null ? request.getFraisLivraison() : BigDecimal.ZERO)
                .build();

        // Assigner un livreur si spécifié
        if (request.getIdLivreur() != null) {
            Livreur livreur = livreurRepository.findById(request.getIdLivreur())
                    .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé"));

            if (!livreur.getDisponible() || livreur.getStatut() != StatutLivreur.ACTIF) {
                throw new BusinessException("Ce livreur n'est pas disponible");
            }

            livraison.setLivreur(livreur);
        }

        Livraison savedLivraison = livraisonRepository.save(livraison);

        // Notifier le client
        notificationService.envoyerNotification(
                commande,
                TypeNotification.EN_LIVRAISON,
                "Votre commande #%d est en cours de préparation pour la livraison.".formatted(commande.getIdCommande())
        );

        log.info("Livraison créée avec succès - ID: {}", savedLivraison.getIdLivraison());
        return convertToResponseDTO(savedLivraison);
    }

    @Transactional(readOnly = true)
    public LivraisonResponseDTO getLivraisonById(Long id) {
        log.info("Récupération de la livraison ID: {}", id);
        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée avec l'ID: " + id));
        return convertToResponseDTO(livraison);
    }



    @Transactional(readOnly = true)
    public LivraisonResponseDTO getLivraisonByCommande(Long idCommande) {
        log.info("Récupération de la livraison pour la commande ID: {}", idCommande);
        Livraison livraison = livraisonRepository.findByCommandeIdCommande(idCommande)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune livraison trouvée pour cette commande"));
        return convertToResponseDTO(livraison);
    }



    @Transactional(readOnly = true)
    public Page<LivraisonResponseDTO> getAllLivraisons(Pageable pageable) {
        log.info("Récupération de toutes les livraisons - Page: {}", pageable.getPageNumber());
        return livraisonRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }



    @Transactional(readOnly = true)
    public List<LivraisonResponseDTO> getLivraisonsByStatut(StatutLivraison statut) {
        log.info("Récupération des livraisons avec le statut: {}", statut);
        return livraisonRepository.findByStatutLivraison(statut)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<LivraisonResponseDTO> getLivraisonsEnCours() {
        log.info("Récupération des livraisons en cours");
        return livraisonRepository.findByStatutLivraisonIn(
                        List.of(StatutLivraison.ASSIGNEE, StatutLivraison.EN_COURS))
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<LivraisonResponseDTO> getLivraisonsByLivreur(Long idLivreur) {
        log.info("Récupération des livraisons du livreur ID: {}", idLivreur);
        return livraisonRepository.findByLivreurIdLivreur(idLivreur)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<LivraisonResponseDTO> getLivraisonsNonAssignees() {
        log.info("Récupération des livraisons non assignées");
        return livraisonRepository.findByLivreurIsNullAndStatutLivraison(StatutLivraison.ASSIGNEE)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    public LivraisonResponseDTO assignerLivreur(Long idLivraison, Long idLivreur) {
        log.info("Assignation du livreur ID: {} à la livraison ID: {}", idLivreur, idLivraison);

        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        if (livraison.getStatutLivraison() != StatutLivraison.ASSIGNEE) {
            throw new BusinessException("Cette livraison ne peut plus être assignée (statut: %s)".formatted(livraison.getStatutLivraison()));
        }

        Livreur livreur = livreurRepository.findById(idLivreur)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé"));

        if (!livreur.getDisponible() || livreur.getStatut() != StatutLivreur.ACTIF) {
            throw new BusinessException("Ce livreur n'est pas disponible");
        }

        livraison.setLivreur(livreur);
        Livraison updatedLivraison = livraisonRepository.save(livraison);

        notificationService.notifierAssignationLivreur(
                livraison.getCommande(),
                livreur
        );

        log.info("Livreur assigné avec succès");
        return convertToResponseDTO(updatedLivraison);
    }


    public LivraisonResponseDTO demarrerLivraison(Long idLivraison) {
        log.info("Démarrage de la livraison ID: {}", idLivraison);

        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        if (livraison.getLivreur() == null) {
            throw new BusinessException("Aucun livreur n'est assigné à cette livraison");
        }

        if (livraison.getStatutLivraison() != StatutLivraison.ASSIGNEE) {
            throw new BusinessException("Cette livraison ne peut pas être démarrée (statut: %s)".formatted(livraison.getStatutLivraison()));
        }

        livraison.setStatutLivraison(StatutLivraison.EN_COURS);
        livraison.setHeureDepart(LocalDateTime.now());

        // Changer le statut du livreur
        Livreur livreur = livraison.getLivreur();
        livreur.setStatut(StatutLivreur.EN_LIVRAISON);
        livreurRepository.save(livreur);

        Livraison updatedLivraison = livraisonRepository.save(livraison);

        // Mettre à jour le statut de la commande
        Commande commande = livraison.getCommande();
        commande.setStatut(StatutCommande.EN_PREPARATION);
        commandeRepository.save(commande);

        // Notifier le client
        notificationService.envoyerNotification(
                commande,
                TypeNotification.EN_LIVRAISON,
                "Votre commande #%d est en cours de livraison!".formatted(commande.getIdCommande())
        );

        log.info("Livraison démarrée avec succès");
        return convertToResponseDTO(updatedLivraison);
    }



    public LivraisonResponseDTO terminerLivraison(Long idLivraison) {
        log.info("Finalisation de la livraison ID: {}", idLivraison);

        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        if (livraison.getStatutLivraison() != StatutLivraison.EN_COURS) {
            throw new BusinessException("Cette livraison n'est pas en cours");
        }

        livraison.setStatutLivraison(StatutLivraison.LIVREE);
        livraison.setHeureArrivee(LocalDateTime.now());

        // Changer le statut du livreur
        Livreur livreur = livraison.getLivreur();
        livreur.setStatut(StatutLivreur.ACTIF);
        livreurRepository.save(livreur);

        Livraison updatedLivraison = livraisonRepository.save(livraison);

        // Mettre à jour le statut de la commande
        Commande commande = livraison.getCommande();
        commande.setStatut(StatutCommande.LIVREE);
        commandeRepository.save(commande);

        // Notifier le client
        notificationService.envoyerNotification(
                commande,
                TypeNotification.LIVREE,
                "Votre commande #%d a été livrée. Bon appétit!".formatted(commande.getIdCommande())
        );

        log.info("Livraison terminée avec succès");
        return convertToResponseDTO(updatedLivraison);
    }


    public LivraisonResponseDTO annulerLivraison(Long idLivraison, String motif) {
        log.info("Annulation de la livraison ID: {}", idLivraison);

        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        if (livraison.getStatutLivraison() == StatutLivraison.LIVREE) {
            throw new BusinessException("Impossible d'annuler une livraison déjà effectuée");
        }

        livraison.setStatutLivraison(StatutLivraison.ANNULEE);

        // Si la livraison était en cours, libérer le livreur
        if (livraison.getLivreur() != null) {
            Livreur livreur = livraison.getLivreur();
            if (livreur.getStatut() == StatutLivreur.EN_LIVRAISON) {
                livreur.setStatut(StatutLivreur.ACTIF);
                livreurRepository.save(livreur);
            }
        }

        Livraison updatedLivraison = livraisonRepository.save(livraison);

        // Mettre à jour la commande
        Commande commande = livraison.getCommande();
        commande.setStatut(StatutCommande.ANNULEE);
        commandeRepository.save(commande);

        // Notifier le client
        notificationService.envoyerNotification(
                commande,
                TypeNotification.COMMANDE_RECUE,
                "Votre livraison a été annulée. Motif: %s".formatted(motif)
        );

        log.info("Livraison annulée avec succès");
        return convertToResponseDTO(updatedLivraison);
    }


    public LivraisonResponseDTO updateAdresseLivraison(Long idLivraison, String nouvelleAdresse) {
        log.info("Mise à jour de l'adresse de livraison ID: {}", idLivraison);

        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        if (livraison.getStatutLivraison() == StatutLivraison.EN_COURS
                || livraison.getStatutLivraison() == StatutLivraison.LIVREE) {
            throw new BusinessException("Impossible de modifier l'adresse d'une livraison en cours ou livrée");
        }

        livraison.setAdresseLivraison(nouvelleAdresse);
        Livraison updatedLivraison = livraisonRepository.save(livraison);

        log.info("Adresse de livraison mise à jour avec succès");
        return convertToResponseDTO(updatedLivraison);
    }


    public LivraisonResponseDTO reassignerLivreur(Long idLivraison, Long idNouveauLivreur) {
        log.info("Réassignation de la livraison ID: {} au livreur ID: {}", idLivraison, idNouveauLivreur);

        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        if (livraison.getStatutLivraison() == StatutLivraison.LIVREE) {
            throw new BusinessException("Impossible de réassigner une livraison déjà effectuée");
        }

        // Libérer l'ancien livreur
        if (livraison.getLivreur() != null) {
            Livreur ancienLivreur = livraison.getLivreur();
            if (ancienLivreur.getStatut() == StatutLivreur.EN_LIVRAISON) {
                ancienLivreur.setStatut(StatutLivreur.ACTIF);
                livreurRepository.save(ancienLivreur);
            }
        }

        // Assigner le nouveau livreur
        Livreur nouveauLivreur = livreurRepository.findById(idNouveauLivreur)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé"));

        if (!nouveauLivreur.getDisponible() || nouveauLivreur.getStatut() != StatutLivreur.ACTIF) {
            throw new BusinessException("Ce livreur n'est pas disponible");
        }

        livraison.setLivreur(nouveauLivreur);

        // Si la livraison était en cours, remettre le statut à ASSIGNEE
        if (livraison.getStatutLivraison() == StatutLivraison.EN_COURS) {
            livraison.setStatutLivraison(StatutLivraison.ASSIGNEE);
            livraison.setHeureDepart(null);
        }

        Livraison updatedLivraison = livraisonRepository.save(livraison);

        log.info("Livraison réassignée avec succès");
        return convertToResponseDTO(updatedLivraison);
    }



    public void signalerRetard(Long idLivraison, int minutesRetard) {
        log.info("Signalement d'un retard de {} minutes pour la livraison ID: {}", minutesRetard, idLivraison);

        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        // Notifier le client
        notificationService.notifierRetardLivraison(livraison.getCommande(), minutesRetard);

        log.info("Notification de retard envoyée");
    }


    @Transactional(readOnly = true)
    public LivraisonStatisticsDTO getStatistiques(LocalDateTime debut, LocalDateTime fin) {
        log.info("Calcul des statistiques des livraisons entre {} et {}", debut, fin);

        List<Livraison> livraisons = livraisonRepository.findByCommandeDateCommandeBetween(debut, fin);

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

        BigDecimal fraisTotaux = livraisons.stream()
                .filter(l -> l.getStatutLivraison() == StatutLivraison.LIVREE)
                .map(Livraison::getFraisLivraison)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return LivraisonStatisticsDTO.builder()
                .totalLivraisons(totalLivraisons)
                .livraisonsEffectuees(livraisonsEffectuees)
                .livraisonsEnCours(livraisonsEnCours)
                .livraisonsAnnulees(livraisonsAnnulees)
                .tempsLivraisonMoyen(tempsLivraisonMoyen)
                .fraisTotaux(fraisTotaux)
                .dateDebut(debut)
                .dateFin(fin)
                .build();
    }


    @Transactional(readOnly = true)
    public int calculerTempsEstime(String adresse) {
        log.info("Calcul du temps estimé de livraison pour l'adresse: {}", adresse);

        // Logique simplifiée - à améliorer avec une API de géolocalisation
        // Pour l'instant, le temps est basé sur l'historique
        List<Livraison> livraisonsHistorique = livraisonRepository.findByStatutLivraison(StatutLivraison.LIVREE);

        if (livraisonsHistorique.isEmpty()) {
            return 30; // 30 minutes par défaut
        }

        double moyenne = livraisonsHistorique.stream()
                .filter(l -> l.getHeureDepart() != null && l.getHeureArrivee() != null)
                .mapToLong(l -> java.time.Duration.between(l.getHeureDepart(), l.getHeureArrivee()).toMinutes())
                .average()
                .orElse(30.0);

        return (int) Math.ceil(moyenne);
    }


    @Transactional(readOnly = true)
    public List<LivraisonResponseDTO> getLivraisonsDuJour() {
        log.info("Récupération des livraisons du jour");

        LocalDateTime debutJour = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finJour = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        return livraisonRepository.findByCommandeDateCommandeBetween(debutJour, finJour)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public void deleteLivraison(Long id) {
        log.info("Suppression de la livraison ID: {}", id);

        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        if (livraison.getStatutLivraison() == StatutLivraison.EN_COURS) {
            throw new BusinessException("Impossible de supprimer une livraison en cours");
        }

        livraisonRepository.delete(livraison);
        log.info("Livraison supprimée avec succès");
    }

// ==================== Méthodes Privées ====================


    private LivraisonResponseDTO convertToResponseDTO(Livraison livraison) {
        return LivraisonResponseDTO.builder()
                .idLivraison(livraison.getIdLivraison())
                .idCommande(livraison.getCommande().getIdCommande())
                .livreur(livraison.getLivreur() != null ? convertLivreurToSimpleDTO(livraison.getLivreur()) : null)
                .adresseLivraison(livraison.getAdresseLivraison())
                .statutLivraison(livraison.getStatutLivraison())
                .heureDepart(livraison.getHeureDepart())
                .heureArrivee(livraison.getHeureArrivee())
                .fraisLivraison(livraison.getFraisLivraison())
                .build();
    }

    private LivreurSimpleDTO convertLivreurToSimpleDTO(Livreur livreur) {
        return LivreurSimpleDTO.builder()
                .idLivreur(livreur.getIdLivreur())
                .nom(livreur.getUser().getNom())
                .prenom(livreur.getUser().getPrenom())
                .telephone(livreur.getUser().getTelephone())
                .vehicule(livreur.getVehicule())
                .build();
    }
}
