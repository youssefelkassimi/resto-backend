package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.Commande;
import com.fst.rsi.resto.entity.Notification;
import com.fst.rsi.resto.entity.enums.TypeNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
    List<Notification> findByCommande(Commande commande);

    List<Notification> findByType(TypeNotification type);

    List<Notification> findByCommandeIdCommandeOrderByDateEnvoiDesc(Long idCommande);


    List<Notification> findByDateEnvoiAfterOrderByDateEnvoiDesc(LocalDateTime date);

    List<Notification> findByDateEnvoiBetween(LocalDateTime debut, LocalDateTime fin);

    List<Notification> findByDateEnvoiBefore(LocalDateTime date);
}

