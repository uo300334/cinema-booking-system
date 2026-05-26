package edu.um.cps2002.logic;

public class StudentBooking extends Booking {
    public StudentBooking(String bookingId, String customerName, Screening screening, int seats) {
        super(bookingId, customerName, screening, seats);
        this.discount = 20; // 20% discount for students
    }

    @Override
    public double calculatePrice(double basePrice) {
        return (basePrice * seats) * (1 - discount / 100.0); // Apply 20% discount
    }

    @Override
    public String toString() {
        return "StudentBooking{" + super.toString() + "}";
    }
}
