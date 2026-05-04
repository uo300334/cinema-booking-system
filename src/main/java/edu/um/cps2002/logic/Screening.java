package edu.um.cps2002.logic;

import java.sql.Time;

public class Screening {
    private String id;
    private String movieTitle;
    private int screenNumber;
    private int seats;
    private int availableSeats;

    public Screening(String id, String movieTitle, int screenNumber, int seats) {
        this.id = id;
        this.movieTitle = movieTitle;
        this.screenNumber = screenNumber;
        this.seats = seats;
        this.availableSeats=seats;
    }

    public String getId() { return id; }
    public String getMovieTitle() { return movieTitle; }
    public int getAvailableSeats() { return availableSeats; }
    public int getScreenNumber(){return screenNumber;}
    public int getSeats(){return seats;}
    public void setSeats(int seats) { this.seats = seats; }
    public void setId(String id) { this.id = id; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public void setScreenNumber(int screenNumber) { this.screenNumber = screenNumber; }
    private void setAvailableSeats(int availableSeats) {
        this.availableSeats=availableSeats;
    }
    public void bookSeats(int bookingSeats) {
        if(bookingSeats>availableSeats){
            throw new IllegalArgumentException(/*"The number of seats to be reserved  cannot be greater than the available seats"*/);
        }else{
            setAvailableSeats(availableSeats-bookingSeats);
        }
    }


}
