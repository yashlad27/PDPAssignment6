package view.dialog;

import java.time.LocalDateTime;

/**
 * Interface for event edit dialogs.
 * This follows the Interface Segregation Principle by defining
 * only methods specific to event editing operations.
 */
public interface IEventEditDialog extends IEventDialog {

  /**
   * Gets the edited subject.
   *
   * @return the edited subject
   */
  String getSubject();

  /**
   * Gets the edited event location.
   *
   * @return the edited event location
   */
  String getEventLocation();

  /**
   * Gets the edited description.
   *
   * @return the edited description
   */
  String getDescription();

  /**
   * Gets the start date and time for the event.
   *
   * @return the start date and time
   */
  LocalDateTime getStartDateTime();

  /**
   * Gets the end date and time for the event.
   *
   * @return the end date and time
   */
  LocalDateTime getEndDateTime();

  /**
   * Checks if the event is set to all day.
   *
   * @return true if the event is all day, false otherwise
   */
  boolean isAllDay();

  /**
   * Checks if the event is private.
   *
   * @return true if the event is private, false otherwise
   */
  boolean isPrivate();

  /**
   * Checks if the event is recurring.
   *
   * @return true if the event is recurring, false otherwise
   */
  boolean isRecurring();
}
