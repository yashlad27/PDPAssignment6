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
import utilities.TimezoneConverter;

/**
 * Strategy for copying all events on a specific date from one calendar to another. Format: copy
 * events on {@code dateString} --target {@code calendarName} to {@code dateString}
 */
public class DayEventsCopyStrategy implements CopyStrategy {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;

  /**
   * Constructs a new DayEventsCopyStrategy.
   *
   * @param calendarManager the calendar manager
   * @param timezoneHandler the timezone handler
   */
  public DayEventsCopyStrategy(CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
    this.calendarManager = calendarManager;
    this.timezoneHandler = timezoneHandler;
  }

  @Override
  public String execute(String[] args) throws CalendarNotFoundException, InvalidEventException {
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
      return copyEvents(sourceDate, targetCalendar, targetDate);
    } catch (Exception e) {
      throw new InvalidEventException("Error copying events: " + e.getMessage());
    }
  }

  @Override
  public boolean canHandle(String[] args) {
    if (args.length < 3) {
      return false;
    }

    return (args[0].equals("copy") && args[1].equals("events") && args[2].equals("on"));
  }

  /**
   * Copies all events on a specific date from the active calendar to a target calendar.
   */
  private String copyEvents(String dateStr, String targetCalendarName, String targetDateStr)
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

    String sourceTimezone = ((model.calendar.Calendar) sourceCalendar).getTimezone();
    String targetTimezone = calendarManager.executeOnCalendar(targetCalendarName,
        calendar -> ((model.calendar.Calendar) calendar).getTimezone());

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