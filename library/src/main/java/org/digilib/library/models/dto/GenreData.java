package org.digilib.library.models.dto;

import org.digilib.library.models.Genre;

public record GenreData(long id, String title) {

    public static GenreData wrapGenre(Genre genre) {
        return new GenreData(genre.getId(), genre.getTitle());
    }
}
