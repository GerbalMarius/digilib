package org.digilib.library.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.exceptions.ResourceNotFoundException;
import org.digilib.library.models.Book;
import org.digilib.library.models.BookCopy;
import org.digilib.library.models.Status;
import org.digilib.library.models.dto.book.LibraryBookData;
import org.digilib.library.repositories.BookCopyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookService bookService;

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

        if (copy.getStatus() != Status.AVAILABLE) {
            throw new IllegalStateException("Copy is not available for reservation.");
        }

        copy.setStatus(Status.RESERVED);

        return LibraryBookData.wrap(copy);
    }
}
