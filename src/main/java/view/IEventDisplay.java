package view;

import model.event.Event;
import model.event.RecurringEvent;
import java.util.List;

/**
 * Interface for event display functionality.
 * This interface defines methods for displaying and updating event-related information.
 */
public interface IEventDisplay {
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
} 