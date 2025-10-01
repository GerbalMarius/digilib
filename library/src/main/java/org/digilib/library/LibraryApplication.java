package org.digilib.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LibraryApplication {

    public static final String BACK_URL = "http://localhost:8080";

    public static final int PAGE_SIZE = 15;

    public  static void main(String[] args) {
		SpringApplication.run(LibraryApplication.class, args);
	}

}
