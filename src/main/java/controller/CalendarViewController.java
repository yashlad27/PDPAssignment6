package controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import utilities.TimeZoneHandler;
import view.GUIView;

/**
 * Controller focused specifically on calendar visualization operations.
 * Part of refactoring for SOLID principles - Single Responsibility.
 */
public class CalendarViewController {
  private final CalendarManager calendarManager;
  private final GUIView view;
  private final TimeZoneHandler timezoneHandler;
  private ICalendar currentCalendar;

  /**
   * Constructs a new CalendarViewController.
   *
   * @param calendarManager the calendar manager
   * @param view the GUI view
   * @param timezoneHandler the timezone handler
   */
  public CalendarViewController(CalendarManager calendarManager, GUIView view, TimeZoneHandler timezoneHandler) {
    this.calendarManager = calendarManager;
    this.view = view;
    this.timezoneHandler = timezoneHandler;
  }

  /**
   * Sets the current calendar.
   *
   * @param calendar the calendar to set as current
   */
  public void setCurrentCalendar(ICalendar calendar) {
    this.currentCalendar = calendar;
  }

  /**
   * Gets the current calendar.
   *
   * @return the current calendar
   */
  public ICalendar getCurrentCalendar() {
    return currentCalendar;
  }

  /**
   * Handles the selection of a calendar.
   *
   * @param calendar the selected calendar
   */
  public void onCalendarSelected(ICalendar calendar) {
    if (calendar == null) {
      view.showErrorMessage("Invalid calendar selected");
      return;
    }
    
    this.currentCalendar = calendar;
    view.updateCalendarView(calendar);
    
    // Update the view to reflect the newly selected calendar
    LocalDate today = LocalDate.now();
    updateEvents(today);
    updateStatus(today);
  }

  /**
   * Handles the creation of a new calendar.
   *
   * @param name the name of the new calendar
   * @param timezone the timezone for the new calendar
   */
  public void onCalendarCreated(String name, String timezone) {
    try {
      ICalendar newCalendar = calendarManager.createCalendar(name, timezone);
      this.currentCalendar = newCalendar;
      
      // Update the view to reflect the newly created calendar
      view.setSelectedCalendar(name);
      view.updateCalendarView(newCalendar);
      view.updateCalendarList(new ArrayList<>(calendarManager.getCalendarRegistry().getCalendarNames()));
      
      view.displayMessage("Calendar created: " + name);
    } catch (Exception e) {
      view.showErrorMessage("Error creating calendar: " + e.getMessage());
    }
  }

  /**
   * Handles the selection of a date.
   *
   * @param date the selected date
   */
  public void onDateSelected(LocalDate date) {
    if (date == null) {
      return;
    }
    
    if (currentCalendar == null) {
      view.showErrorMessage("Please select a calendar first");
      return;
    }
    
    setSelectedDate(date);
  }

  /**
   * Handles a request for events on a specific date.
   *
   * @param date the date to get events for
   */
  public void onEventsListRequested(LocalDate date) {
    if (date == null) {
      return;
    }
    
    if (currentCalendar == null) {
      view.showErrorMessage("Please select a calendar first");
      return;
    }
    
    listEvents(date);
  }

  /**
   * Handles a request for events in a date range.
   *
   * @param startDate the start date of the range
   * @param endDate the end date of the range
   */
  public void onDateRangeSelected(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      return;
    }
    
    if (currentCalendar == null) {
      view.showErrorMessage("Please select a calendar first");
      return;
    }
    
    showRange(startDate, endDate);
  }

  /**
   * Handles a request for status information on a specific date.
   *
   * @param date the date to check status for
   */
  public void onStatusRequested(LocalDate date) {
    if (date == null) {
      return;
    }
    
    if (currentCalendar == null) {
      view.showErrorMessage("Please select a calendar first");
      return;
    }
    
    updateStatus(date);
  }

  /**
   * Updates the busy/available status for a given date.
   *
   * @param date the date to check
   */
  public void updateStatus(LocalDate date) {
    if (currentCalendar == null || date == null) {
      return;
    }
    
    List<Event> events = currentCalendar.getEventsOnDate(date);
    boolean isBusy = !events.isEmpty();
    
    view.updateStatus(date, isBusy, events.size());
  }

  /**
   * Updates the events for the specified date.
   *
   * @param date the date to update events for
   */
  public void updateEvents(LocalDate date) {
    if (currentCalendar == null || date == null) {
      return;
    }
    
    List<Event> events = currentCalendar.getEventsOnDate(date);
    view.updateEvents(date, events);
  }

  /**
   * Lists events for the specified date.
   *
   * @param date the date to list events for
   */
  public void listEvents(LocalDate date) {
    if (currentCalendar == null || date == null) {
      return;
    }
    
    List<Event> events = currentCalendar.getEventsOnDate(date);
    view.updateEventList(events);
  }

  /**
   * Shows events in the specified date range.
   *
   * @param startDate the start date of the range
   * @param endDate the end date of the range
   */
  public void showRange(LocalDate startDate, LocalDate endDate) {
    if (currentCalendar == null || startDate == null || endDate == null) {
      return;
    }
    
    List<Event> events = currentCalendar.getEventsInRange(startDate, endDate);
    view.updateEventListRange(startDate, endDate, events);
  }

  /**
   * Sets the selected date and updates all relevant views.
   *
   * @param date The date to select
   */
  public void setSelectedDate(LocalDate date) {
    if (date == null) {
      return;
    }
    
    // Update the view
    view.setSelectedDate(date);
    
    // Update events and status for this date
    updateEvents(date);
    updateStatus(date);
    listEvents(date);
  }

  /**
   * Initializes the calendar view.
   */
  public void initializeCalendarView() {
    // Clear any existing calendar names
    utilities.CalendarNameValidator.removeAllCalendarNames();

    // Create a default calendar if none exists
    String defaultCalendar = "Default_Calendar";
    if (calendarManager.getCalendarCount() == 0) {
      String timezone = timezoneHandler.getSystemDefaultTimezone();
      try {
        calendarManager.createCalendar(defaultCalendar, timezone);
      } catch (DuplicateCalendarException e) {
        // This shouldn't happen as we checked there are no calendars
        System.err.println("Unexpected error creating default calendar: " + e.getMessage());
      } catch (InvalidTimezoneException e) {
        // Fall back to UTC if the system timezone is invalid
        try {
          calendarManager.createCalendar(defaultCalendar, "UTC");
        } catch (Exception ex) {
          System.err.println("Failed to create default calendar: " + ex.getMessage());
        }
      }
    }

    // Get the first available calendar
    try {
      currentCalendar = calendarManager.getCalendar(defaultCalendar);
      if (currentCalendar == null) {
        // Handle case where calendar is null but no exception was thrown
        view.showErrorMessage("No calendars available");
        return;
      }
    } catch (CalendarNotFoundException e) {
      view.showErrorMessage("Calendar not found: " + e.getMessage());
      return;
    }

    // Set up the view with the first calendar
    view.setSelectedCalendar(defaultCalendar);
    view.updateCalendarView(currentCalendar);
    view.updateCalendarList(new ArrayList<>(calendarManager.getCalendarRegistry().getCalendarNames()));
    
    // Set the initial date to today
    setSelectedDate(LocalDate.now());
  }
}
