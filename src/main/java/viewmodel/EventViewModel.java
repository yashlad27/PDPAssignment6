package viewmodel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import controller.CalendarController;
import controller.command.edit.strategy.ConsolidatedEventEditor;
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
      // Use ConsolidatedEventEditor to create a new event
      UUID eventId = UUID.randomUUID();
      ConsolidatedEventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
          eventId, subject, "subject", subject);
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
      // Use ConsolidatedEventEditor to create a new recurring event
      // Create a UUID for the new event
      UUID eventId = UUID.randomUUID();
      ConsolidatedEventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
          eventId, subject, "subject", subject);
      editor.executeEdit(controller.getCurrentCalendar());

      editor = ConsolidatedEventEditor.createSingleEventEditor(
          eventId, subject, "start", startDateTime.toString());
      editor.executeEdit(controller.getCurrentCalendar());

      editor = ConsolidatedEventEditor.createSingleEventEditor(
          eventId, subject, "end", endDateTime.toString());
      editor.executeEdit(controller.getCurrentCalendar());

      editor = ConsolidatedEventEditor.createSingleEventEditor(
          eventId, subject, "location", location);
      editor.executeEdit(controller.getCurrentCalendar());

      editor = ConsolidatedEventEditor.createSingleEventEditor(
          eventId, subject, "description", description);
      editor.executeEdit(controller.getCurrentCalendar());

      editor = ConsolidatedEventEditor.createSingleEventEditor(
          eventId, subject, "frequency", String.valueOf(frequency));
      editor.executeEdit(controller.getCurrentCalendar());

      editor = ConsolidatedEventEditor.createSingleEventEditor(
          eventId, subject, "weekdays", weekdays.toString());
      editor.executeEdit(controller.getCurrentCalendar());

      editor = ConsolidatedEventEditor.createSingleEventEditor(
          eventId, subject, "endDate", endDate.toString());
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
        // Use ConsolidatedEventEditor to update the event
        ConsolidatedEventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            selectedEvent.getId(), selectedEvent.getSubject(), "subject", subject);
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSingleEventEditor(
            selectedEvent.getId(), selectedEvent.getSubject(), "start", startDateTime.toString());
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSingleEventEditor(
            selectedEvent.getId(), selectedEvent.getSubject(), "end", endDateTime.toString());
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSingleEventEditor(
            selectedEvent.getId(), selectedEvent.getSubject(), "location", location);
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSingleEventEditor(
            selectedEvent.getId(), selectedEvent.getSubject(), "description", description);
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
        // Use ConsolidatedEventEditor to update the recurring event
        LocalDate fromLocalDate = startDateTime.toLocalDate();
        ConsolidatedEventEditor editor = ConsolidatedEventEditor.createSeriesFromDateEditor(
            selectedRecurringEvent.getId(), selectedRecurringEvent.getSubject(), "subject", subject, fromLocalDate);
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSeriesFromDateEditor(
            selectedRecurringEvent.getId(), selectedRecurringEvent.getSubject(), "start", startDateTime.toString(), fromLocalDate);
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSeriesFromDateEditor(
            selectedRecurringEvent.getId(), selectedRecurringEvent.getSubject(), "end", endDateTime.toString(), fromLocalDate);
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSeriesFromDateEditor(
            selectedRecurringEvent.getId(), selectedRecurringEvent.getSubject(), "location", location, fromLocalDate);
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSeriesFromDateEditor(
            selectedRecurringEvent.getId(), selectedRecurringEvent.getSubject(), "description", description, fromLocalDate);
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSeriesFromDateEditor(
            selectedRecurringEvent.getId(), selectedRecurringEvent.getSubject(), "frequency", String.valueOf(frequency), fromLocalDate);
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSeriesFromDateEditor(
            selectedRecurringEvent.getId(), selectedRecurringEvent.getSubject(), "weekdays", weekdays.toString(), fromLocalDate);
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSeriesFromDateEditor(
            selectedRecurringEvent.getId(), selectedRecurringEvent.getSubject(), "endDate", endDate.toString(), fromLocalDate);
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