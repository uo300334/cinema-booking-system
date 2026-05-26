# Cinema Booking System - In-Depth Error Analysis Report

## Executive Summary
The application has **3 critical failing tests** and multiple architectural issues introduced by the Factory Pattern implementation and API design inconsistencies. The main issue is that **endpoints are not properly defined** to match what the tests expect.

---

## 🔴 CRITICAL ISSUES

### 1. **Missing `/user/screenings` Endpoint** ❌
**Status**: TEST FAILING
**Error**: Expected 200, got 404

**Problem**:
- The test expects: `GET /user/screenings`
- Current implementation: `GET /api/screenings` (with `/api` prefix)
- The UserController uses `@RequestMapping("/api")` but routes are at `/api/screenings`, NOT `/user/screenings`

**Test Code** (line 18 in ControllerTest.java):
```java
ResponseEntity<String> response = restTemplate.getForEntity("/user/screenings", String.class);
```

**Current Code** (line 16 in UserController.java):
```java
@GetMapping("/screenings")  // Full path is /api/screenings, NOT /user/screenings
```

**Fix**:
```java
@RestController
@RequestMapping("/user")  // Change from "/api" to "/user"
@CrossOrigin(origins = "*")
public class UserController {
```

---

### 2. **Missing `/user/select-screen` Endpoint** ❌
**Status**: TEST FAILING
**Error**: Expected 200, got 404 (×2 tests)

**Problem**:
- Tests expect: `GET /user/select-screen?screeningId=1`
- This endpoint **does not exist** in UserController
- Tests at lines 31 and 37 in ControllerTest.java reference a non-existent endpoint

**Test Code**:
```java
@Test
public void testSelectScreeningSuccess() {
    ResponseEntity<String> response = restTemplate.getForEntity("/user/select-screen?screeningId=1", String.class);
    assertEquals(200, response.getStatusCode().value());
    assertTrue(response.getBody().contains("You have selected"));
}

@Test
public void testSelectScreeningFailed() {
    ResponseEntity<String> response = restTemplate.getForEntity("/user/select-screen?screeningId=100", String.class);
    assertEquals(200, response.getStatusCode().value());
    assertTrue(response.getBody().contains("Error: Screening ID not found."));
}
```

**Fix**: Add this method to UserController:
```java
@GetMapping("/select-screen")
public ResponseEntity<?> selectScreening(@RequestParam String screeningId) {
    Screening screening = CinemaDatabase.getInstance().findScreeningById(screeningId);
    if (screening == null) {
        return ResponseEntity.ok("Error: Screening ID not found.");
    }
    return ResponseEntity.ok("You have selected: " + screening.getMovieTitle());
}
```

---

### 3. **Admin Controller URL Routing Issue** ⚠️
**Status**: POTENTIAL ISSUE

**Problem**:
- AdminController: `@RequestMapping("/admin")`
- Method: `@PostMapping("/admin/screenings")` 
- Results in double `/admin/admin/screenings` path!

**Current Code** (lines 19-23 in AdminController.java):
```java
@RestController
@RequestMapping("/admin")
public class AdminController {
    @PostMapping("/admin/screenings")  // This creates /admin/admin/screenings
    public Screening addScreening(@RequestBody Screening newScreening) {
```

**Frontend attempts** (script.js, line 59):
```javascript
const response = await fetch('/api/admin/screenings', {  // Frontend expects /api/admin/screenings
```

**Fix**: Remove the `/admin` prefix from method mappings since it's already in the class-level `@RequestMapping`:
```java
@RestController
@RequestMapping("/admin")
public class AdminController {
    @PostMapping("/screenings")  // Will be /admin/screenings
    public Screening addScreening(@RequestBody Screening newScreening) {

    @PutMapping("/screenings/{id}")  // Will be /admin/screenings/{id}
    public String updateScreening(@PathVariable String id, @RequestBody Screening updatedData) {

    @DeleteMapping("/screenings/{id}")  // Will be /admin/screenings/{id}
    public String deleteScreening(@PathVariable String id) {
```

---

### 4. **Frontend-Backend URL Mismatch** 🔄
**Status**: INCONSISTENT

**Frontend (script.js)** makes requests to:
- `/api/screenings` (line 6, 87)
- `/api/admin/screenings` (line 59, 75)

**Backend routes**:
- UserController: `@RequestMapping("/api")` ✓ Matches
- AdminController: `@RequestMapping("/admin")` ✗ Doesn't match `/api/admin`

**Fix**: Add `/api` prefix to AdminController or update frontend to use `/admin` instead of `/api/admin`.

**Option 1**: Update AdminController:
```java
@RestController
@RequestMapping("/api/admin")  // Add /api prefix
public class AdminController {
```

**Option 2**: Update Frontend (script.js):
```javascript
const response = await fetch('/admin/screenings', {  // Remove /api prefix
```

---

## 🟡 FACTORY PATTERN ISSUES

### 5. **Frontend Not Sending `bookingType` Parameter**
**Status**: SILENT FAILURE

**Problem**:
- Frontend (script.js, line 32) doesn't send `bookingType` parameter:
```javascript
const url = `http://localhost:8080/api/book?name=${name}&id=${screeningId}&seats=${seatCount}`;
// Missing &bookingType=STANDARD
```

- Backend (BookingService.java) requires it for the new factory pattern, defaults to STANDARD if not provided (backward compatible)

**Result**: All bookings default to STANDARD type - Factory Pattern isn't being utilized from the frontend!

**Fix**: Update Frontend (script.js, line 32):
```javascript
const bookingType = prompt("Select booking type (STANDARD/STUDENT/SENIOR):", "STANDARD");
const url = `http://localhost:8080/api/book?name=${name}&id=${screeningId}&seats=${seatCount}&bookingType=${bookingType}`;
```

---

### 6. **Frontend Incorrect API Endpoint for Booking**
**Status**: PARAMETER MISMATCH

**Frontend** (script.js, line 32):
```javascript
const url = `http://localhost:8080/api/book?name=${name}&id=${screeningId}&seats=${seatCount}`;
```

**Backend** (UserController.java, line 43):
```java
@GetMapping("/book")
public ResponseEntity<?> book(@RequestParam String id, @RequestParam int seats, 
                               @RequestParam(defaultValue = "STANDARD") String bookingType) {
    // NO 'name' parameter! It's hardcoded to "User"
    try {
        Booking booking = bookingService.createBooking("User", id, seats, bookingType);
```

**Issue**: Frontend sends `name` parameter but backend ignores it and uses hardcoded "User".

**Impact**: All bookings are recorded under generic "User" customer, losing customer personalization!

**Fix Option 1**: Backend should accept name:
```java
@GetMapping("/book")
public ResponseEntity<?> book(@RequestParam String id, @RequestParam int seats,
                               @RequestParam(defaultValue = "User") String name,
                               @RequestParam(defaultValue = "STANDARD") String bookingType) {
    try {
        Booking booking = bookingService.createBooking(name, id, seats, bookingType);
```

---

### 7. **Booking Class Missing Getters After Refactoring** ⚠️
**Status**: POTENTIAL SERIALIZATION ISSUE

**Problem**:
- Frontend expects JSON response with `bookingId` (script.js, line 38):
```javascript
alert(`Success! Booked ${seatCount} seats. ID: ${result.bookingId}`);
```

- Booking.java (lines 19-23) defines protected fields but getters should be public for JSON serialization:
```java
public String getBookingId() { return bookingId; }  // ✓ Exists
public String getCustomerName() { return customerName; }  // ✓ Exists
public Screening getScreening() { return screening; }  // ✓ Exists
public int getSeats() { return seats; }  // ✓ Exists
public double getDiscount() { return discount; }  // ✓ Exists
```

**Status**: Actually OK - getters exist, but Screening object might cause circular reference during JSON serialization.

---

## 🟠 DATABASE & TRANSACTION ISSUES

### 8. **No Transaction Management**
**Status**: RACE CONDITION RISK

**Problem**:
- Booking in Booking.java constructor directly calls `screening.bookSeats(seats)` (line 16):
```java
public Booking(String bookingId, String customerName, Screening screening, int seats) {
    // ...
    screening.bookSeats(seats);  // DIRECT MODIFICATION - No transaction!
}
```

- Then BookingService adds booking to DB (BookingService.java, line 27):
```java
Booking booking = BookingFactory.createBooking(bookingType, name, screening, seats);
db.getBookings().add(booking);  // Happens AFTER seat reduction
```

**Risk**: In concurrent scenarios, two threads could:
1. Thread A: Create booking, reduce seats
2. Thread B: Check availability (sees old count), creates booking
3. Result: Overbooking!

**Fix**: Move seat booking after successful booking creation:
```java
public class Booking {
    protected String bookingId;
    // ... other fields ...

    public Booking(String bookingId, String customerName, Screening screening, int seats) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.screening = screening;
        this.seats = seats;
        this.discount = 0;
        // DON'T call bookSeats here
    }

    public void confirmBooking() {
        screening.bookSeats(seats);  // Only call when confirmed
    }
}
```

---

### 9. **In-Memory Database - Data Loss**
**Status**: DESIGN LIMITATION

**Problem**:
- CinemaDatabase uses ArrayList (line 7):
```java
public class CinemaDatabase {
    private static CinemaDatabase instance;
    private List<Screening> screenings = new ArrayList<>();  // In-memory only!
    private List<Booking> bookings = new ArrayList<>();
```

**Impact**:
- Every application restart loses all bookings and screenings
- Multiple instances can't share data
- No backup or persistence

**Recommendation**: Implement the Repository Pattern you planned earlier!

---

## 🟡 MISSING FEATURES

### 10. **No Input Validation**
**Status**: SECURITY/DATA INTEGRITY ISSUE

**Problem**:
- BookingService doesn't validate seat count:
```java
public Booking createBooking(String name, String screeningId, int seats, String bookingType) {
    // NO VALIDATION: seats could be 0, negative, or massive!
    Screening screening = db.getScreenings().stream()
            .filter(s -> s.getId().equals(screeningId))
            .findFirst()
            .orElseThrow();
```

- Frontend does have limits (script.js, line 18) but it's client-side only

**Risks**:
- `seats = 0` → Creates booking with 0 seats
- `seats = -100` → Could add seats back!
- `seats = 999999` → Massive data

**Fix**: Add validation:
```java
public Booking createBooking(String name, String screeningId, int seats, String bookingType) {
    if (seats <= 0) {
        throw new IllegalArgumentException("Seats must be greater than 0");
    }
    if (seats > 100) {
        throw new IllegalArgumentException("Cannot book more than 100 seats at once");
    }
    // ... rest of method
}
```

---

### 11. **No Booking Retrieval or Cancellation**
**Status**: INCOMPLETE FEATURE

**Problem**:
- Users can create bookings but can't:
  - View their bookings
  - Cancel bookings
  - Modify bookings

**Missing Endpoints**:
```java
@GetMapping("/mybookings")  // Missing!
public List<Booking> getMyBookings(@RequestParam String customerName) { }

@DeleteMapping("/booking/{bookingId}")  // Missing!
public ResponseEntity<?> cancelBooking(@PathVariable String bookingId) { }
```

---

## 📋 SUMMARY TABLE

| # | Issue | Severity | Component | Status |
|---|-------|----------|-----------|--------|
| 1 | Missing `/user/screenings` | 🔴 CRITICAL | UserController | FAILING |
| 2 | Missing `/user/select-screen` | 🔴 CRITICAL | UserController | FAILING |
| 3 | Double `/admin/admin` routing | 🟡 HIGH | AdminController | POTENTIAL BUG |
| 4 | Frontend/Backend URL mismatch | 🟡 HIGH | Admin routes | INCONSISTENT |
| 5 | Frontend not using bookingType | 🟡 HIGH | Frontend + Factory | SILENT FAILURE |
| 6 | Frontend sends unused `name` param | 🟡 MEDIUM | Frontend/Backend | PARAMETER MISMATCH |
| 7 | No transaction management | 🟠 MEDIUM | Booking + Database | RACE CONDITION RISK |
| 8 | In-memory only database | 🟠 MEDIUM | CinemaDatabase | DESIGN LIMITATION |
| 9 | No input validation | 🟠 MEDIUM | BookingService | SECURITY ISSUE |
| 10 | No booking retrieval/cancellation | 🟡 MEDIUM | Backend | INCOMPLETE |

---

## 🔧 QUICK FIX PRIORITY

### Phase 1 - Fix Tests (Must Do):
1. Change UserController `@RequestMapping` from `/api` to `/user`
2. Add `/user/select-screen` endpoint

### Phase 2 - Fix Factory Pattern:
3. Add `bookingType` parameter to frontend booking form
4. Update backend to accept customer `name` parameter

### Phase 3 - Fix Architecture:
5. Align Admin routing (`/admin` vs `/api/admin`)
6. Add input validation to BookingService
7. Implement proper transaction handling

### Phase 4 - Polish:
8. Add booking management endpoints
9. Implement Repository Pattern for persistence
10. Add comprehensive error handling

