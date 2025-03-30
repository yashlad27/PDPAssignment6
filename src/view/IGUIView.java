package view;

import java.time.LocalDate;
import java.util.List;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;

/**
 * Interface for GUI-specific view operations.
 * This interface separates GUI concerns from the general calendar view interface.
 */
public interface IGUIView {
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
   * Sets the selected calendar.
   *
   * @param calendarName the name of the calendar to select
   */
  void setSelectedCalendar(String calendarName);

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