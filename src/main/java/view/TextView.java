package view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;

import controller.CalendarController;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;

/**
 * Text-based implementation of the calendar view.
 * This class provides a text-based user interface for the calendar application,
 * supporting both interactive (console) and headless (file) modes.
 */
public class TextView implements ICalendarView {
  private BufferedReader reader;
  private boolean isInteractive;
  private String currentCalendar;
  private LocalDate selectedDate;

  /**
   * Creates a new TextView.
   *
   * @param controller the controller to use
   */
  public TextView(CalendarController controller) {
    this.reader = new BufferedReader(new InputStreamReader(System.in));
    this.isInteractive = true;
    this.selectedDate = LocalDate.now();
  }

  /**
   * Sets up the view for headless mode.
   *
   * @param filename the file to read commands from
   * @return true if setup was successful, false otherwise
   */
  public boolean setupHeadlessMode(String filename) {
    try {
      this.reader = new BufferedReader(new FileReader(filename));
      this.isInteractive = false;
      return true;
    } catch (IOException e) {
      System.err.println("Error opening file: " + e.getMessage());
      return false;
    }
  }

  @Override
  public String readCommand() {
    try {
      if (isInteractive) {
        System.out.print("Enter command: ");
      }
      String line = reader.readLine();
      if (line == null) {
        return "exit";  // End of file
      }
      return line;
    } catch (IOException e) {
      System.err.println("Error reading input: " + e.getMessage());
      return "exit";
    }
  }

  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  @Override
  public void displayError(String errorMessage) {
    System.err.println("ERROR: " + errorMessage);
  }

  @Override
  public void updateCalendarView(ICalendar calendar) {
    if (calendar == null) {
      displayMessage("No calendar selected.");
      return;
    }

    displayMessage("Calendar: " + currentCalendar);
    displayMessage("Date: " + selectedDate);

    List<Event> events = calendar.getEventsOnDate(selectedDate);
    displayMessage("Events for " + selectedDate + ":");

    if (events.isEmpty()) {
      displayMessage("  No events for this date.");
    } else {
      for (Event event : events) {
        displayMessage("  - " + formatEvent(event));
      }
    }
  }

  private String formatEvent(Event event) {
    return event.getSubject() + " ("
            + event.getStartDateTime().toLocalTime() + " - "
            + event.getEndDateTime().toLocalTime() + ")";
  }

  @Override
  public void updateEventList(List<Event> events) {
    displayMessage("Events:");
    if (events.isEmpty()) {
      displayMessage("  No events.");
    } else {
      for (Event event : events) {
        displayMessage("  - " + formatEvent(event));
      }
    }
  }

  @Override
  public void updateRecurringEventList(List<RecurringEvent> recurringEvents) {
    displayMessage("Recurring Events:");
    if (recurringEvents.isEmpty()) {
      displayMessage("  No recurring events.");
    } else {
      for (RecurringEvent event : recurringEvents) {
        displayMessage("  - " + event.getSubject() + " (Recurring)");
      }
    }
  }

  @Override
  public void showEventDetails(Event event) {
    if (event == null) {
      displayMessage("No event selected.");
      return;
    }

    displayMessage("Event Details:");
    displayMessage("  Name: " + event.getSubject());
    displayMessage("  Description: " + (event.getDescription() != null
            ? event.getDescription() : "None"));
    displayMessage("  Location: " + (event.getLocation() != null ? event.getLocation() : "None"));
    displayMessage("  Start: " + event.getStartDateTime());
    displayMessage("  End: " + event.getEndDateTime());
    displayMessage("  Public: " + (event.isPublic() ? "Yes" : "No"));
    displayMessage("  ID: " + event.getId());

    if (event instanceof RecurringEvent) {
      RecurringEvent recurringEvent = (RecurringEvent) event;
      displayMessage("  Recurring: Yes");
      displayMessage("  Repeat days: " + recurringEvent.getRepeatDays());
    } else {
      displayMessage("  Recurring: No");
    }
  }

  @Override
  public void clearEventDetails() {
    // No action needed for text view
  }

  @Override
  public void updateCalendarList(List<String> calendarNames) {
    displayMessage("Available Calendars:");
    if (calendarNames.isEmpty()) {
      displayMessage("  No calendars available.");
    } else {
      for (String name : calendarNames) {
        if (name.equals(currentCalendar)) {
          displayMessage("  * " + name + " (selected)");
        } else {
          displayMessage("  - " + name);
        }
      }
    }
  }

  @Override
  public void setSelectedCalendar(String calendarName) {
    this.currentCalendar = calendarName;
    displayMessage("Selected calendar: " + calendarName);
  }

  @Override
  public String getSelectedCalendar() {
    return currentCalendar;
  }

  @Override
  public void setSelectedDate(LocalDate date) {
    this.selectedDate = date;
    displayMessage("Display date set to: " + date);
  }

  @Override
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  @Override
  public void updateSelectedDate(LocalDate date) {
    this.selectedDate = date;
    displayMessage("Display date updated to: " + date);
  }

  @Override
  public void refreshView() {
    displayMessage("View refreshed.");
  }

  /**
   * Closes and cleans up resources used by this view.
   */
  public void close() {
    try {
      reader.close();
    } catch (IOException e) {
      System.err.println("Error closing resources: " + e.getMessage());
    }
  }
}
