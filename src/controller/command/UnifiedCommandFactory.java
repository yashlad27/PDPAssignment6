package controller.command;

import java.util.HashMap;
import java.util.Map;

import controller.ICommandFactory;
import controller.command.copy.CopyEventCommand;
import controller.command.create.CreateEventCommand;
import controller.command.edit.EditEventCommand;
import controller.command.event.ExportCalendarCommand;
import controller.command.event.PrintEventsCommand;
import controller.command.event.ShowStatusCommand;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import utilities.TimeZoneHandler;
import view.ICalendarView;

/**
 * Unified factory for creating and managing all calendar and event commands.
 * This factory handles operations for both calendar management and event management.
 */
public class UnifiedCommandFactory implements ICommandFactory {

  private final Map<String, CommandExecutor> commands;
  private final ICalendar calendar;
  private final CalendarManager calendarManager;
  private final ICalendarView view;
  private final CopyEventCommand copyEventCommand;

  /**
   * Creates a UnifiedCommandFactory with required dependencies.
   *
   * @param calendar        the current calendar model
   * @param calendarManager the calendar manager
   * @param view            the view for user interaction
   * @throws IllegalArgumentException if any parameter is null
   */
  public UnifiedCommandFactory(ICalendar calendar, CalendarManager calendarManager, ICalendarView view) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    if (calendarManager == null) {
      throw new IllegalArgumentException("CalendarManager cannot be null");
    }
    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }

    this.commands = new HashMap<>();
    this.calendar = calendar;
    this.calendarManager = calendarManager;
    this.view = view;
    TimeZoneHandler timezoneHandler = calendarManager.getTimezoneHandler();
    this.copyEventCommand = new CopyEventCommand(calendarManager, timezoneHandler);

    registerCommands();
  }

  /**
   * Registers all available commands.
   */
  private void registerCommands() {
    // Event commands
    registerEventCommands();

    // Calendar commands
    registerCalendarCommands();
  }

  /**
   * Registers event-related commands.
   */
  private void registerEventCommands() {
    // Create event command
    CreateEventCommand createCmd = new CreateEventCommand(calendar);
    commands.put("create", createCmd::execute);

    // Edit event command
    EditEventCommand editCmd = new EditEventCommand(calendar);
    commands.put("edit", editCmd::execute);

    // Print events command
    commands.put("print", new PrintEventsCommand(calendar)::execute);

    // Show status command
    commands.put("show", new ShowStatusCommand(calendar)::execute);

    // Export calendar command
    commands.put("export", new ExportCalendarCommand(calendar)::execute);
  }

  /**
   * Registers calendar-related commands.
   */
  private void registerCalendarCommands() {
    // Create calendar command
    commands.put("create", args -> {
      if (args.length < 5) {
        return "Error: Insufficient arguments for create calendar command";
      }
      String calendarName = args[2];
      String timezone = args[4];

      if (calendarName.length() > 100) {
        return "Error: Calendar name cannot exceed 100 characters";
      }

      try {
        calendarManager.createCalendar(calendarName, timezone);
        return "Calendar '" + calendarName + "' created successfully with timezone " + timezone;
      } catch (DuplicateCalendarException | InvalidTimezoneException e) {
        return e.getMessage();
      }
    });

    // Edit calendar command
    commands.put("edit", args -> {
      if (args.length < 6) {
        return "Error: Insufficient arguments for edit calendar command";
      }
      String calendarName = args[2];
      String property = args[4];
      String value = args[5];

      try {
        if (property.equals("timezone")) {
          calendarManager.editCalendarTimezone(calendarName, value);
          return "Timezone updated to " + value + " for calendar '" + calendarName + "'";
        }
        return "Error: Invalid property '" + property + "' for calendar edit";
      } catch (CalendarNotFoundException | InvalidTimezoneException e) {
        return e.getMessage();
      }
    });

    // Use calendar command
    commands.put("use", args -> {
      if (args.length < 3) {
        return "Error: Insufficient arguments for use calendar command";
      }
      String calendarName = args[2];

      try {
        calendarManager.setActiveCalendar(calendarName);
        return "Now using calendar: '" + calendarName + "'";
      } catch (CalendarNotFoundException e) {
        return e.getMessage();
      }
    });

    // Copy command
    commands.put("copy", args -> {
      try {
        return copyEventCommand.execute(args);
      } catch (Exception e) {
        return "Error: " + e.getMessage();
      }
    });
  }

  @Override
  public boolean hasCommand(String commandName) {
    return commands.containsKey(commandName);
  }

  @Override
  public ICommand getCommand(String commandName) {
    if (commandName == null) {
      return null;
    }

    CommandExecutor executor = commands.get(commandName);
    if (executor == null) {
      return null;
    }

    return new CommandAdapter(commandName, executor);
  }

  /**
   * Gets the current calendar instance.
   *
   * @return the calendar instance
   */
  public ICalendar getCalendar() {
    return calendar;
  }

  /**
   * Gets the calendar manager instance.
   *
   * @return the calendar manager instance
   */
  public CalendarManager getCalendarManager() {
    return calendarManager;
  }

  /**
   * Gets the view instance.
   *
   * @return the view instance
   */
  public ICalendarView getView() {
    return view;
  }
} 