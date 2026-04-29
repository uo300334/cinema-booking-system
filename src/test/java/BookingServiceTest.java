import edu.um.cps2002.logic.Booking;
import edu.um.cps2002.logic.BookingService;
import edu.um.cps2002.logic.CinemaDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {
    private BookingService bookingService;

    @BeforeEach
    public void setUp() {
        // Ensures we start with a fresh service before every test
        bookingService = new BookingService();
    }

    @Test
    public void testSuccessfulBooking() {
        // 1. Arrange: Find a screening ID from the DB
        String screeningId = CinemaDatabase.getInstance().getScreenings().get(0).getId();
        int initialSeats = CinemaDatabase.getInstance().getScreenings().get(0).getAvailableSeats();

        // 2. Act: Try to book 2 seats
        Booking booking = bookingService.createBooking("John Doe", screeningId, 2);

        // 3. Assert: Check if the booking exists and seats decreased
        assertNotNull(booking);
        assertEquals(initialSeats - 2, CinemaDatabase.getInstance().getScreenings().get(0).getAvailableSeats());
    }

    @Test
    public void testBookingFailsWhenNotEnoughSeats() {
        String screeningId = CinemaDatabase.getInstance().getScreenings().get(0).getId();

        // Act & Assert: Try to book 100 seats (limit is ~50) and expect an error
        assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking("Too Many", screeningId, 100);
        });
    }
}
