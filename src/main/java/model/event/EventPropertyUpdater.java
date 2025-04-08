package model.event;

/**
 * Functional interface for updating event properties.
 */
@FunctionalInterface
public interface EventPropertyUpdater {
  /**
   * Updates an event property with a new value.
   *
   * @param event    The event to update
   * @param newValue The new value for the property
   * @return true if update was successful, false otherwise
   */
  boolean update(Event event, String newValue);
}
