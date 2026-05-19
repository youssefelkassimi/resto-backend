package com.fst.rsi.resto.service;


import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.entity.*;
import com.fst.rsi.resto.entity.enums.StatutCommande;
import com.fst.rsi.resto.entity.enums.StatutTable;
import com.fst.rsi.resto.entity.enums.TypeCommande;
import com.fst.rsi.resto.entity.enums.TypeNotification;
import com.fst.rsi.resto.exception.BusinessException;
import com.fst.rsi.resto.exception.InsufficientStockException;
import com.fst.rsi.resto.exception.ResourceNotFoundException;
import com.fst.rsi.resto.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommandeService {
    private final CommandeRepo commandeRepository;
    private final ClientRepo clientRepository;
    private final PlatRepo platRepository;
    private final ServeurRepo serveurRepository;
    private final TableRestaurantRepo tableRepository;
    private final IngredientRepo ingredientRepository;
    private final NotificationService notificationService;
    private final FactureService factureService;
    private final CompositionPlatRepo compositionPlatRepo;

    public CommandeResponseDTO createCommande(CommandeRequestDTO request){
        log.info("Création d'une nouvelle commande pour le client ID: {}", request.getIdClient());

        Client client=null;
        if (request.getTypeCommande().equals(TypeCommande.SUR_PLACE)){
             client = clientRepository.findById(request.getIdClient())
                    .orElse(null);

            if (client !=null && !client.getActif()) {
                throw new BusinessException("Le compte client est inactif");
            }
        }else {
            client = clientRepository.findById(request.getIdClient())
                    .orElseThrow(()->new BusinessException("Le compte client n'existe pas"));
        }

        Commande commande = new Commande();
        commande.setClient( client);
        commande.setTypeCommande(request.getTypeCommande());
        commande.setInstructionsSpeciales(request.getInstructionsSpeciales());
        commande.setStatut(StatutCommande.EN_ATTENTE);
        commande.setDateCommande(LocalDateTime.now());

        if(request.getTypeCommande()== TypeCommande.SUR_PLACE&&request.getIdServeur() !=null){
            Serveur serveur = serveurRepository.findById(request.getIdServeur())
                    .orElseThrow(() -> new ResourceNotFoundException("Serveur non trouvé"));
            commande.setServeur(serveur);
        }

        if(request.getIdTable() !=null){
            TableRestaurant table = tableRepository.findById(request.getIdTable())
                    .orElseThrow(() -> new ResourceNotFoundException("Table non trouvée"));

            if (table.getStatut() != StatutTable.LIBRE) {
                throw new BusinessException("La table n°" + table.getNumero() + " n'est pas disponible");
            }
            commande.setTable(table);
            table.setStatut(StatutTable.OCCUPEE);
            tableRepository.save(table);
        }

        List<LigneCommande> ligneCommandes = request.getLignesCommande().stream()
                .map(ligneDTO -> createLigneCommande(ligneDTO,commande))
                .toList();

        commande.setLignesCommande(ligneCommandes);

        BigDecimal montantTotal = calculerMontantTotal(ligneCommandes);

        if (request.getRemise()!=null && request.getRemise().compareTo(BigDecimal.ZERO) > 0){
            if (request.getRemise().compareTo(montantTotal) > 0) {
                throw new BusinessException("La remise ne peut pas être supérieure au montant total");
            }
            commande.setRemise(request.getRemise());
            montantTotal = montantTotal.subtract(request.getRemise());
        }

        commande.setMontantTotal(montantTotal);

        int tempsPreparation = calculerTempsPreparation(ligneCommandes);
        commande.setTempsEstimePreparation(tempsPreparation);

        mettreAJourStocks(ligneCommandes);
        Commande savedCommande = commandeRepository.save(commande);

        notificationService.envoyerNotification(
                savedCommande,
                TypeNotification.COMMANDE_RECUE,
                "Votre commande #" + savedCommande.getIdCommande() + " a été reçue et sera prête dans " + tempsPreparation + " minutes."
        );

        log.info("Commande créée avec succès - ID: {}", savedCommande.getIdCommande());
        return convertToResponseDTO(savedCommande);
    }

    @Transactional(readOnly = true)
    public CommandeResponseDTO getCommandeById(Long id){
        log.info("Récupération de la commande ID: {}", id);
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
        return convertToResponseDTO(commande);
    }


    @Transactional(readOnly = true)
    public Page<CommandeResponseDTO> getAllCommandes(Pageable pageable) {
        log.info("Récupération de toutes les commandes - Page: {}", pageable.getPageNumber());
        return commandeRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<CommandeResponseDTO> getCommandesByClient(Long clientId) {
        log.info("Récupération des commandes du client ID: {}", clientId);
        List<Commande> commandes = commandeRepository.findByClientIdClientOrderByDateCommandeDesc(clientId);
        return commandes.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommandeResponseDTO> getCommandesByStatut(StatutCommande statut) {
        log.info("Récupération des commandes avec le statut: {}", statut);
        List<Commande> commandes = commandeRepository.findByStatut(statut);
        return commandes.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommandeResponseDTO> getCommandesByType(TypeCommande type) {
        log.info("Récupération des commandes de type: {}", type);
        List<Commande> commandes = commandeRepository.findByTypeCommande(type);
        return commandes.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommandeResponseDTO> getCommandesDuJour() {
        log.info("Récupération des commandes du jour");
        LocalDateTime debutJour = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finJour = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        List<Commande> commandes = commandeRepository.findByDateCommandeBetween(debutJour, finJour);
        return commandes.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommandeResponseDTO> getCommandesEnAttente() {
        log.info("Récupération des commandes en attente");
        return getCommandesByStatut(StatutCommande.EN_ATTENTE);
    }

    public CommandeResponseDTO updateStatut(Long id, StatutCommande nouveauStatut){
        log.info("Mise à jour du statut de la commande ID: {} vers {}", id, nouveauStatut);

        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        StatutCommande ancienStatut = commande.getStatut();

        validerTransitionStatut(ancienStatut, nouveauStatut);

        commande.setStatut(nouveauStatut);
        Commande updatedCommande = commandeRepository.save(commande);

        String message = genererMessageNotification(commande, nouveauStatut);
        TypeNotification typeNotif = mapperStatutToTypeNotification(nouveauStatut);

        if (typeNotif != null) {
            notificationService.envoyerNotification(updatedCommande, typeNotif, message);
        }

        if (nouveauStatut == StatutCommande.SERVIE || nouveauStatut == StatutCommande.LIVREE) {
            factureService.genererFacture(updatedCommande);

            if (commande.getTable() != null && nouveauStatut == StatutCommande.SERVIE) {
                TableRestaurant table = commande.getTable();
                table.setStatut(StatutTable.LIBRE);
                tableRepository.save(table);
            }
        }

        log.info("Statut de la commande mis à jour avec succès");
        return convertToResponseDTO(updatedCommande);

    }

    public CommandeResponseDTO updateCommande(Long id, CommandeRequestDTO request){
        log.info("Modification de la commande ID: {}", id);

        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new BusinessException("La commande ne peut plus être modifiée (statut: " + commande.getStatut() + ")");
        }

        restaurerStocks(commande.getLignesCommande());

        commande.getLignesCommande().clear();

        List<LigneCommande> nouvellesLignes = request.getLignesCommande().stream()
                .map(ligneDTO -> createLigneCommande(ligneDTO, commande))
                .toList();

        commande.setLignesCommande(nouvellesLignes);
        commande.setInstructionsSpeciales(request.getInstructionsSpeciales());

        BigDecimal nouveauMontant = calculerMontantTotal(nouvellesLignes);
        if (request.getRemise() != null) {
            commande.setRemise(request.getRemise());
            nouveauMontant = nouveauMontant.subtract(request.getRemise());
        }
        commande.setMontantTotal(nouveauMontant);

        int nouveauTemps = calculerTempsPreparation(nouvellesLignes);
        commande.setTempsEstimePreparation(nouveauTemps);

        mettreAJourStocks(nouvellesLignes);

        Commande updatedCommande = commandeRepository.save(commande);
        log.info("Commande modifiée avec succès");
        return convertToResponseDTO(updatedCommande);

    }

    public CommandeResponseDTO annulerCommande(Long id, String motif){
        log.info("Annulation de la commande ID: {}", id);

        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        if (commande.getStatut() == StatutCommande.SERVIE || commande.getStatut() == StatutCommande.LIVREE) {
            throw new BusinessException("Impossible d'annuler une commande déjà servie ou livrée");
        }

        restaurerStocks(commande.getLignesCommande());

        if (commande.getTable() != null) {
            TableRestaurant table = commande.getTable();
            table.setStatut(StatutTable.LIBRE);
            tableRepository.save(table);
        }

        commande.setStatut(StatutCommande.ANNULEE);
        commande.setInstructionsSpeciales(
                (commande.getInstructionsSpeciales() != null ? commande.getInstructionsSpeciales() + " | " : "") +
                        "ANNULÉE - Motif: " + motif
        );

        Commande cancelledCommande = commandeRepository.save(commande);

        notificationService.envoyerNotification(
                cancelledCommande,
                TypeNotification.COMMANDE_RECUE,
                "Votre commande #" + cancelledCommande.getIdCommande() + " a été annulée. " + motif
        );

        log.info("Commande annulée avec succès");
        return convertToResponseDTO(cancelledCommande);

    }

    public void deleteCommande(Long id) {
        log.info("Suppression de la commande ID: {}", id);

        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        if (commande.getStatut() != StatutCommande.ANNULEE) {
            throw new BusinessException("Seules les commandes annulées peuvent être supprimées");
        }

        commandeRepository.delete(commande);
        log.info("Commande supprimée avec succès");
    }

    @Transactional(readOnly = true)
    public CommandeStatisticsDTO getStatistiques(LocalDateTime debut, LocalDateTime fin){
        log.info("Calcul des statistiques des commandes entre {} et {}", debut, fin);

        List<Commande> commandes = commandeRepository.findByDateCommandeBetween(debut, fin);

        long totalCommandes = commandes.size();

        long commandesAnnulees = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.ANNULEE)
                .count();

        BigDecimal chiffreAffaire = commandes.stream()
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE)
                .map(Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal panierMoyen = totalCommandes > 0
                ? chiffreAffaire.divide(BigDecimal.valueOf(totalCommandes - commandesAnnulees), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long commandesSurPlace = commandes.stream()
                .filter(c -> c.getTypeCommande() == TypeCommande.SUR_PLACE)
                .count();

        long commandesAEmporter = commandes.stream()
                .filter(c -> c.getTypeCommande() == TypeCommande.A_EMPORTER)
                .count();

        long commandesLivraison = commandes.stream()
                .filter(c -> c.getTypeCommande() == TypeCommande.LIVRAISON)
                .count();

        return CommandeStatisticsDTO.builder()
                .totalCommandes(totalCommandes)
                .commandesAnnulees(commandesAnnulees)
                .chiffreAffaire(chiffreAffaire)
                .panierMoyen(panierMoyen)
                .commandesSurPlace(commandesSurPlace)
                .commandesAEmporter(commandesAEmporter)
                .commandesLivraison(commandesLivraison)
                .dateDebut(debut)
                .dateFin(fin)
                .build();
    }


    @Transactional(readOnly = true)
    public boolean verifierDisponibiliteCommande(CommandeRequestDTO request) {
        for (LigneCommandeRequestDTO ligne : request.getLignesCommande()) {
            Plat plat = platRepository.findById(ligne.getIdPlat())
                    .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé"));

            if (!plat.getDisponible()) {
                return false;
            }

            // Vérifier le stock des ingrédients
            List<CompositionPlat> compositions = compositionPlatRepo.findByPlatId(plat.getIdPlat());
            for (CompositionPlat composition : compositions) {
                BigDecimal quantiteNecessaire = composition.getQuantiteNecessaire()
                        .multiply(BigDecimal.valueOf(ligne.getQuantite()));

                if (composition.getIngredient().getQuantiteStock().compareTo(quantiteNecessaire) < 0) {
                    return false;
                }
            }
        }
        return true;
    }


    @Transactional(readOnly = true)
    public MontantCommandeDTO calculerMontantCommande(CommandeRequestDTO request) {
        BigDecimal montantHT = BigDecimal.ZERO;

        for (LigneCommandeRequestDTO ligne : request.getLignesCommande()) {
            Plat plat = platRepository.findById(ligne.getIdPlat())
                    .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé"));

            BigDecimal sousTotal = plat.getPrix().multiply(BigDecimal.valueOf(ligne.getQuantite()));
            montantHT = montantHT.add(sousTotal);
        }

        BigDecimal montantTVA = montantHT.multiply(new BigDecimal("0.20")); // TVA 20%
        BigDecimal montantTTC = montantHT.add(montantTVA);
        BigDecimal remise = request.getRemise() != null ? request.getRemise() : BigDecimal.ZERO;
        BigDecimal montantFinal = montantTTC.subtract(remise);

        return MontantCommandeDTO.builder()
                .montantHT(montantHT)
                .montantTVA(montantTVA)
                .montantTTC(montantTTC)
                .remise(remise)
                .montantFinal(montantFinal)
                .build();
    }



    @Transactional(readOnly = true)
    public byte[] exportCommandesCSV(LocalDateTime debut, LocalDateTime fin) {
        log.info("Export CSV des commandes entre {} et {}", debut, fin);
        List<Commande> commandes = commandeRepository.findByDateCommandeBetween(debut, fin);
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Date,Client,Type,Statut,Montant,Serveur\n");
        for (Commande commande : commandes) {
            csv.append(commande.getIdCommande()).append(",");
            csv.append(commande.getDateCommande()).append(",");
            csv.append(commande.getClient().getUser().getNom()).append(" ")
                    .append(commande.getClient().getUser().getPrenom()).append(",");
            csv.append(commande.getTypeCommande()).append(",");
            csv.append(commande.getStatut()).append(",");
            csv.append(commande.getMontantTotal()).append(",");
            csv.append(commande.getServeur() != null ?
                    commande.getServeur().getUser().getNom() : "N/A").append("\n");
        }
        return csv.toString().getBytes();
    }


    @Transactional(readOnly = true)
    public List<CommandeResponseDTO> getCommandesByPeriode(LocalDateTime debut, LocalDateTime fin) {
        log.info("Récupération des commandes entre {} et {}", debut, fin);
        return commandeRepository.findByDateCommandeBetween(debut, fin)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }



    private LigneCommande createLigneCommande(LigneCommandeRequestDTO ligneDTO, Commande commande) {
        Plat plat = platRepository.findById(ligneDTO.getIdPlat())
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'ID: " + ligneDTO.getIdPlat()));

        if (!plat.getDisponible()) {
            throw new BusinessException("Le plat '" + plat.getNom() + "' n'est pas disponible");
        }

        LigneCommande ligneCommande = new LigneCommande();
        ligneCommande.setCommande(commande);
        ligneCommande.setPlat(plat);
        ligneCommande.setQuantite(ligneDTO.getQuantite());
        ligneCommande.setPrixUnitaire(plat.getPrix());
        ligneCommande.setSousTotal(plat.getPrix().multiply(BigDecimal.valueOf(ligneDTO.getQuantite())));

        return ligneCommande;
    }

    private BigDecimal calculerMontantTotal(List<LigneCommande> ligneCommandes){
        return ligneCommandes.stream()
                .map(LigneCommande::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int calculerTempsPreparation(List<LigneCommande> ligneCommandes){
        return ligneCommandes.stream()
                .mapToInt(ligne->{
                    Integer temps = ligne.getPlat().getTempsPreparation();
                    return  temps != null ? temps : 15;
                })
                .max()
                .orElse(15)+5;
    }

    private void mettreAJourStocks(List<LigneCommande> lignes) {
        for (LigneCommande ligne : lignes) {
            Plat plat = ligne.getPlat();

            // Récupérer les ingrédients nécessaires via le repository
            List<CompositionPlat> compositions = compositionPlatRepo
                    .findByPlatId(plat.getIdPlat());

            for (CompositionPlat composition : compositions) {
                Ingredient ingredient = composition.getIngredient();
                BigDecimal quantiteNecessaire = composition.getQuantiteNecessaire()
                        .multiply(BigDecimal.valueOf(ligne.getQuantite()));

                if (ingredient.getQuantiteStock().compareTo(quantiteNecessaire) < 0) {
                    throw new InsufficientStockException(
                            MessageFormat.format("Stock insuffisant pour l''ingrédient: {0} (disponible: {1} {2}, nécessaire: {3} {4})",
                                    ingredient.getNom(), ingredient.getQuantiteStock(),
                                    ingredient.getUnite(), quantiteNecessaire, ingredient.getUnite())
                    );
                }

                ingredient.setQuantiteStock(ingredient.getQuantiteStock().subtract(quantiteNecessaire));
                ingredientRepository.save(ingredient);
            }
        }
    }


    private void restaurerStocks(List<LigneCommande> lignes) {
        for (LigneCommande ligne : lignes) {
            Plat plat = ligne.getPlat();

            List<CompositionPlat> compositions = compositionPlatRepo
                    .findByPlatId(plat.getIdPlat());

            for (CompositionPlat composition : compositions) {
                Ingredient ingredient = composition.getIngredient();
                BigDecimal quantiteARestaurer = composition.getQuantiteNecessaire()
                        .multiply(BigDecimal.valueOf(ligne.getQuantite()));

                ingredient.setQuantiteStock(ingredient.getQuantiteStock().add(quantiteARestaurer));
                ingredientRepository.save(ingredient);
            }
        }
    }

    private void validerTransitionStatut(StatutCommande ancien, StatutCommande nouveau){
        if (ancien == StatutCommande.ANNULEE) {
            throw new BusinessException("Impossible de modifier une commande annulée");
        }

        if (ancien == StatutCommande.SERVIE || ancien == StatutCommande.LIVREE) {
            throw new BusinessException("Impossible de modifier une commande déjà terminée");
        }

        if (ancien == StatutCommande.EN_ATTENTE && nouveau == StatutCommande.PRETE) {
            throw new BusinessException("La commande doit d'abord passer en préparation");
        }
    }

    private String genererMessageNotification(Commande commande,StatutCommande statutCommande){
        return switch (statutCommande) {
            case EN_PREPARATION -> "Votre commande #" + commande.getIdCommande() + " est en cours de préparation.";
            case PRETE -> "Votre commande #" + commande.getIdCommande() + " est prête !";
            case SERVIE -> "Votre commande #" + commande.getIdCommande() + " a été servie. Bon appétit !";
            case LIVREE -> "Votre commande #" + commande.getIdCommande() + " a été livrée. Bon appétit !";
            default -> null;
        };
    }

    private TypeNotification mapperStatutToTypeNotification(StatutCommande statutCommande){
        return switch (statutCommande) {
            case EN_PREPARATION -> TypeNotification.EN_PREPARATION;
            case PRETE -> TypeNotification.PRETE;
            case LIVREE -> TypeNotification.LIVREE;
            default -> null;
        };
    }
    private CommandeResponseDTO convertToResponseDTO(Commande commande) {
        return CommandeResponseDTO.builder()
                .idCommande(commande.getIdCommande())
                .client(convertClientToDTO(commande.getClient()))
                .serveur(commande.getServeur() != null ? convertServeurToDTO(commande.getServeur()) : null)
                .table(commande.getTable() != null ? convertTableToDTO(commande.getTable()) : null)
                .dateCommande(commande.getDateCommande())
                .statut(commande.getStatut())
                .typeCommande(commande.getTypeCommande())
                .montantTotal(commande.getMontantTotal())
                .remise(commande.getRemise())
                .instructionsSpeciales(commande.getInstructionsSpeciales())
                .tempsEstimePreparation(commande.getTempsEstimePreparation())
                .lignesCommande(commande.getLignesCommande().stream()
                        .map(this::convertLigneToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private ClientDTO convertClientToDTO(Client client){
        if (client == null)
            return null;
        return ClientDTO.builder()
                .idClient(client.getIdClient())
                .nom(client.getUser().getNom())
                .prenom(client.getUser().getPrenom())
                .email(client.getUser().getEmail())
                .telephone(client.getUser().getTelephone())
                .build();
    }


    private ServeurRequestDTO convertServeurToDTO(Serveur serveur) {
        return ServeurRequestDTO.builder()
                .idServeur(serveur.getIdServeur())
                .nom(serveur.getUser().getNom())
                .prenom(serveur.getUser().getPrenom())
                .build();
    }

    private TableDTO convertTableToDTO(TableRestaurant table) {
        return TableDTO.builder()
                .idTable(table.getIdTable())
                .numero(table.getNumero())
                .capacite(table.getCapacite())
                .build();
    }

    private LigneCommandeDTO convertLigneToDTO(LigneCommande ligne) {
        return LigneCommandeDTO.builder()
                .idLigne(ligne.getIdLigne())
                .platNom(ligne.getPlat().getNom())
                .quantite(ligne.getQuantite())
                .prixUnitaire(ligne.getPrixUnitaire())
                .sousTotal(ligne.getSousTotal())
                .build();
    }


}