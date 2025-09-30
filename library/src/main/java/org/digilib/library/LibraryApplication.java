package org.digilib.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LibraryApplication {

    public static final String BACK_URL = "http://localhost:8080";

    static void main(String[] args) {
		SpringApplication.run(LibraryApplication.class, args);
	}

}
