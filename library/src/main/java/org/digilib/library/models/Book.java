package org.digilib.library.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "edition", length = 15)
    private String edition;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Genre genre;

    @Basic(fetch = FetchType.LAZY)
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "book_authors",
            joinColumns = {@JoinColumn(name = "book_id", referencedColumnName = "isbn")},
            inverseJoinColumns = {@JoinColumn(name = "author_id", referencedColumnName = "id")}
    )
    @Builder.Default
    private List<Author> authors =  new ArrayList<>();

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;


    public Book() {}


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
