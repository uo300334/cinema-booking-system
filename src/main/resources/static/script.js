async function loadScreenings() {
    const listElement = document.getElementById('screening-list');
    
    try {
        // Call your Java endpoint
        const response = await fetch('http://localhost:8080/api/screenings');
        const screenings = await response.json();

        listElement.innerHTML = ''; // Clear loading text

        screenings.forEach(s => {
        // 1. Safety check: If the server sent a null screening, skip it
        if (!s || !s.id) return;

        const div = document.createElement('div');
        div.className = 'screening-card';
        
        // 2. Use defaults if values are missing to prevent "undefined" showing on UI
        const title = s.movieTitle || "Unknown Movie";
        const seats = s.availableSeats ?? 0;

        div.innerHTML = `
            <h3>${title}</h3>
            <p>ID: ${s.id} | Available Seats: ${seats}</p>
            <input type="number" id="seats-${s.id}" value="1" min="1" max="${seats}" style="width: 50px;">
            <button onclick="bookTicket('${s.id}')">Book Seats</button>
        `;
        
        // 3. Ensure listElement actually exists before appending
        if (listElement) {
            listElement.appendChild(div);
        } else {
            console.error("Target list element not found in DOM");
        }
    });

    } catch (err) {
        console.error("Error loading screenings:", err);
    }
}

async function bookTicket(screeningId) {
    const name = prompt("Enter your name:");
    if (!name) return;
    const seatCount = document.getElementById(`seats-${screeningId}`).value;
    // Matches your Java @GetMapping("/book")
    const url = `http://localhost:8080/api/book?name=${name}&id=${screeningId}&seats=${seatCount}`;
    try {
        const response = await fetch(url);
        const result = await response.json();
        
        
        alert(`Success! Booked ${seatCount} seats. ID: ${result.bookingId}`);
        loadScreenings(); // Refresh UI to show reduced seat count
        
    } catch (err) {
        alert("Booking failed. Check seat availability.");
    } // Refresh the list to see updated seats
}

function showPage(page) {
    document.getElementById('user-section').classList.toggle('hidden', page !== 'user');
    document.getElementById('admin-section').classList.toggle('hidden', page !== 'admin');
}

// Function to create a screening (POST)
async function createScreening(idElement, movieElement, screenElement) {
    console.log("Create button clicked..."); // Verify trigger in Console (F12)

    // 1. Grab values from the HTML inputs - FIX: Correct parameter mapping
    const idInput = idElement.value;           // ID value
    const movieInput = movieElement.value;     // Movie title value
    const screenInput = screenElement.value;   // Screen number value
    
    // 2. Validate inputs
    if (!movieInput || !idInput || !screenInput) {
        alert("Please fill in all fields.");
        return;
    }

    if (isNaN(screenInput) || parseInt(screenInput) <= 0) {
        alert("Screen number must be a positive integer.");
        return;
    }

    // 3. Create the data object (Must match Java field names exactly!)
    const screeningData = {
        id: idInput,
        movieTitle: movieInput,
        screenNumber: screenInput,     // FIX: Changed from "screen" to "screenNumber"
        seats: 50                       // FIX: Changed from "availableSeats" to "seats" (sets initial capacity)
    };

    console.log("Posting screening data:", screeningData); // Debug log

    try {
        // FIX: Changed endpoint from '/user/admin/screenings' to '/admin/screenings'
        const response = await fetch('http://localhost:8080/admin/screenings', { 
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(screeningData)
        });

        console.log("Response status:", response.status); // Debug log

        if (response.ok) {
            alert("Screening added successfully!");
            
            // 5. Clear inputs
            document.getElementById('new-movie').value = '';
            document.getElementById('new-id').value = '';
            document.getElementById('new-screen').value = '';

            // 6. Refresh the data so the User view is updated instantly
            await loadScreenings(); 
            showPage('user'); 
        } else {
            const errorText = await response.text();
            alert("Failed: " + errorText);
        }
    } catch (error) {
        console.error("Fetch error:", error);
        alert("Could not connect to the server. Is the Java backend running on port 8080?");
    }
}

// Function to delete a screening (DELETE)
async function deleteScreening(id) {
    if (!confirm("Are you sure?")) return;

    const response = await fetch(`/admin/screenings/${id}`, {
        method: 'DELETE'
    });

    if (response.ok) {
        alert("Screening deleted successfully!");
        await loadAdminManagement();
        await loadScreenings(); // Refresh user view too
    } else {
        alert("Failed to delete screening");
    }
}

// List screenings with Delete/Edit buttons
async function loadAdminManagement() {
    const container = document.getElementById('admin-manage-list');
    const response = await fetch('http://localhost:8080/api/screenings');
    const screenings = await response.json();

    container.innerHTML = screenings.map(s => `
        <div class="screening-card">
            <strong>${s.movieTitle}</strong> (ID: ${s.id}) - Screen ${s.screenNumber}
            <button onclick="deleteScreening('${s.id}')" style="color:red">Delete</button>
        </div>
    `).join('');
}

// Initialize
loadScreenings();
loadAdminManagement();
