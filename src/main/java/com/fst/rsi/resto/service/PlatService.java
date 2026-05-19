package com.fst.rsi.resto.service;


import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.entity.*;
import com.fst.rsi.resto.exception.*;
import com.fst.rsi.resto.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlatService {

    private final PlatRepo platRepository;
    private final CompositionPlatRepo compositionPlatRepository;
    private final IngredientRepo ingredientRepository;
    private final ClientRepo clientRepository;
    private final CommandeRepo commandeRepository;
    private final FileService fileService;

    @Value("${project.profile}")
    private String profile;

    @Value("${base.url}")
    private String baseUrl;

    private static final String UPLOAD_PATH = System.getProperty("user.dir") + "/uploads/plats/";

    public PlatDTO createPlat(PlatDTO platDTO) {
        log.info("Création d'un nouveau plat: {}", platDTO.getNom());

        // Vérifier si le plat existe déjà
        if (platRepository.existsByNom(platDTO.getNom())) {
            throw new BusinessException("Un plat avec ce nom existe déjà");
        }

        // Validation du prix
        if (platDTO.getPrix() == null || platDTO.getPrix().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Le prix doit être supérieur à zéro");
        }

        Plat plat = Plat.builder()
                .nom(platDTO.getNom())
                .description(platDTO.getDescription())
                .prix(platDTO.getPrix())
                .categorie(platDTO.getCategorie())
                .tempsPreparation(platDTO.getTempsPreparation())
                .disponible(platDTO.getDisponible() != null ? platDTO.getDisponible() : true)
                .image(platDTO.getImage())
                .dateCreation(LocalDate.now())
                .build();

        Plat savedPlat = platRepository.save(plat);

        log.info("Plat créé avec succès - ID: {}", savedPlat.getIdPlat());
        return convertToDTO(savedPlat);
    }

    public PlatDTO createPlatWithImage(PlatDTO platDTO, MultipartFile image) throws IOException {
        log.info("Création d'un plat avec image: {}", platDTO.getNom());

        // Créer le plat
        PlatDTO createdPlat = createPlat(platDTO);

        // Upload de l'image si fournie
        if (image != null && !image.isEmpty()) {
            String imageUrl = uploadPlatImage(createdPlat.getIdPlat(), image);
            createdPlat.setImage(imageUrl);

            // Mettre à jour le plat avec l'URL de l'image
            Plat plat = platRepository.findById(createdPlat.getIdPlat())
                    .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé"));
            plat.setImage(imageUrl);
            platRepository.save(plat);
        }

        return createdPlat;
    }


    @Transactional(readOnly = true)
    public PlatDTO getPlatById(Long id) {
        log.info("Récupération du plat ID: {}", id);
        Plat plat = platRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'ID: " + id));
        return convertToDTO(plat);
    }



    @Transactional(readOnly = true)
    public PlatDetailDTO getPlatWithIngredients(Long id) {
        log.info("Récupération du plat avec ingrédients - ID: {}", id);

        Plat plat = platRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'ID: " + id));

        return convertToDetailDTO(plat);
    }



    @Transactional(readOnly = true)
    public Page<PlatDTO> getAllPlats(Pageable pageable) {
        log.info("Récupération de tous les plats - Page: {}", pageable.getPageNumber());
        return platRepository.findAll(pageable)
                .map(this::convertToDTO);
    }



    @Transactional(readOnly = true)
    public List<PlatDTO> getPlatsDisponibles() {
        log.info("Récupération des plats disponibles");
        return platRepository.findByDisponibleTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    @Transactional(readOnly = true)
    public List<PlatDTO> getPlatsByCategorie(String categorie) {
        log.info("Récupération des plats de la catégorie: {}", categorie);
        return platRepository.findByCategorie(categorie)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PlatDTO> getPlatsDisponiblesByCategorie(String categorie) {
        log.info("Récupération des plats disponibles de la catégorie: {}", categorie);
        return platRepository.findByCategorieAndDisponibleTrue(categorie)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }




    @Transactional(readOnly = true)
    public List<PlatDTO> searchPlats(String searchTerm) {
        log.info("Recherche de plats avec le terme: {}", searchTerm);
        return platRepository.findByNomContainingIgnoreCase(searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    @Transactional(readOnly = true)
    public List<PlatDTO> searchPlatsAvanced(String searchTerm) {
        log.info("Recherche avancée de plats avec le terme: {}", searchTerm);
        return platRepository.searchByNomOrDescription(searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PlatDTO> getPlatsByPrixRange(BigDecimal minPrix, BigDecimal maxPrix) {
        log.info("Récupération des plats entre {} et {} DH", minPrix, maxPrix);
        return platRepository.findByPrixBetween(minPrix, maxPrix)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<String> getCategories() {
        log.info("Récupération de toutes les catégories");
        return platRepository.findDistinctCategories();
    }



    @Transactional(readOnly = true)
    public List<PlatDTO> getPlatsPlusPopulaires(int limit) {
        log.info("Récupération des {} plats les plus populaires", limit);
        return platRepository.findMostPopularPlats(Pageable.ofSize(limit))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PlatDTO> getNouveauxPlats(int limit) {
        log.info("Récupération des {} nouveaux plats", limit);
        LocalDate dateLimit = LocalDate.now().minusDays(30);
        return platRepository.findByDateCreationAfter(dateLimit, Pageable.ofSize(limit))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    public PlatDTO updatePlat(Long id, PlatDTO platDTO) {
        log.info("Mise à jour du plat ID: {}", id);

        Plat plat = platRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'ID: " + id));

        // Vérifier si le nouveau nom n'existe pas déjà pour un autre plat
        if (!plat.getNom().equals(platDTO.getNom()) && platRepository.existsByNom(platDTO.getNom())) {
            throw new BusinessException("Un plat avec ce nom existe déjà");
        }

        // Validation du prix
        if (platDTO.getPrix() != null && platDTO.getPrix().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Le prix doit être supérieur à zéro");
        }

        // Mettre à jour les champs
        if (platDTO.getNom() != null) {
            plat.setNom(platDTO.getNom());
        }
        if (platDTO.getDescription() != null) {
            plat.setDescription(platDTO.getDescription());
        }
        if (platDTO.getPrix() != null) {
            plat.setPrix(platDTO.getPrix());
        }
        if (platDTO.getCategorie() != null) {
            plat.setCategorie(platDTO.getCategorie());
        }
        if (platDTO.getTempsPreparation() != null) {
            plat.setTempsPreparation(platDTO.getTempsPreparation());
        }
        if (platDTO.getDisponible() != null) {
            plat.setDisponible(platDTO.getDisponible());
        }

        Plat updatedPlat = platRepository.save(plat);

        log.info("Plat mis à jour avec succès");
        return convertToDTO(updatedPlat);
    }



    public PlatDTO updatePlatImage(Long id, MultipartFile image) throws IOException {
        log.info("Mise à jour de l'image du plat ID: {}", id);

        Plat plat = platRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'ID: " + id));

        if (image == null || image.isEmpty()) {
            throw new BusinessException("L'image est obligatoire");
        }

        // Upload de la nouvelle image
        String imageUrl = uploadPlatImage(id, image);
        plat.setImage(imageUrl);

        Plat updatedPlat = platRepository.save(plat);

        log.info("Image du plat mise à jour avec succès");
        return convertToDTO(updatedPlat);
    }



    public PlatDTO toggleDisponibilite(Long id) {
        log.info("Changement de disponibilité du plat ID: {}", id);

        Plat plat = platRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'ID: " + id));

        plat.setDisponible(!plat.getDisponible());
        Plat updatedPlat = platRepository.save(plat);

        log.info("Disponibilité changée: {}", updatedPlat.getDisponible() ? "Disponible" : "Non disponible");
        return convertToDTO(updatedPlat);
    }


    public PlatDTO marquerIndisponible(Long id) {
        log.info("Marquage du plat ID: {} comme indisponible", id);

        Plat plat = platRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'ID: " + id));

        plat.setDisponible(false);
        Plat updatedPlat = platRepository.save(plat);

        log.info("Plat marqué comme indisponible");
        return convertToDTO(updatedPlat);
    }



    public CompositionPlatDTO ajouterIngredient(Long idPlat, CompositionPlatRequestDTO compositionDTO) {
        log.info("Ajout d'un ingrédient au plat ID: {}", idPlat);

        Plat plat = platRepository.findById(idPlat)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé"));

        Ingredient ingredient = ingredientRepository.findById(compositionDTO.getIdIngredient())
                .orElseThrow(() -> new ResourceNotFoundException("Ingrédient non trouvé"));

        // Vérifier si la composition existe déjà
        if (compositionPlatRepository.existsByPlatIdPlatAndIngredientIdIngredient(
                idPlat, compositionDTO.getIdIngredient())) {
            throw new BusinessException("Cet ingrédient est déjà associé à ce plat");
        }

        if (compositionDTO.getQuantiteNecessaire().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La quantité doit être positive");
        }

        CompositionPlat composition = CompositionPlat.builder()
                .plat(plat)
                .ingredient(ingredient)
                .quantiteNecessaire(compositionDTO.getQuantiteNecessaire())
                .build();

        CompositionPlat savedComposition = compositionPlatRepository.save(composition);

        log.info("Ingrédient ajouté au plat avec succès");
        return convertCompositionToDTO(savedComposition);
    }



    public CompositionPlatDTO updateIngredientQuantite(Long idPlat ,
                                                       Long idComposition, BigDecimal nouvelleQuantite) {
        log.info("Mise à jour de la quantité de la composition ID: {}", idComposition);

        CompositionPlat composition = compositionPlatRepository.findByPlatIdPlatAndIngredientIdIngredient(idPlat, idComposition)
                .orElseThrow(() -> new ResourceNotFoundException("Composition non trouvée"));

        if (nouvelleQuantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La quantité doit être positive");
        }

        composition.setQuantiteNecessaire(nouvelleQuantite);
        CompositionPlat updatedComposition = compositionPlatRepository.save(composition);

        log.info("Quantité mise à jour avec succès");
        return convertCompositionToDTO(updatedComposition);
    }



    public void retirerIngredient(Long idPlat, Long idComposition) {
        log.info("Retrait de l'ingrédient - Composition ID: {}", idComposition);

        CompositionPlat composition = compositionPlatRepository.findByPlatIdPlatAndIngredientIdIngredient(idPlat, idComposition)
                .orElseThrow(() -> new ResourceNotFoundException("Composition non trouvée"));

        compositionPlatRepository.delete(composition);
        log.info("Ingrédient retiré du plat avec succès");
    }




    @Transactional(readOnly = true)
    public List<CompositionPlatDTO> getIngredientsPlat(Long idPlat) {
        log.info("Récupération des ingrédients du plat ID: {}", idPlat);

        Plat plat = platRepository.findById(idPlat)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé"));

        return plat.getCompositions().stream()
                .map(this::convertCompositionToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public boolean verifierDisponibiliteStock(Long idPlat, int quantite) {
        log.info("Vérification de la disponibilité du plat ID: {} pour {} unité(s)", idPlat, quantite);

        Plat plat = platRepository.findById(idPlat)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé"));

        if (!plat.getDisponible()) {
            return false;
        }

        // Vérifier chaque ingrédient
        for (CompositionPlat composition : plat.getCompositions()) {
            Ingredient ingredient = composition.getIngredient();
            BigDecimal quantiteNecessaire = composition.getQuantiteNecessaire()
                    .multiply(BigDecimal.valueOf(quantite));

            if (ingredient.getQuantiteStock().compareTo(quantiteNecessaire) < 0) {
                log.warn("Stock insuffisant pour l'ingrédient: {}", ingredient.getNom());
                return false;
            }
        }

        return true;
    }

    /**
     * a faire si on a le temps
     */
//    @Transactional(readOnly = true)
//    public BigDecimal calculerCoutIngredients(Long idPlat) {
//        log.info("Calcul du coût des ingrédients du plat ID: {}", idPlat);
//
//        Plat plat = platRepository.findById(idPlat)
//                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé"));
//
//
//        BigDecimal cout = BigDecimal.ZERO;
//
//        log.info("Coût des ingrédients: {} DH", cout);
//        return cout;
//    }


    @Transactional(readOnly = true)
    public PlatStatisticsDTO getPlatStatistics(Long idPlat) {
        log.info("Calcul des statistiques du plat ID: {}", idPlat);

        Plat plat = platRepository.findById(idPlat)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé"));

        // Calculer les statistiques à partir des lignes de commande
        long totalCommandes = plat.getLignesCommande() != null
                ? plat.getLignesCommande().stream()
                .map(lc -> lc.getCommande().getIdCommande())
                .distinct()
                .count()
                : 0;

        int totalQuantiteVendue = plat.getLignesCommande() != null
                ? plat.getLignesCommande().stream()
                .mapToInt(LigneCommande::getQuantite)
                .sum()
                : 0;

        BigDecimal chiffreAffaire = plat.getLignesCommande() != null
                ? plat.getLignesCommande().stream()
                .map(LigneCommande::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;

        return PlatStatisticsDTO.builder()
                .idPlat(idPlat)
                .nomPlat(plat.getNom())
                .totalCommandes(totalCommandes)
                .totalQuantiteVendue(totalQuantiteVendue)
                .chiffreAffaire(chiffreAffaire)
                .nombreIngredients(plat.getCompositions() != null ? plat.getCompositions().size() : 0)
                .disponible(plat.getDisponible())
                .build();
    }


    public void deletePlat(Long id) {
        log.info("Suppression du plat ID: {}", id);

        Plat plat = platRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'ID: " + id));

        if (plat.getLignesCommande() != null && !plat.getLignesCommande().isEmpty()) {
            throw new BusinessException(
                    MessageFormat.format("Impossible de supprimer ce plat. Il a été commandé {0} fois",
                            plat.getLignesCommande().size())
            );
        }

        platRepository.delete(plat);
        log.info("Plat supprimé avec succès");
    }


    private String uploadPlatImage(Long idPlat, MultipartFile image) throws IOException {
        log.info("Upload de l'image pour le plat ID: {}", idPlat);

        // Valider le fichier
        if (image.getSize() > 5 * 1024 * 1024) { // 5MB max
            throw new BusinessException("La taille de l'image ne doit pas dépasser 5MB");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("Le fichier doit être une image");
        }

        // Upload du fichier
        String fileName = fileService.uploadFile(UPLOAD_PATH, image, idPlat.toString());

        // Construire l'URL complète
        String imageUrl = baseUrl + "/api/plats/" + idPlat + "/image";

        log.info("Image uploadée avec succès: {}", fileName);
        return imageUrl;
    }


    @Transactional(readOnly = true)
    public List<PlatDTO> getPlatsSimilaires(Long id, int limit) {
        log.info("Récupération de {} plats similaires au plat ID: {}", limit, id);

        Plat plat = platRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé"));

        return platRepository.findByCategorieAndDisponibleTrue(plat.getCategorie())
                .stream()
                .filter(p -> !p.getIdPlat().equals(id))
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PlatDTO> getRecommandationsPourClient(Long clientId, int limit) {
        log.info("Génération de {} recommandations pour le client ID: {}", limit, clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));

        List<Commande> commandes = commandeRepository.findByClientIdClientOrderByDateCommandeDesc(clientId);

        if (commandes.isEmpty()) {

            return getPlatsPlusPopulaires(limit);
        }

        List<String> categoriesPreferees = commandes.stream()
                .flatMap(c -> c.getLignesCommande().stream())
                .map(lc -> lc.getPlat().getCategorie())
                .collect(Collectors.groupingBy(cat -> cat, Collectors.counting()))
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        // Extraire les plats déjà commandés
        List<Long> platsDejaCommandes = commandes.stream()
                .flatMap(c -> c.getLignesCommande().stream())
                .map(lc -> lc.getPlat().getIdPlat())
                .distinct()
                .toList();

        // Recommander des plats des catégories préférées non encore commandés
        List<PlatDTO> recommandations = new java.util.ArrayList<>();

        for (String categorie : categoriesPreferees) {
            List<PlatDTO> platsCategorie = platRepository.findByCategorieAndDisponibleTrue(categorie)
                    .stream()
                    .filter(p -> !platsDejaCommandes.contains(p.getIdPlat()))
                    .map(this::convertToDTO)
                    .limit(limit / categoriesPreferees.size() + 1)
                    .toList();

            recommandations.addAll(platsCategorie);

            if (recommandations.size() >= limit) {
                break;
            }
        }

        return recommandations.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }


    public InputStream getPlatImage(Long id) throws IOException {
        log.info("Récupération de l'image du plat ID: {}", id);

        Plat plat = platRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'ID: " + id));

        if (plat.getImage() == null || plat.getImage().isEmpty()) {
            throw new ResourceNotFoundException("Ce plat n'a pas d'image");
        }

        String fileName = id + extractFileNameFromUrl(plat.getImage());

        InputStream inputStream = fileService.getResourceFile(UPLOAD_PATH, fileName);

        if (inputStream == null) {
            throw new ResourceNotFoundException("Fichier image non trouvé");
        }

        return inputStream;
    }


    private String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    // ==================== Méthodes de Conversion ====================


    private PlatDTO convertToDTO(Plat plat) {
        return PlatDTO.builder()
                .idPlat(plat.getIdPlat())
                .nom(plat.getNom())
                .description(plat.getDescription())
                .prix(plat.getPrix())
                .categorie(plat.getCategorie())
                .tempsPreparation(plat.getTempsPreparation())
                .disponible(plat.getDisponible())
                .image(plat.getImage())
                .build();
    }


    private PlatDetailDTO convertToDetailDTO(Plat plat) {
        List<CompositionPlatDTO> ingredients = plat.getCompositions() != null
                ? plat.getCompositions().stream()
                .map(this::convertCompositionToDTO)
                .collect(Collectors.toList())
                : List.of();

        return PlatDetailDTO.builder()
                .idPlat(plat.getIdPlat())
                .nom(plat.getNom())
                .description(plat.getDescription())
                .prix(plat.getPrix())
                .categorie(plat.getCategorie())
                .tempsPreparation(plat.getTempsPreparation())
                .disponible(plat.getDisponible())
                .image(plat.getImage())
                .dateCreation(plat.getDateCreation())
                .ingredients(ingredients)
                .build();
    }

    private CompositionPlatDTO convertCompositionToDTO(CompositionPlat composition) {
        return CompositionPlatDTO.builder()
                .idPlat(composition.getPlat().getIdPlat())
                .idIngredient(composition.getIngredient().getIdIngredient())
                .nomIngredient(composition.getIngredient().getNom())
                .quantiteNecessaire(composition.getQuantiteNecessaire())
                .unite(composition.getIngredient().getUnite())
                .build();
    }
}
