package com.fst.rsi.resto.controller;

import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.service.PlatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/plats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlatController {

    private final PlatService platService;


    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PlatDTO> createPlat(@Valid @RequestBody PlatDTO platDTO) {
        PlatDTO plat = platService.createPlat(platDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(plat);
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PlatDTO> createPlatWithImage(
            @RequestPart("plat") @Valid PlatDTO platDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        PlatDTO plat = platService.createPlatWithImage(platDTO, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(plat);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PlatDTO> getPlatById(@PathVariable Long id) {
        PlatDTO plat = platService.getPlatById(id);
        return ResponseEntity.ok(plat);
    }


    @GetMapping("/{id}/details")
    public ResponseEntity<PlatDetailDTO> getPlatWithIngredients(@PathVariable Long id) {
        PlatDetailDTO plat = platService.getPlatWithIngredients(id);
        return ResponseEntity.ok(plat);
    }


    @GetMapping
    public ResponseEntity<Page<PlatDTO>> getAllPlats(@PageableDefault(size = 20,sort = "id") Pageable pageable) {
        Page<PlatDTO> plats = platService.getAllPlats(pageable);
        return ResponseEntity.ok(plats);
    }


    @GetMapping("/disponibles")
    public ResponseEntity<List<PlatDTO>> getPlatsDisponibles() {
        List<PlatDTO> plats = platService.getPlatsDisponibles();
        return ResponseEntity.ok(plats);
    }


    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<PlatDTO>> getPlatsByCategorie(@PathVariable String categorie) {
        List<PlatDTO> plats = platService.getPlatsByCategorie(categorie);
        return ResponseEntity.ok(plats);
    }


    @GetMapping("/categorie/{categorie}/disponibles")
    public ResponseEntity<List<PlatDTO>> getPlatsDisponiblesByCategorie(@PathVariable String categorie) {
        List<PlatDTO> plats = platService.getPlatsDisponiblesByCategorie(categorie);
        return ResponseEntity.ok(plats);
    }


    @GetMapping("/search")
    public ResponseEntity<List<PlatDTO>> searchPlats(@RequestParam String searchTerm) {
        List<PlatDTO> plats = platService.searchPlats(searchTerm);
        return ResponseEntity.ok(plats);
    }


    @GetMapping("/search/advanced")
    public ResponseEntity<List<PlatDTO>> searchPlatsAdvanced(@RequestParam String searchTerm) {
        List<PlatDTO> plats = platService.searchPlatsAvanced(searchTerm);
        return ResponseEntity.ok(plats);
    }


    @GetMapping("/prix")
    public ResponseEntity<List<PlatDTO>> getPlatsByPrixRange(
            @RequestParam BigDecimal minPrix,
            @RequestParam BigDecimal maxPrix) {

        List<PlatDTO> plats = platService.getPlatsByPrixRange(minPrix, maxPrix);
        return ResponseEntity.ok(plats);
    }


    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = platService.getCategories();
        return ResponseEntity.ok(categories);
    }


    @GetMapping("/populaires")
    public ResponseEntity<List<PlatDTO>> getPlatsPlusPopulaires(
            @RequestParam(defaultValue = "10") int limit) {

        List<PlatDTO> plats = platService.getPlatsPlusPopulaires(limit);
        return ResponseEntity.ok(plats);
    }


    @GetMapping("/nouveaux")
    public ResponseEntity<List<PlatDTO>> getNouveauxPlats(
            @RequestParam(defaultValue = "10") int limit) {

        List<PlatDTO> plats = platService.getNouveauxPlats(limit);
        return ResponseEntity.ok(plats);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PlatDTO> updatePlat(
            @PathVariable Long id,
            @Valid @RequestBody PlatDTO platDTO) {

        PlatDTO plat = platService.updatePlat(id, platDTO);
        return ResponseEntity.ok(plat);
    }


    @PutMapping("/{id}/image")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PlatDTO> updatePlatImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) throws IOException {

        PlatDTO plat = platService.updatePlatImage(id, image);
        return ResponseEntity.ok(plat);
    }


    @GetMapping("/{id}/image")
    public ResponseEntity<InputStreamResource> getPlatImage(@PathVariable Long id) throws IOException {
        InputStream imageStream = platService.getPlatImage(id);

        if (imageStream == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(new InputStreamResource(imageStream));
    }


    @PatchMapping("/{id}/toggle-disponibilite")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PlatDTO> toggleDisponibilite(@PathVariable Long id) {
        PlatDTO plat = platService.toggleDisponibilite(id);
        return ResponseEntity.ok(plat);
    }


    @PatchMapping("/{id}/indisponible")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PlatDTO> marquerIndisponible(@PathVariable Long id) {
        PlatDTO plat = platService.marquerIndisponible(id);
        return ResponseEntity.ok(plat);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlat(@PathVariable Long id) {
        platService.deletePlat(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Endpoints pour les Ingrédients ==========


    @PostMapping("/{idPlat}/ingredients")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<CompositionPlatDTO> ajouterIngredient(
            @PathVariable Long idPlat,
            @Valid @RequestBody CompositionPlatRequestDTO compositionDTO) {

        CompositionPlatDTO result = platService.ajouterIngredient(idPlat, compositionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @GetMapping("/{idPlat}/ingredients")
    public ResponseEntity<List<CompositionPlatDTO>> getIngredientsPlat(@PathVariable Long idPlat) {
        List<CompositionPlatDTO> ingredients = platService.getIngredientsPlat(idPlat);
        return ResponseEntity.ok(ingredients);
    }


    @PutMapping("/{idPlat}/ingredients/{idIngredient}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<CompositionPlatDTO> updateIngredientQuantite(
            @PathVariable Long idPlat,
            @PathVariable Long idIngredient,
            @RequestParam BigDecimal quantite) {

        CompositionPlatDTO result = platService.updateIngredientQuantite(idPlat, idIngredient, quantite);
        return ResponseEntity.ok(result);
    }


    @DeleteMapping("/{idPlat}/ingredients/{idIngredient}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> retirerIngredient(
            @PathVariable Long idPlat,
            @PathVariable Long idIngredient) {

        platService.retirerIngredient(idPlat, idIngredient);
        return ResponseEntity.noContent().build();
    }


    /**
     * Vérifier la disponibilité d'un plat selon le stock
     */
    @GetMapping("/{id}/disponibilite-stock")
    public ResponseEntity<DisponibiliteStockDTO> verifierDisponibiliteStock(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int quantite) {

        boolean disponible = platService.verifierDisponibiliteStock(id, quantite);

        DisponibiliteStockDTO response = DisponibiliteStockDTO.builder()
                .disponible(disponible)
                .message(disponible
                        ? "Le plat est disponible"
                        : "Stock insuffisant pour ce plat")
                .build();

        return ResponseEntity.ok(response);
    }



    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PlatStatisticsDTO> getPlatStatistics(@PathVariable Long id) {
        PlatStatisticsDTO statistics = platService.getPlatStatistics(id);
        return ResponseEntity.ok(statistics);
    }




    @GetMapping("/{id}/similaires")
    public ResponseEntity<List<PlatDTO>> getPlatsSimilaires(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {

        List<PlatDTO> plats = platService.getPlatsSimilaires(id, limit);
        return ResponseEntity.ok(plats);
    }


    @GetMapping("/recommandations/client/{clientId}")
    public ResponseEntity<List<PlatDTO>> getRecommandations(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "10") int limit) {

        List<PlatDTO> plats = platService.getRecommandationsPourClient(clientId, limit);
        return ResponseEntity.ok(plats);
    }
}