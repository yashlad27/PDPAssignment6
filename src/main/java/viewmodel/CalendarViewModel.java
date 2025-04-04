package viewmodel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

/**
 * ViewModel for managing calendar-related operations and state.
 * This class handles the business logic for calendar operations and maintains the state
 * that will be displayed in the view.
 */
public class CalendarViewModel implements IViewModel {
  private ICalendar currentCalendar;
  private String selectedCalendarName;
  private LocalDate selectedDate;
  private List<Event> events;
  private List<RecurringEvent> recurringEvents;
  private List<String> calendarNames;
  private final List<CalendarViewModelListener> listeners;

  /**
   * Interface for listeners that want to be notified of changes in the CalendarViewModel.
   */
  public interface CalendarViewModelListener {
    void onCalendarChanged(ICalendar calendar);

    void onDateSelected(LocalDate date);

    void onEventsUpdated(List<Event> events);

    void onRecurringEventsUpdated(List<RecurringEvent> recurringEvents);

    void onCalendarListUpdated(List<String> calendarNames);

    void onError(String error);
  }

  /**
   * Constructs a new CalendarViewModel.
   */
  public CalendarViewModel() {
    this.listeners = new ArrayList<>();
    this.events = new ArrayList<>();
    this.recurringEvents = new ArrayList<>();
    this.calendarNames = new ArrayList<>();
  }

  @Override
  public void initialize() {
    // Initialize with default calendar if needed
  }

  @Override
  public void dispose() {
    listeners.clear();
  }

  @Override
  public void refresh() {
    notifyCalendarChanged();
    notifyEventsUpdated();
    notifyRecurringEventsUpdated();
    notifyCalendarListUpdated();
  }

  /**
   * Adds a listener to be notified of changes.
   *
   * @param listener the listener to add
   */
  public void addListener(CalendarViewModelListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes a listener from notifications.
   *
   * @param listener the listener to remove
   */
  public void removeListener(CalendarViewModelListener listener) {
    listeners.remove(listener);
  }

  /**
   * Sets the current calendar.
   *
   * @param calendar the calendar to set
   */
  public void setCurrentCalendar(ICalendar calendar) throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    this.currentCalendar = calendar;
    // Get calendar name from toString
    this.selectedCalendarName = calendar != null ? calendar.toString() : "None";
    notifyCalendarChanged();
  }

  /**
   * Sets the selected date.
   *
   * @param date the date to select
   */
  public void setSelectedDate(LocalDate date) {
    this.selectedDate = date;
    notifyDateSelected();
    updateEventsForDate(date);
  }

  /**
   * Updates the list of events for the selected date.
   *
   * @param date the date to update events for
   */
  private void updateEventsForDate(LocalDate date) {
    if (currentCalendar != null) {
      try {
        // Get events directly from calendar
        events = currentCalendar.getEventsOnDate(date);
        recurringEvents = currentCalendar.getAllRecurringEvents().stream()
                .filter(event -> event.getStartDateTime().toLocalDate().equals(date))
                .collect(java.util.stream.Collectors.toList());

        notifyEventsUpdated();
        notifyRecurringEventsUpdated();
      } catch (Exception e) {
        notifyError("Failed to update events for date: " + e.getMessage());
      }
    }
  }

  /**
   * Updates the list of calendar names.
   *
   * @param names the list of calendar names
   */
  public void updateCalendarList(List<String> names) {
    this.calendarNames = new ArrayList<>(names);
    notifyCalendarListUpdated();
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
   * Sets the selected calendar name.
   *
   * @param calendarName the name of the calendar to select
   */
  public void setSelectedCalendarName(String calendarName) {
    this.selectedCalendarName = calendarName;
    notifyCalendarChanged();
  }

  /**
   * Gets the selected calendar name.
   *
   * @return the selected calendar name
   */
  public String getSelectedCalendarName() {
    return selectedCalendarName;
  }

  /**
   * Gets the selected date.
   *
   * @return the selected date
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  /**
   * Gets the list of events.
   *
   * @return the list of events
   */
  public List<Event> getEvents() {
    return events;
  }

  /**
   * Gets the list of recurring events.
   *
   * @return the list of recurring events
   */
  public List<RecurringEvent> getRecurringEvents() {
    return recurringEvents;
  }

  /**
   * Gets the list of calendar names.
   *
   * @return the list of calendar names
   */
  public List<String> getCalendarNames() {
    return calendarNames;
  }

  private void notifyCalendarChanged() {
    for (CalendarViewModelListener listener : listeners) {
      listener.onCalendarChanged(currentCalendar);
    }
  }

  private void notifyDateSelected() {
    for (CalendarViewModelListener listener : listeners) {
      listener.onDateSelected(selectedDate);
    }
  }

  private void notifyEventsUpdated() {
    for (CalendarViewModelListener listener : listeners) {
      listener.onEventsUpdated(events);
    }
  }

  private void notifyRecurringEventsUpdated() {
    for (CalendarViewModelListener listener : listeners) {
      listener.onRecurringEventsUpdated(recurringEvents);
    }
  }

  private void notifyCalendarListUpdated() {
    for (CalendarViewModelListener listener : listeners) {
      listener.onCalendarListUpdated(calendarNames);
    }
  }

  /**
   * Notifies listeners of an error.
   *
   * @param error the error message
   */
  public void notifyError(String error) {
    for (CalendarViewModelListener listener : listeners) {
      listener.onError(error);
    }
  }
} 