package org.digilib.library.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.ResourceNotFoundException;
import org.digilib.library.models.Book;
import org.digilib.library.models.BookCopy;
import org.digilib.library.models.Library;
import org.digilib.library.models.Status;
import org.digilib.library.models.dto.*;
import org.digilib.library.repositories.BookCopyRepository;
import org.digilib.library.repositories.BookRepository;
import org.digilib.library.repositories.LibraryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.digilib.library.utils.Params.setIfPresent;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryRepository libraryRepository;

    private final BookCopyRepository bookCopyRepository;

    private final BookRepository bookRepository;

    private static final Pattern BARCODE_SUFFIX = Pattern.compile("-(\\d{4})$");


    public Page<LibraryData> findAll(Pageable pageable) {
        return libraryRepository.findAll(pageable)
                .map(LibraryData::wrapLibrary);
    }

    public Library findById(long id){
        return libraryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(Library.class, id));
    }

    @Transactional
    public LibraryData createNewLibrary(LibraryCreateView libraryCreateView) {

        String name = libraryCreateView.name();

        Library library = Library.builder()
                .email(libraryCreateView.email())
                .phoneNumber(libraryCreateView.phoneNumber())
                .address(libraryCreateView.address())
                .name(name)
                .build();

        Library saved = libraryRepository.save(library);

        List<String> isbns = libraryCreateView.isbns();
        if (isbns == null || isbns.isEmpty()) {
            return LibraryData.wrapLibrary(saved);
        }


        List<String> distinctIsbns = isbns.stream().distinct().collect(Collectors.toList());
        List<Book> foundBooks = bookRepository.findAllByIsbnIn(distinctIsbns);

        Map<String, Book> isbnToBook = foundBooks.stream()
                .collect(Collectors.toMap(Book::getIsbn, b -> b));

        List<String> missing = distinctIsbns.stream()
                .filter(isbn -> !isbnToBook.containsKey(isbn))
                .collect(Collectors.toList());

        if (!missing.isEmpty()) {
            throw ResourceNotFoundException.of(Book.class, missing);
        }


        int startNum = findNextBarcodeNumberForLibrary(saved.getId());


        String libCode = name.substring(0, 3).toUpperCase();

        List<BookCopy> copiesToSave = new ArrayList<>(isbns.size());
        for (String isbn : isbns) {
            Book book = isbnToBook.get(isbn);

            String barcode = String.format("%s-%04d", libCode, startNum++);

            BookCopy bookCopy = BookCopy.builder()
                    .status(Status.AVAILABLE)
                    .barcode(barcode)
                    .library(saved)
                    .book(book)
                    .build();

            saved.getBookCopies().add(bookCopy);

            copiesToSave.add(bookCopy);
        }

            bookCopyRepository.saveAll(copiesToSave);

        return LibraryData.wrapLibrary(saved);
    }


    private int findNextBarcodeNumberForLibrary(long libraryId) {
        Optional<String> maxBarcodeOpt = bookCopyRepository.findMaxBarcodeByLibraryId(libraryId);

        if (maxBarcodeOpt.isEmpty()) {
            return 1;
        }

        String maxBarcode = maxBarcodeOpt.get();
        Matcher m = BARCODE_SUFFIX.matcher(maxBarcode);
        if (m.find()) {
            int val = Integer.parseInt(m.group(1));
            return val + 1;
        }

        return 1;
    }

    @Transactional
    public void deleteLibrary(long libraryId) {
        Library library = findById(libraryId);

        libraryRepository.delete(library);
    }

    public Page<BookCopyData> findCopiesByLibrary(Library library, Pageable pageable) {
        Page<BookCopy> copies = bookCopyRepository.findAllByLibrary(library, pageable);

        return copies.map(BookCopyData::wrapCopy);
    }

    @Transactional
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

    @Transactional
    public BookCopyData updateBookCopy(Library library, long bookCopyId, BookCopyUpdateView bookCopyUpdateView) {
        BookCopy bookCopy = bookCopyRepository.findBookCopyByIdAndLibrary(bookCopyId, library)
                .orElseThrow(() -> ResourceNotFoundException.of(BookCopy.class, bookCopyId));

        setIfPresent(bookCopyUpdateView.barcode(), String::trim, bookCopy::setBarcode);
        setIfPresent(bookCopyUpdateView.status(), bookCopy::setStatus);

        BookCopy saved = bookCopyRepository.save(bookCopy);

        return BookCopyData.wrapCopy(saved);
    }

    @Transactional
    public void deleteBookCopy(Library library,  long bookCopyId) {
        BookCopy bookCopy = bookCopyRepository.findBookCopyByIdAndLibrary(bookCopyId, library)
                .orElseThrow(() -> ResourceNotFoundException.of(BookCopy.class, bookCopyId));

        bookCopyRepository.delete(bookCopy);
    }

}
