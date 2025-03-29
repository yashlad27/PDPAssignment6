package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import controller.command.Command;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
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
 * 1. Interactive Mode: Processes commands entered by users in real-time
 * 2. Headless Mode: Processes commands from a file without user interaction
 */
public class CalendarController {

    private final ICalendarView view;
    private final ICommandFactory commandFactory;
    private final CalendarManager calendarManager;
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
     * @param commandFactory the factory for creating all commands
     * @param calendarManager the manager for calendar operations
     * @param view the view component for user interaction
     * @throws IllegalArgumentException if any parameter is null
     */
    public CalendarController(ICommandFactory commandFactory,
                            CalendarManager calendarManager,
                            ICalendarView view) {
        if (commandFactory == null) {
            throw new IllegalArgumentException("CommandFactory cannot be null");
        }
        if (calendarManager == null) {
            throw new IllegalArgumentException("CalendarManager cannot be null");
        }
        if (view == null) {
            throw new IllegalArgumentException("View cannot be null");
        }

        this.view = view;
        this.commandFactory = commandFactory;
        this.calendarManager = calendarManager;
    }

    /**
     * Processes a single command and returns the result.
     *
     * <p>This method handles both calendar-level and event-level commands by:
     * 1. Validating and normalizing the command string
     * 2. Getting the appropriate command from the factory
     * 3. Executing the command
     * 4. Returning the command execution result
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
        String[] args = normalizedCommand.split("\\s+");

        if (args.length == 0) {
            return "Error: Invalid command format";
        }

        String commandName = args[0].toLowerCase();
        if (!VALID_COMMANDS.contains(commandName)) {
            return "Error: Unknown command '" + commandName + "'";
        }

        try {
            Command command = commandFactory.getCommand(commandName);
            if (command == null) {
                return "Error: Command '" + commandName + "' not found";
            }
            return command.execute(args);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Starts the application in interactive mode.
     */
    public void startInteractiveMode() {
        view.displayMessage("Welcome to the Calendar Application!");
        view.displayMessage("Enter commands (type 'exit' to quit):");

        while (true) {
            String command = view.readCommand();
            if (command.trim().equalsIgnoreCase(EXIT_COMMAND)) {
                break;
            }

            String result = processCommand(command);
            view.displayMessage(result);
        }

        view.displayMessage("Goodbye!");
    }

    /**
     * Starts the application in headless mode, processing commands from a file.
     *
     * @param filename the name of the file containing commands
     * @return true if all commands were processed successfully, false otherwise
     */
    public boolean startHeadlessMode(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                String result = processCommand(line);
                if (result.startsWith("Error:")) {
                    view.displayError(result);
                    return false;
                }
                view.displayMessage(result);
            }
            return true;
        } catch (IOException e) {
            view.displayError("Error reading file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Normalizes a command string by removing extra whitespace and converting to lowercase.
     *
     * @param command the command string to normalize
     * @return the normalized command string
     */
    private String normalizeCommand(String command) {
        return command.trim().replaceAll("\\s+", " ");
    }
}