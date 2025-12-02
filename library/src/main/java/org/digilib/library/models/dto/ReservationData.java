package org.digilib.library.models.dto;

import org.digilib.library.models.Reservation;
import org.digilib.library.models.dto.book.BookData;

public record ReservationData(
        long id,
        String barcode,

        BookData book
) {
    public static ReservationData of(Reservation reservation) {
        return new ReservationData(reservation.getId(), reservation.getBarcode(), BookData.wrapBook(reservation.getBook()));
    }
}
