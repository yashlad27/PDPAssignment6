package view;

import java.time.LocalDate;
import java.util.List;

import model.calendar.ICalendar;
import model.event.Event;
import viewmodel.ExportImportViewModel;

/**
 * Interface defining the features and operations that a calendar view must provide to interact with
 * the controller. This abstraction reduces coupling between the controller and specific view
 * implementations. Extends IGUIView to maintain compatibility with existing code.
 */
public interface CalendarViewFeatures extends IGUIView {

  /**
   * Displays an informational message to the user.
   *
   * @param message The message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param message The error message to display
   */
  void displayError(String message);

  /**
   * Shows a standard info dialog with a message.
   *
   * @param message The information message to show
   */
  void showInfoMessage(String message);

  /**
   * Shows a standard error dialog with a message.
   *
   * @param message The error message to show
   */
  void showErrorMessage(String message);

  /**
   * Updates the calendar view with the provided calendar.
   *
   * @param calendar The calendar to display
   */
  void updateCalendarView(ICalendar calendar);

  /**
   * Updates the list of available calendars.
   *
   * @param calendarNames List of calendar names
   */
  void updateCalendarList(List<String> calendarNames);

  /**
   * Sets the currently selected calendar.
   *
   * @param calendarName The name of the selected calendar
   */
  void setSelectedCalendar(String calendarName);

  /**
   * Updates the events for a specified date.
   *
   * @param date   The date for which to update events
   * @param events The list of events to display
   */
  void updateEvents(LocalDate date, List<Event> events);

  /**
   * Updates the event list with the provided events.
   *
   * @param events The list of events to display
   */
  void updateEventList(List<Event> events);

  /**
   * Shows detailed information for a specific event.
   *
   * @param event The event to display
   */
  void showEventDetails(Event event);

  /**
   * Opens a dialog to edit an event.
   *
   * @param event       The event to edit
   * @param isRecurring Whether the event is recurring
   */
  void showEventEditDialog(Event event, boolean isRecurring);

  /**
   * Forces a refresh of all view components.
   */
  void refreshView();

  /**
   * Refreshes only the calendar view components.
   */
  void refreshCalendarView();

  /**
   * Refreshes only the event view components.
   */
  void refreshEventView();

  /**
   * Gets the currently selected date in the calendar.
   *
   * @return The currently selected date
   */
  LocalDate getSelectedDate();

  /**
   * Gets the export/import view model.
   *
   * @return The export/import view model
   */
  ExportImportViewModel getExportImportViewModel();

  /**
   * Gets the calendar panel component.
   *
   * @return The calendar panel
   */
  GUICalendarPanel getCalendarPanel();

  /**
   * Gets the event panel component.
   *
   * @return The event panel
   */
  GUIEventPanel getEventPanel();

  /**
   * Gets the calendar selector panel component.
   *
   * @return The calendar selector panel
   */
  GUICalendarSelectorPanel getCalendarSelectorPanel();

  /**
   * Gets the export/import panel component.
   *
   * @return The export/import panel
   */
  GUIExportImportPanel getExportImportPanel();
  
  /**
   * Updates the event list results panel to display events under the calendar grid.
   *
   * @param startDate The start date of the range (or a single date)
   * @param endDate   The end date of the range (same as startDate for single day)
   * @param events    The list of events to display
   */
  void updateEventListResultsPanel(LocalDate startDate, LocalDate endDate, List<Event> events);
} 