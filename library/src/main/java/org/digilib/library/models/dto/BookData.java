package org.digilib.library.models.dto;

import org.digilib.library.models.Book;

import java.time.LocalDate;

public record BookData(
        String isbn,

        String title,

        String summary,

        String imageUrl,

        Integer pageCount,

        LocalDate publicationDate,

        String language,

        String edition
) {
    public static BookData wrapBook(Book book) {
        return new BookData(
                book.getIsbn(),
                book.getTitle(),
                book.getSummary(),
                book.getImageUrl(),
                book.getPageCount(),
                book.getPublicationDate(),
                book.getLanguage(),
                book.getEdition()
        );
    }
}
