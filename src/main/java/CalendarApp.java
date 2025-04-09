import controller.CalendarController;
import controller.GUIController;
import controller.ICommandFactory;
import javax.swing.SwingUtilities;
import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.exceptions.CalendarNotFoundException;
import model.factory.CalendarFactory;
import utilities.TimeZoneHandler;
import view.GUIView;
import view.ICalendarView;
import view.TextView;

/**
 * Main entry point for the Calendar Application. This class handles both interactive and headless
 * modes of operation.
 */
public class CalendarApp {

  private static CalendarManager calendarManager;
  private static ICalendarView view;
  private static CalendarController controller;
  private static String currentMode = "gui";
  private static String[] commandLineArgs;

  /**
   * Main method that serves as the entry point for the application.
   *
   * @param args Command line arguments: --mode interactive : Starts the application in interactive
   *             mode --mode headless file : Starts the application in headless mode with the
   *             specified command file --no args : Starts the application in GUI mode
   */
  public static void main(String[] args) {
    commandLineArgs = args;
    handleCommandLineArguments(args);
    initializeApplication();
    startApplication();
  }

  /**
   * Initializes the core components of the application.
   */
  private static void initializeApplication() {
    CalendarFactory factory = new CalendarFactory();
    TimeZoneHandler timezoneHandler = factory.createTimeZoneHandler();
    calendarManager = factory.createCalendarManager(timezoneHandler);
    ICalendar calendar = new Calendar();

    controller = factory.createController(null, null, calendarManager, null);

    view = CalendarFactory.createView(currentMode, controller);

    ICommandFactory eventCommandFactory = factory.createEventCommandFactory(calendar, view);
    ICommandFactory calendarCommandFactory = factory.createCalendarCommandFactory(calendarManager,
        view);

    controller.setEventCommandFactory(eventCommandFactory);
    controller.setCalendarCommandFactory(calendarCommandFactory);
    controller.setView(view);
  }

  /**
   * Handles command line arguments and sets the appropriate mode.
   *
   * @param args Command line arguments
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
   * Validates the mode argument format.
   *
   * @param modeArg the mode argument to validate
   */
  private static void validateModeArgument(String modeArg) {
    if (!modeArg.toLowerCase().equals("--mode")) {
      System.err.println("Invalid argument. Expected: --mode");
      System.exit(1);
    }
  }

  /**
   * Sets the mode based on command line arguments.
   *
   * @param args the command line arguments
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
   * Sets the application to GUI mode.
   */
  private static void setGUIMode() {
    currentMode = "gui";
  }

  /**
   * Sets the application to interactive mode.
   */
  private static void setInteractiveMode() {
    currentMode = "text";
  }

  /**
   * Sets the application to headless mode.
   *
   * @param args the command line arguments
   */
  private static void setHeadlessMode(String[] args) {
    if (args.length < 3) {
      System.err.println("Headless mode requires a filename." + " Usage: --mode headless filename");
      System.exit(1);
    }
    currentMode = "text";
  }

  /**
   * Starts the application in the appropriate mode.
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
   * Starts the application in text mode (interactive or headless).
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
   * Starts the application in interactive mode.
   */
  private static void startInteractiveMode() {
    controller.startInteractiveMode();
  }

  /**
   * Starts the application in headless mode.
   *
   * @param args Command line arguments
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
   * Starts the application in GUI mode.
   */
  private static void startGUIMode() {
    if (view instanceof GUIView) {
      final GUIView guiView = (GUIView) view;
      final GUIController guiController = new GUIController(calendarManager, guiView);
      SwingUtilities.invokeLater(() -> {
        try {
          guiController.initialize();
          guiView.displayGUI();
        } catch (CalendarNotFoundException e) {
          System.err.println("Failed to initialize GUI: " + e.getMessage());
          System.exit(1);
        }
      });
    }
  }
} 