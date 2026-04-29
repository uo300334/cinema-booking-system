package edu.um.cps2002.logic;

import java.sql.Time;

public class Screening {
    private String id;
    private String movieTitle;
    private int screenNumber;
    private int availableSeats;

    public Screening(String id, String movieTitle, int screenNumber, int availableSeats) {
        this.id = id;
        this.movieTitle = movieTitle;
        this.screenNumber = screenNumber;
        this.availableSeats = availableSeats;
    }

    public String getId() { return id; }
    public String getMovieTitle() { return movieTitle; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
}
