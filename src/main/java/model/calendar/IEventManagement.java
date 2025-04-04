package model.calendar;

import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

import java.time.LocalDateTime;

/**
 * Interface for basic event management operations.
 * Handles creation, modification, and deletion of events.
 */
public interface IEventManagement {
    /**
     * Adds a single event to the calendar with optional conflict checking.
     *
     * @param event       The event to add, must not be null
     * @param autoDecline If true, throws exception on conflict; if false, returns false
     * @return true if the event was added successfully
     * @throws ConflictingEventException if autoDecline is true and event conflicts
     */
    boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException;

    /**
     * Adds a recurring event to the calendar with optional conflict checking.
     *
     * @param recurringEvent The recurring event to add
     * @param autoDecline    If true, throws exception on conflict
     * @return true if successful
     * @throws ConflictingEventException if there's a conflict
     */
    boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) 
            throws ConflictingEventException;

    /**
     * Edits a single event in the calendar.
     *
     * @param subject       The subject of the event to edit
     * @param startDateTime The start date/time of the event
     * @param property      The property to edit
     * @param newValue      The new value for the property
     * @return true if successful
     * @throws EventNotFoundException if event not found
     * @throws InvalidEventException if invalid parameters
     * @throws ConflictingEventException if edit creates conflict
     */
    boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property, String newValue)
            throws EventNotFoundException, InvalidEventException, ConflictingEventException;

    /**
     * Edits all events in a recurring series from a date.
     *
     * @param subject       The subject of the events
     * @param startDateTime The start date/time
     * @param property      The property to edit
     * @param newValue      The new value
     * @return number of events edited
     * @throws InvalidEventException if invalid parameters
     * @throws ConflictingEventException if edit creates conflict
     */
    int editEventsFromDate(String subject, LocalDateTime startDateTime, String property, String newValue)
            throws InvalidEventException, ConflictingEventException;
} 