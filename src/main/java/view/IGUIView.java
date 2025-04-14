package view;

import java.time.LocalDate;
import java.util.List;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import viewmodel.CalendarViewModel;
import viewmodel.EventViewModel;
import viewmodel.ExportImportViewModel;

/**
 * Interface for GUI-specific view functionality.
 */
public interface IGUIView {

  /**
   * Displays the GUI window.
   */
  void displayGUI();

  /**
   * Gets the calendar panel.
   *
   * @return the calendar panel
   */
  GUICalendarPanel getCalendarPanel();

  /**
   * Gets the event panel.
   *
   * @return the event panel
   */
  GUIEventPanel getEventPanel();

  /**
   * Gets the calendar selector panel.
   *
   * @return the calendar selector panel
   */
  GUICalendarSelectorPanel getCalendarSelectorPanel();

  /**
   * Gets the export/import panel.
   *
   * @return the export/import panel
   */
  GUIExportImportPanel getExportImportPanel();

  /**
   * Gets the calendar view model.
   *
   * @return the calendar view model
   */
  CalendarViewModel getCalendarViewModel();

  /**
   * Gets the event view model.
   *
   * @return the event view model
   */
  EventViewModel getEventViewModel();

  /**
   * Gets the export/import view model.
   *
   * @return the export/import view model
   */
  ExportImportViewModel getExportImportViewModel();

  /**
   * Updates the calendar view with new data.
   *
   * @param calendar the calendar to display
   */
  void updateCalendarView(ICalendar calendar);

  /**
   * Updates the list of events.
   *
   * @param events the list of events to display
   */
  void updateEventList(List<Event> events);

  /**
   * Updates the list of recurring events.
   *
   * @param recurringEvents the list of recurring events to display
   */
  void updateRecurringEventList(List<RecurringEvent> recurringEvents);

  /**
   * Shows the details of an event.
   *
   * @param event the event to display
   */
  void showEventDetails(Event event);

  /**
   * Clears the event details form.
   */
  void clearEventDetails();

  /**
   * Updates the list of calendars.
   *
   * @param calendarNames the list of calendar names
   */
  void updateCalendarList(List<String> calendarNames);

  /**
   * Gets the currently selected calendar.
   *
   * @return the selected calendar name
   */
  String getSelectedCalendar();

  /**
   * Gets the currently selected date.
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
   * Refreshes all GUI components.
   */
  void refreshView();

  /**
   * Displays a message to the user.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param error the error message to display
   */
  void displayError(String error);
} 