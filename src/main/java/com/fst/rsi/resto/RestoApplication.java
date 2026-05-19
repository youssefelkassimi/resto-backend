package com.fst.rsi.resto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class RestoApplication {

	 static void main(String[] args) {
		SpringApplication.run(RestoApplication.class, args);
	}

}
