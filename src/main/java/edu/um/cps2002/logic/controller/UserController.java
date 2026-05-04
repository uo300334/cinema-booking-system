package edu.um.cps2002.logic.controller;
import edu.um.cps2002.logic.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;



//To execute the page of user enter into http://localhost:8080/user/screenings
@RestController
@RequestMapping("/api") // Changed to /api for best practice
@CrossOrigin(origins = "*") // Allows VS Code to talk to your Java server
public class UserController {
    private BookingService bookingService = new BookingService();

    @GetMapping("/screenings")
    public List<Screening> listScreenings() {
        // Return the actual List object, Spring will convert it to JSON
        return CinemaDatabase.getInstance().getAllScreenings();
    }

    @GetMapping("/book")
    public ResponseEntity<?> book(@RequestParam String id, @RequestParam int seats) {
        // 1. Find the screening
        Screening screening = CinemaDatabase.getInstance().findScreeningById(id);

        if (screening == null) {
            return ResponseEntity.status(404).body("Screening not found.");
        }

        try {
            bookingService.createBooking("User", id,seats);

            return ResponseEntity.ok(screening);

        } catch (IllegalArgumentException e) {
            // 5. Catch the error and return the string message instead
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}