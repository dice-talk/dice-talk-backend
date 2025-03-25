package com.example.dice_talk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DiceTalkApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiceTalkApplication.class, args);
	}



}
