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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepo notificationRepository;
    private final CommandeRepo commandeRepository;
    private final ClientRepo clientRepository;
    private final ManagerRepo managerRepository;
    private final JavaMailSender mailSender;

    private static final String FROM_EMAIL = "noreply@restaurant.com";
    private static final String RESTAURANT_NAME = "Restaurant RSI";



    public NotificationResponseDTO envoyerNotification(Commande commande,
                                                       TypeNotification type,
                                                       String message) {
        log.info("Envoi de notification pour la commande ID: {} - Type: {}",
                commande.getIdCommande(), type);

        Notification notification = Notification.builder()
                .commande(commande)
                .message(message)
                .dateEnvoi(LocalDateTime.now())
                .type(type)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        if (commande.getClient() != null && commande.getClient().getUser() != null) {
            String email = commande.getClient().getUser().getEmail();
            envoyerEmailNotification(email, type, message, commande);
        }


        log.info("Notification envoyée avec succès - ID: {}", savedNotification.getIdNotification());
        return convertToResponseDTO(savedNotification);
    }


    @Async
    public CompletableFuture<NotificationResponseDTO> creerNotification(NotificationRequestDTO request) {
        log.info("Création d'une notification personnalisée pour la commande ID: {}",
                request.getIdCommande());

        Commande commande = commandeRepository.findById(request.getIdCommande())
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée"));

        return CompletableFuture.completedFuture(envoyerNotification(commande, request.getType(), request.getMessage()));
    }


    @Transactional(readOnly = true)
    public NotificationResponseDTO getNotificationById(Long id) {
        log.info("Récupération de la notification ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée"));
        return convertToResponseDTO(notification);
    }


    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getAllNotifications(Pageable pageable) {
        log.info("Récupération de toutes les notifications - Page: {}", pageable.getPageNumber());
        return notificationRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }


    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotificationsByCommande(Long idCommande) {
        log.info("Récupération des notifications de la commande ID: {}", idCommande);
        return notificationRepository.findByCommandeIdCommandeOrderByDateEnvoiDesc(idCommande)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotificationsByClient(Long idClient) {
        log.info("Récupération des notifications du client ID: {}", idClient);

        Client client = clientRepository.findById(idClient)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));

        List<Commande> commandes = commandeRepository.findByClientIdClientOrderByDateCommandeDesc(idClient);

        return commandes.stream()
                .flatMap(commande -> commande.getNotifications().stream())
                .sorted((n1, n2) -> n2.getDateEnvoi().compareTo(n1.getDateEnvoi()))
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotificationsByType(TypeNotification type) {
        log.info("Récupération des notifications de type: {}", type);
        return notificationRepository.findByType(type)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotificationsRecentes() {
        log.info("Récupération des notifications récentes");
        LocalDateTime il24h = LocalDateTime.now().minusHours(24);
        return notificationRepository.findByDateEnvoiAfterOrderByDateEnvoiDesc(il24h)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotificationsByPeriode(LocalDateTime debut,
                                                                   LocalDateTime fin) {
        log.info("Récupération des notifications entre {} et {}", debut, fin);
        return notificationRepository.findByDateEnvoiBetween(debut, fin)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Async
    public NotificationResponseDTO renvoyerNotification(Long idNotification) {
        log.info("Renvoi de la notification ID: {}", idNotification);

        Notification notification = notificationRepository.findById(idNotification)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée"));

        // Créer une nouvelle notification avec le même contenu
        return envoyerNotification(
                notification.getCommande(),
                notification.getType(),
                notification.getMessage() + " [RENVOI]"
        );
    }


    @Async
    public void notifierChangementStatut(Commande commande, StatutCommande ancienStatut,
                                         StatutCommande nouveauStatut) {
        log.info("Notification de changement de statut pour commande ID: {} - {} -> {}",
                commande.getIdCommande(), ancienStatut, nouveauStatut);

        String message = genererMessageChangementStatut(commande, nouveauStatut);
        TypeNotification type = mapperStatutToTypeNotification(nouveauStatut);

        if (type != null) {
            envoyerNotification(commande, type, message);
        }
    }


    @Async
    public void notifierAssignationLivreur(Commande commande, Livreur livreur) {
        log.info("Notification d'assignation de livreur pour commande ID: {}",
                commande.getIdCommande());

        String message = String.format(
                "Votre commande #%d a été assignée au livreur %s %s. " +
                        "Vous serez livré sous peu!",
                commande.getIdCommande(),
                livreur.getUser().getPrenom(),
                livreur.getUser().getNom()
        );

        envoyerNotification(commande, TypeNotification.EN_LIVRAISON, message);
    }


    @Async
    public void notifierRetardLivraison(Commande commande, int minutesRetard) {
        log.info("Notification de retard pour commande ID: {}", commande.getIdCommande());

        String message = String.format(
                "Nous nous excusons pour le retard. Votre commande #%d sera livrée " +
                        "avec environ %d minutes de retard. Merci de votre patience!",
                commande.getIdCommande(),
                minutesRetard
        );

        envoyerNotification(commande, TypeNotification.EN_LIVRAISON, message);
    }


    @Async
    public void notifierManagers(String sujet, String message) {
        log.info("Notification envoyée à tous les managers - Sujet: {}", sujet);

        List<Manager> managers = managerRepository.findByActifTrue();

        for (Manager manager : managers) {
            String email = manager.getUser().getEmail();
            envoyerEmailSimple(email, sujet, message);
        }
    }


    @Async
    public void notifierManager(Long idManager, String sujet, String message) {
        log.info("Notification envoyée au manager ID: {} - Sujet: {}", idManager, sujet);

        Manager manager = managerRepository.findById(idManager)
                .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé"));

        String email = manager.getUser().getEmail();
        envoyerEmailSimple(email, sujet, message);
    }


    @Async
    public void envoyerNotificationBienvenue(Client client) {
        log.info("Envoi de notification de bienvenue au client ID: {}", client.getIdClient());

        String email = client.getUser().getEmail();
        String sujet = "Bienvenue chez " + RESTAURANT_NAME + " !";
        String message = String.format(
                "Bonjour %s %s,\n\n" +
                        "Bienvenue chez %s!\n\n" +
                        "Nous sommes ravis de vous compter parmi nos clients. " +
                        "Profitez de nos délicieux plats et de notre service de qualité.\n\n" +
                        "Vous avez reçu %d points de fidélité pour votre inscription.\n\n" +
                        "À très bientôt!\n" +
                        "L'équipe %s",
                client.getUser().getPrenom(),
                client.getUser().getNom(),
                RESTAURANT_NAME,
                client.getPointsFidelite(),
                RESTAURANT_NAME
        );

        envoyerEmailSimple(email, sujet, message);
    }


    @Async
    public void notifierPointsFidelite(Client client, int pointsGagnes, int totalPoints) {
        log.info("Notification de points de fidélité pour client ID: {}", client.getIdClient());

        String email = client.getUser().getEmail();
        String sujet = "Points de fidélité gagnés !";
        String message = String.format(
                "Bonjour %s,\n\n" +
                        "Vous avez gagné %d points de fidélité!\n" +
                        "Votre solde actuel : %d points\n\n" +
                        "Statut : %s\n\n" +
                        "Continuez à commander pour gagner encore plus de points!\n\n" +
                        "Cordialement,\n" +
                        "L'équipe %s",
                client.getUser().getPrenom(),
                pointsGagnes,
                totalPoints,
                client.getStatut().name(),
                RESTAURANT_NAME
        );

        envoyerEmailSimple(email, sujet, message);
    }


    @Async
    public void envoyerNotificationPromotion(String emailClient, String nomPromotion,
                                             String description) {
        log.info("Envoi de notification de promotion à: {}", emailClient);

        String sujet = "Nouvelle promotion chez " + RESTAURANT_NAME + " !";
        String message = String.format(
                "🎉 Nouvelle Promotion ! 🎉\n\n" +
                        "%s\n\n" +
                        "%s\n\n" +
                        "Ne manquez pas cette offre exceptionnelle!\n\n" +
                        "Commandez dès maintenant!\n" +
                        "L'équipe %s",
                nomPromotion,
                description,
                RESTAURANT_NAME
        );

        envoyerEmailSimple(emailClient, sujet, message);
    }


//    @Async
//    public void envoyerRappelPanierAbandon(String emailClient, String nomClient) {
//        log.info("Envoi de rappel de panier abandonné à: {}", emailClient);
//
//        String sujet = "Votre commande vous attend !";
//        String message = String.format(
//                "Bonjour %s,\n\n" +
//                        "Nous avons remarqué que vous n'avez pas finalisé votre commande.\n\n" +
//                        "Vos plats préférés vous attendent!\n" +
//                        "Profitez de 10%% de réduction avec le code: COMEBACK10\n\n" +
//                        "Valable pendant 48h.\n\n" +
//                        "À très bientôt,\n" +
//                        "L'équipe %s",
//                nomClient,
//                RESTAURANT_NAME
//        );
//
//        envoyerEmailSimple(emailClient, sujet, message);
//    }




    @Transactional
    public void nettoyerAnciennesNotifications() {
        log.info("Nettoyage des anciennes notifications");

        LocalDateTime dateLimit = LocalDateTime.now().minusMonths(6);
        List<Notification> anciennesNotifications = notificationRepository
                .findByDateEnvoiBefore(dateLimit);

        notificationRepository.deleteAll(anciennesNotifications);

        log.info("{} notifications supprimées", anciennesNotifications.size());
    }

    // ==================== Méthodes Privées ====================


    @Async
    protected void envoyerEmailNotification(String email, TypeNotification type,
                                            String message, Commande commande) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject(genererSujetEmail(type, commande));
            mailMessage.setText(genererCorpsEmail(message, commande));

            mailSender.send(mailMessage);
            log.info("Email envoyé avec succès à: {}", email);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", email, e.getMessage());
        }
    }


    @Async
    protected void envoyerEmailSimple(String email, String sujet, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject(sujet);
            mailMessage.setText(message);

            mailSender.send(mailMessage);
            log.info("Email simple envoyé avec succès à: {}", email);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", email, e.getMessage());
        }


    }

    /**
     * Envoyer un SMS (placeholder pour intégration future avec Twilio, etc.)
     */
    @Async
    protected void envoyerSMSNotification(String telephone, String message) {
        // TODO: ach ban likome nrdiroha
        log.info("SMS prévu pour {}: {}", telephone, message);

    }

    private String genererSujetEmail(TypeNotification type, Commande commande) {
        return switch (type) {
            case COMMANDE_RECUE -> MessageFormat.format("Commande #{0} reçue - {1}", commande.getIdCommande(), RESTAURANT_NAME);
            case EN_PREPARATION -> MessageFormat.format("Commande #{0} en préparation", commande.getIdCommande());
            case PRETE -> MessageFormat.format("Commande #{0} prête !", commande.getIdCommande());
            case EN_LIVRAISON -> MessageFormat.format("Commande #{0} en cours de livraison", commande.getIdCommande());
            case LIVREE -> MessageFormat.format("Commande #{0} livrée - Bon appétit !", commande.getIdCommande());
        };
    }


    private String genererCorpsEmail(String message, Commande commande) {
        Client client = commande.getClient();
        String nomClient = MessageFormat.format("{0} {1}", client.getUser().getPrenom(), client.getUser().getNom());

        return String.format(
                "Bonjour %s,\n\n" +
                        "%s\n\n" +
                        "Détails de votre commande:\n" +
                        "- Numéro: #%d\n" +
                        "- Type: %s\n" +
                        "- Montant: %.2f DH\n" +
                        "- Date: %s\n\n" +
                        "Merci de votre confiance!\n\n" +
                        "Cordialement,\n" +
                        "L'équipe %s\n\n" +
                        "---\n" +
                        "Pour toute question, contactez-nous au: +212 XXX XXX XXX",
                nomClient,
                message,
                commande.getIdCommande(),
                commande.getTypeCommande().name(),
                commande.getMontantTotal(),
                commande.getDateCommande(),
                RESTAURANT_NAME
        );
    }


    private String genererMessageChangementStatut(Commande commande, StatutCommande statut) {
        return switch (statut) {
            case EN_ATTENTE -> MessageFormat.format("Votre commande #{0} a été reçue et sera traitée dans les plus brefs délais.", commande.getIdCommande());
            case EN_PREPARATION -> MessageFormat.format("Votre commande #{0} est en cours de préparation par nos chefs!", commande.getIdCommande());
            case PRETE -> MessageFormat.format("Votre commande #{0} est prête! {1}", commande.getIdCommande(), commande.getTypeCommande() == TypeCommande.SUR_PLACE ?
                    "Vous pouvez venir la chercher." :
                    "Elle sera bientôt livrée.");
            case SERVIE -> MessageFormat.format("Votre commande #{0} a été servie. Bon appétit!", commande.getIdCommande());
            case LIVREE -> MessageFormat.format("Votre commande #{0} a été livrée. Bon appétit!", commande.getIdCommande());
            case ANNULEE -> MessageFormat.format("Votre commande #{0} a été annulée.", commande.getIdCommande());
        };
    }


    private TypeNotification mapperStatutToTypeNotification(StatutCommande statut) {
        return switch (statut) {
            case EN_ATTENTE -> TypeNotification.COMMANDE_RECUE;
            case EN_PREPARATION -> TypeNotification.EN_PREPARATION;
            case PRETE -> TypeNotification.PRETE;
            case SERVIE, LIVREE -> TypeNotification.LIVREE;
            case ANNULEE -> null;
        };
    }


    private NotificationResponseDTO convertToResponseDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .idNotification(notification.getIdNotification())
                .idCommande(notification.getCommande().getIdCommande())
                .message(notification.getMessage())
                .dateEnvoi(notification.getDateEnvoi())
                .type(notification.getType())
                .build();
    }
}
