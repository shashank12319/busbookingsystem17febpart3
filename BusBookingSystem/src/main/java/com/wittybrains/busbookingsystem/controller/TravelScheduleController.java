package com.wittybrains.busbookingsystem.controller;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.wittybrains.busbookingsystem.model.Bus;
import com.wittybrains.busbookingsystem.model.Driver;
import com.wittybrains.busbookingsystem.model.TravelSchedule;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.wittybrains.busbookingsystem.dto.TravelScheduleDTO;

import com.wittybrains.busbookingsystem.exception.InvalidSourceOrDestinationException;
import com.wittybrains.busbookingsystem.exception.UnprocessableEntityException;
import com.wittybrains.busbookingsystem.repository.BusRepository;
import com.wittybrains.busbookingsystem.repository.DriverRepository;
import com.wittybrains.busbookingsystem.repository.TravelScheduleRepository;
import com.wittybrains.busbookingsystem.service.TravelScheduleService;

@RestController
@RequestMapping("/schedules")
public class TravelScheduleController {

	@Autowired
	private TravelScheduleService travelScheduleService;
	@Autowired
	private BusRepository busRepository;
	@GetMapping()
    public ResponseEntity<String> getSchedules(
            @RequestParam("source") String source,
            @RequestParam("destination") String destination,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate date) {
        try {
            List<TravelSchedule> schedules = travelScheduleService.getAvailableSchedules(source, destination, date);
            if (schedules.isEmpty()) {
                return new ResponseEntity<>("No schedules found for the given source, destination, and date", HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>("success", HttpStatus.OK);
            }
        } catch (UnprocessableEntityException ex) {
            return handleUnprocessableEntityException(ex);
        }
    }
	@PostMapping
	public ResponseEntity createSchedule(@RequestBody TravelScheduleDTO travelScheduleDTO) throws ParseException {
	    try {
	        if (travelScheduleService.createSchedule(travelScheduleDTO)) {
	            return new ResponseEntity<>("Successfully created travel schedule", HttpStatus.CREATED);
	        } else {
	            return new ResponseEntity<>("Unable to create travel schedule", HttpStatus.BAD_REQUEST);
	        }
	    } catch (UnprocessableEntityException ex) {
	        return handleUnprocessableEntityException(ex);
	    }
	}

	
//	@GetMapping("/{scheduleId}")
//	public ResponseEntity<TravelSchedule> getScheduleById(@PathVariable Long scheduleId) {
//	    Optional<TravelSchedule> schedule = Optional.ofNullable(travelScheduleService.getScheduleById(scheduleId));
//	    if (schedule.isPresent()) {
//	        return new ResponseEntity<>(schedule.get(), HttpStatus.OK);
//	    } else {
//	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found");
//	    }
//	}
	@ExceptionHandler(UnprocessableEntityException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public ResponseEntity<String> handleUnprocessableEntityException(UnprocessableEntityException ex) {
	    return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
	}
}

