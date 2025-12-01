package org.digilib.library.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_reservations_id", columnList = "id", unique = true),
        @Index(name = "idx_reservations_book_user", columnList = "book_isbn, user_id", unique = true)
})
@Data
@AllArgsConstructor
@Builder
public final class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_isbn", referencedColumnName = "isbn", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        this.reservedAt = now;
        this.expiresAt = now.plus(7, ChronoUnit.DAYS);
    }

    public Reservation() {}
}
