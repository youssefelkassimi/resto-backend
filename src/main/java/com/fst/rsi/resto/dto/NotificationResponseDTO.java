package com.fst.rsi.resto.dto;



import com.fst.rsi.resto.entity.enums.TypeNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private Long idNotification;
    private Long idCommande;
    private String message;
    private LocalDateTime dateEnvoi;
    private TypeNotification type;
}
