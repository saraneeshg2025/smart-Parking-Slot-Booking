# ParkSmart

ParkSmart is a smart parking slot booking backend built with Java 17, Spring Boot, Maven, Spring Data JPA, and MySQL.

## Features
- Create and manage parking lots
- Create and manage parking slots
- Check available slots for a selected time window
- Book a slot without overlap
- Check in and check out vehicles
- Automatically calculate overstay penalty
- View parking lot occupancy
- Validation and custom exceptions

## Tech Stack
- Java 17
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Hibernate
- MySQL
- Jakarta Validation

## MySQL Setup
1. Install MySQL and start the MySQL service.
2. Create the database manually if needed:
   ```sql
  CREATE DATABASE grain_storage_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Set environment variables before running the app:
   ```bash
   set MYSQL_PASSWORD=your_mysql_password
   ```
  Set `MYSQL_PASSWORD` in the same terminal session where you start the app. Do not put the password in `application.properties` or commit it.

## Run the project
```bash
./mvnw spring-boot:run
```

## API Examples

### Create a parking lot
```http
POST /api/parking-lots
Content-Type: application/json

{
  "name": "College Main Parking",
  "location": "Near College Main Gate",
  "totalSlots": 10
}
```

### Create slots
```http
POST /api/slots?parkingLotId=1
Content-Type: application/json

{
  "slotNumber": "A1",
  "status": "AVAILABLE"
}
```

### Check available slots
```http
GET /api/parking-lots/1/available-slots?startTime=2026-09-28T10:00:00&endTime=2026-09-28T12:00:00
```

### Create booking
```http
POST /api/bookings
Content-Type: application/json

{
  "customerName": "Yaswanth",
  "vehicleNumber": "TN01AB1234",
  "slotId": 1,
  "startTime": "2026-09-28T10:00:00",
  "endTime": "2026-09-28T12:00:00"
}
```

### Check in
```http
POST /api/check-in/1
```

### Check out
```http
POST /api/check-out/1
Content-Type: application/json

{
  "checkOutTime": "2026-09-28T13:30:00"
}
```

### Occupancy
```http
GET /api/parking-lots/1/occupancy
```

## Overstay Rule
- Penalty rate is configured in `application.properties` as `parksmart.penalty-rate-per-hour=50`
- Overstay is calculated after the booked end time.
- Penalty is rounded up to the next hour.

## Notes
- The app uses `ddl-auto=update` so database tables are created automatically without deleting existing data.
- The project does not use Spring Security.
