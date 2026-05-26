package edu.um.cps2002.logic;

public abstract class Booking {
    protected String bookingId;
    protected String customerName;
    protected Screening screening;
    protected int seats;
    protected double discount;

    public Booking(String bookingId, String customerName, Screening screening, int seats) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.screening = screening;
        this.seats = seats;
        this.discount = 0;
        screening.bookSeats(seats);
    }

    public String getBookingId() { return bookingId; }
    public String getCustomerName() { return customerName; }
    public Screening getScreening() { return screening; }
    public int getSeats() { return seats; }
    public double getDiscount() { return discount; }

    // Abstract method - each booking type applies discount differently
    public abstract double calculatePrice(double basePrice);

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId='" + bookingId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", seats=" + seats +
                ", discount=" + discount + "%"+
                '}';
    }
}
