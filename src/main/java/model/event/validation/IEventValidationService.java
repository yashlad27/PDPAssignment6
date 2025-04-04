package model.event.validation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import model.event.Event;
import model.exceptions.InvalidEventException;

/**
 * Interface for event validation operations.
 * Provides methods for validating different aspects of events.
 */
public interface IEventValidationService {
  /**
   * Validates event dates.
   *
   * @param start start date/time
   * @param end   end date/time
   * @throws InvalidEventException if dates are invalid
   */
  void validateEventDates(LocalDateTime start, LocalDateTime end) throws InvalidEventException;

  /**
   * Validates recurring event parameters.
   *
   * @param days        days of week for recurrence
   * @param occurrences number of occurrences
   * @throws InvalidEventException if parameters are invalid
   */
  void validateRecurringEventParams(Set<DayOfWeek> days, int occurrences) throws InvalidEventException;

  /**
   * Validates all-day event parameters.
   *
   * @param date the event date
   * @throws InvalidEventException if date is invalid
   */
  void validateAllDayEventParams(LocalDate date) throws InvalidEventException;

  /**
   * Validates event times.
   *
   * @param startTime the start time of the event
   * @param endTime   the end time of the event
   * @throws InvalidEventException if the times are invalid
   */
  void validateEventTimes(LocalDateTime startTime, LocalDateTime endTime) throws InvalidEventException;

  /**
   * Validates an event name.
   *
   * @param name the name to validate
   * @return true if valid, false otherwise
   */
  boolean validateEventName(String name);

  /**
   * Validates an event description.
   *
   * @param description the description to validate
   * @return true if valid, false otherwise
   */
  boolean validateEventDescription(String description);

  /**
   * Validates an event location.
   *
   * @param location the location to validate
   * @return true if valid, false otherwise
   */
  boolean validateEventLocation(String location);

  /**
   * Validates an entire event.
   *
   * @param event the event to validate
   * @throws InvalidEventException if the event is invalid
   */
  void validateEvent(Event event) throws InvalidEventException;
} 