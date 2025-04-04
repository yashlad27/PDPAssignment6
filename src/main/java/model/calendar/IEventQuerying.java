package model.calendar;

import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.EventNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for querying events in a calendar.
 * Provides methods for searching and filtering events.
 */
public interface IEventQuerying {
    /**
     * Gets all events in the calendar.
     *
     * @return list of all events
     */
    List<Event> getAllEvents();

    /**
     * Gets all recurring events in the calendar.
     *
     * @return list of all recurring events
     */
    List<RecurringEvent> getAllRecurringEvents();

    /**
     * Gets all events on a specific date.
     *
     * @param date the date to query
     * @return list of events on that date
     */
    List<Event> getEventsOnDate(LocalDate date);

    /**
     * Gets all events within a date range.
     *
     * @param startDate start of the range
     * @param endDate end of the range
     * @return list of events in the range
     */
    List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate);

    /**
     * Finds a specific event by subject and start time.
     *
     * @param subject event subject
     * @param startDateTime event start time
     * @return the found event
     * @throws EventNotFoundException if event not found
     */
    Event findEvent(String subject, LocalDateTime startDateTime) throws EventNotFoundException;
} 