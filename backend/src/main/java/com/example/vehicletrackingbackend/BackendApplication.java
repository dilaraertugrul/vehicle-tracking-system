package com.example.vehicletrackingbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;
// @Scheduled ile belirli aralıklarla çalışan metodları etkinleştirmek için kullanılır.

@SpringBootApplication // Bu ana uygulama sınıfı. Buradan uygulama başlatılır
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
