package edu.um.cps2002.logic;

import java.util.UUID;

/**
 * BookingFactory - Implements the Factory Pattern
 * 
 * This factory creates different types of Booking objects based on the booking type provided.
 * It centralizes the creation logic and makes it easy to add new booking types in the future.
 */
public class BookingFactory {

    /**
     * Enum to represent different booking types
     */
    public enum BookingType {
        STANDARD,
        STUDENT,
        SENIOR
    }

    /**
     * Creates a Booking object based on the specified type
     *
     * @param bookingType The type of booking to create (STANDARD, STUDENT, SENIOR)
     * @param customerName The name of the customer
     * @param screening The screening for which the booking is made
     * @param seats The number of seats to book
     * @return A Booking object of the specified type
     * @throws IllegalArgumentException if an unknown booking type is provided
     */
    public static Booking createBooking(BookingType bookingType, String customerName, 
                                        Screening screening, int seats) {
        String bookingId = UUID.randomUUID().toString();

        switch (bookingType) {
            case STANDARD:
                return new StandardBooking(bookingId, customerName, screening, seats);
            case STUDENT:
                return new StudentBooking(bookingId, customerName, screening, seats);
            case SENIOR:
                return new SeniorBooking(bookingId, customerName, screening, seats);
            default:
                throw new IllegalArgumentException("Unknown booking type: " + bookingType);
        }
    }

    /**
     * Alternative method that accepts booking type as a String
     * Useful for REST API calls
     *
     * @param bookingTypeStr String representation of booking type
     * @param customerName The name of the customer
     * @param screening The screening for which the booking is made
     * @param seats The number of seats to book
     * @return A Booking object of the specified type
     */
    public static Booking createBooking(String bookingTypeStr, String customerName,
                                        Screening screening, int seats) {
        try {
            BookingType type = BookingType.valueOf(bookingTypeStr.toUpperCase());
            return createBooking(type, customerName, screening, seats);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown booking type: " + bookingTypeStr +
                    ". Valid types are: STANDARD, STUDENT, SENIOR");
        }
    }
}
