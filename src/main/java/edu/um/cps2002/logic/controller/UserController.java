package edu.um.cps2002.logic.controller;
import edu.um.cps2002.logic.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;



//To execute the page of user enter into http://localhost:8080/user/screenings
@RestController
@RequestMapping("/user") // Changed from /api to /user
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
            bookingService.createBooking("User", id, seats);

            return ResponseEntity.ok(screening);

        } catch (IllegalArgumentException e) {
            // 5. Catch the error and return the string message instead
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/select-screen")
    public ResponseEntity<?> selectScreen(@RequestParam String screeningId) {
        try {
            Screening screening = CinemaDatabase.getInstance().findScreeningById(screeningId);
            
            if (screening == null) {
                return ResponseEntity.ok("Error: Screening ID not found.");
            }
            
            return ResponseEntity.ok("You have selected screening: " + screening.getMovieTitle());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }
}
