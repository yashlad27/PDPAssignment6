package controller.command.edit.strategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

/**
 * Consolidated event editor that combines the functionality of all event editor strategies.
 * This class follows the strategy pattern and provides factory methods for creating 
 * different types of edit operations (single event, all series events, series from date).
 */
public class ConsolidatedEventEditor implements EventEditor {
  
  private final UUID eventId;
  private final String subject;
  private final String property;
  private final String newValue;
  private final LocalDate fromDate;
  private final EditType editType;
  private final ICalendar calendar; // For direct editing by subject and date
  private final String startDateTime; // For direct editing by subject and date
  
  /**
   * Types of edit operations available.
   */
  public enum EditType {
    SINGLE_EVENT,
    ALL_EVENTS, 
    SERIES_FROM_DATE
  }
  
  private ConsolidatedEventEditor(UUID eventId, String subject, String property, String newValue, 
                                 LocalDate fromDate, EditType editType) {
    this.eventId = eventId;
    this.subject = subject;
    this.property = property;
    this.newValue = newValue;
    this.fromDate = fromDate;
    this.editType = editType;
    this.calendar = null;
    this.startDateTime = null;
  }
  
  /**
   * Constructor for direct event editing by subject and start date time.
   * 
   * @param calendar      the calendar containing the events
   * @param subject       the event subject/name
   * @param startDateTime the event start date time string
   * @param property      the property to edit
   * @param newValue      the new value for the property
   */
  public ConsolidatedEventEditor(ICalendar calendar, String subject, String startDateTime, 
                                String property, String newValue) {
    this.calendar = calendar;
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.property = property;
    this.newValue = newValue;
    this.eventId = null;
    this.fromDate = null;
    this.editType = null;
  }
  
  /**
   * Creates an editor for editing a single event.
   *
   * @param eventId  the ID of the event to edit
   * @param subject  the subject of the event (for identification)
   * @param property the property to edit
   * @param newValue the new value for the property
   * @return a new event editor for single event editing
   */
  public static ConsolidatedEventEditor createSingleEventEditor(UUID eventId, String subject,
                                                               String property, String newValue) {
    return new ConsolidatedEventEditor(eventId, subject, property, newValue, null, EditType.SINGLE_EVENT);
  }
  
  /**
   * Creates an editor for editing all events in a recurring series.
   *
   * @param eventId  the ID of any event in the series
   * @param subject  the subject of the events (for identification)
   * @param property the property to edit
   * @param newValue the new value for the property
   * @return a new event editor for editing all events in a series
   */
  public static ConsolidatedEventEditor createAllEventsEditor(UUID eventId, String subject,
                                                             String property, String newValue) {
    return new ConsolidatedEventEditor(eventId, subject, property, newValue, null, EditType.ALL_EVENTS);
  }
  
  /**
   * Creates an editor for editing events in a series from a specific date.
   *
   * @param eventId  the ID of any event in the series
   * @param subject  the subject of the events (for identification)
   * @param property the property to edit
   * @param newValue the new value for the property
   * @param fromDate the date from which to start editing
   * @return a new event editor for editing events from a specific date
   */
  public static ConsolidatedEventEditor createSeriesFromDateEditor(UUID eventId, String subject,
                                                                 String property, String newValue,
                                                                 LocalDate fromDate) {
    if (fromDate == null) {
      throw new IllegalArgumentException("From date cannot be null for series from date editing");
    }
    return new ConsolidatedEventEditor(eventId, subject, property, newValue, fromDate, EditType.SERIES_FROM_DATE);
  }
  
  /**
   * Edit an event by subject and start date time.
   * 
   * @param subject       the event subject/name
   * @param startDateTime the event start date time string
   * @param property      the property to edit
   * @param newValue      the new value for the property
   * @return a message indicating the result of the operation
   * @throws EventNotFoundException if the event is not found
   * @throws InvalidEventException if there's an issue with the event properties
   * @throws ConflictingEventException if there's a conflict with another event
   */
  public String editEvent(String subject, String startDateTime, String property, String newValue)
      throws EventNotFoundException, InvalidEventException, ConflictingEventException {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    
    // Process values by removing quotes if present
    String processedSubject = removeQuotes(subject);
    String processedProperty = removeQuotes(property);
    String processedValue = removeQuotes(newValue);
    
    // Validate parameters
    validateParameters(processedSubject, processedProperty, processedValue);
    
    try {
      // Parse the start date time
      LocalDateTime parsedStartDateTime = parseDateTime(startDateTime);
      
      // Find the event by subject and start time
      Event event = calendar.findEvent(processedSubject, parsedStartDateTime);
      if (event == null) {
        throw new EventNotFoundException("Event not found: " + processedSubject + " at " + startDateTime);
      }
      
      // Update the property
      updateEventProperty(event, processedProperty, processedValue);
      
      // Since there's no direct updateEvent method, we need to add the updated event back
      calendar.addEvent(event, true); // Add with conflict checking
      
      return "Event updated: " + event.getSubject();
    } catch (DateTimeParseException e) {
      throw new InvalidEventException("Invalid date/time format: " + startDateTime);
    }
  }
  
  @Override
  public String executeEdit(ICalendar calendar) throws EventNotFoundException, InvalidEventException, 
      ConflictingEventException {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    
    // If this editor was initialized for direct subject+date editing
    if (this.calendar != null && startDateTime != null) {
      return editEvent(subject, startDateTime, property, newValue);
    }
    
    // Process the value by removing quotes if present
    String processedValue = removeQuotes(newValue);
    
    // Validate parameters
    validateParameters(subject, property, processedValue);
    
    switch (editType) {
      case SINGLE_EVENT:
        return editSingleEvent(calendar, eventId, property, processedValue);
      case ALL_EVENTS:
        return editAllEvents(calendar, eventId, property, processedValue);
      case SERIES_FROM_DATE:
        return editSeriesFromDate(calendar, eventId, property, processedValue, fromDate);
      default:
        throw new InvalidEventException("Unknown edit type: " + editType);
    }
  }
  
  /**
   * Edits a single event with the given property and value.
   */
  private String editSingleEvent(ICalendar calendar, UUID eventId, String property, String value) 
      throws EventNotFoundException, InvalidEventException, ConflictingEventException {
    Event event = null;
    for (Event e : calendar.getAllEvents()) {
      if (e.getId().equals(eventId)) {
        event = e;
        break;
      }
    }
    if (event == null) {
      throw new EventNotFoundException("Event not found with ID: " + eventId);
    }
    
    updateEventProperty(event, property, value);
    // Update the event in the calendar
    // Since there's no direct updateEvent(Event) method, we need to handle this differently
    // First remove the old event and then add the updated one
    try {
      calendar.findEvent(event.getSubject(), event.getStartDateTime()); // Check if it exists
      calendar.addEvent(event, true); // Add it with conflict checking
    } catch (ConflictingEventException e) {
      throw e;
    }
    
    return "Event updated: " + event.getSubject();
  }
  
  /**
   * Edits all events in a recurring series.
   */
  private String editAllEvents(ICalendar calendar, UUID eventId, String property, String value) 
      throws EventNotFoundException, InvalidEventException, ConflictingEventException {
    Event event = null;
    for (Event e : calendar.getAllEvents()) {
      if (e.getId().equals(eventId)) {
        event = e;
        break;
      }
    }
    if (event == null) {
      throw new EventNotFoundException("Event not found with ID: " + eventId);
    }
    
    if (!(event instanceof RecurringEvent)) {
      return editSingleEvent(calendar, eventId, property, value);
    }
    
    RecurringEvent recurringEvent = (RecurringEvent) event;
    UUID seriesId = recurringEvent.getId(); // Use getId instead of getSeriesId
    
    // Update all events in the series
    int count = 0;
    for (Event e : calendar.getAllEvents()) {
      if (e instanceof RecurringEvent) {
        RecurringEvent re = (RecurringEvent) e;
        if (re.getId().equals(seriesId)) {
          updateEventProperty(e, property, value);
          try {
            calendar.findEvent(e.getSubject(), e.getStartDateTime()); // Check if it exists
            calendar.addEvent(e, true); // Add it with conflict checking
          } catch (ConflictingEventException ex) {
            throw ex;
          }
          count++;
        }
      }
    }
    
    return "Updated " + count + " events in the series";
  }
  
  /**
   * Edits events in a series from a specific date.
   */
  private String editSeriesFromDate(ICalendar calendar, UUID eventId, String property, String value, LocalDate fromDate) 
      throws EventNotFoundException, InvalidEventException, ConflictingEventException {
    Event event = null;
    for (Event e : calendar.getAllEvents()) {
      if (e.getId().equals(eventId)) {
        event = e;
        break;
      }
    }
    if (event == null) {
      throw new EventNotFoundException("Event not found with ID: " + eventId);
    }
    
    if (!(event instanceof RecurringEvent)) {
      return editSingleEvent(calendar, eventId, property, value);
    }
    
    RecurringEvent recurringEvent = (RecurringEvent) event;
    UUID seriesId = recurringEvent.getId(); // Use getId instead of getSeriesId
    
    // Update events in the series from the specified date
    int count = 0;
    for (Event e : calendar.getAllEvents()) {
      if (e instanceof RecurringEvent) {
        RecurringEvent re = (RecurringEvent) e;
        if (re.getId().equals(seriesId) && !e.getStartDateTime().toLocalDate().isBefore(fromDate)) {
          updateEventProperty(e, property, value);
          try {
            calendar.findEvent(e.getSubject(), e.getStartDateTime()); // Check if it exists
            calendar.addEvent(e, true); // Add it with conflict checking
          } catch (ConflictingEventException ex) {
            throw ex;
          }
          count++;
        }
      }
    }
    
    return "Updated " + count + " events in the series from " + fromDate;
  }
  
  /**
   * Updates a specific property of an event.
   */
  private void updateEventProperty(Event event, String property, String value) throws InvalidEventException {
    switch (property.toLowerCase()) {
      case "title":
      case "subject":
        event.setSubject(value);
        break;
      case "description":
        event.setDescription(value);
        break;
      case "location":
        event.setLocation(value);
        break;
      case "start":
        try {
          LocalDateTime startDateTime = parseDateTime(value);
          event.setStartDateTime(startDateTime);
        } catch (DateTimeParseException e) {
          throw new InvalidEventException("Invalid start date/time format: " + value);
        }
        break;
      case "end":
        try {
          LocalDateTime endDateTime = parseDateTime(value);
          event.setEndDateTime(endDateTime);
        } catch (DateTimeParseException e) {
          throw new InvalidEventException("Invalid end date/time format: " + value);
        }
        break;
      case "privacy":
      case "private":
      case "public":
        boolean isPublic = "public".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
        event.setPublic(isPublic);
        break;
      default:
        throw new InvalidEventException("Unknown property: " + property);
    }
  }
  
  /**
   * Parses a date-time string into a LocalDateTime object.
   */
  private LocalDateTime parseDateTime(String dateTimeStr) {
    // Try several common formats
    try {
      return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (DateTimeParseException e1) {
      try {
        // Try parsing date only
        LocalDate date = LocalDate.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE);
        return LocalDateTime.of(date, LocalTime.MIDNIGHT);
      } catch (DateTimeParseException e2) {
        try {
          // Try custom format
          return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (DateTimeParseException e3) {
          throw new DateTimeParseException("Unable to parse date-time: " + dateTimeStr, dateTimeStr, 0);
        }
      }
    }
  }
  
  /**
   * Removes surrounding quotes from a string value if present.
   */
  private String removeQuotes(String value) {
    if (value != null && value.length() >= 2) {
      if ((value.startsWith("\"") && value.endsWith("\"")) ||
              (value.startsWith("'") && value.endsWith("'"))) {
        return value.substring(1, value.length() - 1);
      }
    }
    return value;
  }
  
  /**
   * Validates edit parameters.
   */
  private void validateParameters(String subject, String property, String newValue) throws InvalidEventException {
    if (property == null || property.trim().isEmpty()) {
      throw new InvalidEventException("Property to edit cannot be null or empty");
    }
    
    if (newValue == null) {
      throw new InvalidEventException("New value cannot be null");
    }
  }
}
