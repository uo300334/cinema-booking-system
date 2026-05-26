package edu.um.cps2002.logic;

import java.util.Objects;

/**
 * BookingService - Service layer for managing bookings
 * 
 * Now uses BookingFactory to create different types of bookings
 * instead of hardcoding the Booking class instantiation
 */
public class BookingService {
    private CinemaDatabase db = CinemaDatabase.getInstance();

    /**
     * Creates a booking with a specific type
     *
     * @param name Customer name
     * @param screeningId ID of the screening
     * @param seats Number of seats to book
     * @param bookingType Type of booking (STANDARD, STUDENT, SENIOR)
     * @return The created Booking object
     */
    public Booking createBooking(String name, String screeningId, int seats, String bookingType) {
        Screening screening = db.getScreenings().stream()
                .filter(s -> s.getId().equals(screeningId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Screening not found"));

        // Use BookingFactory to create the appropriate booking type
        Booking booking = BookingFactory.createBooking(bookingType, name, screening, seats);
        db.getBookings().add(booking);
        return booking;
    }

    /**
     * Creates a standard booking (for backward compatibility)
     *
     * @param name Customer name
     * @param screeningId ID of the screening
     * @param seats Number of seats to book
     * @return The created Booking object
     */
    public Booking createBooking(String name, String screeningId, int seats) {
        return createBooking(name, screeningId, seats, "STANDARD");
    }

    /**
     * Finds a screening by ID
     *
     * @param screeningId The ID of the screening
     * @return The Screening object, or null if not found
     */
    public Screening findScreeningById(String screeningId) {
        for(Screening screening: db.getScreenings()){
            if(Objects.equals(screening.getId(), screeningId))
                return screening;
        }
        return null;
    }
}
