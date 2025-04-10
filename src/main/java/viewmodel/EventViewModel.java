package viewmodel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  private Map<LocalDate, List<Event>> eventsByDate = new HashMap<>();
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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

    void onEventsUpdated(List<Event> events);

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
    // Refresh the event state if needed
    try {
      if (controller.getCurrentCalendar() != null) {
        updateEvents(controller.getCurrentCalendar().getAllEvents());
      }
    } catch (Exception e) {
      notifyError("Error refreshing events: " + e.getMessage());
    }
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
                selectedEvent.getId(), selectedEvent.getSubject(), "start",
                startDateTime.toString());
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSingleEventEditor(
                selectedEvent.getId(), selectedEvent.getSubject(), "end",
                endDateTime.toString());
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSingleEventEditor(
                selectedEvent.getId(), selectedEvent.getSubject(), "location", location);
        editor.executeEdit(controller.getCurrentCalendar());

        editor = ConsolidatedEventEditor.createSingleEventEditor(
                selectedEvent.getId(), selectedEvent.getSubject(), "description",
                description);
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

  private void notifyEventsUpdated(List<Event> events) {
    for (EventViewModelListener listener : listeners) {
      listener.onEventsUpdated(events);
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

  /**
   * Adds a listener to be notified of events.
   *
   * @param listener the listener to add
   */
  public void addListener(EventViewModelListener listener) {
    listeners.add(listener);
  }

  /**
   * Updates the event collection with a new set of events.
   * Moved from GUICalendarPanel to follow the MVVM pattern.
   *
   * @param events the events to update with
   */
  public void updateEvents(List<Event> events) {
    // Track which dates are being updated in this operation
    Set<LocalDate> datesToUpdate = new HashSet<>();
    for (Event event : events) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();
      datesToUpdate.add(eventDate);
    }

    // Clear events only for dates that we're updating
    if (!eventsByDate.isEmpty()) {
      for (LocalDate date : datesToUpdate) {
        eventsByDate.put(date, new ArrayList<>());
      }
    } else {
      eventsByDate.clear();
    }

    // Add each event to its corresponding date in the map
    for (Event event : events) {
      // Ensure we're using the correct date from the event
      LocalDate eventDate = event.getStartDateTime().toLocalDate();

      // Get or create the list for this date
      List<Event> dateEvents = eventsByDate.computeIfAbsent(eventDate, k -> new ArrayList<>());

      // Avoid duplicate events
      if (!containsEventWithSameId(dateEvents, event)) {
        dateEvents.add(event);
      }
    }

    // Notify listeners about the update
    notifyEventsUpdated(events);
  }

  /**
   * Checks if the list of events contains an event with the same ID or
   * the same subject on the same day.
   * Moved from GUICalendarPanel to follow business logic separation.
   *
   * @param events the list of events to check
   * @param event  the event to check for
   * @return true if the event already exists in the list
   */
  public boolean containsEventWithSameId(List<Event> events, Event event) {
    if (events == null || event == null) {
      return false;
    }

    for (Event e : events) {
      if (e.getId().equals(event.getId())) {
        return true;
      }
      if (e.getSubject().equals(event.getSubject()) &&
              isSameDay(e.getStartDateTime(), event.getStartDateTime())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks if two datetime values represent the same day.
   *
   * @param dt1 the first datetime
   * @param dt2 the second datetime
   * @return true if both datetimes fall on the same day
   */
  private boolean isSameDay(LocalDateTime dt1, LocalDateTime dt2) {
    return dt1.toLocalDate().equals(dt2.toLocalDate());
  }

  /**
   * Gets events for a specific date.
   *
   * @param date the date to get events for
   * @return the list of events for the date
   */
  public List<Event> getEventsForDate(LocalDate date) {
    return eventsByDate.getOrDefault(date, new ArrayList<>());
  }

  /**
   * Creates a formatted event panel for display.
   * This method formats event details for display, moved from GUICalendarPanel.
   *
   * @param event the event to create a panel for
   * @return the formatted event details as HTML
   */
  public String formatEventForDisplay(Event event) {
    if (event == null) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("<div style='margin-bottom: 10px; padding: 8px; border-left: " +
            "4px solid #4a86e8; background-color: #f8f9fa;'>");

    // Title with time
    sb.append("<div style='font-weight: bold; color: #4a86e8;'>")
            .append(event.getSubject()).append("</div>");

    // Time information
    sb.append("<div style='font-size: 0.9em; color: #555;'>");
    sb.append(event.getStartDateTime().format(TIME_FORMATTER))
            .append(" to ")
            .append(event.getEndDateTime().format(TIME_FORMATTER));
    sb.append("</div>");

    // Location if available
    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      sb.append("<div style='font-size: 0.9em; color: #666;'><i>Location:</i> ")
              .append(event.getLocation())
              .append("</div>");
    }

    // Description if available
    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      sb.append("<div style='font-size: 0.9em; color: #666;" +
                      " margin-top: 4px;'><i>Description:</i> ")
              .append(event.getDescription())
              .append("</div>");
    }

    sb.append("</div>");
    return sb.toString();
  }

  /**
   * Creates a formatted HTML list of events for a date range.
   *
   * @param events    the events to format
   * @param startDate the start date of the range
   * @param endDate   the end date of the range
   * @return formatted HTML content
   */
  public String formatEventsListForRange(List<Event> events, LocalDate startDate, LocalDate endDate) {
    StringBuilder html = new StringBuilder();
    html.append("<html><body style='font-family: Arial, sans-serif;'>");

    if (events == null || events.isEmpty()) {
      html.append("<p>No events found between ").append(startDate)
              .append(" and ").append(endDate).append("</p>");
      html.append("</body></html>");
      return html.toString();
    }

    // Group events by date
    Map<LocalDate, List<Event>> eventsByDate = new HashMap<>();
    for (Event event : events) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();
      eventsByDate.computeIfAbsent(eventDate, k -> new ArrayList<>()).add(event);
    }

    // Sort dates and create sections for each date
    List<LocalDate> sortedDates = new ArrayList<>(eventsByDate.keySet());
    sortedDates.sort(LocalDate::compareTo);

    for (LocalDate date : sortedDates) {
      html.append("<div style='margin-bottom: 15px;'>");
      html.append("<h3 style='color: #4a86e8; margin-bottom: 5px;'>").append(date).append("</h3>");

      List<Event> dateEvents = eventsByDate.get(date);
      for (Event event : dateEvents) {
        html.append(formatEventForDisplay(event));
      }

      html.append("</div>");
    }

    html.append("</body></html>");
    return html.toString();
  }
}