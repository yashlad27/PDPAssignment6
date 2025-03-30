package viewmodel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import controller.CalendarController;
import controller.command.edit.strategy.EventEditor;
import controller.command.edit.strategy.SeriesFromDateEditor;
import controller.command.edit.strategy.SingleEventEditor;
import model.event.Event;
import model.event.RecurringEvent;

/**
 * ViewModel for managing event-related operations and state.
 * This class handles the business logic for event operations and maintains the state
 * that will be displayed in the view.
 */
public class EventViewModel implements IViewModel {
  private final CalendarController controller;
  private Event selectedEvent;
  private RecurringEvent selectedRecurringEvent;
  private final List<EventViewModelListener> listeners;

  /**
   * Interface for listeners that want to be notified of changes in the EventViewModel.
   */
  public interface EventViewModelListener {
    void onEventSelected(Event event);

    void onRecurringEventSelected(RecurringEvent event);

    void onEventCreated(Event event);

    void onRecurringEventCreated(RecurringEvent event);

    void onEventUpdated(Event event);

    void onRecurringEventUpdated(RecurringEvent event);

    void onError(String error);
  }

  /**
   * Constructs a new EventViewModel.
   *
   * @param controller the calendar controller
   */
  public EventViewModel(CalendarController controller) {
    this.controller = controller;
    this.listeners = new ArrayList<>();
  }

  @Override
  public void initialize() {
    // Initialize with default state if needed
  }

  @Override
  public void dispose() {
    listeners.clear();
  }

  @Override
  public void refresh() {
    // Refresh the current state
  }

  /**
   * Adds a listener to be notified of changes.
   *
   * @param listener the listener to add
   */
  public void addListener(EventViewModelListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes a listener from notifications.
   *
   * @param listener the listener to remove
   */
  public void removeListener(EventViewModelListener listener) {
    listeners.remove(listener);
  }

  /**
   * Sets the selected event.
   *
   * @param event the event to select
   */
  public void setSelectedEvent(Event event) {
    this.selectedEvent = event;
    notifyEventSelected();
  }

  /**
   * Sets the selected recurring event.
   *
   * @param event the recurring event to select
   */
  public void setSelectedRecurringEvent(RecurringEvent event) {
    this.selectedRecurringEvent = event;
    notifyRecurringEventSelected();
  }

  /**
   * Creates a new event.
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   * @param location      the event location
   * @param description   the event description
   */
  public void createEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                          String location, String description) {
    try {
      // Use SingleEventEditor to create a new event
      String[] args = new String[]{"single", "create", subject, startDateTime.toString(),
              String.format("%s,%s,%s,%s", subject, startDateTime, endDateTime, location)};
      EventEditor editor = new SingleEventEditor(args);
      editor.executeEdit(controller.getCurrentCalendar());

      Event event = new Event(subject, startDateTime, endDateTime, description, location, true);
      notifyEventCreated(event);
    } catch (Exception e) {
      notifyError("Failed to create event: " + e.getMessage());
    }
  }

  /**
   * Creates a new recurring event.
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   * @param location      the event location
   * @param description   the event description
   * @param frequency     the frequency of recurrence
   * @param weekdays      the weekdays on which the event occurs
   * @param endDate       the end date of the recurring event
   */
  public void createRecurringEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                                   String location, String description, int frequency,
                                   List<DayOfWeek> weekdays, LocalDate endDate) {
    try {
      // Use SeriesFromDateEditor to create a new recurring event
      String[] args = new String[]{"series_from_date", "create", subject, startDateTime.toString(),
              String.format("%s,%s,%s,%s,%d,%s,%s", subject, startDateTime, endDateTime, location,
                      frequency, weekdays, endDate)};
      EventEditor editor = new SeriesFromDateEditor(args);
      editor.executeEdit(controller.getCurrentCalendar());

      RecurringEvent event = new RecurringEvent.Builder(subject, startDateTime, endDateTime, Set.copyOf(weekdays))
              .description(description)
              .location(location)
              .isPublic(true)
              .occurrences(frequency)
              .endDate(endDate)
              .build();
      notifyRecurringEventCreated(event);
    } catch (Exception e) {
      notifyError("Failed to create recurring event: " + e.getMessage());
    }
  }

  /**
   * Updates the selected event.
   *
   * @param subject       the new subject
   * @param startDateTime the new start date and time
   * @param endDateTime   the new end date and time
   * @param location      the new location
   * @param description   the new description
   */
  public void updateEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                          String location, String description) {
    if (selectedEvent != null) {
      try {
        // Use SingleEventEditor for updating a single event
        String[] args = new String[]{"single", "subject", subject, startDateTime.toString(), subject};
        EventEditor editor = new SingleEventEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        args = new String[]{"single", "startDateTime", subject, startDateTime.toString(), startDateTime.toString()};
        editor = new SingleEventEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        args = new String[]{"single", "endDateTime", subject, startDateTime.toString(), endDateTime.toString()};
        editor = new SingleEventEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        args = new String[]{"single", "location", subject, startDateTime.toString(), location};
        editor = new SingleEventEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        args = new String[]{"single", "description", subject, startDateTime.toString(), description};
        editor = new SingleEventEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        notifyEventUpdated(selectedEvent);
      } catch (Exception e) {
        notifyError("Failed to update event: " + e.getMessage());
      }
    }
  }

  /**
   * Updates the selected recurring event.
   *
   * @param subject       the new subject
   * @param startDateTime the new start date and time
   * @param endDateTime   the new end date and time
   * @param location      the new location
   * @param description   the new description
   * @param frequency     the new frequency
   * @param weekdays      the new weekdays
   * @param endDate       the new end date
   */
  public void updateRecurringEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                                   String location, String description, int frequency,
                                   List<DayOfWeek> weekdays, LocalDate endDate) {
    if (selectedRecurringEvent != null) {
      try {
        // Use SeriesFromDateEditor for updating recurring events from a specific date
        String[] args = new String[]{"series_from_date", "subject", subject, startDateTime.toString(), subject};
        EventEditor editor = new SeriesFromDateEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        args = new String[]{"series_from_date", "startDateTime", subject, startDateTime.toString(), startDateTime.toString()};
        editor = new SeriesFromDateEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        args = new String[]{"series_from_date", "endDateTime", subject, startDateTime.toString(), endDateTime.toString()};
        editor = new SeriesFromDateEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        args = new String[]{"series_from_date", "location", subject, startDateTime.toString(), location};
        editor = new SeriesFromDateEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        args = new String[]{"series_from_date", "description", subject, startDateTime.toString(), description};
        editor = new SeriesFromDateEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        args = new String[]{"series_from_date", "frequency", subject, startDateTime.toString(), String.valueOf(frequency)};
        editor = new SeriesFromDateEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        args = new String[]{"series_from_date", "weekdays", subject, startDateTime.toString(), weekdays.toString()};
        editor = new SeriesFromDateEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        args = new String[]{"series_from_date", "endDate", subject, startDateTime.toString(), endDate.toString()};
        editor = new SeriesFromDateEditor(args);
        editor.executeEdit(controller.getCurrentCalendar());

        notifyRecurringEventUpdated(selectedRecurringEvent);
      } catch (Exception e) {
        notifyError("Failed to update recurring event: " + e.getMessage());
      }
    }
  }

  /**
   * Gets the selected event.
   *
   * @return the selected event
   */
  public Event getSelectedEvent() {
    return selectedEvent;
  }

  /**
   * Gets the selected recurring event.
   *
   * @return the selected recurring event
   */
  public RecurringEvent getSelectedRecurringEvent() {
    return selectedRecurringEvent;
  }

  private void notifyEventSelected() {
    for (EventViewModelListener listener : listeners) {
      listener.onEventSelected(selectedEvent);
    }
  }

  private void notifyRecurringEventSelected() {
    for (EventViewModelListener listener : listeners) {
      listener.onRecurringEventSelected(selectedRecurringEvent);
    }
  }

  private void notifyEventCreated(Event event) {
    for (EventViewModelListener listener : listeners) {
      listener.onEventCreated(event);
    }
  }

  private void notifyRecurringEventCreated(RecurringEvent event) {
    for (EventViewModelListener listener : listeners) {
      listener.onRecurringEventCreated(event);
    }
  }

  private void notifyEventUpdated(Event event) {
    for (EventViewModelListener listener : listeners) {
      listener.onEventUpdated(event);
    }
  }

  private void notifyRecurringEventUpdated(RecurringEvent event) {
    for (EventViewModelListener listener : listeners) {
      listener.onRecurringEventUpdated(event);
    }
  }

  /**
   * Notifies listeners of an error.
   *
   * @param error the error message
   */
  public void notifyError(String error) {
    for (EventViewModelListener listener : listeners) {
      listener.onError(error);
    }
  }
} 