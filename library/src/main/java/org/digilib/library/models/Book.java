package org.digilib.library.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public final class Book {

    @Id
    @Column(name = "isbn", length = 15)
    private String isbn;

    @Column(name = "title", length = 300, nullable = false)
    private String title;

}
