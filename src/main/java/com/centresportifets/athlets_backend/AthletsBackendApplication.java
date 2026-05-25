package com.centresportifets.athlets_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AthletsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AthletsBackendApplication.class, args);
	}

	@GetMapping("/")
	public String helloWorld() {
		return "Athlets";
	}
}
