package edu.um.cps2002.logic;



public class Booking {
    private String bookingId;
    private String customerName;
    private String screeningId;
    private int seats;

    public Booking(String bookingId, String customerName, String screeningId, int seats) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.screeningId = screeningId;
        this.seats = seats;
    }
}
