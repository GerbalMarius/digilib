package org.digilib.library.models.dto.book;

import org.digilib.library.models.BookCopy;
import org.digilib.library.models.Status;

public record LibraryBookData(
        long id,
        String barcode,
        Status status,
        long libraryId,
        String libraryName,
        String libraryAddress
) {
    public static LibraryBookData wrap(BookCopy copy) {
        var lib = copy.getLibrary();
        return new LibraryBookData(
                copy.getId(),
                copy.getBarcode(),
                copy.getStatus(),
                lib.getId(),
                lib.getName(),
                lib.getAddress()
        );
    }
}
