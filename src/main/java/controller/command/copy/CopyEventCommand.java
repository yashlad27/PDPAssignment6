package controller.command.copy;

import controller.command.ICommand;
import controller.command.copy.strategy.ConsolidatedCopyStrategy;
import controller.command.copy.strategy.CopyStrategy;
import model.calendar.CalendarManager;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import utilities.TimeZoneHandler;

/**
 * Command for copying events between calendars.
 * Uses the Strategy pattern to handle different copy operations.
 */
public class CopyEventCommand implements ICommand {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;

  /**
   * Constructs a new CopyEventCommand.
   *
   * @param calendarManager the calendar manager
   * @param timezoneHandler the timezone handler
   */
  public CopyEventCommand(CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
    if (calendarManager == null) {
      throw new IllegalArgumentException("CalendarManager cannot be null");
    }
    if (timezoneHandler == null) {
      throw new IllegalArgumentException("TimeZoneHandler cannot be null");
    }

    this.calendarManager = calendarManager;
    this.timezoneHandler = timezoneHandler;
  }

  @Override
  public String execute(String[] args) throws ConflictingEventException, InvalidEventException,
          EventNotFoundException {
    if (args.length < 1) {
      return "Error: Insufficient arguments for copy command";
    }

    try {
      CopyStrategy strategy = null;
      
      // Determine the appropriate strategy based on the command format
      if (args.length >= 2 && args[1].equals("event")) {
        strategy = ConsolidatedCopyStrategy.createSingleEventStrategy(
                calendarManager, timezoneHandler, args);
      } else if (args.length >= 3 && args[1].equals("events") && args[2].equals("on")) {
        strategy = ConsolidatedCopyStrategy.createDayEventsStrategy(
                calendarManager, timezoneHandler, args);
      } else if (args.length >= 3 && args[1].equals("events") && args[2].equals("between")) {
        strategy = ConsolidatedCopyStrategy.createRangeEventsStrategy(
                calendarManager, timezoneHandler, args);
      }

      if (strategy == null) {
        return "Error: Unknown copy command format";
      }

      return strategy.execute(args);

    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return "copy";
  }
}