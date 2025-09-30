package org.digilib.library.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.digilib.library.models.dto.BookCreateView;
import org.digilib.library.models.dto.BookUpdateView;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "books", indexes = {
        @Index(name = "idx_books_title", columnList = "title"),
        @Index(name = "idx_books_isbn", columnList = "isbn", unique = true)
})
@Builder
@AllArgsConstructor
public final class Book {

    @Id
    @Column(name = "isbn", length = 13, updatable = false)
    @Size(min = 10, max = 13)
    private String isbn;

    @Column(name = "title", length = 300, nullable = false)
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(name = "image_url", length = 800,  nullable = false)
    private String imageUrl;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "language", length = 10)
    private String language;

    @Column(name = "edition", length = 50)
    private String edition;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;


    public Book() {}

    public static Book createFrom(BookCreateView createData) {
        return Book.builder()
                .isbn(createData.isbn())
                .title(createData.title())
                .summary(createData.summary())
                .imageUrl(createData.imageUrl())
                .pageCount(createData.pageCount())
                .publicationDate(createData.publicationDate())
                .language(createData.language())
                .edition(createData.edition())
                .build();
    }

    public Book updateFrom(BookUpdateView updateData) {

        if (updateData.title() != null) {
            this.setTitle(updateData.title().trim());
        }
        if (updateData.summary() != null) {
            this.setSummary(updateData.summary().trim());
        }
        if (updateData.imageUrl() != null) {
            this.setImageUrl(updateData.imageUrl().trim());
        }
        if (updateData.pageCount() != null) {
            this.setPageCount(updateData.pageCount());
        }
        if (updateData.publicationDate() != null) {
            this.setPublicationDate(updateData.publicationDate());
        }
        if (updateData.language() != null) {
            this.setLanguage(updateData.language().trim());
        }
        if (updateData.edition() != null) {
            this.setEdition(updateData.edition().trim());
        }
        this.setUpdatedAt(Instant.now());

        return this;
    }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

}
