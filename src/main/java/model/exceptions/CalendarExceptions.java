package model.exceptions;

/**
 * Consolidated class containing all exception types used in the calendar application.
 * This class follows the consolidation pattern used throughout the application,
 * combining related exception types into a single file to reduce the number of classes.
 * Each exception maintains its original behavior and purpose but is implemented
 * as a static nested class.
 */
public class CalendarExceptions {

  /**
   * Exception thrown when attempting to access a calendar that does not exist in the system.
   * This exception is used to indicate that a requested calendar could not be found,
   * either because it was deleted or never created.
   */
  public static class CalendarNotFoundException extends Exception {
    /**
     * Constructs a new CalendarNotFoundException with the specified error message.
     *
     * @param message the error message describing the missing calendar
     */
    public CalendarNotFoundException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when attempting to create or modify an event that conflicts
   * with existing events.
   * This exception is used to prevent scheduling conflicts by indicating that the requested
   * event's time slot overlaps with one or more existing events in the calendar.
   */
  public static class ConflictingEventException extends Exception {
    /**
     * Constructs a new ConflictingEventException with the specified error message.
     *
     * @param message the error message describing the event conflict
     */
    public ConflictingEventException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when attempting to create a calendar with a name that already exists.
   * This exception is used to prevent duplicate calendar names in the system and maintain
   * unique identification of calendars.
   */
  public static class DuplicateCalendarException extends Exception {
    /**
     * Constructs a new DuplicateCalendarException with the specified error message.
     *
     * @param message the error message describing the duplicate calendar situation
     */
    public DuplicateCalendarException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when attempting to access or modify an event that does not exist in
   * the calendar.
   * This exception is used to indicate that a requested event could not be found,
   * either because it was deleted or never created.
   */
  public static class EventNotFoundException extends Exception {
    /**
     * Constructs a new EventNotFoundException with the specified error message.
     *
     * @param message the error message describing the missing event
     */
    public EventNotFoundException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when an event is created with invalid parameters or properties.
   * This exception is used to indicate that an event's data violates the application's
   * business rules or constraints, such as invalid dates, missing required fields,
   * or invalid property values.
   */
  public static class InvalidEventException extends Exception {
    /**
     * Constructs a new InvalidEventException with the specified error message.
     *
     * @param message the error message describing the invalid event parameters
     */
    public InvalidEventException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when an invalid timezone is specified for a calendar.
   * This exception is used to indicate that a timezone string provided to the application
   * is not recognized or is not a valid timezone identifier.
   */
  public static class InvalidTimezoneException extends Exception {
    /**
     * Constructs a new InvalidTimezoneException with the specified error message.
     *
     * @param message the error message describing the invalid timezone
     */
    public InvalidTimezoneException(String message) {
      super(message);
    }
  }
}
