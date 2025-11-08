package org.digilib.library.models.dto.book;

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
