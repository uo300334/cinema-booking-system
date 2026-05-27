

import edu.um.cps2002.logic.CinemaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
@SpringBootTest(classes = CinemaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)public class ControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testUserScreeningsEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/screenings", String.class);
        assertEquals(404, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("movieTitle"));
    }

    @Test
    public void testAdminReportEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/report", String.class);
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Occupancy Report"));
    }
    @Test
    public void testSelectScreeningSuccess() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/select-screen?screeningId=1", String.class);
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("You have selected screening:"));
    }
    @Test
    public void testSelectScreeningFailed() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/select-screen?screeningId=100", String.class);
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Error: Screening ID not found."));
    }
}
