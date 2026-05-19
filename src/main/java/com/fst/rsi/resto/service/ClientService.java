package com.fst.rsi.resto.service;


import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.entity.*;
import com.fst.rsi.resto.entity.enums.StatutClient;
import com.fst.rsi.resto.entity.enums.StatutCommande;
import com.fst.rsi.resto.entity.enums.UserRole;
import com.fst.rsi.resto.exception.*;
import com.fst.rsi.resto.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientService {

    private final ClientRepo clientRepository;
    private final UserRepo userRepository;
    private final CommandeRepo commandeRepository;
    private final PasswordEncoder passwordEncoder;


    public ClientResponseDTO createClient(ClientRequestDTO request) {
        log.info("Création d'un nouveau client: {}", request.getEmail());

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
                .enabled(true)
                .emailVerified(false)
                .build();

        user.addRole(UserRole.CLIENT);

        User savedUser = userRepository.save(user);

        Client client = Client.builder()
                .user(savedUser)
                .actif(true)
                .pointsFidelite(0)
                .statut(StatutClient.BRONZE)
                .preferences(request.getPreferences())
                .build();

        Client savedClient = clientRepository.save(client);

        log.info("Client créé avec succès - ID: {}", savedClient.getIdClient());
        return convertToResponseDTO(savedClient);
    }


    @Transactional(readOnly = true)
    public ClientResponseDTO getClientById(Long id) {
        log.info("Récupération du client ID: {}", id);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));
        return convertToResponseDTO(client);
    }


    @Transactional(readOnly = true)
    public ClientResponseDTO getClientByEmail(String email) {
        log.info("Récupération du client par email: {}", email);
        Client client = clientRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'email: " + email));
        return convertToResponseDTO(client);
    }


    @Transactional(readOnly = true)
    public ClientResponseDTO getClientByTelephone(String telephone) {
        log.info("Récupération du client par téléphone: {}", telephone);
        Client client = clientRepository.findByUserTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec le téléphone: " + telephone));
        return convertToResponseDTO(client);
    }


    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> getAllClients(Pageable pageable) {
        log.info("Récupération de tous les clients - Page: {}", pageable.getPageNumber());
        return clientRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getClientsActifs() {
        log.info("Récupération des clients actifs");
        return clientRepository.findByActifTrue()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getClientsByStatut(StatutClient statut) {
        log.info("Récupération des clients avec le statut: {}", statut);
        return clientRepository.findByStatut(statut)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ClientResponseDTO> searchClients(String searchTerm) {
        log.info("Recherche de clients avec le terme: {}", searchTerm);
        return clientRepository.searchByNomOrPrenom(searchTerm)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    public ClientResponseDTO updateClient(Long id, ClientUpdateDTO updateDTO) {
        log.info("Mise à jour du client ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        User user = client.getUser();

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

        if (updateDTO.getPreferences() != null) {
            client.setPreferences(updateDTO.getPreferences());
        }

        userRepository.save(user);
        Client updatedClient = clientRepository.save(client);

        log.info("Client mis à jour avec succès");
        return convertToResponseDTO(updatedClient);
    }


    public void changePassword(Long id, ChangePasswordDTO passwordDTO) {
        log.info("Changement de mot de passe pour le client ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        User user = client.getUser();

        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("L'ancien mot de passe est incorrect");
        }

        if (passwordEncoder.matches(passwordDTO.getNewPassword(), user.getPassword())) {
            throw new BusinessException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);

        log.info("Mot de passe changé avec succès pour le client ID: {}", id);
    }


    public ClientResponseDTO toggleClientStatus(Long id) {
        log.info("Changement du statut du client ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        client.setActif(!client.getActif());
        client.getUser().setEnabled(client.getActif());

        userRepository.save(client.getUser());
        Client updatedClient = clientRepository.save(client);

        log.info("Statut du client changé: {}", client.getActif() ? "Actif" : "Inactif");
        return convertToResponseDTO(updatedClient);
    }


    public void deactivateClient(Long id) {
        log.info("Désactivation du client ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        client.setActif(false);
        client.getUser().setEnabled(false);

        userRepository.save(client.getUser());
        clientRepository.save(client);

        log.info("Client désactivé avec succès");
    }


    public void deleteClient(Long id) {
        log.info("Suppression du client ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        List<Commande> commandesActives = commandeRepository.findByClientIdClientOrderByDateCommandeDesc(id)
                .stream()
                .filter(c -> c.getStatut() != StatutCommande.SERVIE
                        && c.getStatut() != StatutCommande.LIVREE
                        && c.getStatut() != StatutCommande.ANNULEE)
                .toList();

        if (!commandesActives.isEmpty()) {
            throw new BusinessException("Impossible de supprimer le client. Il a " +
                    commandesActives.size() + " commande(s) active(s)");
        }

        client.setActif(false);
        client.getUser().setEnabled(false);
        clientRepository.save(client);

        log.info("Client supprimé (désactivé) avec succès");
    }


    public ClientResponseDTO ajouterPointsFidelite(Long id, Integer points) {
        log.info("Ajout de {} points de fidélité au client ID: {}", points, id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        if (points < 0) {
            throw new BusinessException("Le nombre de points doit être positif");
        }

        client.setPointsFidelite(client.getPointsFidelite() + points);

        mettreAJourStatutFidelite(client);

        Client updatedClient = clientRepository.save(client);
        log.info("Points de fidélité ajoutés. Nouveau total: {}", updatedClient.getPointsFidelite());

        return convertToResponseDTO(updatedClient);
    }


    public ClientResponseDTO utiliserPointsFidelite(Long id, Integer points) {
        log.info("Utilisation de {} points de fidélité pour le client ID: {}", points, id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        if (points < 0) {
            throw new BusinessException("Le nombre de points doit être positif");
        }

        if (client.getPointsFidelite() < points) {
            throw new BusinessException("Points de fidélité insuffisants. Disponible: " +
                    client.getPointsFidelite() + ", demandé: " + points);
        }

        client.setPointsFidelite(client.getPointsFidelite() - points);

        mettreAJourStatutFidelite(client);

        Client updatedClient = clientRepository.save(client);
        log.info("Points de fidélité utilisés. Nouveau solde: {}", updatedClient.getPointsFidelite());

        return convertToResponseDTO(updatedClient);
    }

    @Transactional(readOnly = true)
    public List<CommandeHistoriqueDTO> getHistoriqueCommandes(Long id) {
        log.info("Récupération de l'historique des commandes du client ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        return commandeRepository.findByClientIdClientOrderByDateCommandeDesc(id)
                .stream()
                .map(this::convertToHistoriqueDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientStatisticsDTO getClientStatistics(Long id) {
        log.info("Calcul des statistiques pour le client ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        List<Commande> commandes = commandeRepository.findByClientIdClientOrderByDateCommandeDesc(id);

        long totalCommandes = commandes.size();
        long commandesTerminees = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.SERVIE || c.getStatut() == StatutCommande.LIVREE)
                .count();
        long commandesAnnulees = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.ANNULEE)
                .count();

        java.math.BigDecimal depenseTotal = commandes.stream()
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE)
                .map(Commande::getMontantTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal panierMoyen = totalCommandes > 0
                ? depenseTotal.divide(java.math.BigDecimal.valueOf(commandesTerminees), 2, RoundingMode.HALF_UP)
                : java.math.BigDecimal.ZERO;

        Commande derniereCommande = commandes.isEmpty() ? null : commandes.get(0);

        String platPrefere = commandes.stream()
                .flatMap(c -> c.getLignesCommande().stream())
                .collect(Collectors.groupingBy(
                        lc -> lc.getPlat().getNom(),
                        Collectors.summingInt(LigneCommande::getQuantite)
                ))
                .entrySet().stream()
                .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                .map(Map.Entry::getKey)
                .orElse("Aucun");

        return ClientStatisticsDTO.builder()
                .totalCommandes(totalCommandes)
                .commandesTerminees(commandesTerminees)
                .commandesAnnulees(commandesAnnulees)
                .depenseTotal(depenseTotal)
                .panierMoyen(panierMoyen)
                .pointsFidelite(client.getPointsFidelite())
                .statutFidelite(client.getStatut())
                .dateInscription(LocalDate.from(client.getUser().getCreationDate()))
                .dateDerniereCommande(derniereCommande != null ? derniereCommande.getDateCommande() : null)
                .platPrefere(platPrefere)
                .build();
    }


    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getClientsVIP() {
        log.info("Récupération des clients VIP");
        return clientRepository.findByStatutIn(List.of(StatutClient.GOLD, StatutClient.PLATINUM))
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getNouveauxClients() {
        log.info("Récupération des nouveaux clients");
        LocalDate dateDebut = LocalDate.now().minusDays(30);
        return clientRepository.findByUserCreationDateAfter(dateDebut)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getClientsInactifs(int joursInactivite) {
        log.info("Récupération des clients inactifs depuis {} jours", joursInactivite);

        List<Client> tousCLients = clientRepository.findByActifTrue();
        LocalDateTime dateLimit = LocalDateTime.now().minusDays(joursInactivite);

        return tousCLients.stream()
                .filter(client -> {
                    List<Commande> commandes = commandeRepository.findByClientIdClientOrderByDateCommandeDesc(client.getIdClient());
                    if (commandes.isEmpty()) return true;
                    return commandes.get(0).getDateCommande().isBefore(dateLimit);
                })
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== Méthodes Privées ====================


    private void mettreAJourStatutFidelite(Client client) {
        int points = client.getPointsFidelite();

        StatutClient nouveauStatut;
        if (points >= 1000) {
            nouveauStatut = StatutClient.PLATINUM;
        } else if (points >= 500) {
            nouveauStatut = StatutClient.GOLD;
        } else if (points >= 100) {
            nouveauStatut = StatutClient.SILVER;
        } else {
            nouveauStatut = StatutClient.BRONZE;
        }

        if (client.getStatut() != nouveauStatut) {
            log.info("Changement de statut pour le client ID {}: {} -> {}",
                    client.getIdClient(), client.getStatut(), nouveauStatut);
            client.setStatut(nouveauStatut);
        }
    }


    private ClientResponseDTO convertToResponseDTO(Client client) {
        User user = client.getUser();

        return ClientResponseDTO.builder()
                .idClient(client.getIdClient())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .adresse(user.getAdresse())
                .photo(user.getPhoto())
                .dateInscription(LocalDate.from(client.getUser().getCreationDate()))
                .actif(client.getActif())
                .pointsFidelite(client.getPointsFidelite())
                .statut(client.getStatut())
                .preferences(client.getPreferences())
                .emailVerified(user.isEmailVerified())
                .build();
    }


    private CommandeHistoriqueDTO convertToHistoriqueDTO(Commande commande) {
        return CommandeHistoriqueDTO.builder()
                .idCommande(commande.getIdCommande())
                .dateCommande(commande.getDateCommande())
                .typeCommande(commande.getTypeCommande())
                .statut(commande.getStatut())
                .montantTotal(commande.getMontantTotal())
                .nombrePlats(commande.getLignesCommande().size())
                .build();
    }
}