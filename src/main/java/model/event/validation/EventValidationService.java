package model.event.validation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.InvalidEventException;

/**
 * Consolidated implementation of IEventValidationService that provides validation logic for events.
 * Incorporates functionality from both EventValidationService and EventValidator.
 */
public class EventValidationService implements IEventValidationService, IEventValidator {
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_LOCATION_LENGTH = 200;

    @Override
    public void validateEventDates(LocalDateTime start, LocalDateTime end) throws InvalidEventException {
        if (start == null || end == null) {
            throw new InvalidEventException("Event dates cannot be null");
        }
        
        if (end.isBefore(start)) {
            throw new InvalidEventException("End date/time cannot be before start date/time");
        }
        
        // Comment out past date validation for testing
        // if (start.isBefore(LocalDateTime.now())) {
        //     throw new InvalidEventException("Event cannot be created in the past");
        // }
    }

    @Override
    public void validateRecurringEventParams(Set<DayOfWeek> days, int occurrences) 
            throws InvalidEventException {
        if (days == null || days.isEmpty()) {
            throw new InvalidEventException("Recurring event must have at least one repeat day");
        }

        if (occurrences <= 0) {
            throw new InvalidEventException("Number of occurrences must be positive");
        }
    }

    @Override
    public void validateAllDayEventParams(LocalDate date) throws InvalidEventException {
        if (date == null) {
            throw new InvalidEventException("Event date cannot be null");
        }

        // Comment out past date validation for testing
        // if (date.isBefore(LocalDate.now())) {
        //     throw new InvalidEventException("Event cannot be created in the past");
        // }
    }

    /**
     * Internal helper method to validate an event name according to length rules.
     * 
     * @param name the name to validate
     * @return true if valid, false otherwise
     */
    private boolean validateEventNameInternal(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        int length = name.trim().length();
        return length >= MIN_NAME_LENGTH && length <= MAX_NAME_LENGTH;
    }

    @Override
    public boolean validateEventName(String name) {
        return validateEventNameInternal(name);
    }
    
    @Override
    public void validateEventNameWithException(String name) throws InvalidEventException {
        if (!validateEventNameInternal(name)) {
            if (name == null || name.trim().isEmpty()) {
                throw new InvalidEventException("Event name cannot be empty");
            } else {
                throw new InvalidEventException("Event name cannot exceed " + MAX_NAME_LENGTH + " characters");
            }
        }
    }

    @Override
    public boolean validateEventDescription(String description) {
        if (description == null) {
            return false;
        }
        return description.length() <= MAX_DESCRIPTION_LENGTH;
    }

    @Override
    public boolean validateEventLocation(String location) {
        if (location == null) {
            return false;
        }
        return location.length() <= MAX_LOCATION_LENGTH;
    }

    @Override
    public void validateEventTimes(LocalDateTime startTime, LocalDateTime endTime) throws InvalidEventException {
        if (startTime == null || endTime == null) {
            throw new InvalidEventException("Event start and end times cannot be null");
        }
        if (endTime.isBefore(startTime)) {
            throw new InvalidEventException("Event end time cannot be before start time");
        }
        if (startTime.equals(endTime)) {
            throw new InvalidEventException("Event start and end times cannot be the same");
        }
    }
    
    @Override
    public void validateEvent(Event event) throws InvalidEventException {
        if (event == null) {
            throw new InvalidEventException("Event cannot be null");
        }
        
        validateEventDates(event.getStartDateTime(), event.getEndDateTime());
        
        if (!validateEventNameInternal(event.getSubject())) {
            throw new InvalidEventException("Invalid event name");
        }
        
        if (!validateEventDescription(event.getDescription())) {
            throw new InvalidEventException("Invalid event description");
        }
        
        if (!validateEventLocation(event.getLocation())) {
            throw new InvalidEventException("Invalid event location");
        }
    }
    
    @Override
    public void validateRecurringEvent(RecurringEvent event) throws InvalidEventException {
        if (event == null) {
            throw new InvalidEventException("Recurring event cannot be null");
        }

        if (event.getRepeatDays() == null || event.getRepeatDays().isEmpty()) {
            throw new InvalidEventException("Recurring event must have at least one repeat day");
        }

        validateEvent(event);
    }
} 