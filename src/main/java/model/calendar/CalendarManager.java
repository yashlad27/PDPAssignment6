package model.calendar;

import java.util.Set;

import model.calendar.iterator.ConsolidatedIterator;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import utilities.CalendarNameValidator;
import utilities.TimeZoneHandler;

/**
 * Manages calendar operations and coordinates between the CalendarRegistry and TimeZoneHandler.
 * Following the Single Responsibility Principle, this class is focused on high-level
 * calendar management operations rather than storage details.
 */
public class CalendarManager {

  private final CalendarRegistry calendarRegistry;
  private final TimeZoneHandler timezoneHandler;

  /**
   * Private constructor used by the builder to create a CalendarManager instance.
   */
  protected CalendarManager(Builder builder) {
    this.calendarRegistry = new CalendarRegistry();
    this.timezoneHandler = builder.timezoneHandler;
  }

  /**
   * Builder class for creating CalendarManager instances.
   */
  public static class Builder {
    private TimeZoneHandler timezoneHandler;

    /**
     * Constructor for Builder.
     */
    public Builder() {
      this.timezoneHandler = new TimeZoneHandler();
    }

    /**
     * Sets the timezone handler to use.
     *
     * @param timezoneHandler the timezone handler
     * @return the builder instance
     */
    public Builder timezoneHandler(TimeZoneHandler timezoneHandler) {
      this.timezoneHandler = timezoneHandler;
      return this;
    }

    /**
     * Builds a new CalendarManager with the configured parameters.
     *
     * @return a new CalendarManager instance
     */
    public CalendarManager build() {
      return new CalendarManager(this);
    }
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     the unique name for the calendar
   * @param timezone the timezone for the calendar
   * @return the newly created calendar
   * @throws DuplicateCalendarException if a calendar with the specified name already exists
   * @throws InvalidTimezoneException   if the timezone is invalid
   */
  public Calendar createCalendar(String name, String timezone)
          throws DuplicateCalendarException, InvalidTimezoneException {
    if (!timezoneHandler.isValidTimezone(timezone)) {
      throw new InvalidTimezoneException("Invalid timezone: " + timezone);
    }

    CalendarNameValidator.validateCalendarName(name);

    Calendar calendar = new Calendar(name, timezone);

    try {
      calendarRegistry.registerCalendar(name, calendar);
    } catch (DuplicateCalendarException e) {
      throw e;
    }

    return calendar;
  }

  /**
   * Gets a calendar by name.
   *
   * @param name the name of the calendar
   * @return the calendar with the specified name
   * @throws CalendarNotFoundException if no calendar with the specified name exists
   */
  public Calendar getCalendar(String name) throws CalendarNotFoundException {
    return calendarRegistry.getCalendarByName(name);
  }

  /**
   * Gets the currently active calendar.
   *
   * @return the active calendar
   * @throws CalendarNotFoundException if no calendar is currently active
   */
  public Calendar getActiveCalendar() throws CalendarNotFoundException {
    return calendarRegistry.getActiveCalendar();
  }

  /**
   * Executes an operation on a calendar by name and returns a result.
   *
   * @param <T>          the result type
   * @param calendarName the name of the calendar
   * @param operation    the operation to execute
   * @return the result of the operation
   * @throws CalendarNotFoundException if the calendar cannot be found
   * @throws Exception                 if the operation throws an exception
   */
  public <T> T executeOnCalendar(String calendarName, CalendarOperation<T> operation)
          throws CalendarNotFoundException, Exception {
    Calendar calendar = calendarRegistry.getCalendarByName(calendarName);
    return operation.execute(calendar);
  }

  /**
   * Sets the active calendar by name.
   *
   * @param name the name of the calendar to set as active
   * @throws CalendarNotFoundException if no calendar with the specified name exists
   */
  public void setActiveCalendar(String name) throws CalendarNotFoundException {
    calendarRegistry.setActiveCalendar(name);
  }

  /**
   * Checks if the specified calendar name exists.
   *
   * @param name the calendar name to check
   * @return true if a calendar with the specified name exists, false otherwise
   */
  public boolean hasCalendar(String name) {
    return calendarRegistry.hasCalendar(name);
  }

  /**
   * Edits a calendar's timezone.
   *
   * @param calendarName the name of the calendar
   * @param newTimezone  the new timezone for the calendar
   * @throws CalendarNotFoundException if no calendar with the specified name exists
   * @throws InvalidTimezoneException  if the timezone is invalid
   */
  public void editCalendarTimezone(String calendarName, String newTimezone)
          throws CalendarNotFoundException, InvalidTimezoneException {
    if (!timezoneHandler.isValidTimezone(newTimezone)) {
      throw new InvalidTimezoneException("Invalid timezone: " + newTimezone);
    }
    calendarRegistry.applyToCalendar(calendarName, calendar -> calendar
            .setTimezone(newTimezone));
  }

  /**
   * Edits a calendar's name.
   *
   * @param oldName the current name of the calendar
   * @param newName the new name for the calendar
   * @throws CalendarNotFoundException if no calendar with the specified name exists
   * @throws IllegalArgumentException if a calendar with the new name already exists
   */
  public void editCalendarName(String oldName, String newName)
          throws CalendarNotFoundException {
    if (!calendarRegistry.hasCalendar(oldName)) {
      throw new CalendarNotFoundException("Calendar not found: " + oldName);
    }
    if (calendarRegistry.hasCalendar(newName)) {
      throw new IllegalArgumentException("Calendar with name '" + newName + "' already exists");
    }
    
    // Validate the new name
    try {
      CalendarNameValidator.validateCalendarName(newName);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid new calendar name: " + e.getMessage());
    }
    
    // Update the calendar name in the registry
    calendarRegistry.updateCalendarName(oldName, newName);
  }

  /**
   * Gets the timezone handler.
   *
   * @return the timezone handler
   */
  public TimeZoneHandler getTimezoneHandler() {
    return timezoneHandler;
  }

  /**
   * Gets all calendar names.
   *
   * @return a set of calendar names
   */
  public Set<String> getCalendarNames() {
    return calendarRegistry.getCalendarNames();
  }

  /**
   * Gets an iterator for all calendars in this manager.
   *
   * @return an iterator for all calendars
   */
  public ConsolidatedIterator.ICalendarIterator getCalendarIterator() {
    return calendarRegistry.getCalendarIterator();
  }

  /**
   * Gets the number of calendars.
   *
   * @return the number of calendars
   */
  public int getCalendarCount() {
    return calendarRegistry.getCalendarCount();
  }

  /**
   * Gets the calendar registry.
   *
   * @return the calendar registry
   */
  public CalendarRegistry getCalendarRegistry() {
    return calendarRegistry;
  }
}