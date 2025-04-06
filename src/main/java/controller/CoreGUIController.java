package controller;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.CalendarNotFoundException;
import utilities.TimeZoneHandler;
import view.EventFormData;
import view.GUIView;

/**
 * Main controller that coordinates between specialized controllers.
 * This follows the SOLID principles by having a clear single responsibility as a coordinator,
 * while delegating specific functionality to specialized controllers.
 */
public class CoreGUIController {
  private final CalendarManager calendarManager;
  private final GUIView view;
  private final TimeZoneHandler timezoneHandler;

  // Specialized controllers
  private final EventController eventController;
  private final CalendarViewController calendarViewController;
  private final ImportExportController importExportController;

  private ICalendar currentCalendar;

  /**
   * Constructs a new CoreGUIController.
   *
   * @param calendarManager the calendar manager
   * @param view            the GUI view
   */
  public CoreGUIController(CalendarManager calendarManager, GUIView view) {
    this.calendarManager = calendarManager;
    this.view = view;
    this.timezoneHandler = new TimeZoneHandler();

    // Initialize specialized controllers
    this.eventController = new EventController(view);
    this.calendarViewController = new CalendarViewController(calendarManager, view, timezoneHandler);
    this.importExportController = new ImportExportController(view);
  }

  /**
   * Initializes the application.
   */
  public void initialize() throws CalendarNotFoundException {
    System.out.println("Initializing GUI controller...");

    // Initialize the calendar view
    calendarViewController.initializeCalendarView();

    // Get the current calendar from the calendar view controller
    this.currentCalendar = calendarViewController.getCurrentCalendar();

    // Set the current calendar in all controllers
    eventController.setCurrentCalendar(currentCalendar);
    importExportController.setCurrentCalendar(currentCalendar);

    // Set up event listeners
    setupEventListeners();
  }

  /**
   * Sets up event listeners for the view components.
   */
  private void setupEventListeners() {
    // Set up controller connections
    // Note: Don't need to call setController since the view already has the controllers it needs
  }

  /**
   * Handles the selection of a calendar.
   *
   * @param calendar the selected calendar
   */
  public void onCalendarSelected(ICalendar calendar) {
    calendarViewController.onCalendarSelected(calendar);

    // Update the current calendar and propagate to other controllers
    this.currentCalendar = calendar;
    eventController.setCurrentCalendar(calendar);
    importExportController.setCurrentCalendar(calendar);
  }

  /**
   * Handles the creation of a new calendar.
   *
   * @param name     the name of the new calendar
   * @param timezone the timezone for the new calendar
   */
  public void onCalendarCreated(String name, String timezone) {
    calendarViewController.onCalendarCreated(name, timezone);

    // Update the current calendar and propagate to other controllers
    this.currentCalendar = calendarViewController.getCurrentCalendar();
    eventController.setCurrentCalendar(currentCalendar);
    importExportController.setCurrentCalendar(currentCalendar);
  }

  /**
   * Handles the selection of a date.
   *
   * @param date the selected date
   */
  public void onDateSelected(LocalDate date) {
    calendarViewController.onDateSelected(date);
  }

  /**
   * Handles the selection of an event.
   *
   * @param event the selected event
   */
  public void onEventSelected(Event event) {
    // We don't need to handle this in specialized controllers
    // Just display the event in the view
    if (event != null) {
      view.showEventDetails(event);
    }
  }

  /**
   * Handles the selection of a recurring event.
   *
   * @param event the selected recurring event
   */
  public void onRecurringEventSelected(RecurringEvent event) {
    // We don't need to handle this in specialized controllers
    // Just display the recurring event in the view
    if (event != null) {
      // Currently no direct method to show recurring events details in IGUIView
      // Using a generic message instead
      view.displayMessage("Selected recurring event: " + event.getSubject());
    }
  }

  /**
   * Handles a request for status information on a specific date.
   *
   * @param date the date to check status for
   */
  public void onStatusRequested(LocalDate date) {
    calendarViewController.onStatusRequested(date);
  }

  /**
   * Handles a request for events on a specific date.
   *
   * @param date the date to get events for
   */
  public void onEventsListRequested(LocalDate date) {
    calendarViewController.onEventsListRequested(date);
  }

  /**
   * Handles a request for events in a date range.
   *
   * @param startDate the start date of the range
   * @param endDate   the end date of the range
   */
  public void onDateRangeSelected(LocalDate startDate, LocalDate endDate) {
    calendarViewController.onDateRangeSelected(startDate, endDate);
  }

  /**
   * Handles the editing of an event.
   *
   * @param event the event to edit
   */
  public void onEditEvent(Event event) {
    if (event instanceof RecurringEvent) {
      eventController.editRecurringEvent((RecurringEvent) event);
    } else {
      eventController.editEvent(event);
    }
  }

  /**
   * Handles the copying of an event.
   *
   * @param event the event to copy
   */
  public void onCopyEvent(Event event) {
    eventController.showCopyEventDialog(event);
  }

  /**
   * Handles the printing of an event.
   *
   * @param event the event to print
   */
  public void onPrintEvent(Event event) {
    eventController.printEvent(event);
  }

  /**
   * Handles a new event being saved from the event panel.
   *
   * @param formData the form data collected from the event panel
   */
  public String onEventSaved(EventFormData formData) {
    return eventController.onEventSaved(formData);
  }

  /**
   * Handles an event being cancelled.
   */
  public void onEventCancelled() {
    // Just reset the view to show the calendar
    view.refreshView();
  }

  /**
   * Handles an event being updated from the event panel.
   *
   * @param formData the form data collected from the event panel
   */
  public void onEventUpdated(EventFormData formData) {
    eventController.onEventUpdated(formData);
  }

  /**
   * Handles copying an event.
   *
   * @param event               the event to copy
   * @param targetCalendarName  the name of the target calendar
   * @param targetStartDateTime the target start date/time
   * @param targetEndDateTime   the target end date/time
   */
  public void onEventCopied(String targetCalendarName, LocalDateTime targetStartDateTime, LocalDateTime targetEndDateTime) {
    if (currentCalendar == null) {
      view.showErrorMessage("Please select a calendar first");
      return;
    }

    // Get event data from the event panel
    Event eventToCopy = view.getEventPanel().getCurrentEvent();
    if (eventToCopy == null) {
      view.showErrorMessage("No event selected");
      return;
    }

    // Delegate to event controller
    try {
      eventController.copyEvent(eventToCopy, targetCalendarName, targetStartDateTime, targetEndDateTime);
    } catch (Exception e) {
      view.showErrorMessage("Error copying event: " + e.getMessage());
    }
  }

  /**
   * Gets the names of all available calendars.
   *
   * @return list of calendar names
   */
  public List<String> getAvailableCalendarNames() {
    try {
      return new ArrayList<>(calendarManager.getCalendarRegistry().getCalendarNames());
    } catch (Exception e) {
      view.showErrorMessage("Error getting calendar names: " + e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * Handles importing a calendar from a file.
   *
   * @param file the file to import from
   */
  public void onImport(File file) {
    importExportController.onImport(file);
  }

  /**
   * Handles exporting a calendar to a file.
   *
   * @param file the file to export to
   */
  public void onExport(File file) {
    importExportController.onExport(file);
  }

  /**
   * Handles the creation of a new event.
   */
  public void createNewEvent() {
    eventController.createNewEvent();
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
   * Handles the closing of the application.
   */
  public void handleApplicationClose() {
    // Perform any cleanup required before closing
    System.out.println("Application closing...");
  }
}
