package edu.um.cps2002.logic;

public class SeniorBooking extends Booking {
    public SeniorBooking(String bookingId, String customerName, Screening screening, int seats) {
        super(bookingId, customerName, screening, seats);
        this.discount = 30; // 30% discount for seniors
    }

    @Override
    public double calculatePrice(double basePrice) {
        return (basePrice * seats) * (1 - discount / 100.0); // Apply 30% discount
    }

    @Override
    public String toString() {
        return "SeniorBooking{" + super.toString() + "}";
    }
}
