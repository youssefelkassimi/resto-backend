package com.fst.rsi.resto.entity.enums;


import lombok.Getter;

@Getter
public enum UserRole {
    CLIENT("Client", "Utilisateur client du restaurant"),
    SERVEUR("Serveur", "Personnel de service en salle"),
    MANAGER("Manager", "Gestionnaire du restaurant"),
    LIVREUR("Livreur", "Personnel de livraison"),
    RESPONSABLE_STOCK("Responsable Stock", "Gestionnaire des stocks et approvisionnements"),
    RESPONSABLE_CUISINE("Responsable Cuisine", "Chef de cuisine"),
    ADMIN("Administrateur", "Administrateur système");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}