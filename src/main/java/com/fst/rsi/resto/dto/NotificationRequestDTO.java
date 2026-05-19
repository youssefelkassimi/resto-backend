package com.fst.rsi.resto.dto;

import com.fst.rsi.resto.entity.enums.TypeNotification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {

    @NotNull(message = "L'ID de la commande est obligatoire")
    private Long idCommande;

    @NotNull(message = "Le type de notification est obligatoire")
    private TypeNotification type;

    @NotBlank(message = "Le message est obligatoire")
    private String message;
}