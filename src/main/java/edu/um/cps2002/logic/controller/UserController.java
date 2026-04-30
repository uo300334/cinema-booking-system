package edu.um.cps2002.logic.controller;
import edu.um.cps2002.logic.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;



//To execute the page of user enter into http://localhost:8080/user/screenings
@RestController
@RequestMapping("/user")
public class UserController {
    private BookingService bookingService = new BookingService();
    @GetMapping("/screenings")
    public String listScreenings() {
        return "The available screens are:%n "+ CinemaDatabase.getInstance().getScreeningsString();
    }

    @GetMapping("/book")
    public Booking book(@RequestParam String name, @RequestParam String id, @RequestParam int seats) {
        return bookingService.createBooking(name, id, seats);
    }
    @GetMapping("/select-screen")
    public String selectScreen(@RequestParam String screeningId) {
        Screening selected = bookingService.findScreeningById(screeningId);

        if (selected != null) {
            return "You have selected: " + selected.getMovieTitle() ;
        } else {
            return "Error: Screening ID not found.";
        }
    }
}
