
import controller.CalendarController;
import controller.GUIController;
import controller.ICommandFactory;
import javax.swing.SwingUtilities;
import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.exceptions.CalendarExceptions.CalendarNotFoundException;
import model.factory.CalendarFactory;
import utilities.TimeZoneHandler;
import view.CalendarViewFeatures;
import view.GUIView;
import view.ICalendarView;
import view.TextView;

/**
 * Main entry point for the Calendar Application. This class handles both interactive and headless
 * modes of operation.
 *
 * <p>The application supports three modes of operation:
 * <ul>
 *   <li>GUI Mode: A graphical user interface for calendar management</li>
 *   <li>Interactive Mode: A command-line interface for direct user interaction</li>
 *   <li>Headless Mode: Batch processing of commands from a file</li>
 * </ul>
 *
 * <p>The class manages the initialization and startup of the application components including:
 * <ul>
 *   <li>Calendar Manager for handling multiple calendars</li>
 *   <li>View implementation based on the selected mode</li>
 *   <li>Controller for handling user interactions and business logic</li>
 * </ul>
 */
public class CalendarApp {

  /**
   * Manages multiple calendars and their operations.
   */
  private static CalendarManager calendarManager;

  /**
   * View interface for displaying calendar information.
   */
  private static ICalendarView view;

  /**
   * Controller for handling user interactions and business logic.
   */
  private static CalendarController controller;

  /**
   * Current operating mode of the application (gui, text, or headless).
   */
  private static String currentMode = "gui";

  /**
   * Command line arguments passed to the application.
   */
  private static String[] commandLineArgs;

  /**
   * Main method that serves as the entry point for the application. Processes command line
   * arguments and initializes the application in the appropriate mode.
   *
   * @param args Command line arguments with the following options:
   *             <ul>
   *               <li>No arguments: Starts in GUI mode</li>
   *               <li>--mode interactive: Starts in interactive command-line mode</li>
   *               <li>--mode headless file: Processes commands from specified file</li>
   *               <li>--mode gui: Explicitly starts in GUI mode</li>
   *             </ul>
   */
  public static void main(String[] args) {
    commandLineArgs = args;
    handleCommandLineArguments(args);
    initializeApplication();
    startApplication();
  }

  /**
   * Initializes the core components of the application. Sets up the calendar manager, view,
   * controller, and command factories using the factory pattern to ensure proper dependency
   * injection and component creation.
   *
   * <p>This method:
   * <ul>
   *   <li>Creates the calendar factory for component instantiation</li>
   *   <li>Initializes the timezone handler</li>
   *   <li>Sets up the calendar manager</li>
   *   <li>Creates appropriate view and controller instances</li>
   *   <li>Configures command factories for event and calendar operations</li>
   * </ul>
   */
  private static void initializeApplication() {
    CalendarFactory factory = new CalendarFactory();
    TimeZoneHandler timezoneHandler = factory.createTimeZoneHandler();
    calendarManager = factory.createCalendarManager(timezoneHandler);
    ICalendar calendar = new Calendar();

    controller = factory.createController(null,
        null, calendarManager, null);

    view = CalendarFactory.createView(currentMode, controller);

    ICommandFactory eventCommandFactory = factory.createEventCommandFactory(calendar, view);
    ICommandFactory calendarCommandFactory = factory.createCalendarCommandFactory(calendarManager,
        view);

    controller.setEventCommandFactory(eventCommandFactory);
    controller.setCalendarCommandFactory(calendarCommandFactory);
    controller.setView(view);
  }

  /**
   * Handles command line arguments and sets the appropriate application mode. Validates the format
   * of arguments and ensures they meet the expected pattern.
   *
   * @param args Command line arguments to process
   * @throws IllegalArgumentException if arguments are invalid or in unexpected format
   */
  private static void handleCommandLineArguments(String[] args) {
    if (args.length == 0) {
      setGUIMode();
      return;
    }

    validateModeArgument(args[0]);
    setModeFromArguments(args);
  }

  /**
   * Validates the mode argument format to ensure it matches expected pattern. Checks if the first
   * argument is "--mode" as required.
   *
   * @param modeArg the mode argument to validate
   * @throws IllegalArgumentException if the mode argument is invalid
   */
  private static void validateModeArgument(String modeArg) {
    if (!modeArg.toLowerCase().equals("--mode")) {
      System.err.println("Invalid argument. Expected: --mode");
      System.exit(1);
    }
  }

  /**
   * Sets the application mode based on provided command line arguments. Processes the mode value
   * and configures the application accordingly.
   *
   * @param args the command line arguments containing the mode specification
   * @throws IllegalArgumentException if the specified mode is invalid
   */
  private static void setModeFromArguments(String[] args) {
    String modeValue = args.length > 1 ? args[1].toLowerCase() : "";

    switch (modeValue) {
      case "interactive":
        setInteractiveMode();
        break;
      case "headless":
        setHeadlessMode(args);
        break;
      case "gui":
        setGUIMode();
        break;
      default:
        System.err.println("Invalid mode. Expected: interactive, headless, or gui");
        System.exit(1);
    }
  }

  /**
   * Sets the application to GUI mode. This is the default mode when no specific mode is specified.
   */
  private static void setGUIMode() {
    currentMode = "gui";
  }

  /**
   * Sets the application to interactive text mode. Enables command-line interaction with the
   * calendar application.
   */
  private static void setInteractiveMode() {
    currentMode = "text";
  }

  /**
   * Sets the application to headless mode for batch processing. Validates that a command file is
   * specified in the arguments.
   *
   * @param args the command line arguments containing the file specification
   * @throws IllegalArgumentException if no filename is provided for headless mode
   */
  private static void setHeadlessMode(String[] args) {
    if (args.length < 3) {
      System.err.println("Headless mode requires a filename."
          + " Usage: --mode headless filename");
      System.exit(1);
    }
    currentMode = "text";
  }

  /**
   * Starts the application in the configured mode. Delegates to the appropriate start method based
   * on the current mode setting.
   *
   * @throws IllegalStateException if the current mode is invalid
   */
  private static void startApplication() {
    switch (currentMode) {
      case "text":
        startTextMode();
        break;
      case "gui":
        startGUIMode();
        break;
      default:
        System.err.println("Invalid mode: " + currentMode);
        System.exit(1);
    }
  }

  /**
   * Starts the application in text mode (either interactive or headless). Verifies the view
   * implementation and delegates to the appropriate start method.
   *
   * @throws IllegalStateException if the view is not properly configured for text mode
   */
  private static void startTextMode() {
    if (view instanceof TextView) {
      if (commandLineArgs.length > 1 && commandLineArgs[1].equals("headless")) {
        startHeadlessMode(commandLineArgs);
      } else {
        startInteractiveMode();
      }
    } else {
      System.err.println("Error: TextView not created correctly for text mode.");
      System.exit(1);
    }
  }

  /**
   * Starts the application in interactive mode. Initializes the controller for command-line
   * interaction with the user.
   */
  private static void startInteractiveMode() {
    controller.startInteractiveMode();
  }

  /**
   * Starts the application in headless mode for batch processing. Sets up the view and controller
   * for processing commands from a file.
   *
   * @param args Command line arguments containing the command file specification
   * @throws IllegalStateException if headless mode setup or execution fails
   */
  private static void startHeadlessMode(String[] args) {
    String filename = args[2];

    if (view instanceof TextView) {
      boolean setupSuccess = ((TextView) view).setupHeadlessMode(filename);

      if (!setupSuccess) {
        System.err.println("Failed to set up headless mode with file: " + filename);
        System.exit(1);
      }
    }

    boolean success = controller.startHeadlessMode(filename);
    if (!success) {
      System.err.println("Headless mode execution failed.");
      System.exit(1);
    }
  }

  /**
   * Starts the application in GUI mode. Initializes the graphical user interface and sets up event
   * handling.
   *
   * <p>This method:
   * <ul>
   *   <li>Creates and initializes the GUI controller</li>
   *   <li>Sets up the GUI view components</li>
   *   <li>Handles any initialization errors</li>
   * </ul>
   *
   * @throws IllegalStateException if GUI initialization fails
   */
  private static void startGUIMode() {
    if (view instanceof CalendarViewFeatures) {
      final CalendarViewFeatures guiView = (CalendarViewFeatures) view;
      final GUIController guiController = new GUIController(calendarManager, guiView);
      SwingUtilities.invokeLater(() -> {
        try {
          guiController.initialize();
          if (guiView instanceof GUIView) {
            ((GUIView) guiView).displayGUI();
          } else {
            System.err.println("View does not implement displayGUI method");
            System.exit(1);
          }
        } catch (CalendarNotFoundException e) {
          System.err.println("Failed to initialize GUI: " + e.getMessage());
          System.exit(1);
        }
      });
    }
  }
} 