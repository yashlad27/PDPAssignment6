package view;

import java.time.LocalDate;
import java.util.List;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import view.GUICalendarPanel.CalendarPanelListener;
import view.GUICalendarSelectorPanel.CalendarSelectorListener;
import view.GUIEventPanel.EventPanelListener;
import view.GUIExportImportPanel.ExportImportListener;
import viewmodel.CalendarViewModel;
import viewmodel.EventViewModel;
import viewmodel.ExportImportViewModel;

/**
 * Interface for the calendar view that handles user interaction.
 * This interface defines the contract for any view implementation in the calendar application,
 * establishing a clear separation between the user interface and the underlying business logic.
 * By using this interface, the application can support multiple view implementations
 * (console-based, GUI, web, etc.) without changing the core functionality.
 */
public interface ICalendarView {

  /**
   * Reads a command from the user.
   * This method is responsible for obtaining input from the user through the
   * appropriate input mechanism (console, GUI elements, etc.). It should handle
   * any necessary formatting or preprocessing of the raw input before returning
   * it as a command string.
   *
   * @return the command string entered by the user
   */
  String readCommand();

  /**
   * Displays a message to the user.
   * This method is responsible for showing informational messages to the user
   * through the appropriate output mechanism (console, GUI elements, etc.).
   * It should present the information in a clear and readable format appropriate
   * to the view implementation.
   *
   * @param message the message to display to the user
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   * This method is responsible for showing error messages to the user
   * through the appropriate output mechanism (console, GUI elements, etc.).
   * Error messages should be visually distinct from normal messages to
   * clearly indicate that an error has occurred.
   *
   * @param errorMessage the error message to display to the user
   */
  void displayError(String errorMessage);

  /**
   * Updates the calendar view.
   *
   * @param calendar the calendar to display
   */
  void updateCalendarView(ICalendar calendar);

  /**
   * Updates the event list.
   *
   * @param events the list of events to display
   */
  void updateEventList(List<Event> events);

  /**
   * Updates the recurring event list.
   *
   * @param recurringEvents the list of recurring events to display
   */
  void updateRecurringEventList(List<RecurringEvent> recurringEvents);

  /**
   * Shows event details.
   *
   * @param event the event to display
   */
  void showEventDetails(Event event);

  /**
   * Clears event details.
   */
  void clearEventDetails();

  /**
   * Updates the calendar list.
   *
   * @param calendarNames the list of calendar names
   */
  void updateCalendarList(List<String> calendarNames);

  /**
   * Sets the selected calendar.
   *
   * @param calendarName the name of the calendar to select
   */
  void setSelectedCalendar(String calendarName);

  /**
   * Gets the selected calendar.
   *
   * @return the name of the selected calendar
   */
  String getSelectedCalendar();

  /**
   * Gets the selected date.
   *
   * @return the selected date
   */
  LocalDate getSelectedDate();

  /**
   * Sets the selected date.
   *
   * @param date the date to select
   */
  void setSelectedDate(LocalDate date);

  /**
   * Refreshes the view.
   */
  void refreshView();

  /**
   * Updates the selected date in the view.
   *
   * @param date the date to update to
   */
  void updateSelectedDate(LocalDate date);
  
  /**
   * Shows an error message to the user (more specific than displayError).
   *
   * @param message the error message to display
   */
  void showErrorMessage(String message);
  
  /**
   * Shows an informational message to the user.
   *
   * @param message the information message to display
   */
  void showInfoMessage(String message);
  
  /**
   * Add a listener for calendar selection events.
   *
   * @param listener the listener to add
   */
  void addCalendarSelectorListener(CalendarSelectorListener listener);
  
  /**
   * Add a listener for event panel events.
   *
   * @param listener the listener to add
   */
  void addEventPanelListener(EventPanelListener listener);
  
  /**
   * Add a listener for calendar panel events.
   *
   * @param listener the listener to add
   */
  void addCalendarPanelListener(CalendarPanelListener listener);
  
  /**
   * Add a listener for export/import events.
   *
   * @param listener the listener to add
   */
  void addExportImportListener(ExportImportListener listener);
  
  /**
   * Updates calendar name in display.
   *
   * @param name the name to display
   */
  void updateCalendarName(String name);
  
  /**
   * Clear all events from the display.
   */
  void clearEvents();
  
  /**
   * Get the current event being edited/displayed.
   * 
   * @return the current event
   */
  Event getCurrentEvent();
  
  /**
   * Display an event in the edit form.
   *
   * @param event the event to display
   */
  void displayEvent(Event event);
  
  /**
   * Display a recurring event in the edit form.
   *
   * @param event the recurring event to display
   */
  void displayRecurringEvent(RecurringEvent event);
  
  /**
   * Clear the edit form.
   */
  void clearForm();
  
  /**
   * Update the event list for a specific date.
   *
   * @param date the date to show events for
   */
  void updateEventList(LocalDate date);
  
  /**
   * Update the event list for a date range.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param filter optional filter criteria
   */
  void updateEventListRange(LocalDate startDate, LocalDate endDate, String filter);
  
  /**
   * Update the status of a date (busy/available).
   *
   * @param date the date to update
   * @param isBusy whether the date is busy
   * @param eventCount number of events on that date
   */
  void updateStatus(LocalDate date, boolean isBusy, int eventCount);
  
  /**
   * Refresh the calendar view portion of the display.
   */
  void refreshCalendarView();
  
  /**
   * Refresh the event view portion of the display.
   */
  void refreshEventView();
  
  /**
   * Get the calendar view model.
   *
   * @return the calendar view model
   */
  CalendarViewModel getCalendarViewModel();
  
  /**
   * Get the event view model.
   *
   * @return the event view model
   */
  EventViewModel getEventViewModel();
  
  /**
   * Get the export/import view model.
   *
   * @return the export/import view model
   */
  ExportImportViewModel getExportImportViewModel();
  
  /**
   * Updates events for a specific date.
   *
   * @param date the date to update events for
   * @param events the list of events
   */
  void updateEvents(LocalDate date, List<Event> events);
}