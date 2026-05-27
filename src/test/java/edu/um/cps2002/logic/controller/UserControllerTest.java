package edu.um.cps2002.logic.controller;

import edu.um.cps2002.logic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for UserController
 * Tests user operations: List screenings, select screening, book seats
 */
public class UserControllerTest {
    
    private UserController userController;
    private CinemaDatabase db;
    
    @BeforeEach
    public void setUp() {
        userController = new UserController();
        db = CinemaDatabase.getInstance();
        // Clear and set up test data
        db.getAllScreenings().clear();
        db.getBookings().clear();
        db.getAllScreenings().add(new Screening("1", "Avatar", 1, 50));
        db.getAllScreenings().add(new Screening("2", "Inception", 2, 40));
        db.getAllScreenings().add(new Screening("3", "Interstellar", 3, 60));
    }
    
    // ===== LIST SCREENINGS Tests =====
    
    @Test
    public void testListScreenings_Success() {
        ResponseEntity<?> response = userController.listScreenings();
        
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
    
    @Test
    public void testListScreenings_ReturnsCorrectFormat() {
        ResponseEntity<?> response = userController.listScreenings();
        
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof List);
    }
    
    @Test
    public void testListScreenings_ContainsAllScreenings() {
        ResponseEntity<?> response = userController.listScreenings();
        List<?> screenings = (List<?>) response.getBody();
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(3, screenings.size());
    }
    
    @Test
    public void testListScreenings_ScreeningDataIntegrity() {
        ResponseEntity<?> response = userController.listScreenings();
        List<Screening> screenings = (List<Screening>) response.getBody();
        
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(screenings.stream().anyMatch(s -> s.getMovieTitle().equals("Avatar")));
        assertTrue(screenings.stream().anyMatch(s -> s.getMovieTitle().equals("Inception")));
        assertTrue(screenings.stream().anyMatch(s -> s.getMovieTitle().equals("Interstellar")));
    }
    
    @Test
    public void testListScreenings_EmptyDatabase() {
        db.getAllScreenings().clear();
        
        ResponseEntity<?> response = userController.listScreenings();
        List<?> screenings = (List<?>) response.getBody();
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, screenings.size());
    }
    
    @Test
    public void testListScreenings_AvailableSeatsCorrect() {
        ResponseEntity<?> response = userController.listScreenings();
        List<Screening> screenings = (List<Screening>) response.getBody();
        
        Screening avatar = screenings.stream()
                .filter(s -> s.getId().equals("1"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(avatar);
        assertEquals(50, avatar.getAvailableSeats());
    }
    
    // ===== SELECT SCREENING Tests =====
    
    @Test
    public void testSelectScreening_Success() {
        ResponseEntity<?> response = userController.selectScreening(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("selected"));
    }
    
    @Test
    public void testSelectScreening_ValidScreeningId() {
        ResponseEntity<?> response = userController.selectScreening(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("screening"));
    }
    
    @Test
    public void testSelectScreening_InvalidScreeningId() {
        ResponseEntity<?> response = userController.selectScreening(999L);
        
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("not found"));
    }
    
    @Test
    public void testSelectScreening_NegativeScreeningId() {
        ResponseEntity<?> response = userController.selectScreening(-1L);
        
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Error"));
    }
    
    @Test
    public void testSelectScreening_ZeroScreeningId() {
        ResponseEntity<?> response = userController.selectScreening(0L);
        
        assertEquals(400, response.getStatusCodeValue());
    }
    
    @Test
    public void testSelectScreening_NullScreeningId() {
        ResponseEntity<?> response = userController.selectScreening(null);
        
        assertEquals(400, response.getStatusCodeValue());
    }
    
    @Test
    public void testSelectScreening_StringIdFormat() {
        ResponseEntity<?> response = userController.selectScreening(2L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Inception"));
    }
    
    // ===== BOOK SEATS Tests =====
    
    @Test
    public void testBook_Success() {
        ResponseEntity<?> response = userController.book("1", 5);
        
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
    
    @Test
    public void testBook_ReducesAvailableSeats() {
        int initialSeats = db.findScreeningById("1").getAvailableSeats();
        
        userController.book("1", 10);
        
        int remainingSeats = db.findScreeningById("1").getAvailableSeats();
        assertEquals(initialSeats - 10, remainingSeats);
    }
    
    @Test
    public void testBook_InvalidScreeningId() {
        ResponseEntity<?> response = userController.book("999", 5);
        
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("not found"));
    }
    
    @Test
    public void testBook_InsufficientSeats() {
        ResponseEntity<?> response = userController.book("1", 100);
        
        assertEquals(400, response.getStatusCodeValue());
    }
    
    @Test
    public void testBook_BookExactlyAvailableSeats() {
        ResponseEntity<?> response = userController.book("3", 60);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, db.findScreeningById("3").getAvailableSeats());
    }
    
    @Test
    public void testBook_SingleSeat() {
        int initialSeats = db.findScreeningById("1").getAvailableSeats();
        
        ResponseEntity<?> response = userController.book("1", 1);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(initialSeats - 1, db.findScreeningById("1").getAvailableSeats());
    }
    
    @Test
    public void testBook_ZeroSeats() {
        ResponseEntity<?> response = userController.book("1", 0);
        
        assertEquals(400, response.getStatusCodeValue());
    }
    
    @Test
    public void testBook_NegativeSeats() {
        ResponseEntity<?> response = userController.book("1", -5);
        
        assertEquals(400, response.getStatusCodeValue());
    }
    
    @Test
    public void testBook_MultipleBookings() {
        userController.book("1", 10);
        userController.book("1", 15);
        
        assertEquals(25, db.findScreeningById("1").getAvailableSeats());
    }
    
    @Test
    public void testBook_CreatesBookingRecord() {
        int initialBookings = db.getBookings().size();
        
        userController.book("1", 5);
        
        assertEquals(initialBookings + 1, db.getBookings().size());
    }
    
    @Test
    public void testBook_BookingContainsCorrectSeats() {
        userController.book("1", 7);
        Booking booking = db.getBookings().get(db.getBookings().size() - 1);
        
        assertEquals(7, booking.getSeats());
    }
    
    @Test
    public void testBook_NullScreeningId() {
        ResponseEntity<?> response = userController.book(null, 5);
        
        assertEquals(404, response.getStatusCodeValue());
    }
    
    @Test
    public void testBook_EmptyStringScreeningId() {
        ResponseEntity<?> response = userController.book("", 5);
        
        assertEquals(404, response.getStatusCodeValue());
    }
    
    // ===== Integration Tests =====
    
    @Test
    public void testFullUserFlow_ListThenSelectThenBook() {
        // List available screenings
        ResponseEntity<?> listResponse = userController.listScreenings();
        assertEquals(200, listResponse.getStatusCodeValue());
        
        // Select a screening
        ResponseEntity<?> selectResponse = userController.selectScreening(1L);
        assertEquals(200, selectResponse.getStatusCodeValue());
        
        // Book seats
        ResponseEntity<?> bookResponse = userController.book("1", 5);
        assertEquals(200, bookResponse.getStatusCodeValue());
        
        // Verify booking was recorded
        assertEquals(1, db.getBookings().size());
    }
    
    @Test
    public void testMultipleUsers_SequentialBookings() {
        userController.book("1", 10);
        userController.book("1", 15);
        userController.book("2", 5);
        
        assertEquals(3, db.getBookings().size());
        assertEquals(25, db.findScreeningById("1").getAvailableSeats());
        assertEquals(35, db.findScreeningById("2").getAvailableSeats());
    }
    
    @Test
    public void testBookingPersistence_AfterMultipleBookings() {
        userController.book("1", 5);
        
        ResponseEntity<?> response = userController.listScreenings();
        List<Screening> screenings = (List<Screening>) response.getBody();
        
        Screening screening1 = screenings.stream()
                .filter(s -> s.getId().equals("1"))
                .findFirst()
                .orElse(null);
        
        assertEquals(45, screening1.getAvailableSeats());
    }
    
    @Test
    public void testBook_DifferentScreeningIds() {
        ResponseEntity<?> response1 = userController.book("1", 5);
        ResponseEntity<?> response2 = userController.book("2", 8);
        ResponseEntity<?> response3 = userController.book("3", 12);
        
        assertEquals(200, response1.getStatusCodeValue());
        assertEquals(200, response2.getStatusCodeValue());
        assertEquals(200, response3.getStatusCodeValue());
    }
    
    @Test
    public void testSelectScreening_AllScreenings() {
        for (long i = 1; i <= 3; i++) {
            ResponseEntity<?> response = userController.selectScreening(i);
            assertEquals(200, response.getStatusCodeValue());
        }
    }
    
    // ===== Edge Cases =====
    
    @Test
    public void testBook_MaxIntegerSeats() {
        ResponseEntity<?> response = userController.book("1", Integer.MAX_VALUE);
        
        assertEquals(400, response.getStatusCodeValue());
    }
    
    @Test
    public void testListScreenings_AfterBooking() {
        userController.book("1", 25);
        
        ResponseEntity<?> response = userController.listScreenings();
        List<Screening> screenings = (List<Screening>) response.getBody();
        
        Screening screening = screenings.stream()
                .filter(s -> s.getId().equals("1"))
                .findFirst()
                .orElse(null);
        
        assertEquals(25, screening.getAvailableSeats());
    }
    
    @Test
    public void testBook_AllSeatsBooked_ThenAttemptMore() {
        userController.book("3", 60);
        ResponseEntity<?> response = userController.book("3", 1);
        
        assertEquals(400, response.getStatusCodeValue());
    }
}
