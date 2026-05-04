async function loadScreenings() {
    const listElement = document.getElementById('screenings-list');
    
    try {
        // Call your Java endpoint
        const response = await fetch('http://localhost:8080/api/screenings');
        const screenings = await response.json();

        listElement.innerHTML = ''; // Clear loading text

        screenings.forEach(s => {
            const div = document.createElement('div');
            div.className = 'screening-card';
            div.innerHTML = `
                <h3>${s.movieTitle}</h3>
                <p>ID: ${s.id} | Available Seats: ${s.availableSeats}</p>
                <input type="number" id="seats-${s.id}" value="1" min="1" max="${s.availableSeats}" style="width: 50px;">
                <button onclick="bookTicket('${s.id}')">Book Seats</button>
            `;
            listElement.appendChild(div);
        });
    } catch (err) {
        listElement.innerHTML = "Error connecting to Java backend.";
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
async function createScreening() {
    const screeningData = {
        id: document.getElementById('new-id').value,
        movieTitle: document.getElementById('new-movie').value,
        screen: document.getElementById('new-screen').value,
        availableSeats: 50 // Fixed capacity per brief
    };

    const response = await fetch('/api/admin/screenings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(screeningData)
    });

    if (response.ok) {
        alert("Screening Created!");
        loadAdminManagement(); // Refresh the list
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