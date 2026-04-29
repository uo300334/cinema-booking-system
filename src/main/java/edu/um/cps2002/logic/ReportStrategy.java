package edu.um.cps2002.logic;

import java.util.List;

public interface ReportStrategy {
    String generate(List<Screening> screenings);
}
