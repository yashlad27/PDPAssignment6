package controller.command.copy.strategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;
import utilities.TimeZoneHandler;

/**
 * Strategy for copying events within a date range from one calendar to another. Format: copy events
 * between {@code dateString} and {@code dateString} --target {@code calendarName} to
 * {@code dateString}
 */
public class RangeEventsCopyStrategy implements CopyStrategy {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;

  /**
   * Constructs a new RangeEventsCopyStrategy.
   *
   * @param calendarManager the calendar manager
   * @param timezoneHandler the timezone handler
   */
  public RangeEventsCopyStrategy(CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
    this.calendarManager = calendarManager;
    this.timezoneHandler = timezoneHandler;
  }

  @Override
  public String execute(String[] args) throws CalendarNotFoundException, InvalidEventException {
    if (args.length < 10) {
      throw new InvalidEventException(
              "Insufficient arguments for copy events between " + "dates command");
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
      return copyEvents(sourceStartDate, sourceEndDate, targetCalendar, targetStartDate);
    } catch (Exception e) {
      throw new InvalidEventException("Error copying events: " + e.getMessage());
    }
  }

  @Override
  public boolean canHandle(String[] args) {
    if (args.length < 3) {
      return false;
    }

    return (args[0].equals("copy") && args[1].equals("events") && args[2].equals("between"));
  }

  /**
   * Copies events within a date range from the active calendar to a target calendar.
   */
  private String copyEvents(String startDateStr, String endDateStr, String targetCalendarName,
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

    String sourceTimezone = ((Calendar) sourceCalendar).getTimeZone().getID();
    String targetTimezone = ((Calendar) calendarManager.getCalendar(targetCalendarName)).getTimeZone().getID();

    int successCount = 0;
    for (Event sourceEvent : eventsToCopy) {
      // Get the source event's UTC time (it's already in UTC in storage)
      LocalDateTime sourceEventUTC = sourceEvent.getStartDateTime();
      long durationMinutes = java.time.Duration.between(
              sourceEvent.getStartDateTime(),
              sourceEvent.getEndDateTime()
      ).toMinutes();

      // Convert UTC time to target timezone
      LocalDateTime targetDateTime = timezoneHandler.convertFromUTC(sourceEventUTC, targetTimezone);

      // Create new event with target time and duration
      Event newEvent = new Event(
              sourceEvent.getSubject(),
              targetDateTime,
              targetDateTime.plusMinutes(durationMinutes),
              sourceEvent.getDescription(),
              sourceEvent.getLocation(),
              sourceEvent.isPublic()
      );

      // Add the event to the target calendar
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
        // Continue with next event if one fails
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