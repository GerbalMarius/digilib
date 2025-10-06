package org.digilib.library.services;

import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.ResourceNotFoundException;
import org.digilib.library.models.Book;
import org.digilib.library.models.BookCopy;
import org.digilib.library.models.Library;
import org.digilib.library.models.Status;
import org.digilib.library.models.dto.BookCopyCreateView;
import org.digilib.library.models.dto.BookCopyData;
import org.digilib.library.models.dto.BookCopyUpdateView;
import org.digilib.library.models.dto.LibraryData;
import org.digilib.library.repositories.BookCopyRepository;
import org.digilib.library.repositories.BookRepository;
import org.digilib.library.repositories.LibraryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static org.digilib.library.utils.Params.setIfPresent;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryRepository libraryRepository;

    private final BookCopyRepository bookCopyRepository;

    private final BookRepository bookRepository;


    public Page<LibraryData> findAll(Pageable pageable) {
        return libraryRepository.findAll(pageable)
                .map(LibraryData::wrapLibrary);
    }

    public Library findById(long id){
        return libraryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(Library.class, id));
    }

    public Page<BookCopyData> findCopiesByLibrary(Library library, Pageable pageable) {
        Page<BookCopy> copies = bookCopyRepository.findAllByLibrary(library, pageable);

        return copies.map(BookCopyData::wrapCopy);
    }

    public BookCopyData addBookCopyTo(Library library, BookCopyCreateView bookCopyCreateView) {
        Book book = bookRepository.findByIsbn(bookCopyCreateView.bookIsbn())
                .orElseThrow(() -> ResourceNotFoundException.of(Book.class, bookCopyCreateView.bookIsbn()));

        BookCopy bookCopy = BookCopy.builder()
                .barcode(bookCopyCreateView.barcode())
                .book(book)
                .library(library)
                .status(Status.AVAILABLE)
                .build();

        BookCopy saved = bookCopyRepository.save(bookCopy);

        return BookCopyData.wrapCopy(saved);
    }

    public BookCopyData updateBookCopy(Library library, long bookCopyId, BookCopyUpdateView bookCopyUpdateView) {
        BookCopy bookCopy = bookCopyRepository.findBookCopyByIdAndLibrary(bookCopyId, library)
                .orElseThrow(() -> ResourceNotFoundException.of(BookCopy.class, bookCopyId));

        setIfPresent(bookCopyUpdateView.barcode(), String::trim, bookCopy::setBarcode);
        setIfPresent(bookCopyUpdateView.status(), bookCopy::setStatus);

        BookCopy saved = bookCopyRepository.save(bookCopy);

        return BookCopyData.wrapCopy(saved);
    }

    public void deleteBookCopy(Library library,  long bookCopyId) {
        BookCopy bookCopy = bookCopyRepository.findBookCopyByIdAndLibrary(bookCopyId, library)
                .orElseThrow(() -> ResourceNotFoundException.of(BookCopy.class, bookCopyId));

        bookCopyRepository.delete(bookCopy);
    }

}
