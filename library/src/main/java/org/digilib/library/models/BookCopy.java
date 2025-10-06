package org.digilib.library.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "book_copies", indexes = {
        @Index(name = "idx_book_copies_id", columnList = "id", unique = true),
        @Index(name = "idx_book_copies_barcode", columnList = "barcode", unique = true)
})
@AllArgsConstructor
@Builder
@Data
public final class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_isbn", referencedColumnName = "isbn")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "library_id", referencedColumnName = "id")
    private Library library;

    @Column(name = "barcode", unique = true, nullable = false, length = 50)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status = Status.AVAILABLE;

    public BookCopy() {}
}
