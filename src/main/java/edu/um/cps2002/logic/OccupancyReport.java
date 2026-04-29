package edu.um.cps2002.logic;

import java.util.List;

public class OccupancyReport implements ReportStrategy{
    @Override
    public String generate(List<Screening> screenings) {
        StringBuilder sb = new StringBuilder("Occupancy Report:\n");
        for (Screening s : screenings) {
            sb.append(s.getMovieTitle()).append(": ").append(s.getAvailableSeats()).append(" left\n");
        }
        return sb.toString();
    }
}
