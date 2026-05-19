package com.fst.rsi.resto.entity.enums;


public enum TypeOperationStock {
    AJOUT,              // Ajout de stock
    RETRAIT,            // Retrait de stock
    AJUSTEMENT,         // Ajustement d'inventaire
    RECEPTION_COMMANDE, // Réception commande fournisseur
    PERTE,              // Perte (périmé, cassé)
    INVENTAIRE          // Comptage inventaire
}