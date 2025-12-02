package org.digilib.library.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.exceptions.ResourceNotFoundException;
import org.digilib.library.models.*;
import org.digilib.library.models.dto.book.LibraryBookData;
import org.digilib.library.repositories.BookCopyRepository;
import org.digilib.library.repositories.ReservationRepository;
import org.digilib.library.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookService bookService;

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public List<LibraryBookData> findCopiesForBook(String rawIsbn) {
        String normalized = rawIsbn.replaceAll("[-\\s]", "");
        Book book = bookService.findByIsbn(normalized)
                .orElseThrow(() -> ResourceNotFoundException.of(Book.class, rawIsbn));

        return bookCopyRepository.findByBookIsbnWithLibrary(book.getIsbn())
                .stream()
                .map(LibraryBookData::wrap)
                .toList();
    }

    @Transactional
    public LibraryBookData reserveCopy(long copyId, String email) {
        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> ResourceNotFoundException.of(BookCopy.class, copyId));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ResourceNotFoundException.of(User.class, email));

        if (copy.getStatus() != Status.AVAILABLE) {
            throw new IllegalStateException("Copy is not available for reservation.");
        }

        copy.setStatus(Status.RESERVED);

        Reservation reservation = Reservation.builder()
                .user(user)
                .book(copy.getBook())
                .barcode(copy.getBarcode())
                .build();
        reservationRepository.save(reservation);

        return LibraryBookData.wrap(copy);
    }
}
