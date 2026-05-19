package com.fst.rsi.resto.entity.enums;


public enum TypeAlerte {
    STOCK_BAS,          // Stock en dessous du seuil
    STOCK_EPUISE,       // Stock à zéro
    DATE_PEREMPTION,    // Proche de la péremption
    PERIME              // Déjà périmé
}
