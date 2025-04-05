package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import controller.command.event.CommandFactory;
import controller.parser.CommandParser;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.CalendarNotFoundException;
import view.ICalendarView;

/**
 * Controller class that manages calendar operations and user interactions.
 *
 * <p>This class serves as the main controller in the MVC architecture, handling
 * Command processing and execution, Calendar management operations, Event management operations and
 * Interactive and headless mode operations
 *
 * <p>The controller supports two modes of operation:
 * 1. Interactive Mode: Processes commands entered by users in real-time 2. Headless Mode: Processes
 * commands from a file without user interaction
 *
 * <p>The controller uses command factories to create appropriate command objects
 * for both calendar-level operations (e.g., creating calendars) and event-level operations (e.g.,
 * creating events).
 */
public class CalendarController {

  private ICalendarView view;
  private CommandParser parser;
  private ICommandFactory calendarCommandFactory;
  private final CalendarManager calendarManager;
  private ICommandFactory commandFactory;
  private static final String EXIT_COMMAND = "exit";
  private static final Set<String> VALID_COMMANDS = new HashSet<>(
          Arrays.asList("create", "use", "show", "edit", "copy", "exit"));

  /**
   * Constructs a new CalendarController with all necessary dependencies.
   *
   * <p>This constructor follows the Dependency Injection pattern to maintain
   * loose coupling between components. It validates all dependencies to ensure the controller is
   * properly initialized.
   *
   * @param commandFactory         Factory for creating event-related commands
   * @param calendarCommandFactory Factory for creating calendar-related commands
   * @param calendarManager        Manager for calendar operations
   * @param view                   View component for user interaction
   * @throws IllegalArgumentException if calendarManager is null
   */
  public CalendarController(ICommandFactory commandFactory, ICommandFactory calendarCommandFactory,
                            CalendarManager calendarManager, ICalendarView view) {
    if (calendarManager == null) {
      throw new IllegalArgumentException("CalendarManager cannot be null");
    }

    this.calendarManager = calendarManager;
    this.commandFactory = commandFactory;
    this.calendarCommandFactory = calendarCommandFactory;
    this.view = view;
    if (commandFactory != null) {
      this.parser = new CommandParser(commandFactory);
    }
  }

  /**
   * Sets the event command factory.
   *
   * @param commandFactory the command factory to set
   */
  public void setEventCommandFactory(ICommandFactory commandFactory) {
    this.commandFactory = commandFactory;
    this.parser = new CommandParser(commandFactory);
  }

  /**
   * Sets the calendar command factory.
   *
   * @param calendarCommandFactory the calendar command factory to set
   */
  public void setCalendarCommandFactory(ICommandFactory calendarCommandFactory) {
    this.calendarCommandFactory = calendarCommandFactory;
  }

  /**
   * Sets the view.
   *
   * @param view the view to set
   */
  public void setView(ICalendarView view) {
    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }
    this.view = view;
  }

  /**
   * Gets the currently active calendar.
   *
   * @return the currently active calendar
   * @throws CalendarNotFoundException if no calendar is currently active
   */
  public ICalendar getCurrentCalendar() throws CalendarNotFoundException {
    return calendarManager.getActiveCalendar();
  }

  /**
   * Gets a calendar by name.
   *
   * @param name the name of the calendar to get
   * @return the calendar with the given name
   * @throws CalendarNotFoundException if no calendar with the given name exists
   */
  public ICalendar getCalendar(String name) throws CalendarNotFoundException {
    return calendarManager.getCalendar(name);
  }
  
  /**
   * Gets the calendar manager.
   *
   * @return the calendar manager
   */
  public CalendarManager getCalendarManager() {
    return calendarManager;
  }
  
  /**
   * Gets all calendar names.
   *
   * @return a set of all calendar names
   */
  public Set<String> getCalendarNames() {
    return calendarManager.getCalendarRegistry().getCalendarNames();
  }

  /**
   * Processes a single command and returns the result.
   *
   * <p>This method handles both calendar-level and event-level commands by:
   * 1. Validating and normalizing the command string 2. Determining if it's a calendar management
   * command 3. Routing to appropriate handler (calendar or event) 4. Updating command factory if
   * calendar context changes 5. Returning the command execution result
   *
   * @param commandString The command to process
   * @return Result message from command execution
   * @throws IllegalArgumentException if command is null or empty
   */
  public String processCommand(String commandString) {
    if (commandString == null || commandString.trim().isEmpty()) {
      return "Error: Command cannot be empty";
    }

    String normalizedCommand = normalizeCommand(commandString);

    try {
      if (isCalendarCommand(normalizedCommand)) {
        String result = processCalendarCommand(normalizedCommand);

        if (normalizedCommand.startsWith("use calendar")) {
          String calendarName = extractCalendarName(normalizedCommand);
          if (calendarName != null) {
            updateCommandFactory();
          }
        }

        return result;
      }

      CommandParser.CommandWithArgs commandWithArgs = parser.parseCommand(normalizedCommand);
      return commandWithArgs.execute();
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Checks if a command is a calendar management command.
   *
   * <p>Calendar management commands include, create calendar, edit calendar, use calendar, copy
   * event, copy events
   *
   * @param command The command to check
   * @return true if it's a calendar command, false otherwise
   */
  private boolean isCalendarCommand(String command) {
    return command.startsWith("create calendar") || command.startsWith("edit calendar")
            || command.startsWith("use calendar") || command.startsWith("copy event")
            || command.startsWith("copy events");
  }

  /**
   * Updates the command factory when switching between calendars.
   *
   * <p>This method: 1. Gets the currently active calendar 2. Creates a new command factory for
   * that
   * calendar 3. Updates the command parser with the new factory
   *
   * <p>This ensures that commands are executed in the context of the currently active calendar.
   */
  private void updateCommandFactory() {
    try {
      ICalendar activeCalendar = calendarManager.getActiveCalendar();
      if (this.commandFactory instanceof CommandFactory) {
        // Create a new CommandFactory with the active calendar
        this.commandFactory = new CommandFactory(activeCalendar, view);
        // Create a new parser with the updated factory
        this.parser = new CommandParser(this.commandFactory);
      }
    } catch (CalendarNotFoundException e) {
      view.displayError("Error updating command factory: " + e.getMessage());
    }
  }

  /**
   * Processes a calendar-specific command.
   *
   * <p>This method handles commands that operate on calendars rather than events. It parses the
   * command into components and routes it to the appropriate handler in the calendar command
   * factory.
   *
   * @param commandStr The calendar command string
   * @return Result of command execution
   * @throws Exception if command execution fails
   */
  private String processCalendarCommand(String commandStr) throws Exception {
    String[] parts = parseCommand(commandStr);
    if (parts.length < 2) {
      return "Error: Invalid calendar command format";
    }

    String action = parts[0];
    String targetType = parts[1];

    if (action.equals("copy")) {
      return calendarCommandFactory.getCommand("copy").execute(parts);
    }

    // For other calendar commands
    String[] args;
    if (targetType.equals("calendar")) {
      args = new String[parts.length - 1];
      args[0] = "calendar";
      System.arraycopy(parts, 2, args, 1, parts.length - 2);
    } else {
      return "Error: Expected 'calendar' after '" + action + "'";
    }

    if (calendarCommandFactory.hasCommand(action)) {
      return calendarCommandFactory.getCommand(action).execute(args);
    } else {
      return "Error: Unknown calendar command: " + action;
    }
  }

  /**
   * Parses a command string into tokens, properly handling quoted strings.
   *
   * <p>This method: 1. Splits the command on whitespace 2. Preserves quoted strings as single
   * tokens
   * 3. Handles both single and double quotes 4. Removes the quotes from the final tokens
   *
   * <p>Example: Input: create event "Team Meeting" from 2023-01-01 Output: ["create", "event",
   * "Team
   * Meeting", "from", "2023-01-01"]
   *
   * @param commandStr The command string to parse
   * @return Array of command tokens
   */
  private String[] parseCommand(String commandStr) {
    List<String> tokens = new ArrayList<>();
    Pattern pattern = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
    Matcher matcher = pattern.matcher(commandStr);

    while (matcher.find()) {
      if (matcher.group(1) != null) {
        tokens.add(matcher.group(1));
      } else if (matcher.group(2) != null) {
        tokens.add(matcher.group(2));
      } else {
        tokens.add(matcher.group());
      }
    }

    return tokens.toArray(new String[0]);
  }

  private String extractCalendarName(String command) {
    Pattern pattern = Pattern.compile("use calendar --name ([\\w-]+)");
    Matcher matcher = pattern.matcher(command);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  /**
   * Starts the controller in interactive mode.
   *
   * <p>In this mode, the controller: 1. Displays welcome message 2. Enters a command processing
   * loop
   * 3. Reads commands from user input 4. Processes each command and displays results 5. Continues
   * until 'exit' command is received 6. Displays termination message
   */
  public void startInteractiveMode() {
    view.displayMessage("Calendar Application Started");
    view.displayMessage("Enter commands (type 'exit' to quit):");

    String command;
    while (!(command = view.readCommand()).equalsIgnoreCase(EXIT_COMMAND)) {
      String result = processCommand(command);
      view.displayMessage(result);
    }

    view.displayMessage("Calendar Application Terminated");
  }

  /**
   * Starts the controller in headless mode.
   *
   * <p>In this mode, the controller: 1. Reads commands from the specified file 2. Processes each
   * command in sequence 3. Stops on first error or after processing all commands 4. Requires 'exit'
   * as the last command
   *
   * <p>The method enforces several validations: - File must not be empty - File must contain at
   * least
   * one command - Last command must be 'exit' - Commands must be properly formatted
   *
   * @param commandsFilePath Path to the file containing commands
   * @return true if all commands were executed successfully
   */
  public boolean startHeadlessMode(String commandsFilePath) {
    if (!validateCommandFilePath(commandsFilePath)) {
      return false;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(commandsFilePath))) {
      List<String> commands = readCommands(reader);
      if (!validateCommands(commands)) {
        return false;
      }

      return executeCommands(commands);
    } catch (IOException e) {
      view.displayError("Error reading command file: " + e.getMessage());
      return false;
    }
  }

  private boolean validateCommandFilePath(String filePath) {
    if (filePath == null || filePath.trim().isEmpty()) {
      view.displayError("Error: File path cannot be empty");
      return false;
    }
    return true;
  }

  private List<String> readCommands(BufferedReader reader) {
    return reader.lines().map(String::trim).filter(line -> !line.isEmpty())
            .collect(Collectors.toList());
  }

  private boolean validateCommands(List<String> commands) {
    if (commands.isEmpty()) {
      view.displayError("Error: Command file is empty. At least one command (exit) is required.");
      return false;
    }

    String lastCommand = commands.get(commands.size() - 1);
    if (!lastCommand.equalsIgnoreCase(EXIT_COMMAND)) {
      view.displayError("Headless mode requires the last command to be 'exit'");
      return false;
    }

    return true;
  }

  private boolean executeCommands(List<String> commands) {
    for (String command : commands) {
      String result = processCommand(command);
      if (result.startsWith("Error")) {
        view.displayError(result);
        return false;
      }
      if (!command.equalsIgnoreCase(EXIT_COMMAND)) {
        view.displayMessage(result);
      }
    }
    return true;
  }

  private String normalizeCommand(String commandString) {
    String[] parts = commandString.trim().split("\\s+");
    return String.join(" ", parts);
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   * This method is specifically designed for the GUI interaction.
   *
   * @param name     The name of the calendar to create
   * @param timezone The timezone for the calendar
   * @return true if the calendar was created successfully, false otherwise
   * @throws IllegalArgumentException if the name or timezone is invalid
   */
  public boolean createCalendar(String name, String timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }
    if (timezone == null || timezone.trim().isEmpty()) {
      throw new IllegalArgumentException("Timezone cannot be empty");
    }

    try {
      String command = "create calendar \"" + name + "\" " + timezone;
      String result = processCommand(command);
      return !result.startsWith("Error");
    } catch (Exception e) {
      view.displayError("Error creating calendar: " + e.getMessage());
      return false;
    }
  }

  /**
   * Sets the selected calendar by name.
   * This method is specifically designed for the GUI interaction.
   *
   * @param calendarName The name of the calendar to select
   * @return true if the calendar was selected successfully, false otherwise
   */
  public boolean setSelectedCalendarByName(String calendarName) {
    if (calendarName == null || calendarName.trim().isEmpty()) {
      return false;
    }

    try {
      String command = "use calendar \"" + calendarName + "\"";
      String result = processCommand(command);
      updateCommandFactory();
      return !result.startsWith("Error");
    } catch (Exception e) {
      view.displayError("Error selecting calendar: " + e.getMessage());
      return false;
    }
  }

  /**
   * Sets the selected calendar.
   * This method is specifically designed for the GUI interaction.
   *
   * @param calendar The calendar to select
   * @return true if the calendar was selected successfully, false otherwise
   */
  public boolean setSelectedCalendar(ICalendar calendar) {
    if (calendar == null) {
      return false;
    }

    try {
      return setSelectedCalendarByName(calendar.toString());
    } catch (Exception e) {
      view.displayError("Error selecting calendar: " + e.getMessage());
      return false;
    }
  }

  /**
   * Updates the calendar list in the view.
   * This method is specifically designed for the GUI interaction.
   */
  public void updateCalendarList() {
    try {
      List<String> calendarNames = new ArrayList<>(calendarManager.getCalendarRegistry().getCalendarNames());
      view.updateCalendarList(calendarNames);
    } catch (Exception e) {
      view.displayError("Error updating calendar list: " + e.getMessage());
    }
  }

  /**
   * Imports a calendar from a CSV file.
   * This method is specifically designed for the GUI interaction.
   *
   * @param filePath The path to the CSV file
   * @return true if the calendar was imported successfully, false otherwise
   */
  public boolean importCalendarFromCSV(String filePath) {
    if (filePath == null || filePath.trim().isEmpty()) {
      return false;
    }

    try {
      ICalendar activeCalendar = calendarManager.getActiveCalendar();
      if (activeCalendar == null) {
        view.displayError("No active calendar to import to");
        return false;
      }

      String command = "import " + filePath;
      String result = processCommand(command);
      return !result.startsWith("Error");
    } catch (Exception e) {
      view.displayError("Error importing calendar: " + e.getMessage());
      return false;
    }
  }

  /**
   * Exports the active calendar to a CSV file.
   * This method is specifically designed for the GUI interaction.
   *
   * @param filePath The path to the CSV file
   * @return true if the calendar was exported successfully, false otherwise
   */
  public boolean exportCalendarToCSV(String filePath) {
    if (filePath == null || filePath.trim().isEmpty()) {
      return false;
    }

    try {
      ICalendar activeCalendar = calendarManager.getActiveCalendar();
      if (activeCalendar == null) {
        view.displayError("No active calendar to export");
        return false;
      }

      String command = "export " + filePath;
      String result = processCommand(command);
      return !result.startsWith("Error");
    } catch (Exception e) {
      view.displayError("Error exporting calendar: " + e.getMessage());
      return false;
    }
  }

  /**
   * Sets the selected date and retrieves events for that date.
   * This method is specifically designed for the GUI interaction.
   *
   * @param date The date to select
   */
  public void setSelectedDate(LocalDate date) {
    if (date == null) {
      return;
    }

    try {
      // Get events for the selected date from the model
      ICalendar activeCalendar = calendarManager.getActiveCalendar();
      if (activeCalendar != null) {
        // Update the view with the selected date and its events
        List<Event> events = activeCalendar.getEventsOnDate(date);
        view.updateSelectedDate(date);
        view.updateEventList(events);
      }
    } catch (Exception e) {
      view.displayError("Error setting selected date: " + e.getMessage());
    }
  }

  /**
   * Retrieves events for a specific date from the active calendar.
   * This method is specifically designed for the GUI interaction.
   *
   * @param date The date to get events for
   * @return A list of events on the specified date, or an empty list if there are none or an error occurs
   */
  public List<Event> getEventsForDate(LocalDate date) {
    if (date == null) {
      return new ArrayList<>();
    }

    try {
      ICalendar activeCalendar = calendarManager.getActiveCalendar();
      if (activeCalendar != null) {
        return activeCalendar.getEventsOnDate(date);
      }
    } catch (Exception e) {
      view.displayError("Error getting events for date: " + e.getMessage());
    }

    return new ArrayList<>();
  }

  /**
   * Retrieves events within a date range from the active calendar.
   * This method is specifically designed for the GUI interaction.
   *
   * @param startDate The start date of the range (inclusive)
   * @param endDate   The end date of the range (inclusive)
   * @return A list of events in the specified date range, or an empty list if there are none or an error occurs
   */
  public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      return new ArrayList<>();
    }

    try {
      ICalendar activeCalendar = calendarManager.getActiveCalendar();
      if (activeCalendar != null) {
        return activeCalendar.getEventsInRange(startDate, endDate);
      }
    } catch (Exception e) {
      view.displayError("Error getting events in range: " + e.getMessage());
    }

    return new ArrayList<>();
  }

  /**
   * Checks the status of a specific date (busy or free).
   * This method is specifically designed for the GUI interaction.
   *
   * @param date The date to check
   * @return true if the date has events (busy), false otherwise (free)
   */
  public boolean isDateBusy(LocalDate date) {
    if (date == null) {
      return false;
    }

    try {
      ICalendar activeCalendar = calendarManager.getActiveCalendar();
      if (activeCalendar != null) {
        // Check if there are any events on this date
        List<Event> events = activeCalendar.getEventsOnDate(date);
        return !events.isEmpty();
      }
    } catch (Exception e) {
      view.displayError("Error checking date status: " + e.getMessage());
    }

    return false;
  }
}