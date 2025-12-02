package org.digilib.library.repositories;

import org.digilib.library.models.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findAllByUserId(long userId, Pageable pageable);
}
