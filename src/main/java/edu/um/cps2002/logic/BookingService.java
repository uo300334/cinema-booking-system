package edu.um.cps2002.logic;

import java.util.Objects;
import java.util.UUID;

public class BookingService {
    private CinemaDatabase db = CinemaDatabase.getInstance();

    public Booking createBooking(String name, String screeningId, int seats) {
        Screening screening = db.getScreenings().stream()
                .filter(s -> s.getId().equals(screeningId))
                .findFirst()
                .orElseThrow();

        Booking booking = new Booking(UUID.randomUUID().toString(), name, screening, seats);
        db.getBookings().add(booking);
        return booking;
    }

    public Screening findScreeningById(String screeningId) {
        for(Screening screening: db.getScreenings()){
            if(Objects.equals(screening.getId(), screeningId))
                return screening;
        }

        return null;
    }
}