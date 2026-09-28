package com.example.ParkSmart;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParkSmartApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void hibernateCreatesAllParkingTables() {
		Integer tableCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
						+ "WHERE TABLE_SCHEMA = 'public' "
						+ "AND TABLE_NAME IN ('parking_lots', 'slots', 'bookings', 'check_in_out')",
				Integer.class);

		assertEquals(4, tableCount);
	}

	@Test
	void completesParkingBookingWorkflowOverHttp() throws Exception {
		LocalDateTime startTime = LocalDateTime.now().minusMinutes(2).withNano(0);
		LocalDateTime endTime = LocalDateTime.now().plusMinutes(30).withNano(0);

		String parkingLotJson = mockMvc.perform(post("/api/parking-lots")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
						"name", "Integration Test Lot",
						"location", "Test Location",
						"totalSlots", 1))))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		JsonNode parkingLot = objectMapper.readTree(parkingLotJson);
		long parkingLotId = parkingLot.path("id").asLong();

		String slotJson = mockMvc.perform(post("/api/slots")
				.queryParam("parkingLotId", Long.toString(parkingLotId))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
						"slotNumber", "T1",
						"status", "AVAILABLE"))))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		long slotId = objectMapper.readTree(slotJson).path("id").asLong();

		mockMvc.perform(get("/api/parking-lots/{id}/available-slots", parkingLotId)
				.queryParam("startTime", startTime.toString())
				.queryParam("endTime", endTime.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].slotNumber").value("T1"));

		String bookingJson = mockMvc.perform(post("/api/bookings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
						"customerName", "Test Customer",
						"vehicleNumber", "TEST123",
						"slotId", slotId,
						"startTime", startTime,
						"endTime", endTime))))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		long bookingId = objectMapper.readTree(bookingJson).path("id").asLong();

		mockMvc.perform(post("/api/check-in/{bookingId}", bookingId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CHECKED_IN"));

		mockMvc.perform(post("/api/check-out/{bookingId}", bookingId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
						"checkOutTime", endTime.plusHours(1).plusSeconds(1)))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.overstayMinutes").value(61))
			.andExpect(jsonPath("$.penaltyAmount").value(100.0));

		mockMvc.perform(get("/api/bookings/{id}", bookingId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));
		mockMvc.perform(get("/api/parking-lots/{id}/occupancy", parkingLotId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.availableSlots").value(1))
				.andExpect(jsonPath("$.occupiedSlots").value(0));
	}

}
