package com.mercury;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MercuryOrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MercuryOrderServiceApplication.class, args);
	}

}
