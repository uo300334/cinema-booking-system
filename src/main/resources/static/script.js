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
// script.js

async function createScreening(id,movieTitle,screen) {
    console.log("Create button clicked..."); // Verify trigger in Console (F12)

    // 1. Grab values from the HTML inputs
    const movieInput = id.value;
    const idInput = movieTitle.value;
    const screenInput = screen.value;
    
    if(isNaN(screenInput) || parseInt(screenInput) <= 0) {
        alert("Screen number must be a positive integer.");
        return;
    }

    if (!movieInput || !idInput || !screenInput) {
        alert("Please fill in all fields.");
        return;
    }

    // 3. Create the data object (Must match Java field names exactly!)
    const screeningData = {
        id: idInput,
        movieTitle: movieInput,
        screen: screenInput,
        availableSeats: 50 // Default capacity as per project brief
    };

    try {
       const response = await fetch('/user/admin/screenings', { 
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(screeningData)
        });

        if (/*response.ok*/true) {
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
        alert("Could not connect to the server. Is IntelliJ running?");
    }
}
// Function to delete a screening (DELETE)
async function deleteScreening(id) {
    if (!confirm("Are you sure?")) return;

    const response = await fetch(`/api/admin/screenings/${id}`, {
        method: 'DELETE'
    });

    if (response.ok) {
        loadAdminManagement();
    }
}

// List screenings with Delete/Edit buttons
async function loadAdminManagement() {
    const container = document.getElementById('admin-manage-list');
    const response = await fetch('/api/screenings');
    const screenings = await response.json();

    container.innerHTML = screenings.map(s => `
        <div class="screening-card">
            <strong>${s.movieTitle}</strong> (${s.id})
            <button onclick="deleteScreening('${s.id}')" style="color:red">Delete</button>
        </div>
    `).join('');
}

// Initialize
loadScreenings();