package edu.um.cps2002.logic;

import java.util.*;

public class CinemaDatabase {
    private static CinemaDatabase instance;
    private List<Screening> screenings = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();

    private CinemaDatabase() {
        screenings.add(new Screening("1", "Movie A", 1, 50));
        screenings.add(new Screening("2", "Movie B", 2, 50));
    }

    public static synchronized CinemaDatabase getInstance() {
        if (instance == null) instance = new CinemaDatabase();
        return instance;
    }

    public List<Screening> getScreenings() { return screenings; }
    public String getScreeningsString(){
        String s = "";
        for(Screening screen: screenings){
            s+=" Id:"+screen.getId()+" Movie Title:"+screen.getMovieTitle()+" Available Seats:"+Integer.toString(
                    screen.getAvailableSeats())+"%n";
        }return s;
    }
    public List<Booking> getBookings() { return bookings; }
}
