package com.fst.rsi.resto;

import com.fst.rsi.resto.dto.*;
import com.fst.rsi.resto.service.AuthenticationService;
import com.fst.rsi.resto.service.LivreurService;
import com.fst.rsi.resto.service.ManagerService;
import com.fst.rsi.resto.service.ServeurService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class test implements CommandLineRunner {
    private final ServeurService serveurService;
    private final AuthenticationService authenticationService;
    private final LivreurService livreurService;

    public test(ManagerService managerService, ServeurService serveurService, AuthenticationService authenticationService, LivreurService livreurService) {
        this.serveurService = serveurService;
        this.authenticationService = authenticationService;
        this.livreurService = livreurService;
    }

    @Override
    public void run(String... args) throws Exception {
//        ServeurRequestDTO serveur = ServeurRequestDTO.builder()
//                .idServeur(1L)
//                .nom("amine")
//                .prenom("daniel")
//                .email("amine@daniel.com")
//                .nombreHeuresSemaine(24)
//                .telephone("89780767656452343")
//                .salaire(BigDecimal.valueOf(100))
//                .password("amine@daniel.com")
//                .dateEmbauche(LocalDate.now())
//                .build();
//        serveurService.createServeur(serveur);
//
//        AuthResponseDTO authResponseDTO=authenticationService.login(LoginRequestDTO.builder().email("amine@daniel.com").password("amine@daniel.com").build());
//        System.out.println(authResponseDTO);
//
//        ServeurResponseDTO serveurResponseDTO = serveurService.getServeurByEmail("amine@daniel.com");
//        System.out.println(serveurResponseDTO);






//        LivreurRequestDTO livreurRequestDTO = LivreurRequestDTO.builder()
//                .nom("hamza")
//                .prenom("hamza")
//                .email("hamza@hamza.na")
//                .password("hamza@hamza.na")
//                .dateEmbauche(LocalDate.now())
//                .salaireBase(BigDecimal.valueOf(23354))
//                .telephone("666667754446666")
//                .vehicule("bican")
//                .build();
//        System.out.println(livreurService.createLivreur(livreurRequestDTO));


    }
}
