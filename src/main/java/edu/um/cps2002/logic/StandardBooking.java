package edu.um.cps2002.logic;

public class StandardBooking extends Booking {
    public StandardBooking(String bookingId, String customerName, Screening screening, int seats) {
        super(bookingId, customerName, screening, seats);
        this.discount = 0; // No discount for standard bookings
    }

    @Override
    public double calculatePrice(double basePrice) {
        return basePrice * seats; // Full price
    }

    @Override
    public String toString() {
        return "StandardBooking{" + super.toString() + "}";
    }
}
