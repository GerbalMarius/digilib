package org.digilib.library.models.dto;

import org.digilib.library.models.Author;

import java.time.LocalDate;

public record AuthorData(

        long id,

        String firstName,

        String lastName,

        LocalDate birthDate,

        LocalDate deathDate
) {
    public static AuthorData wrapAuthor(Author author) {
        return new AuthorData(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getBirthDate(),
                author.getDeathDate()
        );
    }
}
