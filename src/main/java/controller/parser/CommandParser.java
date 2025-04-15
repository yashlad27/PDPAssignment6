package controller.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import controller.ICommandFactory;
import controller.command.ICommand;

/**
 * A robust command parser that handles the parsing and interpretation of user commands
 * in the calendar application. This parser supports multiple command formats
 * including calendar management, event creation/modification, and various utility operations.
 * The parser uses regular expressions to match command patterns and delegates the command execution
 * to appropriate command handlers through a command factory.
 *
 * <p>Supported command categories include:
 * <ul>
 *   <li>Calendar Management (create, edit, use)</li>
 *   <li>Event Management (create, edit, copy)</li>
 *   <li>Event Queries (print, show status)</li>
 *   <li>System Commands (exit)</li>
 * </ul></p>
 *
 * <p>The parser is designed to be extensible, allowing new command patterns to be registered
 * at runtime through the {@link #registerPattern} method.</p>
 */
public class CommandParser {

  private final ICommandFactory commandFactory;
  private final Map<String, CommandPattern> commandPatterns;
  private static final List<String> VALID_COMMANDS = Arrays.asList("create", "use", "show", "edit",
          "copy", "exit", "print", "export");
  private static final Set<String> VALID_COMMANDS_SET = new HashSet<>(VALID_COMMANDS);

  /**
   * Constructs a new CommandParser with the specified command factory.
   * Initializes the command patterns and prepares the parser for use.
   *
   * @param commandFactory An implementation of ICommandFactory that creates command objects
   * @throws IllegalArgumentException if commandFactory is null
   */
  public CommandParser(ICommandFactory commandFactory) {
    if (commandFactory == null) {
      throw new IllegalArgumentException("Command factory cannot be null");
    }
    this.commandFactory = commandFactory;
    this.commandPatterns = new HashMap<>();
    initializePatterns();
  }

  /**
   * Initializes the default command patterns supported by the parser.
   * This method sets up regular expressions for matching various command formats and
   * associates them with their respective parsing functions.
   *
   * <p>Supported command patterns include:
   * <ul>
   *   <li>Calendar creation and modification</li>
   *   <li>Event creation (single, recurring, all-day)</li>
   *   <li>Event modification and copying</li>
   *   <li>Event querying and status checks</li>
   * </ul></p>
   */
  private void initializePatterns() {
    // Create calendar pattern
    registerPattern("create_calendar", Pattern.compile(
                    "create calendar --name ([\"']?[^\"']+[\"']?|[^\\s]+) "
                            + "--timezone ([\\w/]+)"),
            this::parseCreateCalendarCommand);

    // Create event pattern
    registerPattern("create_event", Pattern.compile(
                    "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) "
                            + "from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                            + "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
                            + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")"
                            + "?(?:\\s+(private))?"),
            this::parseCreateEventCommand);

    // Create recurring event pattern
    registerPattern("create_recurring_event", Pattern.compile(
                    "create event ([\"']?[^\"']+[\"']?|[^\\s]+) "
                            + "from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                            + "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                            + "repeats ([MTWRFSU]+) for (\\d+) times"
                            + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")?"),
            this::parseCreateRecurringEventCommand);

    // Create all-day event pattern
    registerPattern("create_all_day_event", Pattern.compile(
                    "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) on"
                            + " (\\d{4}-\\d{2}-\\d{2})"
                            + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")"
                            + "?(?:\\s+(private))?"),
            this::parseCreateAllDayEventCommand);

    // Create event patterns
    registerPattern("create_recurring_until_event", Pattern.compile(
                    "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) from "
                            + "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to "
                            + "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                            + "repeats ([MTWRFSU]+) until (\\d{4}-\\d{2}-\\d{2})"
                            + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s"
                            + "+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateRecurringUntilEventCommand);

    registerPattern("create_all_day_recurring_event", Pattern.compile(
                    "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) "
                            + "on (\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+) for (\\d+) times"
                            + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s"
                            + "+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateAllDayRecurringEventCommand);

    registerPattern("create_all_day_recurring_until_event", Pattern.compile(
                    "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) "
                            + "on (\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+)"
                            + " until (\\d{4}-\\d{2}-\\d{2})"
                            + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s"
                            + "+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateAllDayRecurringUntilEventCommand);

    registerPattern("edit_single_event",
            Pattern.compile("edit event (\\w+) \"([^\"]+)\" from (\\S+T\\S+) "
                    + "with \"?([^\"]+)\"?"),
            this::parseEditSingleEventCommand);

    registerPattern("edit_event_time", Pattern.compile(
                    "edit event (\\w+) \"([^\"]+)\" from (\\S+T\\S+) "
                            + "to (\\S+T\\S+) with \"?([^\"]+)\"?"),
            this::parseEditEventTimeCommand);

    registerPattern("print_events_date", Pattern.compile("print events "
                    + "on (\\d{4}-\\d{2}-\\d{2})"),
            this::parsePrintEventsDateCommand);

    registerPattern("print_events_range",
            Pattern.compile("print events from (\\d{4}-\\d{2}-\\d{2}(?:T\\d{2}:\\d{2})?) "
                    + "to (\\d{4}-\\d{2}-\\d{2}(?:T\\d{2}:\\d{2})?)"),
            this::parsePrintEventsRangeCommand);

    // Edit calendar pattern
    registerPattern("edit_calendar",
            Pattern.compile("edit calendar --name ([\\w-]+) --property (\\w+) ([\\w/]+)"),
            this::parseEditCalendarCommand);

    // Use calendar pattern
    registerPattern("use_calendar",
            Pattern.compile("use calendar --name ([\"']?[^\"']+[\"']?|[^\\s]+)"),
            this::parseUseCalendarCommand);

    // Copy single event pattern
    registerPattern("copy_event", Pattern.compile(
                    "copy event ([\"']?[^\"']+[\"']?|[^\\s]+) "
                            + "on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                            + "--target ([\\w-]+) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"),
            this::parseCopyEventCommand);

    // Copy events on date pattern
    registerPattern("copy_events_on_date", Pattern.compile(
                    "copy events on (\\d{4}-\\d{2}-\\d{2}) --target ([\\w-]+) "
                            + "to (\\d{4}-\\d{2}-\\d{2})"),
            this::parseCopyEventsOnDateCommand);

    // Copy events between dates pattern
    registerPattern("copy_events_between_dates", Pattern.compile(
                    "copy events between (\\d{4}-\\d{2}-\\d{2}) "
                            + "and (\\d{4}-\\d{2}-\\d{2}) "
                            + "--target ([\\w-]+) to (\\d{4}-\\d{2}-\\d{2})"),
            this::parseCopyEventsBetweenDatesCommand);

    registerPattern("show_status",
            Pattern.compile("show status on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"),
            this::parseShowStatusCommand);

    registerPattern("export_calendar", Pattern.compile("export cal (.+)"),
            this::parseExportCommand);

    registerPattern("exit", Pattern.compile("exit"), this::parseExitCommand);
  }

  /**
   * Registers a new command pattern with the parser.
   * This method allows extending the parser's capabilities by adding new command formats.
   *
   * @param name    The unique identifier for this command pattern
   * @param pattern The regular expression pattern that matches the command format
   * @param parser  The function that parses the matched command into executable form
   * @throws IllegalArgumentException if any parameter is null
   */
  public void registerPattern(String name, Pattern pattern, CommandPatternParser parser) {
    if (name == null || pattern == null || parser == null) {
      throw new IllegalArgumentException("Pattern registration parameters cannot be null");
    }
    commandPatterns.put(name, new CommandPattern(pattern, parser));
  }

  /**
   * Parses a command string into an executable command with its arguments.
   * This method validates the command format and delegates to appropriate pattern parsers.
   *
   * @param commandString The raw command string to parse
   * @return A CommandWithArgs object containing the executable command and its arguments
   * @throws IllegalArgumentException if the command is invalid, empty, or unsupported
   */
  public CommandWithArgs parseCommand(String commandString) {
    if (commandString == null || commandString.trim().isEmpty()) {
      throw new IllegalArgumentException("Command cannot be empty");
    }

    commandString = commandString.trim().replaceAll("\\s+", " ");

    String[] parts = commandString.split("\\s+", 2);
    if (parts.length == 0) {
      throw new IllegalArgumentException("Invalid command format");
    }

    String command = parts[0].toLowerCase();

    if (!VALID_COMMANDS_SET.contains(command)) {
      throw new IllegalArgumentException(
              "Invalid command: " + command + ". Valid commands are: " + String.join(", ",
                      VALID_COMMANDS));
    }

    for (Map.Entry<String, CommandPattern> entry : commandPatterns.entrySet()) {
      Matcher matcher = entry.getValue().getPattern().matcher(commandString);
      if (matcher.matches()) {
        return entry.getValue().getParser().parse(matcher);
      }
    }

    throw new IllegalArgumentException("Invalid command format");
  }

  /**
   * Removes surrounding quotes (single or double) from a string value.
   * This helper method ensures consistent handling of quoted parameters in commands.
   *
   * @param value The string that might have surrounding quotes
   * @return The string with quotes removed, or the original string if no quotes present
   */
  private String removeQuotes(String value) {
    if (value == null) {
      return null;
    }

    if ((value.startsWith("\"") && value.endsWith("\"")) ||
            (value.startsWith("'") && value.endsWith("'"))) {
      return value.substring(1, value.length() - 1);
    }

    return value;
  }

  /**
   * Parse create event command.
   */
  private CommandWithArgs parseCreateEventCommand(Matcher matcher) {
    boolean autoDecline = matcher.group(1) != null;
    String eventName = removeQuotes(matcher.group(2));
    String startTime = matcher.group(3);
    String endTime = matcher.group(4);
    String description = matcher.group(5);
    String location = matcher.group(6);
    boolean isPrivate = matcher.group(7) != null;

    String[] args = {"single", eventName, startTime, endTime, description, location,
            String.valueOf(!isPrivate),
            String.valueOf(autoDecline)
    };
    return new CommandWithArgs(commandFactory.getCommand("create"), args);
  }

  /**
   * Parse create all day event command.
   */
  private CommandWithArgs parseCreateAllDayEventCommand(Matcher matcher) {
    boolean autoDecline = matcher.group(1) != null;
    String eventName = removeQuotes(matcher.group(2));
    String date = matcher.group(3);
    String description = matcher.group(4);
    String location = matcher.group(5);
    boolean isPrivate = matcher.group(6) != null;

    String[] args = {"allday", eventName, date, description, location, String.valueOf(!isPrivate),
            String.valueOf(autoDecline)
    };
    return new CommandWithArgs(commandFactory.getCommand("create"), args);
  }

  /**
   * Parse create recurring event command.
   */
  private CommandWithArgs parseCreateRecurringEventCommand(Matcher matcher) {
    String eventName = removeQuotes(matcher.group(1));
    String startTime = matcher.group(2);
    String endTime = matcher.group(3);
    String weekdays = matcher.group(4);
    String occurrences = matcher.group(5);
    String description = matcher.group(6);
    String location = matcher.group(7);

    String[] args = {"recurring", eventName, startTime, endTime, weekdays, occurrences,
            description != null ? description : "", location != null ? location : "",
            "true", "false"
    };
    return new CommandWithArgs(commandFactory.getCommand("create"), args);
  }

  /**
   * Parse create recurring until event command.
   */
  private CommandWithArgs parseCreateRecurringUntilEventCommand(Matcher matcher) {
    ICommand createCommand = commandFactory.getCommand("create");

    boolean autoDecline = matcher.group(1) != null;

    String eventName = removeQuotes(matcher.group(2));

    String[] args = {"recurring-until", eventName, matcher.group(3), matcher.group(4),
            matcher.group(5), matcher.group(6), String.valueOf(autoDecline), matcher.group(7),
            matcher.group(8), matcher.group(9) != null ? "false" : "true"};
    return new CommandWithArgs(createCommand, args);
  }

  /**
   * Parse create all day recurring event command.
   */
  private CommandWithArgs parseCreateAllDayRecurringEventCommand(Matcher matcher) {
    ICommand createCommand = commandFactory.getCommand("create");

    boolean autoDecline = matcher.group(1) != null;

    String eventName = removeQuotes(matcher.group(2));

    String[] args = {"allday-recurring", eventName, matcher.group(3), matcher.group(4),
            matcher.group(5), String.valueOf(autoDecline), matcher.group(6), matcher.group(7),
            matcher.group(8) != null ? "false" : "true"};
    return new CommandWithArgs(createCommand, args);
  }

  /**
   * Parse create all day recurring until event command.
   */
  private CommandWithArgs parseCreateAllDayRecurringUntilEventCommand(Matcher matcher) {
    ICommand createCommand = commandFactory.getCommand("create");

    boolean autoDecline = matcher.group(1) != null;

    String eventName = removeQuotes(matcher.group(2));

    String[] args = {"allday-recurring-until", eventName, matcher.group(3), matcher.group(4),
            matcher.group(5), String.valueOf(autoDecline), matcher.group(6), matcher.group(7),
            matcher.group(8) != null ? "false" : "true"};
    return new CommandWithArgs(createCommand, args);
  }

  /**
   * Parse edit single event command.
   */
  private CommandWithArgs parseEditSingleEventCommand(Matcher matcher) {
    ICommand editCommand = commandFactory.getCommand("edit");

    String property = matcher.group(1);
    String subject = matcher.group(2);
    String startDateTime = matcher.group(3);
    String newValue = matcher.group(4);

    String[] args = {"single", property, subject, startDateTime, newValue};
    return new CommandWithArgs(editCommand, args);
  }

  /**
   * Parse print events on date command.
   */
  private CommandWithArgs parsePrintEventsDateCommand(Matcher matcher) {
    ICommand printCommand = commandFactory.getCommand("print");

    String[] args = {"on_date", matcher.group(1)};
    return new CommandWithArgs(printCommand, args);
  }

  /**
   * Parse print events range command.
   */
  private CommandWithArgs parsePrintEventsRangeCommand(Matcher matcher) {
    ICommand printCommand = commandFactory.getCommand("print");

    String[] args = {"date_range", matcher.group(1), matcher.group(2)};
    return new CommandWithArgs(printCommand, args);
  }

  private CommandWithArgs parseCreateCalendarCommand(Matcher matcher) {
    String calendarName = removeQuotes(matcher.group(1));
    String timezone = matcher.group(2);
    return new CommandWithArgs(commandFactory.getCommand("create"),
            new String[]{"calendar", "--name", calendarName, "--timezone", timezone});
  }

  private CommandWithArgs parseEditCalendarCommand(Matcher matcher) {
    ICommand calendarCommand = commandFactory.getCommand("edit");

    String[] args = {"calendar", "--name", matcher.group(1), "--property", matcher.group(2),
            matcher.group(3)};
    return new CommandWithArgs(calendarCommand, args);
  }

  private CommandWithArgs parseUseCalendarCommand(Matcher matcher) {
    ICommand calendarCommand = commandFactory.getCommand("use");

    String calendarName = matcher.group(1);
    if (calendarName == null || calendarName.equals("null")) {
      throw new IllegalArgumentException("Calendar name cannot be null");
    }

    calendarName = removeQuotes(calendarName);

    String[] args = {"calendar", "--name", calendarName};
    return new CommandWithArgs(calendarCommand, args);
  }

  private CommandWithArgs parseCopyEventCommand(Matcher matcher) {
    ICommand copyCommand = commandFactory.getCommand("copy");

    String eventName = removeQuotes(matcher.group(1));

    String[] args = {"copy", "event", eventName, "on", matcher.group(2), "--target",
                     matcher.group(3), "to", matcher.group(4)};
    return new CommandWithArgs(copyCommand, args);
  }

  private CommandWithArgs parseCopyEventsOnDateCommand(Matcher matcher) {
    ICommand copyCommand = commandFactory.getCommand("copy");

    String[] args = {"copy", "events", "on", matcher.group(1), "--target", matcher.group(2), "to",
                     matcher.group(3)};
    return new CommandWithArgs(copyCommand, args);
  }

  private CommandWithArgs parseCopyEventsBetweenDatesCommand(Matcher matcher) {
    ICommand copyCommand = commandFactory.getCommand("copy");

    String[] args = {"copy", "events", "between", matcher.group(1), "and", matcher.group(2),
                     "--target", matcher.group(3), "to", matcher.group(4)};
    return new CommandWithArgs(copyCommand, args);
  }

  /**
   * Parse show status command.
   */
  private CommandWithArgs parseShowStatusCommand(Matcher matcher) {
    ICommand statusCommand = commandFactory.getCommand("show");

    String dateTime = matcher.group(1);
    if (dateTime == null || dateTime.trim().isEmpty()) {
      throw new IllegalArgumentException(
              "Invalid date time format. Expected format: YYYY-MM-DDThh:mm");
    }

    if (!dateTime.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")) {
      throw new IllegalArgumentException(
              "Invalid date time format. Expected format: YYYY-MM-DDThh:mm");
    }

    String[] args = {dateTime.trim()};
    return new CommandWithArgs(statusCommand, args);
  }

  /**
   * Parse export command.
   */
  private CommandWithArgs parseExportCommand(Matcher matcher) {
    ICommand exportCommand = commandFactory.getCommand("export");

    String filePath = matcher.group(1);
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("Export file path cannot be empty");
    }

    String[] args = {filePath.trim()};
    return new CommandWithArgs(exportCommand, args);
  }

  /**
   * Parse exit command.
   */
  private CommandWithArgs parseExitCommand(Matcher matcher) {
    ICommand exitCommand = commandFactory.getCommand("exit");
    return new CommandWithArgs(exitCommand, new String[0]);
  }

  private CommandWithArgs parseEditEventTimeCommand(Matcher matcher) {
    ICommand editCommand = commandFactory.getCommand("edit");

    String property = matcher.group(1);
    String subject = matcher.group(2);
    String startDateTime = matcher.group(3);
    String newValue = matcher.group(5);

    String[] args = {"single", property, subject, startDateTime, newValue};
    return new CommandWithArgs(editCommand, args);
  }

  /**
   * Represents a command along with its arguments, providing a way to execute the command.
   * This class encapsulates the relationship between a command and its parameters.
   */
  public static class CommandWithArgs {

    private final ICommand command;
    private final String[] args;

    /**
     * Creates a new command with its arguments.
     *
     * @param command The command to execute
     * @param args    The arguments for the command
     * @throws IllegalArgumentException if command is null
     */
    public CommandWithArgs(ICommand command, String[] args) {
      if (command == null) {
        throw new IllegalArgumentException("Command cannot be null");
      }
      this.command = command;
      this.args = args != null ? args : new String[0];
    }

    /**
     * Gets the command object.
     *
     * @return The command object
     */
    public ICommand getCommand() {
      return command;
    }

    /**
     * Gets the command arguments.
     *
     * @return Array of command arguments
     */
    public String[] getArgs() {
      return args.clone(); // Return a copy to prevent modification
    }

    /**
     * Executes the command with its arguments.
     *
     * @return The result of command execution
     * @throws Exception if command execution fails
     */
    public String execute() throws Exception {
      return command.execute(args);
    }
  }

  /**
   * Encapsulates a command pattern with its matching regex and parsing function.
   * This class helps organize the relationship between command formats and their parsers.
   */
  private static class CommandPattern {

    private final Pattern pattern;
    private final CommandPatternParser parser;

    /**
     * Creates a new command pattern.
     *
     * @param pattern The regex pattern for matching commands
     * @param parser  The function to parse matched commands
     * @throws IllegalArgumentException if either parameter is null
     */
    public CommandPattern(Pattern pattern, CommandPatternParser parser) {
      if (pattern == null || parser == null) {
        throw new IllegalArgumentException("Pattern and parser cannot be null");
      }
      this.pattern = pattern;
      this.parser = parser;
    }

    /**
     * Gets the regex pattern.
     *
     * @return The command's regex pattern
     */
    public Pattern getPattern() {
      return pattern;
    }

    /**
     * Gets the command parser.
     *
     * @return The command's parser function
     */
    public CommandPatternParser getParser() {
      return parser;
    }
  }
}