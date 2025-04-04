package model.event.validation;

import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.InvalidEventException;

/**
 * Interface for event validation functionality.
 * This interface defines methods for validating different types of events.
 */
public interface IEventValidator {
  /**
   * Validates a single event.
   *
   * @param event the event to validate
   * @throws InvalidEventException if the event is invalid
   */
  void validateEvent(Event event) throws InvalidEventException;

  /**
   * Validates a recurring event.
   *
   * @param event the recurring event to validate
   * @throws InvalidEventException if the event is invalid
   */
  void validateRecurringEvent(RecurringEvent event) throws InvalidEventException;

  /**
   * Validates event dates.
   *
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   * @throws InvalidEventException if the dates are invalid
   */
  void validateEventDates(java.time.LocalDateTime startDateTime, java.time.LocalDateTime endDateTime)
          throws InvalidEventException;

  /**
   * Validates event name.
   *
   * @param name the event name to validate
   * @throws InvalidEventException if the name is invalid
   */
  void validateEventNameWithException(String name) throws InvalidEventException;
} 