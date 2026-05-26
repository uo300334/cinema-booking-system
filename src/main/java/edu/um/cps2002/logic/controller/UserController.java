package edu.um.cps2002.logic.controller;
import edu.um.cps2002.logic.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * UserController - REST API endpoints for regular users
 * 
 * Handles screening listings and booking creation
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {
    private BookingService bookingService = new BookingService();

    /**
     * Get all available screenings
     */
    @GetMapping("/screenings")
    public List<Screening> listScreenings() {
        return CinemaDatabase.getInstance().getAllScreenings();
    }

    /**
     * Book seats for a screening with a specific booking type
     * 
     * @param id Screening ID
     * @param seats Number of seats
     * @param bookingType Type of booking (STANDARD, STUDENT, SENIOR) - defaults to STANDARD
     */
    @GetMapping("/book")
    public ResponseEntity<?> book(@RequestParam String id, @RequestParam int seats, 
                                   @RequestParam(defaultValue = "STANDARD") String bookingType) {
        Screening screening = CinemaDatabase.getInstance().findScreeningById(id);

        if (screening == null) {
            return ResponseEntity.status(404).body("Screening not found.");
        }

        try {
            Booking booking = bookingService.createBooking("User", id, seats, bookingType);
            return ResponseEntity.ok(booking);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
