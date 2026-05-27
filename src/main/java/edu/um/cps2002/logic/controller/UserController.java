package edu.um.cps2002.logic.controller;
import edu.um.cps2002.logic.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;



//To execute the page of user enter into http://localhost:8080/user/screenings
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*") // Allows VS Code to talk to your Java server
public class UserController {
    private BookingService bookingService = new BookingService();

    @GetMapping("/screenings")
    public ResponseEntity<?> listScreenings() {
        // Return the actual List object, Spring will convert it to JSON
        try {
            List<Screening> screenings = CinemaDatabase.getInstance().getAllScreenings();
            return ResponseEntity.ok(screenings);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/select-screen")
    public ResponseEntity<?> selectScreening(@RequestParam Long screeningId) {
        try {
            // Validate screeningId
            if (screeningId == null || screeningId < 1) {
                return ResponseEntity.badRequest().body("Error: Screening ID not found.");
            }

            // Find the screening
            Screening screening = CinemaDatabase.getInstance().findScreeningById(String.valueOf(screeningId));
            
            if (screening == null) {
                return ResponseEntity.badRequest().body("Error: Screening ID not found.");
            }
            
            return ResponseEntity.ok("You have selected screening: " + screeningId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
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
