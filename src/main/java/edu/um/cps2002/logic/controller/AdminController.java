package edu.um.cps2002.logic.controller;

import edu.um.cps2002.logic.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private BookingService bookingService = new BookingService();
    private ReportStrategy report = new OccupancyReport();

    @GetMapping("/report")
    public String viewReport() {
        return report.generate(CinemaDatabase.getInstance().getScreenings());

    }

    // CREATE a new screening
    @PostMapping("/admin/screenings")
    public Screening addScreening(@RequestBody Screening newScreening) {
        CinemaDatabase.getInstance().getAllScreenings().add(newScreening);
        return newScreening;
    }

    // UPDATE an existing screening
    @PutMapping("/admin/screenings/{id}")
    public String updateScreening(@PathVariable String id, @RequestBody Screening updatedData) {
        Screening s = bookingService.findScreeningById(id);
        if (s != null) {
            s.setMovieTitle(updatedData.getMovieTitle());
            s.setScreenNumber(updatedData.getScreenNumber());
            s.setSeats(updatedData.getSeats());
            return "Updated successfully";
        }
        return "Error: Screening not found";
    }

    // DELETE a screening
    @DeleteMapping("/admin/screenings/{id}")
    public String deleteScreening(@PathVariable String id) {
        boolean removed = CinemaDatabase.getInstance().getAllScreenings()
                .removeIf(s -> s.getId().equals(id));
        return removed ? "Deleted" : "Error";
    }
}
