package controller.command.create.strategy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;

/**
 * Consolidated class for all event creation strategies.
 * Replaces multiple separate creator classes with a single class that handles all event types.
 */
public class ConsolidatedEventCreator implements EventCreator {

  // Common fields
  private final String eventName;
  private final String description;
  private final String location;
  private final boolean isPublic;
  private final boolean autoDecline;

  // Event type
  private final EventType eventType;

  // Fields for single events
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;

  // Fields for all-day events
  private LocalDate date;

  // Fields for recurring events
  private Set<DayOfWeek> repeatDays;
  private int occurrences;
  private LocalDate untilDate;

  /**
   * Enum representing the different types of events that can be created.
   */
  public enum EventType {
    SINGLE,
    ALL_DAY,
    RECURRING,
    RECURRING_UNTIL,
    ALL_DAY_RECURRING,
    ALL_DAY_RECURRING_UNTIL
  }

  /**
   * Private constructor used by factory methods.
   */
  private ConsolidatedEventCreator(String eventName, String description, String location,
                                   boolean isPublic, boolean autoDecline, EventType eventType) {
    this.eventName = eventName;
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
    this.autoDecline = autoDecline;
    this.eventType = eventType;
  }

  /**
   * Factory method for creating a single event creator.
   */
  public static ConsolidatedEventCreator createSingleEvent(String[] args) {
    if (args == null || args.length < 4) {
      throw new IllegalArgumentException("Insufficient arguments for creating a single event");
    }

    try {
      String eventName = args[1];
      LocalDateTime startDateTime = DateTimeUtil.parseDateTime(args[2]);
      LocalDateTime endDateTime = DateTimeUtil.parseDateTime(args[3]);

      String description = args.length > 4 ? removeQuotes(args[4]) : null;
      String location = args.length > 5 ? removeQuotes(args[5]) : null;
      boolean isPublic = args.length > 6 ? Boolean.parseBoolean(args[6]) : true;
      boolean autoDecline = args.length > 7 ? Boolean.parseBoolean(args[7]) : false;

      ConsolidatedEventCreator creator = new ConsolidatedEventCreator(
              eventName, description, location, isPublic, autoDecline, EventType.SINGLE);
      creator.startDateTime = startDateTime;
      creator.endDateTime = endDateTime;

      return creator;
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing arguments: " + e.getMessage(), e);
    }
  }

  /**
   * Factory method for creating an all-day event creator.
   */
  public static ConsolidatedEventCreator createAllDayEvent(String[] args) {
    if (args == null || args.length < 3) {
      throw new IllegalArgumentException("Insufficient arguments for creating an all-day event");
    }

    try {
      String eventName = args[1];
      LocalDate date = DateTimeUtil.parseDate(args[2]);

      String description = args.length > 3 ? removeQuotes(args[3]) : null;
      String location = args.length > 4 ? removeQuotes(args[4]) : null;
      boolean isPublic = args.length > 5 ? Boolean.parseBoolean(args[5]) : true;
      boolean autoDecline = args.length > 6 ? Boolean.parseBoolean(args[6]) : false;

      ConsolidatedEventCreator creator = new ConsolidatedEventCreator(
              eventName, description, location, isPublic, autoDecline, EventType.ALL_DAY);
      creator.date = date;

      return creator;
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing arguments: " + e.getMessage(), e);
    }
  }

  /**
   * Factory method for creating a recurring event creator.
   */
  public static ConsolidatedEventCreator createRecurringEvent(String[] args) {
    if (args == null || args.length < 6) {
      throw new IllegalArgumentException("Insufficient arguments for creating a recurring event");
    }

    try {
      String eventName = args[1];
      LocalDateTime startDateTime = DateTimeUtil.parseDateTime(args[2]);
      LocalDateTime endDateTime = DateTimeUtil.parseDateTime(args[3]);
      String weekdays = args[4];
      int occurrences = Integer.parseInt(args[5]);

      String description = args.length > 6 ? removeQuotes(args[6]) : null;
      String location = args.length > 7 ? removeQuotes(args[7]) : null;
      boolean isPublic = args.length > 8 ? Boolean.parseBoolean(args[8]) : true;
      boolean autoDecline = args.length > 9 ? Boolean.parseBoolean(args[9]) : false;

      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      ConsolidatedEventCreator creator = new ConsolidatedEventCreator(
              eventName, description, location, isPublic, autoDecline, EventType.RECURRING);
      creator.startDateTime = startDateTime;
      creator.endDateTime = endDateTime;
      creator.repeatDays = repeatDays;
      creator.occurrences = occurrences;

      return creator;
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing arguments: " + e.getMessage(), e);
    }
  }

  /**
   * Factory method for creating a recurring event with end date creator.
   */
  public static ConsolidatedEventCreator createRecurringUntilEvent(String[] args) {
    if (args == null || args.length < 6) {
      throw new IllegalArgumentException("Insufficient arguments for creating a recurring event with end date");
    }

    try {
      String eventName = args[1];
      LocalDateTime startDateTime = DateTimeUtil.parseDateTime(args[2]);
      LocalDateTime endDateTime = DateTimeUtil.parseDateTime(args[3]);
      String weekdays = args[4];
      LocalDate untilDate = DateTimeUtil.parseDate(args[5]);

      String description = args.length > 6 ? removeQuotes(args[6]) : null;
      String location = args.length > 7 ? removeQuotes(args[7]) : null;
      boolean isPublic = args.length > 8 ? Boolean.parseBoolean(args[8]) : true;
      boolean autoDecline = args.length > 9 ? Boolean.parseBoolean(args[9]) : false;

      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      ConsolidatedEventCreator creator = new ConsolidatedEventCreator(
              eventName, description, location, isPublic, autoDecline, EventType.RECURRING_UNTIL);
      creator.startDateTime = startDateTime;
      creator.endDateTime = endDateTime;
      creator.repeatDays = repeatDays;
      creator.untilDate = untilDate;

      return creator;
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing arguments: " + e.getMessage(), e);
    }
  }

  /**
   * Factory method for creating an all-day recurring event creator.
   */
  public static ConsolidatedEventCreator createAllDayRecurringEvent(String[] args) {
    if (args == null || args.length < 5) {
      throw new IllegalArgumentException("Insufficient arguments for creating an all-day recurring event");
    }

    try {
      String eventName = args[1];
      LocalDate date = DateTimeUtil.parseDate(args[2]);
      String weekdays = args[3];
      int occurrences = Integer.parseInt(args[4]);

      String description = args.length > 5 ? removeQuotes(args[5]) : null;
      String location = args.length > 6 ? removeQuotes(args[6]) : null;
      boolean isPublic = args.length > 7 ? Boolean.parseBoolean(args[7]) : true;
      boolean autoDecline = args.length > 8 ? Boolean.parseBoolean(args[8]) : false;

      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      ConsolidatedEventCreator creator = new ConsolidatedEventCreator(
              eventName, description, location, isPublic, autoDecline, EventType.ALL_DAY_RECURRING);
      creator.date = date;
      creator.repeatDays = repeatDays;
      creator.occurrences = occurrences;

      return creator;
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing arguments: " + e.getMessage(), e);
    }
  }

  /**
   * Factory method for creating an all-day recurring event with end date creator.
   */
  public static ConsolidatedEventCreator createAllDayRecurringUntilEvent(String[] args) {
    if (args == null || args.length < 5) {
      throw new IllegalArgumentException("Insufficient arguments for creating an all-day recurring event with end date");
    }

    try {
      String eventName = args[1];
      LocalDate date = DateTimeUtil.parseDate(args[2]);
      String weekdays = args[3];
      LocalDate untilDate = DateTimeUtil.parseDate(args[4]);

      String description = args.length > 5 ? removeQuotes(args[5]) : null;
      String location = args.length > 6 ? removeQuotes(args[6]) : null;
      boolean isPublic = args.length > 7 ? Boolean.parseBoolean(args[7]) : true;
      boolean autoDecline = args.length > 8 ? Boolean.parseBoolean(args[8]) : false;

      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      ConsolidatedEventCreator creator = new ConsolidatedEventCreator(
              eventName, description, location, isPublic, autoDecline, EventType.ALL_DAY_RECURRING_UNTIL);
      creator.date = date;
      creator.repeatDays = repeatDays;
      creator.untilDate = untilDate;

      return creator;
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing arguments: " + e.getMessage(), e);
    }
  }

  /**
   * Validates event parameters.
   *
   * @throws InvalidEventException if required parameters are invalid
   */
  protected void validateEventParameters() throws InvalidEventException {
    if (eventName == null || eventName.trim().isEmpty()) {
      throw new InvalidEventException("Event name cannot be empty");
    }

    switch (eventType) {
      case SINGLE:
        if (startDateTime == null || endDateTime == null) {
          throw new InvalidEventException("Start and end date/time are required for single events");
        }
        if (startDateTime.isAfter(endDateTime)) {
          throw new InvalidEventException("Start date/time must be before end date/time");
        }
        break;

      case ALL_DAY:
        if (date == null) {
          throw new InvalidEventException("Date is required for all-day events");
        }
        break;

      case RECURRING:
      case RECURRING_UNTIL:
        if (startDateTime == null || endDateTime == null || repeatDays == null) {
          throw new InvalidEventException("Start date/time, end date/time, and repeat days are required for recurring events");
        }
        if (startDateTime.isAfter(endDateTime)) {
          throw new InvalidEventException("Start date/time must be before end date/time");
        }
        if (eventType == EventType.RECURRING && occurrences <= 0) {
          throw new InvalidEventException("Number of occurrences must be positive");
        }
        if (eventType == EventType.RECURRING_UNTIL && untilDate == null) {
          throw new InvalidEventException("Until date is required for recurring events with end date");
        }
        break;

      case ALL_DAY_RECURRING:
      case ALL_DAY_RECURRING_UNTIL:
        if (date == null || repeatDays == null) {
          throw new InvalidEventException("Date and repeat days are required for all-day recurring events");
        }
        if (eventType == EventType.ALL_DAY_RECURRING && occurrences <= 0) {
          throw new InvalidEventException("Number of occurrences must be positive");
        }
        if (eventType == EventType.ALL_DAY_RECURRING_UNTIL && untilDate == null) {
          throw new InvalidEventException("Until date is required for all-day recurring events with end date");
        }
        break;
    }
  }

  @Override
  public Event createEvent() throws InvalidEventException {
    validateEventParameters();

    switch (eventType) {
      case SINGLE:
        return new Event(eventName, startDateTime, endDateTime, description, location, isPublic);

      default:
        // For other event types, we'll create the event during executeCreation
        return null;
    }
  }

  @Override
  public String executeCreation(ICalendar calendar) throws ConflictingEventException, InvalidEventException {
    validateEventParameters();

    boolean success = false;

    try {
      switch (eventType) {
        case SINGLE:
          Event event = createEvent();
          success = calendar.addEvent(event, autoDecline);
          return success ? "Event created successfully" : "Failed to create event";

        case ALL_DAY:
          // Create an all-day event (spans the entire day)
          LocalDateTime startOfDay = date.atStartOfDay();
          LocalDateTime endOfDay = date.atTime(23, 59, 59);
          Event allDayEvent = new Event(eventName, startOfDay, endOfDay, description, location, isPublic);
          success = calendar.addEvent(allDayEvent, autoDecline);
          return success ? "All-day event created successfully" : "Failed to create all-day event";

        case RECURRING:
          // Create a recurring event with occurrences
          RecurringEvent recurringEvent = new RecurringEvent.Builder(eventName, startDateTime, endDateTime, repeatDays)
                  .description(description)
                  .location(location)
                  .isPublic(isPublic)
                  .occurrences(occurrences)
                  .build();
          success = calendar.addRecurringEvent(recurringEvent, autoDecline);
          return success ? "Recurring event created successfully" : "Failed to create recurring event";

        case RECURRING_UNTIL:
          // Create a recurring event with end date
          RecurringEvent recurringUntilEvent = new RecurringEvent.Builder(eventName, startDateTime, endDateTime, repeatDays)
                  .description(description)
                  .location(location)
                  .isPublic(isPublic)
                  .endDate(untilDate)
                  .build();
          success = calendar.addRecurringEvent(recurringUntilEvent, autoDecline);
          return success ? "Recurring event created successfully" : "Failed to create recurring event";

        case ALL_DAY_RECURRING:
          // Create an all-day recurring event with occurrences
          LocalDateTime allDayStart = date.atStartOfDay();
          LocalDateTime allDayEnd = date.atTime(23, 59, 59);
          RecurringEvent allDayRecurringEvent = new RecurringEvent.Builder(eventName, allDayStart, allDayEnd, repeatDays)
                  .description(description)
                  .location(location)
                  .isPublic(isPublic)
                  .occurrences(occurrences)
                  .isAllDay(true)
                  .build();
          success = calendar.addRecurringEvent(allDayRecurringEvent, autoDecline);
          return success ? "All-day recurring event created successfully" : "Failed to create all-day recurring event";

        case ALL_DAY_RECURRING_UNTIL:
          // Create an all-day recurring event with end date
          LocalDateTime allDayUntilStart = date.atStartOfDay();
          LocalDateTime allDayUntilEnd = date.atTime(23, 59, 59);
          RecurringEvent allDayRecurringUntilEvent = new RecurringEvent.Builder(eventName, allDayUntilStart, allDayUntilEnd, repeatDays)
                  .description(description)
                  .location(location)
                  .isPublic(isPublic)
                  .endDate(untilDate)
                  .isAllDay(true)
                  .build();
          success = calendar.addRecurringEvent(allDayRecurringUntilEvent, autoDecline);
          return success ? "All-day recurring event created successfully" : "Failed to create all-day recurring event";

        default:
          throw new InvalidEventException("Unknown event type: " + eventType);
      }
    } catch (ConflictingEventException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidEventException("Error creating event: " + e.getMessage());
    }
  }

  /**
   * Removes surrounding quotes from a string value if present.
   *
   * @param value the string value to process
   * @return the string without surrounding quotes, or the original string if no quotes
   */
  private static String removeQuotes(String value) {
    if (value != null && value.length() >= 2) {
      if ((value.startsWith("\"") && value.endsWith("\"")) ||
              (value.startsWith("'") && value.endsWith("'"))) {
        return value.substring(1, value.length() - 1);
      }
    }
    return value;
  }
}
