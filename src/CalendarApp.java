import controller.CalendarController;
import controller.ICommandFactory;
import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.factory.CalendarFactory;
import utilities.TimeZoneHandler;
import view.ConsoleView;
import view.GUIView;
import view.ICalendarView;

/**
 * Main entry point for the Calendar Application. This class handles both interactive and headless
 * modes of operation.
 */
public class CalendarApp {
  /**
   * Main method that serves as the entry point for the application.
   *
   * @param args Command line arguments:
   *             --mode interactive : Starts the application in interactive mode
   *             --mode headless file : Starts the application in headless mode with the specified command file
   *             --mode gui : Starts the application in GUI mode
   */
  public static void main(String[] args) {
    CalendarFactory factory = new CalendarFactory();
    TimeZoneHandler timezoneHandler = factory.createTimeZoneHandler();
    CalendarManager calendarManager = factory.createCalendarManager(timezoneHandler);
    ICalendar calendar = new Calendar();

    // Create controller with null command factories first
    CalendarController controller = factory.createController(null, null, calendarManager, null);

    // Create view
    ICalendarView view = factory.createView(args.length > 0 ? args[0] : "gui", controller);

    // Now create command factories with the view
    ICommandFactory eventCommandFactory = factory.createEventCommandFactory(calendar, view);
    ICommandFactory calendarCommandFactory = factory.createCalendarCommandFactory(calendarManager, view);

    // Update controller with the command factories
    controller.setEventCommandFactory(eventCommandFactory);
    controller.setCalendarCommandFactory(calendarCommandFactory);
    controller.setView(view);

    try {
      // Process command line arguments
      if (args.length == 0) {
        // No arguments means GUI mode
        if (view instanceof GUIView) {
          ((GUIView) view).displayGUI();
        }
        controller.startInteractiveMode();
        return;
      }

      String modeArg = args[0].toLowerCase();
      String modeValue = args.length > 1 ? args[1].toLowerCase() : "";

      if (!modeArg.equals("--mode")) {
        view.displayError("Invalid argument. Expected: --mode");
        return;
      }

      // Handle different modes
      switch (modeValue) {
        case "interactive":
          controller.startInteractiveMode();
          break;

        case "headless":
          if (args.length < 3) {
            view.displayError("Headless mode requires a filename. Usage: --mode headless filename");
            return;
          }
          String filename = args[2];
          boolean success = controller.startHeadlessMode(filename);
          if (!success) {
            view.displayError("Headless mode execution failed.");
            System.exit(1);
          }
          break;

        case "gui":
          if (view instanceof GUIView) {
            ((GUIView) view).displayGUI();
          }
          controller.startInteractiveMode();
          break;

        default:
          view.displayError("Invalid mode. Expected: interactive, headless, or gui");
          return;
      }

    } catch (Exception e) {
      view.displayError("An error occurred: " + e.getMessage());
      System.exit(1);
    } finally {
      // Clean up resources
      if (view instanceof ConsoleView) {
        ((ConsoleView) view).close();
      }
    }
  }
}