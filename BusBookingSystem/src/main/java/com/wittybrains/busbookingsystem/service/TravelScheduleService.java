package com.wittybrains.busbookingsystem.service;



import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wittybrains.busbookingsystem.dto.TravelScheduleDTO;
import com.wittybrains.busbookingsystem.exception.InvalidSourceOrDestinationException;
import com.wittybrains.busbookingsystem.model.Bus;
import com.wittybrains.busbookingsystem.model.TravelSchedule;
import com.wittybrains.busbookingsystem.repository.BusRepository;
import com.wittybrains.busbookingsystem.repository.TravelScheduleRepository;


@Service
public class TravelScheduleService {
	private static final int MAX_SEARCH_DAYS = 30;
    @Autowired
    private TravelScheduleRepository scheduleRepository;

    @Autowired
    private BusRepository busRepository;

    public List<TravelSchedule> getAvailableSchedules(String source, String destination, LocalDate searchDate) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime searchDateTime = LocalDateTime.of(searchDate, LocalTime.MIDNIGHT);
        LocalDateTime minimumDateTime;

        if (searchDateTime.isBefore(currentDateTime)) {
            // cannot search for past schedules
            throw new IllegalArgumentException("Cannot search for schedules in the past");
        } else if (searchDateTime.toLocalDate().equals(currentDateTime.toLocalDate())) {
            // search for schedules at least 1 hour from now
            currentDateTime = currentDateTime.plusHours(1);
        }
       
        LocalDateTime maxSearchDateTime = currentDateTime.plusDays(MAX_SEARCH_DAYS);
        if (searchDateTime.isAfter(maxSearchDateTime)) {
            // cannot search for schedules more than one month in the future
            throw new IllegalArgumentException("Cannot search for schedules more than one month in the future");
        }
        
        List<TravelSchedule> travelScheduleList = scheduleRepository
                .findBySourceAndDestinationAndEstimatedArrivalTimeAfter(source, destination, currentDateTime);
        return travelScheduleList;
    }
    public TravelSchedule getScheduleById(Long scheduleId) {
        Optional<TravelSchedule> optionalSchedule = scheduleRepository.findById(scheduleId);

        if (!optionalSchedule.isPresent()) {
            throw new IllegalArgumentException("Schedule with ID " + scheduleId + " not found");
        }

        return optionalSchedule.get();
    }

    public boolean createSchedule(TravelScheduleDTO travelScheduleDTO) throws ParseException {
        if (travelScheduleDTO.getBusId() == null) {
            throw new IllegalArgumentException("Bus ID cannot be null");
        }

        Optional<Bus> optionalBus = busRepository.findById(travelScheduleDTO.getBusId());

        if (!optionalBus.isPresent()) {
            throw new IllegalArgumentException("Bus with ID " + travelScheduleDTO.getBusId() + " not found");
        }

        TravelSchedule travelschedule = new TravelSchedule();

        travelschedule.setBus(optionalBus.get());

        String source = travelScheduleDTO.getSource();
        String destination = travelScheduleDTO.getDestination();
        if (source == null || source.isBlank() || destination == null || destination.isBlank()) {
            throw new InvalidSourceOrDestinationException("Invalid source or destination");
        }

        travelschedule.setDestination(destination);

        try {
            travelschedule.setEstimatedArrivalTime(LocalDateTime.parse(travelScheduleDTO.getEstimatedArrivalTime()));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid estimated arrival time format: " + ex.getMessage());
        }

        try {
            travelschedule.setEstimatedDepartureTime(LocalDateTime.parse(travelScheduleDTO.getEstimatedDepartureTime()));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid estimated departure time format: " + ex.getMessage());
        }

        try {
            travelschedule.setFareAmount(travelScheduleDTO.getFareAmount());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid fare amount: " + ex.getMessage());
        }

        travelschedule.setSource(source);

       

        travelschedule = scheduleRepository.save(travelschedule);
        return travelschedule.getScheduleId() != null;
    }




}







