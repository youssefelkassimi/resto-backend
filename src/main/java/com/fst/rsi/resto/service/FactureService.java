package com.fst.rsi.resto.service;

import com.fst.rsi.resto.dto.FactureDTO;
import com.fst.rsi.resto.entity.*;
import com.fst.rsi.resto.entity.enums.*;
import com.fst.rsi.resto.exception.*;
import com.fst.rsi.resto.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FactureService {

    private final FactureRepo factureRepository;
    private final CommandeRepo commandeRepository;

    private static final BigDecimal TAUX_TVA = new BigDecimal("0.20");

    // ==================== CRUD FACTURES ====================


    public Facture genererFacture(Commande commande) {
        log.info("Génération de facture pour la commande ID: {}", commande.getIdCommande());

        if (factureRepository.existsByCommandeIdCommande(commande.getIdCommande())) {
            throw new BusinessException("Une facture existe déjà pour cette commande");
        }

        if (commande.getStatut() != StatutCommande.SERVIE && commande.getStatut() != StatutCommande.LIVREE) {
            throw new BusinessException("Impossible de générer une facture pour une commande non terminée");
        }

        BigDecimal montantHT = calculerMontantHT(commande);
        BigDecimal montantTVA = montantHT.multiply(TAUX_TVA).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montantTTC = montantHT.add(montantTVA);

        Facture facture = Facture.builder()
                .commande(commande)
                .dateFacture(LocalDateTime.now())
                .montantHT(montantHT)
                .montantTVA(montantTVA)
                .montantTTC(montantTTC)
                .modePaiement(ModePaiement.ESPECES)
                .statutPaiement(StatutPaiement.EN_ATTENTE)
                .build();

        Facture savedFacture = factureRepository.save(facture);

        log.info("Facture générée avec succès - ID: {}", savedFacture.getIdFacture());
        return savedFacture;

    }


    public FactureDTO createFacture(Long idCommande, ModePaiement modePaiement) {
        log.info("Création manuelle de facture pour la commande ID: {}", idCommande);

        Commande commande = commandeRepository.findById(idCommande)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée"));

        Facture facture = genererFacture(commande);
        facture.setModePaiement(modePaiement);

        Facture factureSaved = factureRepository.save(facture);
        return convertToFactureDTO(factureSaved);

    }


    @Transactional(readOnly = true)
    public Facture getFactureById(Long id) {
        log.info("Récupération de la facture ID: {}", id);
        return factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture non trouvée avec l'ID: " + id));
    }


    @Transactional(readOnly = true)
    public FactureDTO getFactureByCommande(Long idCommande) {
        log.info("Récupération de la facture pour la commande ID: {}", idCommande);
        Facture facture = factureRepository.findByCommandeIdCommande(idCommande)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune facture trouvée pour cette commande"));
        return convertToFactureDTO(facture);
    }


    @Transactional(readOnly = true)
    public Page<FactureDTO> getAllFactures(Pageable pageable) {
        log.info("Récupération de toutes les factures");
        Page<Facture> facturePage = factureRepository.findAll(pageable);
        List<FactureDTO> dtoList = facturePage.getContent().stream()
                .map(this::convertToFactureDTO)
                .toList();

        return new PageImpl<>(dtoList, pageable, facturePage.getTotalElements());
    }


    @Transactional(readOnly = true)
    public List<FactureDTO> getFacturesByClient(Long idClient) {
        log.info("Récupération des factures du client ID: {}", idClient);
        return factureRepository.findByCommandeClientIdClient(idClient).stream()
                .map(this::convertToFactureDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<FactureDTO> getFacturesByStatut(StatutPaiement statut) {
        log.info("Récupération des factures avec le statut: {}", statut);
        return factureRepository.findByStatutPaiement(statut).stream()
                .map(this::convertToFactureDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<FactureDTO> getFacturesByModePaiement(ModePaiement mode) {
        log.info("Récupération des factures par mode de paiement: {}", mode);
        return factureRepository.findByModePaiement(mode).stream()
                .map(this::convertToFactureDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<FactureDTO> getFacturesByPeriode(LocalDateTime debut, LocalDateTime fin) {
        log.info("Récupération des factures entre {} et {}", debut, fin);
        return factureRepository.findByDateFactureBetween(debut, fin).stream()
                .map(this::convertToFactureDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<FactureDTO> getFacturesImpayees() {
        log.info("Récupération des factures impayées");
        return factureRepository.findByStatutPaiement(StatutPaiement.EN_ATTENTE).stream()
                .map(this::convertToFactureDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<FactureDTO> getFacturesDuJour() {
        log.info("Récupération des factures du jour");
        LocalDateTime debutJour = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finJour = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return factureRepository.findByDateFactureBetween(debutJour, finJour).stream()
                .map(this::convertToFactureDTO)
                .toList();
    }

    // ==================== GESTION DES PAIEMENTS ====================


    public FactureDTO marquerCommePaye(Long idFacture, ModePaiement modePaiement) {
        log.info("Marquage de la facture ID: {} comme payée", idFacture);

        Facture facture = getFactureById(idFacture);

        if (facture.getStatutPaiement() == StatutPaiement.PAYEE) {
            throw new BusinessException("Cette facture est déjà payée");
        }

        if (facture.getStatutPaiement() == StatutPaiement.ANNULEE) {
            throw new BusinessException("Impossible de payer une facture annulée");
        }

        facture.setStatutPaiement(StatutPaiement.PAYEE);
        facture.setModePaiement(modePaiement);

        Facture updatedFacture = factureRepository.save(facture);

        log.info("Facture marquée comme payée avec succès");
        return convertToFactureDTO(updatedFacture);
    }


    public FactureDTO annulerFacture(Long idFacture) {
        log.info("Annulation de la facture ID: {}", idFacture);

        Facture facture = getFactureById(idFacture);

        if (facture.getStatutPaiement() == StatutPaiement.PAYEE) {
            throw new BusinessException("Impossible d'annuler une facture déjà payée");
        }

        facture.setStatutPaiement(StatutPaiement.ANNULEE);
        Facture updatedFacture = factureRepository.save(facture);

        log.info("Facture annulée avec succès");
        return convertToFactureDTO(updatedFacture);
    }


    public FactureDTO changerModePaiement(Long idFacture, ModePaiement nouveauMode) {
        log.info("Changement du mode de paiement pour la facture ID: {}", idFacture);

        Facture facture = getFactureById(idFacture);

        if (facture.getStatutPaiement() == StatutPaiement.ANNULEE) {
            throw new BusinessException("Impossible de modifier une facture annulée");
        }

        facture.setModePaiement(nouveauMode);
        Facture updatedFacture = factureRepository.save(facture);

        log.info("Mode de paiement modifié avec succès");
        return convertToFactureDTO(updatedFacture);
    }

    // ==================== STATISTIQUES ====================


    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiques(LocalDateTime debut, LocalDateTime fin) {
        log.info("Calcul des statistiques des factures entre {} et {}", debut, fin);

        List<Facture> factures = factureRepository.findByDateFactureBetween(debut, fin);

        Map<String, Object> stats = new HashMap<>();

        // Statistiques générales
        long totalFactures = factures.size();
        long facturesPayees = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .count();
        long facturesImpayees = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.EN_ATTENTE)
                .count();
        long facturesAnnulees = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.ANNULEE)
                .count();

        // Montants
        BigDecimal montantTotalHT = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .map(Facture::getMontantHT)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montantTotalTVA = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .map(Facture::getMontantTVA)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montantTotalTTC = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .map(Facture::getMontantTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montantImpaye = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.EN_ATTENTE)
                .map(Facture::getMontantTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Répartition par mode de paiement
        Map<ModePaiement, Long> parModePaiement = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .collect(Collectors.groupingBy(Facture::getModePaiement, Collectors.counting()));

        // Montants par mode de paiement
        Map<ModePaiement, BigDecimal> montantsParMode = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .collect(Collectors.groupingBy(
                        Facture::getModePaiement,
                        Collectors.reducing(BigDecimal.ZERO, Facture::getMontantTTC, BigDecimal::add)
                ));

        // Moyenne
        BigDecimal montantMoyen = facturesPayees > 0
                ? montantTotalTTC.divide(BigDecimal.valueOf(facturesPayees), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        stats.put("dateDebut", debut);
        stats.put("dateFin", fin);
        stats.put("totalFactures", totalFactures);
        stats.put("facturesPayees", facturesPayees);
        stats.put("facturesImpayees", facturesImpayees);
        stats.put("facturesAnnulees", facturesAnnulees);
        stats.put("montantTotalHT", montantTotalHT);
        stats.put("montantTotalTVA", montantTotalTVA);
        stats.put("montantTotalTTC", montantTotalTTC);
        stats.put("montantImpaye", montantImpaye);
        stats.put("montantMoyen", montantMoyen);
        stats.put("repartitionParModePaiement", parModePaiement);
        stats.put("montantsParModePaiement", montantsParMode);

        return stats;
    }

    @Transactional(readOnly = true)
    public BigDecimal getChiffreAffaire(LocalDateTime debut, LocalDateTime fin) {
        log.info("Calcul du chiffre d'affaires entre {} et {}", debut, fin);

        return factureRepository.findByDateFactureBetween(debut, fin).stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .map(Facture::getMontantTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @Transactional(readOnly = true)
    public BigDecimal getChiffreAffaireDuJour() {
        log.info("Calcul du chiffre d'affaires du jour");

        LocalDateTime debutJour = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finJour = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        return getChiffreAffaire(debutJour, finJour);
    }


    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopClientsByMontant(int limit) {
        log.info("Récupération du top {} clients par montant", limit);

        List<Facture> factures = factureRepository.findAll();

        Map<Long, BigDecimal> montantsParClient = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .collect(Collectors.groupingBy(
                        f -> f.getCommande().getClient().getIdClient(),
                        Collectors.reducing(BigDecimal.ZERO, Facture::getMontantTTC, BigDecimal::add)
                ));

        return montantsParClient.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> clientData = new HashMap<>();

                    // Récupérer les informations du client
                    Facture facture = factures.stream()
                            .filter(f -> f.getCommande().getClient().getIdClient().equals(entry.getKey()))
                            .findFirst()
                            .orElse(null);

                    if (facture != null) {
                        Client client = facture.getCommande().getClient();
                        clientData.put("idClient", client.getIdClient());
                        clientData.put("nom", client.getUser().getNom());
                        clientData.put("prenom", client.getUser().getPrenom());
                        clientData.put("email", client.getUser().getEmail());
                        clientData.put("montantTotal", entry.getValue());
                    }

                    return clientData;
                })
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Map<String, Object> getRapportTVA(LocalDateTime debut, LocalDateTime fin) {
        log.info("Génération du rapport de TVA entre {} et {}", debut, fin);

        List<Facture> factures = factureRepository.findByDateFactureBetween(debut, fin);

        BigDecimal totalTVACollectee = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .map(Facture::getMontantTVA)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHT = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .map(Facture::getMontantHT)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTTC = factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .map(Facture::getMontantTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> rapport = new HashMap<>();
        rapport.put("periode", Map.of("debut", debut, "fin", fin));
        rapport.put("tauxTVA", TAUX_TVA.multiply(new BigDecimal("100")) + "%");
        rapport.put("totalHT", totalHT);
        rapport.put("totalTVACollectee", totalTVACollectee);
        rapport.put("totalTTC", totalTTC);
        rapport.put("nombreFactures", factures.stream()
                .filter(f -> f.getStatutPaiement() == StatutPaiement.PAYEE)
                .count());

        return rapport;
    }

// ==================== GÉNÉRATION DE DOCUMENTS ====================


    @Transactional(readOnly = true)
    public String genererContenuFacture(Long idFacture) {
        log.info("Génération du contenu de la facture ID: {}", idFacture);

        Facture facture = getFactureById(idFacture);
        Commande commande = facture.getCommande();
        Client client = commande.getClient();

        StringBuilder contenu = new StringBuilder();

        contenu.append("========================================\n");
        contenu.append("           FACTURE N° ").append(facture.getIdFacture()).append("\n");
        contenu.append("========================================\n\n");

        contenu.append("Restaurant RSI\n");
        contenu.append("FST SETTAT\n");
        contenu.append("75001 SETTAT\n");
        contenu.append("Tél: +212 11 11 11 11 \n\n");

        contenu.append("Client:\n");
        contenu.append(client.getUser().getPrenom()).append(" ").append(client.getUser().getNom()).append("\n");
        contenu.append(client.getUser().getEmail()).append("\n");
        contenu.append(client.getUser().getTelephone()).append("\n\n");

        contenu.append("Date: ").append(facture.getDateFacture()).append("\n");
        contenu.append("Commande N°: ").append(commande.getIdCommande()).append("\n");
        contenu.append("Mode de paiement: ").append(facture.getModePaiement()).append("\n\n");

        contenu.append("========================================\n");
        contenu.append("DÉTAILS DE LA COMMANDE\n");
        contenu.append("========================================\n\n");

        for (LigneCommande ligne : commande.getLignesCommande()) {
            contenu.append(String.format("%-30s x%d  %10.2f DH\n",
                    ligne.getPlat().getNom(),
                    ligne.getQuantite(),
                    ligne.getSousTotal()));
        }

        contenu.append("\n");

        contenu.append("----------------------------------------\n");
        contenu.append(String.format("Montant HT:          %10.2f DH\n", facture.getMontantHT()));
        contenu.append(String.format("TVA (20%%):           %10.2f DH\n", facture.getMontantTVA()));
        if (commande.getRemise() != null && commande.getRemise().compareTo(BigDecimal.ZERO) > 0) {
            contenu.append(String.format("Remise:             -%10.2f DH\n", commande.getRemise()));
        }
        contenu.append("========================================\n");
        contenu.append(String.format("TOTAL TTC:           %10.2f DH\n", facture.getMontantTTC()));
        contenu.append("========================================\n\n");

        contenu.append("Statut: ").append(facture.getStatutPaiement()).append("\n\n");

        contenu.append("Merci de votre visite!\n");
        contenu.append("========================================\n");

        return contenu.toString();
    }


    public void deleteFacture(Long id) {
        log.info("Suppression de la facture ID: {}", id);

        Facture facture = getFactureById(id);

        if (facture.getStatutPaiement() == StatutPaiement.PAYEE) {
            throw new BusinessException("Impossible de supprimer une facture payée");
        }

        factureRepository.delete(facture);
        log.info("Facture supprimée avec succès");
    }

    // ==================== MÉTHODES PRIVÉES ====================


    private BigDecimal calculerMontantHT(Commande commande) {
        BigDecimal montantTotal = commande.getMontantTotal();

        if (commande.getRemise() != null && commande.getRemise().compareTo(BigDecimal.ZERO) > 0) {
            montantTotal = montantTotal.subtract(commande.getRemise());
        }

        // Calculer le montant HT à partir du TTC
        // HT = TTC / (1 + TVA)
        BigDecimal montantHT = montantTotal.divide(
                BigDecimal.ONE.add(TAUX_TVA),
                2,
                RoundingMode.HALF_UP
        );

        return montantHT;
    }

    public FactureDTO convertToFactureDTO(Facture facture) {
        if (facture == null) {
            return null;
        }

        return FactureDTO.builder()
                .idFacture(facture.getIdFacture())
                .commandeId(facture.getCommande() != null ? facture.getCommande().getIdCommande() : null)
                .dateFacture(facture.getDateFacture())
                .montantHT(facture.getMontantHT())
                .montantTVA(facture.getMontantTVA())
                .montantTTC(facture.getMontantTTC())
                .modePaiement(facture.getModePaiement())
                .statutPaiement(facture.getStatutPaiement())
                .build();
    }
}
