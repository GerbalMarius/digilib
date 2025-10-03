package org.digilib.library.models.dto;

import org.digilib.library.models.Book;
import org.digilib.library.models.BookCopy;
import org.digilib.library.models.Status;

public record BookCopyData(
        long id,

        String barcode,

        Status status,

        BookData bookData
) {
    public static BookCopyData wrapCopy(BookCopy bookCopy) {
        return new BookCopyData(
                bookCopy.getId(),
                bookCopy.getBarcode(),
                bookCopy.getStatus(),
                BookData.wrapBook(bookCopy.getBook())
        );
    }
}
