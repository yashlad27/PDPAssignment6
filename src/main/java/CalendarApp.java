import javax.swing.*;

import controller.CalendarController;
import controller.GUIController;
import controller.ICommandFactory;
import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.exceptions.CalendarNotFoundException;
import model.factory.CalendarFactory;
import utilities.TimeZoneHandler;
import view.GUIView;
import view.ICalendarView;

/**
 * Main entry point for the Calendar Application. This class handles both interactive and headless
 * modes of operation.
 */
public class CalendarApp {
  private static CalendarManager calendarManager;
  private static ICalendarView view;
  private static CalendarController controller;

  /**
   * Main method that serves as the entry point for the application.
   *
   * @param args Command line arguments:
   *             --mode interactive : Starts the application in interactive mode
   *             --mode headless file : Starts the application in headless mode
   *                with the specified command file
   *             --mode gui : Starts the application in GUI mode
   */
  public static void main(String[] args) {
    initializeApplication();
    handleCommandLineArguments(args);
  }

  /**
   * Initializes the core components of the application.
   */
  private static void initializeApplication() {
    CalendarFactory factory = new CalendarFactory();
    TimeZoneHandler timezoneHandler = factory.createTimeZoneHandler();
    calendarManager = factory.createCalendarManager(timezoneHandler);
    ICalendar calendar = new Calendar();

    // Create controller with null command factories first
    controller = factory.createController(null,
            null, calendarManager, null);

    // Create view
    view = factory.createView("gui", controller);

    // Now create command factories with the view
    ICommandFactory eventCommandFactory = factory.createEventCommandFactory(calendar, view);
    ICommandFactory calendarCommandFactory = factory.createCalendarCommandFactory(calendarManager,
            view);

    // Update controller with the command factories
    controller.setEventCommandFactory(eventCommandFactory);
    controller.setCalendarCommandFactory(calendarCommandFactory);
    controller.setView(view);
  }

  /**
   * Handles command line arguments and starts the appropriate mode.
   *
   * @param args Command line arguments
   */
  private static void handleCommandLineArguments(String[] args) {
    try {
      if (args.length == 0) {
        startGUIMode();
        return;
      }

      String modeArg = args[0].toLowerCase();
      String modeValue = args.length > 1 ? args[1].toLowerCase() : "";

      if (!modeArg.equals("--mode")) {
        view.displayError("Invalid argument. Expected: --mode");
        return;
      }

      startMode(modeValue, args);
    } catch (Exception e) {
      view.displayError("An error occurred: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Starts the appropriate mode based on the command line arguments.
   *
   * @param modeValue The mode to start
   * @param args      The full command line arguments
   */
  private static void startMode(String modeValue, String[] args) {
    switch (modeValue) {
      case "interactive":
        startInteractiveMode();
        break;
      case "headless":
        startHeadlessMode(args);
        break;
      case "gui":
        startGUIMode();
        break;
      default:
        view.displayError("Invalid mode. Expected: interactive, headless, or gui");
        break;
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
    if (args.length < 3) {
      view.displayError("Headless mode requires a filename."
              + " Usage: --mode headless filename");
      return;
    }
    String filename = args[2];
    boolean success = controller.startHeadlessMode(filename);
    if (!success) {
      view.displayError("Headless mode execution failed.");
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
          view.displayError("Failed to initialize GUI: " + e.getMessage());
          System.exit(1);
        }
      });
    }
  }
} 