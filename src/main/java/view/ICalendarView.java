package view;

import java.time.LocalDate;
import java.util.List;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;

/**
 * Interface for the calendar view that handles user interaction. This interface defines the
 * contract for any view implementation in the calendar application, establishing a clear separation
 * between the user interface and the underlying business logic. By using this interface, the
 * application can support multiple view implementations (console-based, GUI, web, etc.) without
 * changing the core functionality.
 */
public interface ICalendarView {

  /**
   * Reads a command from the user. This method is responsible for obtaining input from the user
   * through the appropriate input mechanism (console, GUI elements, etc.). It should handle any
   * necessary formatting or preprocessing of the raw input before returning it as a command
   * string.
   *
   * @return the command string entered by the user
   */
  String readCommand();

  /**
   * Displays a message to the user. This method is responsible for showing informational messages
   * to the user through the appropriate output mechanism (console, GUI elements, etc.). It should
   * present the information in a clear and readable format appropriate to the view implementation.
   *
   * @param message the message to display to the user
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user. This method is responsible for showing error messages to
   * the user through the appropriate output mechanism (console, GUI elements, etc.). Error messages
   * should be visually distinct from normal messages to clearly indicate that an error has
   * occurred.
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
}