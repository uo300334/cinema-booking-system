package edu.um.cps2002.logic.controller;

import edu.um.cps2002.logic.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    private BookingService bookingService = new BookingService();
    private ReportStrategy report = new OccupancyReport();

    @GetMapping("/report")
    public String viewReport() {
        return report.generate(CinemaDatabase.getInstance().getScreenings());

    }

    // UserController.java

    @PostMapping("/screenings")
    public ResponseEntity<?> createScreening(@RequestBody Screening newScreening) {
        try {
            // Validation: Ensure the ID isn't a duplicate
            boolean exists = CinemaDatabase.getInstance().getAllScreenings()
                    .stream().anyMatch(s -> s.getId().equals(newScreening.getId()));

            if (exists) {
                return ResponseEntity.badRequest().body("Error: Screening ID already exists.");
            }

            // Add to the Singleton list (Single Source of Truth)
            CinemaDatabase.getInstance().addScreening(newScreening);

            System.out.println("Created screening: " + newScreening.getMovieTitle());
            return ResponseEntity.ok(newScreening);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    // UPDATE an existing screening
    @PutMapping("/screenings/{id}") // 👈 Ensure there isn't a duplicate "/admin/admin" segment here
    @CrossOrigin(origins = "*")    // 👈 IMPORTANT FOR BROWSER PERMISSIONS
    public ResponseEntity<String> updateScreening(@PathVariable String id, @RequestBody Screening updatedData) {
        Screening s = bookingService.findScreeningById(id);
        if (s != null) {
            s.setMovieTitle(updatedData.getMovieTitle());
            s.setScreenNumber(updatedData.getScreenNumber());
            s.setSeats(updatedData.getSeats());
            return ResponseEntity.ok("Updated successfully"); // Standardize with a proper wrapper response status
        }
        return ResponseEntity.status(404).body("Error: Screening not found");
    }

    // DELETE a screening

    @DeleteMapping("/screenings/{id}")
    @CrossOrigin(origins = "*")
    public String deleteScreening(@PathVariable String id) {
        boolean removed = CinemaDatabase.getInstance().getAllScreenings()
                .removeIf(s -> s.getId().equals(id));
        return removed ? "Deleted" : "Error";
    }
}
