package edu.um.cps2002.logic;



public class Booking {
    private String bookingId;
    private String customerName;
    private Screening screening;
    private int seats;

    public Booking(String bookingId, String customerName, Screening screening, int seats) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.screening  = screening;
        this.seats = seats;
        screening.bookSeats(seats);
    }

}
