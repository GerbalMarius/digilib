package org.digilib.library.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.digilib.library.models.dto.BookCreateData;

import java.time.Instant;

@Entity
@Data
@Table(name = "books")
public final class Book {

    @Id
    @Column(name = "isbn", length = 13)
    private String isbn;

    @Column(name = "title", length = 300, nullable = false)
    private String title;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;


    public Book() {}

    public Book(String isbn, String title) {
        this.isbn = isbn;
        this.title = title;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Book createFrom(BookCreateData createData) {
        return new Book(createData.isbn(),  createData.title());

    }

}
