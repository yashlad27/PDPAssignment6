package model.factory;

import controller.CalendarController;
import controller.ICommandFactory;
import controller.command.UnifiedCommandFactory;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import utilities.TimeZoneHandler;
import view.ConsoleView;
import view.ICalendarView;

/**
 * Factory class that creates and provides dependencies for the calendar application.
 * This class centralizes the creation of application components and follows the
 * Factory pattern to support Dependency Inversion.
 */
public class CalendarFactory {

  /**
   * Creates a view implementation.
   *
   * @return an implementation of ICalendarView
   */
  public ICalendarView createView() {
    return new ConsoleView();
  }

  /**
   * Creates a timezone handler.
   *
   * @return a TimeZoneHandler instance
   */
  public TimeZoneHandler createTimeZoneHandler() {
    return new TimeZoneHandler();
  }

  /**
   * Creates a calendar manager with the given timezone handler.
   *
   * @param timezoneHandler the timezone handler to use
   * @return a CalendarManager instance
   * @throws IllegalArgumentException if timezoneHandler is null
   */
  public CalendarManager createCalendarManager(TimeZoneHandler timezoneHandler) {
    if (timezoneHandler == null) {
      throw new IllegalArgumentException("TimeZoneHandler cannot be null");
    }
    return new CalendarManager.Builder().timezoneHandler(timezoneHandler).build();
  }

  /**
   * Creates a unified command factory for all commands.
   *
   * @param calendar the calendar to operate on
   * @param calendarManager the calendar manager
   * @param view the view to interact with
   * @return an ICommandFactory for all commands
   */
  public ICommandFactory createCommandFactory(ICalendar calendar, CalendarManager calendarManager, ICalendarView view) {
    return new UnifiedCommandFactory(calendar, calendarManager, view);
  }

  /**
   * Creates a calendar controller with all its dependencies.
   *
   * @param commandFactory the unified command factory
   * @param calendarManager the calendar manager
   * @param view the view to interact with
   * @return a CalendarController instance
   */
  public CalendarController createController(ICommandFactory commandFactory,
                                             CalendarManager calendarManager,
                                             ICalendarView view) {
    return new CalendarController(commandFactory, calendarManager, view);
  }
}