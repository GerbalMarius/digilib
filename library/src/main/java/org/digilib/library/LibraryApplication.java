package org.digilib.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {

    public static final int PAGE_SIZE = 15;

	public static void main(String[] args) {
		SpringApplication.run(LibraryApplication.class, args);
	}

}
