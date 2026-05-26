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
async function updateScreening(id, currentTitle, currentScreen, currentSeats) {
    // 1. Present a choice menu to the administrator
    const choice = prompt(
        `Update Menu for Screening ID: ${id}\n\n` +
        `Type "1" to change the Movie Title\n` +
        `Type "2" to change the Maximum Seats\n` +
        `Type "3" to change Both\n\n` +
        `Enter choice number:`
    );

    // If user presses cancel, abort the workflow smoothly
    if (choice === null) return; 

    let newTitle = currentTitle;
    let newSeats = currentSeats;

    if (choice === "1") {
        const titleInput = prompt("Enter new Movie Title:", currentTitle);
        if (titleInput === null) return;
        if (!titleInput.trim()) {
            alert("Title cannot be empty.");
            return;
        }
        newTitle = titleInput.trim();

    } else if (choice === "2") {
        const seatsInput = prompt("Enter maximum number of seats:", currentSeats);
        if (seatsInput === null) return;
        const parsedSeats = parseInt(seatsInput);
        if (isNaN(parsedSeats) || parsedSeats < 1) {
            alert("Please enter a valid positive number for seats.");
            return;
        }
        newSeats = parsedSeats;

    } else if (choice === "3") {
        const titleInput = prompt("Enter new Movie Title:", currentTitle);
        if (titleInput === null) return;
        
        const seatsInput = prompt("Enter maximum number of seats:", currentSeats);
        if (seatsInput === null) return;

        const parsedSeats = parseInt(seatsInput);
        if (!titleInput.trim() || isNaN(parsedSeats) || parsedSeats < 1) {
            alert("Invalid input configurations. Update aborted.");
            return;
        }
        newTitle = titleInput.trim();
        newSeats = parsedSeats;

    } else {
        alert("Invalid option choice selection. Please try again.");
        return;
    }

    // 2. Prepare the data matching your Java backend object parameters
    const updatedPayload = {
        id: id,
        movieTitle: newTitle,
        screenNumber: currentScreen, // Keep the existing screen number intact
        seats: newSeats
    };

    const url = `http://localhost:8080/admin/screenings/${id}`;

    try {
        const response = await fetch(url, {
            method: 'PUT', // Send as HTTP PUT to target your update endpoint
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedPayload)
        });

        if (response.ok) {
            alert("Screening records altered successfully!");
            
            // 3. Re-fetch data lists immediately to refresh both admin and user view components seamlessly
            await loadAdminManagement();
        } else {
            const errorText = await response.text();
            alert(`Failed to execute update: ${errorText}`);
        }
    } catch (err) {
        console.error("Network communication failure:", err);
        alert("Could not connect to the backend server.");
    }
}

// Function to delete a screening (DELETE)
async function deleteScreening(screeningId) {
    const url = `http://localhost:8080/admin/screenings/${screeningId}`;
    
    try {
        const response = await fetch(url, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            // 1. Notify the administrator
            alert(`Screening with ID ${screeningId} has been successfully deleted.`);
            
            // 2. IMPORTANT: Re-fetch the screenings list so the User Page 
            // has the fresh data right before we switch screens!
            if (typeof loadScreenings === 'function') {
                await loadScreenings(); 
            }

            // 3. Smoothly switch to the user screen without reloading the browser
            window.location.reload()
            
        } else {
            const errorText = await response.text();
            alert(`Failed to delete screening: ${errorText}`);
        }
    } catch (err) {
        console.error("Network error during deletion:", err);
        alert("A network error occurred. Could not connect to the backend server.");
    }
}

// List screenings with Delete/Edit buttons
async function loadAdminManagement() {
    loadScreenings(); // Ensure screenings are loaded for the user view as well
    const container = document.getElementById('admin-manage-list');
    
    try {
        const response = await fetch('http://localhost:8080/api/screenings');
        const screenings = await response.json();
        
        container.innerHTML = `
            <h2>Manage Screenings</h2>
            ${screenings.map(s => `
                <div class="screening-card" id="card-${s.id}">
                    <div class="view-mode">
                        <strong>${s.movieTitle}</strong> (ID: ${s.id}) - Screen ${s.screenNumber} (Seats: ${s.seats})
                        <br><br>
                        <button onclick="deleteScreening('${s.id}')" style="color:red;">Delete</button>
                        <button onclick="toggleEditForm('${s.id}', true)" style="color:grey; margin-left: 5px;">Update</button>
                    </div>

                    <div class="edit-mode" style="display: none;">
                        <label>Movie Title: </label>
                        <input type="text" id="edit-title-${s.id}" value="${s.movieTitle.replace(/"/g, '&quot;')}"><br><br>
                        
                        <label>Screen Number: </label>
                        <input type="number" id="edit-screen-${s.id}" value="${s.screenNumber}"><br><br>
                        
                        <label>Max Seats: </label>
                        <input type="number" id="edit-seats-${s.id}" value="${s.seats}"><br><br>
                        
                        <button onclick="saveScreeningUpdate('${s.id}')" style="color:green;">Save</button>
                        <button onclick="toggleEditForm('${s.id}', false)" style="color:red; margin-left: 5px;">Cancel</button>
                    </div>
                </div>
            `).join('')}
        `;
    } catch (error) {
        console.error("Error loading administration management panel:", error);
    }
}

// Switches the visibility between the static details and the editable form
function toggleEditForm(id, isEditing) {
    const card = document.getElementById(`card-${id}`);
    const viewMode = card.querySelector('.view-mode');
    const editMode = card.querySelector('.edit-mode');

    if (isEditing) {
        viewMode.style.display = 'none';
        editMode.style.display = 'block';
    } else {
        viewMode.style.display = 'block';
        editMode.style.display = 'none';
    }
}

// Handles collecting form inputs and sending them to your Spring Boot REST backend
async function saveScreeningUpdate(id) {
    const updatedTitle = document.getElementById(`edit-title-${id}`).value;
    const updatedScreen = parseInt(document.getElementById(`edit-screen-${id}`).value);
    const updatedSeats = parseInt(document.getElementById(`edit-seats-${id}`).value);

    // Simple validation rule checks
    if (!updatedTitle.trim() || isNaN(updatedScreen) || isNaN(updatedSeats)) {
        alert("Please make sure all form fields are filled out correctly.");
        return;
    }

    // Build payload to match your Screening.java model structure
    const updatedPayload = {
        id: id,
        movieTitle: updatedTitle.trim(),
        screenNumber: updatedScreen,
        seats: updatedSeats
    };

    try {
        const response = await fetch(`http://localhost:8080/admin/screenings/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedPayload)
        });

        if (response.ok) {
            // Smooth update: reload the list to snap back to the updated view mode automatically
            await loadAdminManagement();
        } else {
            const errorText = await response.text();
            alert(`Failed to save changes: ${errorText}`);
        }
    } catch (err) {
        console.error("Network communication failure:", err);
    }
}
// Initialize
loadScreenings();
loadAdminManagement();
