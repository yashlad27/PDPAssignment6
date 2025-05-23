package controller.command.event;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import controller.ICommandFactory;
import controller.command.ICommand;
import controller.command.create.CreateEventCommand;
import controller.command.edit.EditEventCommand;
import model.calendar.ICalendar;
import view.ICalendarView;

/**
 * Factory for creating and registering commands using functional interfaces.
 * Implements ICommandFactory to allow for dependency inversion.
 */
public class CommandFactory implements ICommandFactory {

  private final Map<String, Function<String[], String>> commands;
  private final ICalendar calendar;
  private final ICalendarView view;

  /**
   * Constructs a new CommandFactory and registers all available commands.
   *
   * @param calendar the calendar model
   * @param view     the view for user interaction
   */
  public CommandFactory(ICalendar calendar, ICalendarView view) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }

    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }

    this.commands = new HashMap<>();
    this.calendar = calendar;
    this.view = view;

    registerCommands();
  }

  /**
   * Registers all command executors.
   */
  private void registerCommands() {
    registerCreateCommand();
    registerEditCommand();
    registerUseCommand();

    commands.put("print", new PrintEventsCommand(calendar)::execute);

    commands.put("show", new ShowStatusCommand(calendar)::execute);

    commands.put("export", new ExportCalendarCommand(calendar)::execute);

    commands.put("import", new ImportCalendarCommand(calendar)::execute);

    commands.put("copy", args -> "Command forwarded to CalendarCommandFactory");

    commands.put("exit", args -> "Exiting application.");
  }

  /**
   * Registers the create command with all its subcommands.
   */
  private void registerCreateCommand() {
    CreateEventCommand createCmd = new CreateEventCommand(calendar);
    commands.put("create", createCmd::execute);
  }

  /**
   * Registers the edit command with all its subcommands.
   */
  private void registerEditCommand() {
    EditEventCommand editCmd = new EditEventCommand(calendar);

    commands.put("edit", (args) -> {
      if (args.length > 0 && args[0].equals("calendar")) {
        return "Command forwarded to CalendarCommandFactory";
      }
      return editCmd.execute(args);
    });
  }

  /**
   * Registers the use command for selecting a calendar.
   */
  private void registerUseCommand() {
    commands.put("use", (args) -> {
      return "Command forwarded to CalendarCommandFactory";
    });
  }

  /**
   * Gets a command executor by name.
   *
   * @param name the name of the command
   * @return the command executor, or null if not found
   */
  public Function<String[], String> getCommandExecutor(String name) {
    return commands.get(name);
  }

  /**
   * Checks if a command is registered.
   *
   * @param name the name of the command
   * @return true if the command is registered, false otherwise
   */
  @Override
  public boolean hasCommand(String name) {
    return commands.containsKey(name);
  }

  /**
   * Gets a command by name.
   *
   * @param commandName the name of the command
   * @return the command, or null if not found
   */
  @Override
  public ICommand getCommand(String commandName) {
    Function<String[], String> executor = getCommandExecutor(commandName);
    if (executor == null) {
      return null;
    }
    return ICommand.fromExecutor(commandName, executor);
  }

  /**
   * Gets the calendar instance.
   *
   * @return the calendar instance
   */
  public ICalendar getCalendar() {
    return calendar;
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