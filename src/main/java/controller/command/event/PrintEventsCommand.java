package controller.command.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import controller.command.ICommand;
import model.calendar.ICalendar;
import model.event.Event;
import model.export.CSVExporter;
import utilities.DateTimeUtil;

/**
 * Command for printing events on a specific date or within a date range.
 */
public class PrintEventsCommand implements ICommand {

  private final ICalendar calendar;
  private final CSVExporter csvExporter;

  /**
   * Creates a PrintEventsCommand with the given calendar.
   *
   * @param calendar the calendar to query
   */
  public PrintEventsCommand(ICalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
    this.csvExporter = new CSVExporter();
  }

  /**
   * Executes the command to print events based on arguments provided.
   *
   * @param args the command arguments.
   * @return a string containing the list of events or error message.
   */
  @Override
  public String execute(String[] args) {
    if (args == null) {
      throw new IllegalArgumentException("Arguments array cannot be null");
    }
    if (args.length < 2) {
      return "Error: Insufficient arguments for print command";
    }

    String type = args[0];

    if (type.equals("on_date")) {
      if (args.length < 2) {
        return "Error: Missing date for 'print events on' command";
      }

      LocalDate date;
      try {
        date = DateTimeUtil.parseDate(args[1]);
      } catch (Exception e) {
        return "Error parsing date: " + e.getMessage();
      }

      return printEventsOnDate(date);

    } else if (type.equals("date_range") || type.equals("from_range")) {
      if (args.length < 3) {
        return "Error: Missing dates for 'print events from...to' command";
      }

      try {
        String startStr = args[1];
        String endStr = args[2];
        
        // Check if the inputs contain time components
        boolean hasTimeComponent = startStr.contains("T") || endStr.contains("T");
        
        if (hasTimeComponent) {
          // Handle datetime range
          LocalDateTime startDateTime = startStr.contains("T") ? 
                  DateTimeUtil.parseDateTime(startStr) :
                  DateTimeUtil.parseDate(startStr).atStartOfDay();
                  
          LocalDateTime endDateTime = endStr.contains("T") ? 
                  DateTimeUtil.parseDateTime(endStr) :
                  DateTimeUtil.parseDate(endStr).atTime(23, 59, 59);
          
          return printEventsInDateTimeRange(startDateTime, endDateTime);
        } else {
          // Handle date-only range (existing functionality)
          LocalDate startDate = DateTimeUtil.parseDate(args[1]);
          LocalDate endDate = DateTimeUtil.parseDate(args[2]);
          return printEventsInRange(startDate, endDate);
        }
      } catch (Exception e) {
        return "Error parsing dates: " + e.getMessage();
      }
    } else {
      return "Unknown print command type: " + type;
    }
  }

  private String printEventsOnDate(LocalDate date) {
    // Get events directly from the calendar
    List<Event> eventsOnDate = calendar.getEventsOnDate(date);

    // Format the date in a more human-readable format
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Check for events that occur just after midnight (early hours of next day)
    LocalDate nextDay = date.plusDays(1);
    List<Event> earlyMorningEvents = calendar.getEventsOnDate(nextDay)
            .stream()
            .filter(e -> {
                LocalDateTime start = e.getStartDateTime();
                return start != null && 
                       start.toLocalDate().equals(nextDay) && 
                       start.getHour() < 6;  // Events in early morning hours
            })
            .collect(Collectors.toList());
    
    // Create a new mutable list and add all events to it
    List<Event> allEvents = new ArrayList<>(eventsOnDate);
    // Add early morning events that belong logically to the current day
    allEvents.addAll(earlyMorningEvents);
    
    if (allEvents.isEmpty()) {
      return "No events on " + date.format(dateFormatter);
    }

    // Get calendar's timezone for display
    TimeZone calendarTimeZone = calendar.getTimeZone();
    String timeZoneId = calendarTimeZone.getID();

    StringBuilder result = new StringBuilder();
    result.append("Events on ").append(date.format(dateFormatter)).append(":\n");
    result.append(csvExporter.formatForDisplay(allEvents, true, timeZoneId));

    return result.toString();
  }

  private String printEventsInRange(LocalDate startDate, LocalDate endDate) {
    // Get events directly from the calendar for the requested range
    List<Event> eventsInRange = calendar.getEventsInRange(startDate, endDate);
    
    // Format the dates in a more human-readable format
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    if (eventsInRange.isEmpty()) {
      return "No events from " + startDate.format(dateFormatter) + " to "
              + endDate.format(dateFormatter);
    }

    // Get calendar's timezone for display
    TimeZone calendarTimeZone = calendar.getTimeZone();
    String timeZoneId = calendarTimeZone.getID();

    StringBuilder result = new StringBuilder();
    result.append("Events from ").append(startDate.format(dateFormatter))
            .append(" to ").append(endDate.format(dateFormatter)).append(":\n");
    result.append(csvExporter.formatForDisplay(eventsInRange, true, timeZoneId));

    return result.toString();
  }

  private String printEventsInDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    // Get events by date range first, then filter by time
    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate endDate = endDateTime.toLocalDate();
    
    List<Event> allEvents = calendar.getEventsInRange(startDate, endDate);
    
    // Filter the events to only include those within the specific time range
    List<Event> eventsInTimeRange = allEvents.stream()
        .filter(event -> {
            LocalDateTime eventStart = event.getStartDateTime();
            LocalDateTime eventEnd = event.getEndDateTime();
            
            // Check if event overlaps with the time range
            return !(eventEnd.isBefore(startDateTime) || eventStart.isAfter(endDateTime));
        })
        .collect(Collectors.toList());
    
    // Format the times in a more human-readable format
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    if (eventsInTimeRange.isEmpty()) {
      return "No events from " + startDateTime.format(formatter) + " to "
              + endDateTime.format(formatter);
    }

    // Get calendar's timezone for display
    TimeZone calendarTimeZone = calendar.getTimeZone();
    String timeZoneId = calendarTimeZone.getID();

    StringBuilder result = new StringBuilder();
    result.append("Events from ").append(startDateTime.format(formatter))
            .append(" to ").append(endDateTime.format(formatter)).append(":\n");
    result.append(csvExporter.formatForDisplay(eventsInTimeRange, true, timeZoneId));

    return result.toString();
  }

  /**
   * fetches the name of the command.
   *
   * @return the name of command as String.
   */
  @Override
  public String getName() {
    return "print";
  }
}