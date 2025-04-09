package controller.command.copy.strategy;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;
import utilities.TimeZoneHandler;

/**
 * A consolidated strategy for copying events between calendars.
 * This class combines the functionality of SingleEventCopyStrategy,
 * DayEventsCopyStrategy, and RangeEventsCopyStrategy into a single class
 * with factory methods for creating the appropriate strategy.
 */
public class ConsolidatedCopyStrategy implements CopyStrategy {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;
  private final CopyType copyType;

  /**
   * Enum representing the different types of copy operations.
   */
  public enum CopyType {
    SINGLE_EVENT,
    DAY_EVENTS,
    RANGE_EVENTS
  }

  /**
   * Private constructor for ConsolidatedCopyStrategy.
   *
   * @param calendarManager the calendar manager
   * @param timezoneHandler the timezone handler
   * @param copyType        the type of copy operation
   * @param args            the command arguments
   */
  private ConsolidatedCopyStrategy(CalendarManager calendarManager, TimeZoneHandler timezoneHandler,
                                   CopyType copyType, String[] args) {
    if (calendarManager == null) {
      throw new IllegalArgumentException("CalendarManager cannot be null");
    }
    if (timezoneHandler == null) {
      throw new IllegalArgumentException("TimeZoneHandler cannot be null");
    }
    this.calendarManager = calendarManager;
    this.timezoneHandler = timezoneHandler;
    this.copyType = copyType;
  }

  /**
   * Factory method to create a single event copy strategy.
   *
   * @param calendarManager the calendar manager
   * @param timezoneHandler the timezone handler
   * @param args            the command arguments
   * @return a new ConsolidatedCopyStrategy configured for single event copying
   */
  public static ConsolidatedCopyStrategy createSingleEventStrategy(
          CalendarManager calendarManager, TimeZoneHandler timezoneHandler, String[] args) {
    return new ConsolidatedCopyStrategy(calendarManager, timezoneHandler, CopyType.SINGLE_EVENT, args);
  }

  /**
   * Factory method to create a day events copy strategy.
   *
   * @param calendarManager the calendar manager
   * @param timezoneHandler the timezone handler
   * @param args            the command arguments
   * @return a new ConsolidatedCopyStrategy configured for day events copying
   */
  public static ConsolidatedCopyStrategy createDayEventsStrategy(
          CalendarManager calendarManager, TimeZoneHandler timezoneHandler, String[] args) {
    return new ConsolidatedCopyStrategy(calendarManager, timezoneHandler, CopyType.DAY_EVENTS, args);
  }

  /**
   * Factory method to create a range events copy strategy.
   *
   * @param calendarManager the calendar manager
   * @param timezoneHandler the timezone handler
   * @param args            the command arguments
   * @return a new ConsolidatedCopyStrategy configured for range events copying
   */
  public static ConsolidatedCopyStrategy createRangeEventsStrategy(
          CalendarManager calendarManager, TimeZoneHandler timezoneHandler, String[] args) {
    return new ConsolidatedCopyStrategy(calendarManager, timezoneHandler, CopyType.RANGE_EVENTS, args);
  }

  @Override
  public String execute(String[] args) throws CalendarNotFoundException,
          EventNotFoundException, ConflictingEventException, InvalidEventException {
    switch (copyType) {
      case SINGLE_EVENT:
        return executeSingleEventCopy(args);
      case DAY_EVENTS:
        return executeDayEventsCopy(args);
      case RANGE_EVENTS:
        return executeRangeEventsCopy(args);
      default:
        throw new InvalidEventException("Unknown copy strategy type");
    }
  }

  @Override
  public boolean canHandle(String[] args) {
    if (args.length < 2) {
      return false;
    }

    if (!args[0].equals("copy")) {
      return false;
    }

    switch (copyType) {
      case SINGLE_EVENT:
        return args[1].equals("event");
      case DAY_EVENTS:
        return args[1].equals("events") && args.length > 2 && args[2].equals("on");
      case RANGE_EVENTS:
        return args[1].equals("events") && args.length > 2 && args[2].equals("between");
      default:
        return false;
    }
  }

  /**
   * Execute a single event copy operation.
   *
   * @param args the command arguments
   * @return result message of the copy operation
   * @throws CalendarNotFoundException if the target calendar is not found
   * @throws EventNotFoundException    if the source event is not found
   * @throws ConflictingEventException if there's a conflict in the target calendar
   * @throws InvalidEventException     if the event parameters are invalid
   */
  private String executeSingleEventCopy(String[] args) throws CalendarNotFoundException,
          EventNotFoundException, ConflictingEventException, InvalidEventException {
    if (args.length < 7) {
      throw new InvalidEventException("Insufficient arguments for copy event command");
    }

    if (!args[0].equals("copy") || !args[1].equals("event")) {
      throw new InvalidEventException("Invalid command format");
    }

    String eventName = args[2];
    if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
      eventName = eventName.substring(1, eventName.length() - 1);
    }

    if (!args[3].equals("on")) {
      throw new InvalidEventException("Expected 'on' keyword after event name");
    }

    String sourceDateTime = args[4];

    if (!args[5].equals("--target")) {
      throw new InvalidEventException("Expected '--target' flag");
    }

    String targetCalendar = args[6];

    try {
      return copySingleEvent(eventName, sourceDateTime, targetCalendar, null);
    } catch (Exception e) {
      throw new InvalidEventException("Error copying events: " + e.getMessage());
    }
  }

  /**
   * Execute a day events copy operation.
   *
   * @param args the command arguments
   * @return result message of the copy operation
   * @throws CalendarNotFoundException if the target calendar is not found
   * @throws InvalidEventException     if the event parameters are invalid
   */
  private String executeDayEventsCopy(String[] args)
          throws CalendarNotFoundException, InvalidEventException {
    // Validate format: copy events on <dateString> --target <calendarName> to <dateString>
    if (args.length < 8) {
      throw new InvalidEventException("Insufficient arguments for copy events on date command");
    }

    if (!args[0].equals("copy") || !args[1].equals("events") || !args[2].equals("on")) {
      throw new InvalidEventException("Invalid command format");
    }

    String sourceDate = args[3];

    if (!args[4].equals("--target")) {
      throw new InvalidEventException("Expected '--target' flag");
    }

    String targetCalendar = args[5];

    if (!args[6].equals("to")) {
      throw new InvalidEventException("Expected 'to' keyword");
    }

    String targetDate = args[7];

    try {
      return copyDayEvents(sourceDate, targetCalendar, targetDate);
    } catch (Exception e) {
      throw new InvalidEventException("Error copying events: " + e.getMessage());
    }
  }

  /**
   * Execute a range events copy operation.
   *
   * @param args the command arguments
   * @return result message of the copy operation
   * @throws CalendarNotFoundException if the target calendar is not found
   * @throws InvalidEventException     if the event parameters are invalid
   */
  private String executeRangeEventsCopy(String[] args)
          throws CalendarNotFoundException, InvalidEventException {
    if (args.length < 10) {
      throw new InvalidEventException(
              "Insufficient arguments for copy events between dates command");
    }

    if (!args[0].equals("copy") || !args[1].equals("events") || !args[2].equals("between")) {
      throw new InvalidEventException("Invalid command format");
    }

    String sourceStartDate = args[3];

    if (!args[4].equals("and")) {
      throw new InvalidEventException("Expected 'and' keyword");
    }

    String sourceEndDate = args[5];

    if (!args[6].equals("--target")) {
      throw new InvalidEventException("Expected '--target' flag");
    }

    String targetCalendar = args[7];

    if (!args[8].equals("to")) {
      throw new InvalidEventException("Expected 'to' keyword");
    }

    String targetStartDate = args[9];

    try {
      return copyRangeEvents(sourceStartDate, sourceEndDate, targetCalendar, targetStartDate);
    } catch (Exception e) {
      throw new InvalidEventException("Error copying events: " + e.getMessage());
    }
  }

  /**
   * Copies a single event from the active calendar to a target calendar.
   */
  private String copySingleEvent(String eventName, String dateTimeStr, String targetCalendarName,
                                 String targetDateTimeStr) throws Exception {
    LocalDateTime sourceDateTime = DateTimeUtil.parseDateTime(dateTimeStr);

    if (!calendarManager.hasCalendar(targetCalendarName)) {
      throw new CalendarNotFoundException(
              "Target calendar '" + targetCalendarName + "' does not exist");
    }

    ICalendar sourceCalendar = calendarManager.getActiveCalendar();

    Event sourceEvent = sourceCalendar.findEvent(eventName, sourceDateTime);
    if (sourceEvent == null) {
      throw new EventNotFoundException("Event not found: " + eventName + " at " + sourceDateTime);
    }

    ICalendar targetCalendar = calendarManager.getCalendar(targetCalendarName);
    String targetTimezone = ((Calendar) targetCalendar).getTimeZone().getID();

    LocalDateTime startInTargetTz = timezoneHandler.convertFromUTC(sourceEvent.getStartDateTime(), targetTimezone);
    LocalDateTime endInTargetTz = timezoneHandler.convertFromUTC(sourceEvent.getEndDateTime(), targetTimezone);

    Event newEvent = new Event(
            sourceEvent.getSubject(),
            startInTargetTz,
            endInTargetTz,
            sourceEvent.getDescription(),
            sourceEvent.getLocation(),
            sourceEvent.isPublic()
    );

    // Add the event to the target calendar
    calendarManager.executeOnCalendar(targetCalendarName, calendar -> {
      try {
        calendar.addEvent(newEvent, false);
        return true;
      } catch (ConflictingEventException e) {
        return false;
      }
    });

    return "Event '" + eventName + "' copied successfully to calendar '" + targetCalendarName + "'.";
  }

  /**
   * Copies all events on a specific date from the active calendar to a target calendar.
   */
  private String copyDayEvents(String dateStr, String targetCalendarName, String targetDateStr)
          throws Exception {
    LocalDate sourceDate = DateTimeUtil.parseDate(dateStr);

    if (!calendarManager.hasCalendar(targetCalendarName)) {
      throw new CalendarNotFoundException(
              "Target calendar '" + targetCalendarName + "' does not exist");
    }

    ICalendar sourceCalendar = calendarManager.getActiveCalendar();
    List<Event> eventsToCopy = sourceCalendar.getEventsOnDate(sourceDate);

    if (eventsToCopy.isEmpty()) {
      return "No events found on " + sourceDate + " to copy.";
    }

    String targetTimezone = ((Calendar) calendarManager.getCalendar(targetCalendarName)).getTimeZone().getID();

    int successCount = 0;
    for (Event sourceEvent : eventsToCopy) {
      LocalDateTime sourceEventUTC = sourceEvent.getStartDateTime();
      long durationMinutes = Duration.between(
              sourceEvent.getStartDateTime(),
              sourceEvent.getEndDateTime()
      ).toMinutes();

      LocalDateTime targetDateTime = timezoneHandler.convertFromUTC(sourceEventUTC, targetTimezone);

      Event newEvent = new Event(
              sourceEvent.getSubject(),
              targetDateTime,
              targetDateTime.plusMinutes(durationMinutes),
              sourceEvent.getDescription(),
              sourceEvent.getLocation(),
              sourceEvent.isPublic()
      );

      try {
        calendarManager.executeOnCalendar(targetCalendarName, calendar -> {
          try {
            calendar.addEvent(newEvent, false);
            return true;
          } catch (ConflictingEventException e) {
            return false;
          }
        });
        successCount++;
      } catch (Exception e) {
        continue;
      }
    }

    if (successCount == 0) {
      return "Failed to copy any events to calendar '" + targetCalendarName + "'.";
    } else if (successCount < eventsToCopy.size()) {
      return "Copied " + successCount + " out of " + eventsToCopy.size() + " events to calendar '"
              + targetCalendarName + "'.";
    } else {
      return "Successfully copied all " + successCount + " events to calendar '" + targetCalendarName
              + "'.";
    }
  }

  /**
   * Copies events within a date range from the active calendar to a target calendar.
   */
  private String copyRangeEvents(String startDateStr, String endDateStr, String targetCalendarName,
                                 String targetStartDateStr) throws Exception {
    LocalDate sourceStartDate = DateTimeUtil.parseDate(startDateStr);
    LocalDate sourceEndDate = DateTimeUtil.parseDate(endDateStr);

    if (!calendarManager.hasCalendar(targetCalendarName)) {
      throw new CalendarNotFoundException(
              "Target calendar '" + targetCalendarName + "' does not exist");
    }

    ICalendar sourceCalendar = calendarManager.getActiveCalendar();
    List<Event> eventsToCopy = sourceCalendar.getEventsInRange(sourceStartDate, sourceEndDate);

    if (eventsToCopy.isEmpty()) {
      return "No events found between " + sourceStartDate + " and " + sourceEndDate + " to copy.";
    }

    String targetTimezone = ((Calendar) calendarManager.getCalendar(targetCalendarName)).getTimeZone().getID();

    int successCount = 0;
    for (Event sourceEvent : eventsToCopy) {
      LocalDateTime sourceEventUTC = sourceEvent.getStartDateTime();
      long durationMinutes = Duration.between(
              sourceEvent.getStartDateTime(),
              sourceEvent.getEndDateTime()
      ).toMinutes();

      LocalDateTime targetDateTime = timezoneHandler.convertFromUTC(sourceEventUTC, targetTimezone);

      Event newEvent = new Event(
              sourceEvent.getSubject(),
              targetDateTime,
              targetDateTime.plusMinutes(durationMinutes),
              sourceEvent.getDescription(),
              sourceEvent.getLocation(),
              sourceEvent.isPublic()
      );

      try {
        calendarManager.executeOnCalendar(targetCalendarName, calendar -> {
          try {
            calendar.addEvent(newEvent, false);
            return true;
          } catch (ConflictingEventException e) {
            return false;
          }
        });
        successCount++;
      } catch (Exception e) {
        continue;
      }
    }

    if (successCount == 0) {
      return "Failed to copy any events to calendar '" + targetCalendarName + "'.";
    } else if (successCount < eventsToCopy.size()) {
      return "Copied " + successCount + " out of " + eventsToCopy.size() + " events to calendar '"
              + targetCalendarName + "'.";
    } else {
      return "Successfully copied all " + successCount + " events to calendar '" + targetCalendarName
              + "'.";
    }
  }
}
