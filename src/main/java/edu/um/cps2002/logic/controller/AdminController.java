package edu.um.cps2002.logic.controller;

import edu.um.cps2002.logic.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private ReportStrategy report = new OccupancyReport();

    @GetMapping("/report")
    public String viewReport() {
        return report.generate(CinemaDatabase.getInstance().getScreenings());
    }
}
