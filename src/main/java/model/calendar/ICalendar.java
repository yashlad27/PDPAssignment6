package model.calendar;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import model.export.IDataExporter;

/**
 * Interface defining the core functionality of a calendar system.
 *
 * <p>This interface provides a comprehensive API for managing calendar events, including: -
 * Creating and managing single events - Creating and managing recurring events - Querying events by
 * date or date range - Editing event properties - Checking schedule conflicts - Exporting calendar
 * data
 *
 * <p>The calendar supports two main types of events: 1. Single events: One-time events with a
 * specific start and end time 2. Recurring events: Events that repeat on specified days with
 * either: - A fixed number of occurrences - An end date
 *
 * <p>All operations that modify events include conflict checking to maintain calendar consistency
 * and prevent double-booking.
 */
public interface ICalendar {

  /**
   * Gets the name of the calendar.
   *
   * @return the name of the calendar
   */
  String getName();

  /**
   * Sets the name of the calendar.
   *
   * @param name the name to set
   */
  void setName(String name);

  /**
   * Adds a single event to the calendar with optional conflict checking.
   *
   * <p>If autoDecline is true, the method will throw a ConflictingEventException when the event
   * conflicts with any existing events. If false, it will return false for conflicts, allowing the
   * caller to handle the situation.
   *
   * @param event       The event to add, must not be null
   * @param autoDecline If true, throws exception on conflict; if false, returns false
   * @return true if the event was added successfully, false if there was a conflict
   * @throws ConflictingEventException if autoDecline is true and the event conflicts with existing
   *                                   events.
   */
  boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException;

  /**
   * Adds a recurring event to the calendar with optional conflict checking.
   *
   * <p>The method checks for conflicts across all occurrences of the recurring event. If
   * autoDecline is true, it will throw an exception if any occurrence conflicts with existing
   * events. If false, it will return false for conflicts.
   *
   * @param recurringEvent The recurring event to add, must not be null
   * @param autoDecline    If true, throws exception on conflict; if false, returns false
   * @return true if the event was added successfully, false if there was a conflict
   */
  boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline)
      throws ConflictingEventException;

  /**
   * Creates a recurring event that repeats on specified weekdays until a given end date.
   *
   * <p>Example weekdays format: "MWF" for Monday, Wednesday, Friday Valid weekday codes: M
   * (Monday), T (Tuesday), W (Wednesday), R (Thursday), F (Friday), S (Saturday), U (Sunday)
   *
   * <p>The event will repeat on the specified weekdays starting from the start date until the
   * untilDate (inclusive). Each occurrence will have the same duration as the first occurrence.
   *
   * @param name        Event name/subject
   * @param start       Start date and time of the first occurrence
   * @param end         End date and time of the first occurrence
   * @param weekdays    String specifying which days of the week the event repeats on
   * @param untilDate   The last date on which the event can occur
   * @param autoDecline If true, throws exception on conflict; if false, returns false
   * @return true if event was created successfully
   * @throws InvalidEventException     if any parameters are invalid
   * @throws ConflictingEventException if autoDecline is true and any occurrence conflicts
   */
  boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
      String weekdays, LocalDate untilDate, boolean autoDecline)
      throws InvalidEventException, ConflictingEventException;

  /**
   * Creates an all-day recurring event with a fixed number of occurrences.
   *
   * <p>An all-day event spans from the start to the end of the specified date(s). The event will
   * repeat on the specified weekdays for the given number of occurrences, starting from the initial
   * date.
   *
   * @param name        Event name/subject
   * @param date        Initial date of the event
   * @param weekdays    String specifying which days of the week the event repeats on
   * @param occurrences Number of times the event should occur
   * @param autoDecline If true, throws exception on conflict; if false, returns false
   * @param description Optional description of the event
   * @param location    Optional location of the event
   * @param isPublic    Whether the event is public or private
   * @return true if event was created successfully
   * @throws InvalidEventException     if any parameters are invalid
   * @throws ConflictingEventException if autoDecline is true and any occurrence conflicts
   */
  boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays, int occurrences,
      boolean autoDecline, String description, String location, boolean isPublic)
      throws InvalidEventException, ConflictingEventException;

  /**
   * Creates an all-day recurring event that repeats until a specific date.
   *
   * @param name        the name of the event
   * @param date        the date of the event
   * @param weekdays    the days of the week to repeat on (e.g., "MWF")
   * @param untilDate   the date until which to repeat (inclusive)
   * @param autoDecline whether to automatically decline if there's a conflict
   * @return true if event was created successfully
   * @throws InvalidEventException     if any parameters are invalid
   * @throws ConflictingEventException if autoDecline is true and any occurrence conflicts
   */
  boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
      LocalDate untilDate, boolean autoDecline, String description, String location,
      boolean isPublic) throws InvalidEventException, ConflictingEventException;

  /**
   * Gets all events occurring on a specific date.
   *
   * <p>This includes: - Single events that occur on the date - Occurrences of recurring events
   * that fall on the date - All-day events scheduled for the date
   *
   * @param date The date to query
   * @return List of events occurring on the date, empty list if none found
   * @throws IllegalArgumentException if date is null
   */
  List<Event> getEventsOnDate(LocalDate date);

  /**
   * Gets all events occurring within a date range (inclusive).
   *
   * <p>This includes: - Single events within the range - Occurrences of recurring events that fall
   * within the range - All-day events scheduled within the range
   *
   * <p>Events that partially overlap with the range (start before but end within, or start within
   * but end after) are included.
   *
   * @param startDate Start of the date range (inclusive)
   * @param endDate   End of the date range (inclusive)
   * @return List of events within the range, empty list if none found
   * @throws IllegalArgumentException if either date is null or endDate is before startDate
   */
  List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate);

  /**
   * Checks if there are any events at a specific date and time.
   *
   * @param dateTime the date and time to check
   * @return true if there is at least one event at the given date and time, false otherwise
   * @throws IllegalArgumentException if the dateTime is null
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Finds an event by its subject and start date/time.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time of the event
   * @return the matching event, or null if not found
   * @throws EventNotFoundException if no matching event is found
   */
  Event findEvent(String subject, LocalDateTime startDateTime) throws EventNotFoundException;

  /**
   * Gets all events in the calendar.
   *
   * @return a list of all events in the calendar
   */
  List<Event> getAllEvents();

  /**
   * Edits a single event identified by its subject and start date/time.
   *
   * <p>Supported properties for editing: - subject: The event's name/title - startTime: The
   * event's start time (format: HH:mm) - endTime: The event's end time (format: HH:mm) -
   * description: The event's description - location: The event's location - isPublic: The event's
   * visibility (true/false)
   *
   * @param subject       The subject of the event to edit
   * @param startDateTime The start date/time of the event to edit
   * @param property      The property to edit (case-sensitive)
   * @param newValue      The new value for the property
   * @return true if the event was found and edited successfully
   * @throws EventNotFoundException    if no matching event is found
   * @throws InvalidEventException     if the property or new value is invalid
   * @throws ConflictingEventException if the edit would create a conflict
   */
  boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
      String newValue)
      throws EventNotFoundException, InvalidEventException, ConflictingEventException;

  /**
   * Updates an existing event with a new version.
   *
   * @param eventId      The UUID of the event to update
   * @param updatedEvent The new version of the event
   * @return true if the event was successfully updated, false otherwise
   * @throws ConflictingEventException if the updated event conflicts with existing events
   */
  boolean updateEvent(UUID eventId, Event updatedEvent) throws ConflictingEventException;

  /**
   * Edits all events in a recurring series starting from a specific date.
   *
   * @param subject       the subject of the recurring events to edit
   * @param startDateTime the start date/time to begin editing from
   * @param property      the property to edit
   * @param newValue      the new value for the property
   * @return the number of events that were edited
   * @throws InvalidEventException     if the property or new value is invalid
   * @throws ConflictingEventException if the edit would create a conflict
   */
  int editEventsFromDate(String subject, LocalDateTime startDateTime, String property,
      String newValue) throws InvalidEventException, ConflictingEventException;

  /**
   * Edits all events with a specific subject.
   *
   * @param subject  the subject of the events to edit
   * @param property the property to edit
   * @param newValue the new value for the property
   * @return the number of events that were edited
   * @throws InvalidEventException     if the property or new value is invalid
   * @throws ConflictingEventException if the edit would create a conflict
   */
  int editAllEvents(String subject, String property, String newValue)
      throws InvalidEventException, ConflictingEventException;

  /**
   * Gets all recurring events in the calendar.
   *
   * @return a list of all recurring events in the calendar
   */
  List<RecurringEvent> getAllRecurringEvents();

  /**
   * Exports the calendar data using the specified exporter.
   *
   * @param filePath the path where the file should be created
   * @param exporter the exporter to use for formatting the data
   * @return the path of the created file
   * @throws IOException              if there are issues writing to the file
   * @throws IllegalArgumentException if filePath is null or empty
   */
  String exportData(String filePath, IDataExporter exporter) throws IOException;

  /**
   * Gets the timezone of this calendar.
   *
   * @return the timezone of the calendar
   */
  TimeZone getTimeZone();
}