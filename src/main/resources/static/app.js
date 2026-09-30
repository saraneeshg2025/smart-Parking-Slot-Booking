/* ============================================
   ParkSmart — Application Logic
   ============================================ */

const API = '/api';

// ============ Navigation ============
document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();
        const page = link.dataset.page;
        navigateTo(page);
    });
});

function navigateTo(page) {
    // Update nav links
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    const activeLink = document.querySelector(`[data-page="${page}"]`);
    if (activeLink) activeLink.classList.add('active');

    // Update pages
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    const activePage = document.getElementById(`page-${page}`);
    if (activePage) activePage.classList.add('active');

    // Update title
    const titles = {
        'dashboard': 'Dashboard',
        'parking-lots': 'Parking Lots',
        'slots': 'Slots',
        'bookings': 'Bookings',
        'checkinout': 'Check In / Out'
    };
    document.getElementById('page-title').textContent = titles[page] || 'Dashboard';

    // Load data for the page
    loadPageData(page);

    // Close sidebar on mobile
    document.getElementById('sidebar').classList.remove('open');
}

function loadPageData(page) {
    switch (page) {
        case 'dashboard': loadDashboard(); break;
        case 'parking-lots': loadParkingLots(); break;
        case 'slots': loadSlots(); break;
        case 'bookings': loadBookings(); break;
    }
}

// ============ Mobile Menu ============
document.getElementById('menu-toggle').addEventListener('click', () => {
    document.getElementById('sidebar').classList.toggle('open');
});

// ============ Live Clock ============
function updateClock() {
    const now = new Date();
    document.getElementById('live-clock').textContent = now.toLocaleString('en-IN', {
        weekday: 'short', day: '2-digit', month: 'short',
        hour: '2-digit', minute: '2-digit', second: '2-digit',
        hour12: true
    });
}
updateClock();
setInterval(updateClock, 1000);

// ============ API Helpers ============
async function apiGet(endpoint) {
    const res = await fetch(`${API}${endpoint}`);
    if (!res.ok) {
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || 'Request failed');
    }
    return res.json();
}

async function apiPost(endpoint, body) {
    const res = await fetch(`${API}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || 'Request failed');
    }
    const text = await res.text();
    return text ? JSON.parse(text) : {};
}

async function apiPut(endpoint, body) {
    const res = await fetch(`${API}${endpoint}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || 'Request failed');
    }
    return res.json();
}

async function apiDelete(endpoint) {
    const res = await fetch(`${API}${endpoint}`, { method: 'DELETE' });
    if (!res.ok) {
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || 'Request failed');
    }
    const text = await res.text();
    return text || 'Deleted';
}

// ============ Toast Notifications ============
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    const icons = {
        success: '<svg class="toast-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>',
        error: '<svg class="toast-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>',
        info: '<svg class="toast-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>'
    };

    toast.innerHTML = `${icons[type] || icons.info}<span class="toast-message">${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('toast-out');
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// ============ Modal Helpers ============
function openModal(id) {
    document.getElementById(id).classList.add('show');

    // Load dropdown data when modals open
    if (id === 'slot-modal') loadLotDropdown();
    if (id === 'booking-modal') loadSlotDropdown();
}

function closeModal(id) {
    document.getElementById(id).classList.remove('show');
    // Reset forms
    const modal = document.getElementById(id);
    const form = modal.querySelector('form');
    if (form) form.reset();
    // Clear hidden edit IDs
    const hiddenId = modal.querySelector('input[type="hidden"]');
    if (hiddenId) hiddenId.value = '';
}

// Close modal on overlay click
document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) {
            overlay.classList.remove('show');
        }
    });
});

// ============ Format Helpers ============
function formatDateTime(dt) {
    if (!dt) return '—';
    const d = new Date(dt);
    return d.toLocaleString('en-IN', {
        day: '2-digit', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit', hour12: true
    });
}

function formatDateTimeLocal(dt) {
    if (!dt) return '';
    // Handle ISO string format "2026-09-29T12:00:00"
    if (typeof dt === 'string' && dt.length >= 16) {
        return dt.substring(0, 16);
    }
    const d = new Date(dt);
    const pad = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function statusBadge(status) {
    const s = (status || '').toUpperCase();
    const cls = {
        'AVAILABLE': 'badge-available',
        'BOOKED': 'badge-booked',
        'OCCUPIED': 'badge-occupied',
        'CHECKED_IN': 'badge-checked-in',
        'COMPLETED': 'badge-completed',
        'CANCELLED': 'badge-cancelled'
    };
    return `<span class="badge ${cls[s] || 'badge-booked'}">${status || 'Unknown'}</span>`;
}

// ============ Dashboard ============
async function loadDashboard() {
    try {
        const [lots, slots, bookings] = await Promise.all([
            apiGet('/parking-lots'),
            apiGet('/slots'),
            apiGet('/bookings')
        ]);

        // Stats
        const availableSlots = slots.filter(s => s.status === 'AVAILABLE').length;
        const activeBookings = bookings.filter(b => b.status === 'BOOKED' || b.status === 'CHECKED_IN').length;

        animateCounter('stat-total-lots', lots.length);
        animateCounter('stat-total-slots', slots.length);
        animateCounter('stat-available-slots', availableSlots);
        animateCounter('stat-total-bookings', activeBookings);

        // Recent bookings (last 5)
        const recent = bookings.slice(-5).reverse();
        const tbody = document.getElementById('dashboard-bookings-body');
        if (recent.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No bookings yet</td></tr>';
        } else {
            tbody.innerHTML = recent.map(b => `
                <tr>
                    <td>#${b.id}</td>
                    <td>${b.customerName}</td>
                    <td>${b.vehicleNumber}</td>
                    <td>${statusBadge(b.status)}</td>
                    <td>${formatDateTime(b.startTime)}</td>
                </tr>
            `).join('');
        }

        // Lots overview
        const lotsDiv = document.getElementById('lots-overview');
        if (lots.length === 0) {
            lotsDiv.innerHTML = '<p class="empty-state">No parking lots configured</p>';
        } else {
            lotsDiv.innerHTML = lots.map(l => `
                <div class="lot-overview-item">
                    <div>
                        <div class="lot-overview-name">${l.name}</div>
                        <div class="lot-overview-location">${l.location}</div>
                    </div>
                    <span class="lot-overview-slots">${l.totalSlots} slots</span>
                </div>
            `).join('');
        }
    } catch (err) {
        console.error('Dashboard load error:', err);
    }
}

function animateCounter(elementId, target) {
    const el = document.getElementById(elementId);
    const current = parseInt(el.textContent) || 0;
    if (current === target) return;

    const duration = 600;
    const start = performance.now();

    function step(now) {
        const progress = Math.min((now - start) / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3);
        el.textContent = Math.round(current + (target - current) * eased);
        if (progress < 1) requestAnimationFrame(step);
    }
    requestAnimationFrame(step);
}

// ============ Parking Lots CRUD ============
async function loadParkingLots() {
    try {
        const lots = await apiGet('/parking-lots');
        const tbody = document.getElementById('lots-body');

        if (lots.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No parking lots found. Add one to get started!</td></tr>';
            return;
        }

        tbody.innerHTML = lots.map(l => `
            <tr>
                <td>#${l.id}</td>
                <td style="color: var(--text-primary); font-weight: 600;">${l.name}</td>
                <td>${l.location}</td>
                <td><span class="badge badge-available">${l.totalSlots}</span></td>
                <td>
                    <div class="action-btns">
                        <button class="btn-icon" onclick="editParkingLot(${l.id})" title="Edit">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                        </button>
                        <button class="btn-icon danger" onclick="deleteParkingLot(${l.id})" title="Delete">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        showToast('Failed to load parking lots', 'error');
    }
}

async function saveParkingLot(e) {
    e.preventDefault();
    const editId = document.getElementById('lot-edit-id').value;
    const data = {
        name: document.getElementById('lot-name').value,
        location: document.getElementById('lot-location').value,
        totalSlots: parseInt(document.getElementById('lot-total-slots').value)
    };

    try {
        if (editId) {
            await apiPut(`/parking-lots/${editId}`, data);
            showToast('Parking lot updated successfully!', 'success');
        } else {
            await apiPost('/parking-lots', data);
            showToast('Parking lot created successfully!', 'success');
        }
        closeModal('lot-modal');
        loadParkingLots();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function editParkingLot(id) {
    try {
        const lot = await apiGet(`/parking-lots/${id}`);
        document.getElementById('lot-edit-id').value = lot.id;
        document.getElementById('lot-name').value = lot.name;
        document.getElementById('lot-location').value = lot.location;
        document.getElementById('lot-total-slots').value = lot.totalSlots;
        document.getElementById('lot-modal-title').textContent = 'Edit Parking Lot';
        openModal('lot-modal');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteParkingLot(id) {
    if (!confirm('Are you sure you want to delete this parking lot?')) return;
    try {
        await apiDelete(`/parking-lots/${id}`);
        showToast('Parking lot deleted', 'success');
        loadParkingLots();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ============ Slots CRUD ============
async function loadSlots() {
    try {
        const slots = await apiGet('/slots');
        const tbody = document.getElementById('slots-body');

        if (slots.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="empty-state">No slots found. Add one to get started!</td></tr>';
            return;
        }

        tbody.innerHTML = slots.map(s => `
            <tr>
                <td>#${s.id}</td>
                <td style="color: var(--text-primary); font-weight: 600;">${s.slotNumber}</td>
                <td>${statusBadge(s.status)}</td>
                <td>
                    <div class="action-btns">
                        <button class="btn-icon danger" onclick="deleteSlot(${s.id})" title="Delete">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        showToast('Failed to load slots', 'error');
    }
}

async function loadLotDropdown() {
    try {
        const lots = await apiGet('/parking-lots');
        const select = document.getElementById('slot-parking-lot');
        select.innerHTML = '<option value="">Select a parking lot</option>' +
            lots.map(l => `<option value="${l.id}">${l.name} — ${l.location}</option>`).join('');
    } catch (err) {
        showToast('Failed to load parking lots', 'error');
    }
}

async function saveSlot(e) {
    e.preventDefault();
    const parkingLotId = document.getElementById('slot-parking-lot').value;
    const slotData = {
        slotNumber: document.getElementById('slot-number').value
    };

    try {
        const res = await fetch(`${API}/slots?parkingLotId=${parkingLotId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(slotData)
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({ message: res.statusText }));
            throw new Error(err.message || 'Failed to create slot');
        }
        showToast('Slot created successfully!', 'success');
        closeModal('slot-modal');
        loadSlots();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteSlot(id) {
    if (!confirm('Are you sure you want to delete this slot?')) return;
    try {
        await apiDelete(`/slots/${id}`);
        showToast('Slot deleted', 'success');
        loadSlots();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ============ Bookings CRUD ============
async function loadBookings() {
    try {
        const bookings = await apiGet('/bookings');
        const tbody = document.getElementById('bookings-body');

        if (bookings.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No bookings found. Create one to get started!</td></tr>';
            return;
        }

        tbody.innerHTML = bookings.map(b => `
            <tr>
                <td>#${b.id}</td>
                <td style="color: var(--text-primary); font-weight: 600;">${b.customerName}</td>
                <td>${b.vehicleNumber}</td>
                <td>${b.slot ? b.slot.slotNumber : '—'}</td>
                <td>${formatDateTime(b.startTime)}</td>
                <td>${formatDateTime(b.endTime)}</td>
                <td>${statusBadge(b.status)}</td>
                <td>
                    <div class="action-btns">
                        <button class="btn-icon" onclick="editBooking(${b.id})" title="Edit">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                        </button>
                        <button class="btn-icon danger" onclick="cancelBooking(${b.id})" title="Cancel">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        showToast('Failed to load bookings', 'error');
    }
}

async function loadSlotDropdown() {
    try {
        const slots = await apiGet('/slots');
        const select = document.getElementById('booking-slot');
        const availableSlots = slots.filter(s => s.status === 'AVAILABLE');
        select.innerHTML = '<option value="">Select a slot</option>' +
            availableSlots.map(s => `<option value="${s.id}">${s.slotNumber}</option>`).join('');
    } catch (err) {
        showToast('Failed to load slots', 'error');
    }
}

async function saveBooking(e) {
    e.preventDefault();
    const editId = document.getElementById('booking-edit-id').value;
    const data = {
        customerName: document.getElementById('booking-customer').value,
        vehicleNumber: document.getElementById('booking-vehicle').value,
        slotId: parseInt(document.getElementById('booking-slot').value),
        startTime: document.getElementById('booking-start').value,
        endTime: document.getElementById('booking-end').value
    };

    try {
        if (editId) {
            await apiPut(`/bookings/${editId}`, data);
            showToast('Booking updated successfully!', 'success');
        } else {
            await apiPost('/bookings', data);
            showToast('Booking created successfully!', 'success');
        }
        closeModal('booking-modal');
        loadBookings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function editBooking(id) {
    try {
        const booking = await apiGet(`/bookings/${id}`);
        await loadSlotDropdown();

        // Add current slot as an option if not available
        const select = document.getElementById('booking-slot');
        if (booking.slot) {
            const exists = Array.from(select.options).some(o => o.value === String(booking.slot.id));
            if (!exists) {
                const opt = document.createElement('option');
                opt.value = booking.slot.id;
                opt.textContent = booking.slot.slotNumber + ' (current)';
                select.appendChild(opt);
            }
            select.value = booking.slot.id;
        }

        document.getElementById('booking-edit-id').value = booking.id;
        document.getElementById('booking-customer').value = booking.customerName;
        document.getElementById('booking-vehicle').value = booking.vehicleNumber;
        document.getElementById('booking-start').value = formatDateTimeLocal(booking.startTime);
        document.getElementById('booking-end').value = formatDateTimeLocal(booking.endTime);
        document.getElementById('booking-modal-title').textContent = 'Edit Booking';
        document.getElementById('booking-submit-btn').textContent = 'Update';
        openModal('booking-modal');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function cancelBooking(id) {
    if (!confirm('Are you sure you want to cancel this booking?')) return;
    try {
        await apiDelete(`/bookings/${id}`);
        showToast('Booking cancelled', 'success');
        loadBookings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ============ Check In / Out ============
async function performCheckIn() {
    const bookingId = document.getElementById('checkin-booking-id').value;
    if (!bookingId) {
        showToast('Please enter a booking ID', 'error');
        return;
    }

    try {
        const result = await apiPost(`/check-in/${bookingId}`, null);
        const resultBox = document.getElementById('checkin-result');
        resultBox.innerHTML = `
            <div class="result-item"><span class="result-label">Booking ID</span><span class="result-value">#${result.bookingId}</span></div>
            <div class="result-item"><span class="result-label">Status</span><span class="result-value">${result.status}</span></div>
            <div class="result-item"><span class="result-label">Message</span><span class="result-value" style="color: var(--accent-emerald);">${result.message}</span></div>
        `;
        resultBox.classList.add('show');
        showToast('Checked in successfully!', 'success');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function performCheckOut() {
    const bookingId = document.getElementById('checkout-booking-id').value;
    if (!bookingId) {
        showToast('Please enter a booking ID', 'error');
        return;
    }

    try {
        const result = await apiPost(`/check-out/${bookingId}`, null);
        const resultBox = document.getElementById('checkout-result');
        resultBox.innerHTML = `
            <div class="result-item"><span class="result-label">Booking ID</span><span class="result-value">#${result.bookingId}</span></div>
            <div class="result-item"><span class="result-label">Slot</span><span class="result-value">${result.slotNumber || '—'}</span></div>
            <div class="result-item"><span class="result-label">Check-In Time</span><span class="result-value">${formatDateTime(result.checkInTime)}</span></div>
            <div class="result-item"><span class="result-label">Booked End Time</span><span class="result-value">${formatDateTime(result.bookedEndTime)}</span></div>
            <div class="result-item"><span class="result-label">Actual Checkout</span><span class="result-value">${formatDateTime(result.actualCheckoutTime)}</span></div>
            <div class="result-item"><span class="result-label">Overstay</span><span class="result-value ${result.overstayMinutes > 0 ? 'penalty' : ''}">${result.overstayMinutes || 0} minutes</span></div>
            <div class="result-item"><span class="result-label">Penalty</span><span class="result-value ${result.penaltyAmount > 0 ? 'penalty' : ''}">₹${result.penaltyAmount || 0}</span></div>
            <div class="result-item"><span class="result-label">Message</span><span class="result-value" style="color: var(--accent-amber);">${result.message}</span></div>
        `;
        resultBox.classList.add('show');
        showToast('Checked out successfully!', 'success');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ============ Initialize ============
document.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
});
