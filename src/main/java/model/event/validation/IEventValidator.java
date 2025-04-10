package model.event.validation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.InvalidEventException;

/**
 * Comprehensive interface for event validation functionality.
 * This interface consolidates all event validation methods to provide
 * a unified interface for validating all aspects of events.
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
  void validateEventDates(LocalDateTime startDateTime, LocalDateTime endDateTime)
          throws InvalidEventException;

  /**
   * Validates event name.
   *
   * @param name the event name to validate
   * @throws InvalidEventException if the name is invalid
   */
  void validateEventNameWithException(String name) throws InvalidEventException;

  /**
   * Validates recurring event parameters.
   *
   * @param days        days of week for recurrence
   * @param occurrences number of occurrences
   * @throws InvalidEventException if parameters are invalid
   */
  void validateRecurringEventParams(Set<DayOfWeek> days, int occurrences)
          throws InvalidEventException;

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
  void validateEventTimes(LocalDateTime startTime, LocalDateTime endTime)
          throws InvalidEventException;

  /**
   * Validates event subject/name.
   *
   * @param subject the event subject
   * @return true if valid, false if invalid
   */
  boolean validateEventName(String subject);

  /**
   * Validates weekday string format.
   *
   * @param weekdays the weekday string
   * @return true if valid, false if invalid
   */
  boolean validateWeekdayString(String weekdays);

  /**
   * Parses and validates a weekday string, returning the corresponding days of week.
   *
   * @param weekdays the weekday string
   * @return a set of days of week
   * @throws InvalidEventException if the weekday string is invalid
   */
  Set<DayOfWeek> parseWeekdays(String weekdays) throws InvalidEventException;

  /**
   * Validates event description.
   *
   * @param description the event description
   * @return true if valid, false if invalid
   */
  boolean validateEventDescription(String description);

  /**
   * Validates event location.
   *
   * @param location the event location
   * @return true if valid, false if invalid
   */
  boolean validateEventLocation(String location);
}