package com.example.plataforma_eventos_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlataformaEventosBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlataformaEventosBackendApplication.class, args);
	}

}
