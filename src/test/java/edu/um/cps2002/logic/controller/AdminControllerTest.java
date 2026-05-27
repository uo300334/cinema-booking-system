package edu.um.cps2002.logic.controller;

import edu.um.cps2002.logic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for AdminController
 * Tests all admin operations: Create, Read, Update, Delete screenings and view reports
 */
public class AdminControllerTest {
    
    private AdminController adminController;
    private CinemaDatabase db;
    
    @BeforeEach
    public void setUp() {
        adminController = new AdminController();
        db = CinemaDatabase.getInstance();
        // Clear screenings before each test to ensure clean state
        db.getAllScreenings().clear();
        db.getAllScreenings().add(new Screening("1", "Test Movie 1", 1, 50));
        db.getAllScreenings().add(new Screening("2", "Test Movie 2", 2, 50));
    }
    
    // ===== CREATE Screening Tests =====
    
    @Test
    public void testCreateScreening_Success() {
        Screening newScreening = new Screening("S99", "New Movie", 5, 100);
        
        ResponseEntity<?> response = adminController.createScreening(newScreening);
        
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(db.getAllScreenings().stream()
                .anyMatch(s -> s.getId().equals("S99")));
    }
    
    @Test
    public void testCreateScreening_DuplicateId() {
        Screening duplicate = new Screening("1", "Duplicate Movie", 3, 50);
        
        ResponseEntity<?> response = adminController.createScreening(duplicate);
        
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("already exists"));
    }
    
    @Test
    public void testCreateScreening_ValidData() {
        Screening screening = new Screening("S100", "Avatar 2", 7, 120);
        
        ResponseEntity<?> response = adminController.createScreening(screening);
        
        assertEquals(200, response.getStatusCodeValue());
        Screening created = db.findScreeningById("S100");
        assertNotNull(created);
        assertEquals("Avatar 2", created.getMovieTitle());
        assertEquals(7, created.getScreenNumber());
        assertEquals(120, created.getSeats());
    }
    
    @Test
    public void testCreateScreening_ZeroSeats() {
        Screening screening = new Screening("S101", "Zero Seats Movie", 1, 0);
        
        ResponseEntity<?> response = adminController.createScreening(screening);
        
        assertEquals(200, response.getStatusCodeValue()); // Should still create
        Screening created = db.findScreeningById("S101");
        assertEquals(0, created.getSeats());
    }
    
    // ===== READ/VIEW Report Tests =====
    
    @Test
    public void testViewReport_Success() {
        ResponseEntity<?> response = adminController.viewReport();
        
        assertEquals(200, response.getStatusCodeValue());
        String report = response.getBody().toString();
        assertTrue(report.contains("Occupancy Report"));
    }
    
    @Test
    public void testViewReport_ContainsScreeningData() {
        ResponseEntity<?> response = adminController.viewReport();
        
        assertEquals(200, response.getStatusCodeValue());
        String report = response.getBody().toString();
        assertTrue(report.contains("Test Movie 1") || report.contains("Test Movie 2"));
    }
    
    // ===== UPDATE Screening Tests =====
    
    @Test
    public void testUpdateScreening_Success() {
        Screening updatedData = new Screening("1", "Updated Movie Title", 3, 75);
        
        ResponseEntity<String> response = adminController.updateScreening("1", updatedData);
        
        assertEquals(200, response.getStatusCodeValue());
        Screening updated = db.findScreeningById("1");
        assertEquals("Updated Movie Title", updated.getMovieTitle());
        assertEquals(3, updated.getScreenNumber());
        assertEquals(75, updated.getSeats());
    }
    
    @Test
    public void testUpdateScreening_NonExistentId() {
        Screening updatedData = new Screening("999", "Non Existent", 1, 50);
        
        ResponseEntity<String> response = adminController.updateScreening("999", updatedData);
        
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("not found"));
    }
    
    @Test
    public void testUpdateScreening_PartialUpdate() {
        Screening original = db.findScreeningById("2");
        original.setAvailableSeats(25); // Some seats already booked
        
        Screening updatedData = new Screening("2", "Updated Title", 4, 100);
        ResponseEntity<String> response = adminController.updateScreening("2", updatedData);
        
        assertEquals(200, response.getStatusCodeValue());
        Screening updated = db.findScreeningById("2");
        assertEquals("Updated Title", updated.getMovieTitle());
        assertEquals(100, updated.getAvailableSeats()); // Reset available seats
    }
    
    @Test
    public void testUpdateScreening_NullScreeningId() {
        Screening updatedData = new Screening("2", "Some Movie", 1, 50);
        
        ResponseEntity<String> response = adminController.updateScreening(null, updatedData);
        
        assertEquals(404, response.getStatusCodeValue());
    }
    
    // ===== DELETE Screening Tests =====
    
    @Test
    public void testDeleteScreening_Success() {
        int initialSize = db.getAllScreenings().size();
        
        String response = adminController.deleteScreening("1");
        
        assertEquals("Deleted", response);
        assertEquals(initialSize - 1, db.getAllScreenings().size());
        assertNull(db.findScreeningById("1"));
    }
    
    @Test
    public void testDeleteScreening_NonExistentId() {
        int initialSize = db.getAllScreenings().size();
        
        String response = adminController.deleteScreening("999");
        
        assertEquals("Error", response);
        assertEquals(initialSize, db.getAllScreenings().size()); // Size unchanged
    }
    
    @Test
    public void testDeleteScreening_MultipleDeletes() {
        adminController.deleteScreening("1");
        adminController.deleteScreening("2");
        
        assertTrue(db.getAllScreenings().isEmpty());
    }
    
    // ===== Edge Cases & Validation Tests =====
    
    @Test
    public void testCreateScreening_SpecialCharactersInTitle() {
        Screening screening = new Screening("S102", "Movie: The Sequel (2024) & More!", 1, 50);
        
        ResponseEntity<?> response = adminController.createScreening(screening);
        
        assertEquals(200, response.getStatusCodeValue());
        Screening created = db.findScreeningById("S102");
        assertEquals("Movie: The Sequel (2024) & More!", created.getMovieTitle());
    }
    
    @Test
    public void testUpdateScreening_LargeScreenNumber() {
        Screening updatedData = new Screening("1", "Movie", 9999, 50);
        
        ResponseEntity<String> response = adminController.updateScreening("1", updatedData);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(9999, db.findScreeningById("1").getScreenNumber());
    }
    
    @Test
    public void testCreateScreening_EmptyTitle() {
        Screening screening = new Screening("S103", "", 1, 50);
        
        ResponseEntity<?> response = adminController.createScreening(screening);
        
        assertEquals(200, response.getStatusCodeValue()); // Should still create
        Screening created = db.findScreeningById("S103");
        assertEquals("", created.getMovieTitle());
    }
    
    @Test
    public void testViewReport_EmptyDatabase() {
        db.getAllScreenings().clear();
        
        ResponseEntity<?> response = adminController.viewReport();
        
        assertEquals(200, response.getStatusCodeValue());
        String report = response.getBody().toString();
        assertTrue(report.contains("Occupancy Report"));
    }
}
