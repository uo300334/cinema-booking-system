package edu.um.cps2002.logic;

import java.util.UUID;

public class BookingService {
    private CinemaDatabase db = CinemaDatabase.getInstance();

    public Booking createBooking(String name, String screeningId, int seats) {
        Screening screening = db.getScreenings().stream()
                .filter(s -> s.getId().equals(screeningId))
                .findFirst()
                .orElseThrow();

        if (screening.getAvailableSeats() >= seats) {
            screening.setAvailableSeats(screening.getAvailableSeats() - seats);
            Booking booking = new Booking(UUID.randomUUID().toString(), name, screeningId, seats);
            db.getBookings().add(booking);
            return booking;
        }
        throw new RuntimeException("No seats");
    }
}