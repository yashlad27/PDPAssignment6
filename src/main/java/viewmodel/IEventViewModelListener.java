package viewmodel;

import java.util.List;

import model.event.Event;

/**
 * Interface for listeners that want to be notified of changes in the EventViewModel.
 * This interface defines the contract for objects that need to respond to event-related
 * changes in the calendar application's view model layer.
 *
 * <p>The listener methods are called when:
 * <ul>
 *   <li>New events are created</li>
 *   <li>Existing events are modified</li>
 *   <li>Multiple events are updated simultaneously</li>
 *   <li>Errors occur during event operations</li>
 * </ul>
 *
 * <p>Implementing classes should handle these notifications appropriately to maintain
 * UI consistency and provide feedback to users.
 */
public interface IEventViewModelListener {

  /**
   * Called when a new event is successfully created in the system.
   * Implementers should update their UI or state to reflect the new event.
   *
   * @param event The newly created Event object containing all event details
   */
  void onEventCreated(Event event);

  /**
   * Called when an existing event is modified.
   * Implementers should update their UI or state to reflect the changes made to the event.
   *
   * @param event The updated Event object containing the modified event details
   */
  void onEventUpdated(Event event);

  /**
   * Called when multiple events are updated simultaneously.
   * This could happen during bulk operations like importing events or updating recurring events.
   *
   * @param events List of Event objects that have been updated
   */
  void onEventsUpdated(List<Event> events);

  /**
   * Called when an error occurs during event operations.
   * Implementers should handle the error appropriately, typically by displaying
   * the error message to the user or logging it.
   *
   * @param error A string describing the error that occurred
   */
  void onError(String error);
}
