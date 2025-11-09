package org.digilib.library.repositories;

import jakarta.transaction.Transactional;
import org.digilib.library.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Page<User> findAllByIdNot(long id, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isDisabled = :disabled WHERE u.id = :id")
    void updateDisabled(@Param("id") long id, @Param("disabled") boolean disabled);
}
