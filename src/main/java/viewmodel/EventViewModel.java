package viewmodel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
      UUID eventId = UUID.randomUUID();
      ConsolidatedEventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
              eventId, subject, "subject", subject);
      editor.executeEdit(controller.getCurrentCalendar());

      Event event = new Event(subject, startDateTime, endDateTime,
              description, location, true);
      notifyEventCreated(event);
    } catch (Exception e) {
      notifyError("Failed to create event: " + e.getMessage());
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

  private void notifyEventCreated(Event event) {
    for (EventViewModelListener listener : listeners) {
      listener.onEventCreated(event);
    }
  }

  private void notifyEventUpdated(Event event) {
    for (EventViewModelListener listener : listeners) {
      listener.onEventUpdated(event);
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