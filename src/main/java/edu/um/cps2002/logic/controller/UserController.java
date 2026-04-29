package edu.um.cps2002.logic.controller;
import edu.um.cps2002.logic.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/user")
public class UserController {
    private BookingService bookingService = new BookingService();

    @GetMapping("/screenings")
    public List<Screening> listScreenings() {
        return CinemaDatabase.getInstance().getScreenings();
    }

    @PostMapping("/book")
    public Booking book(@RequestParam String name, @RequestParam String id, @RequestParam int seats) {
        return bookingService.createBooking(name, id, seats);
    }
}
