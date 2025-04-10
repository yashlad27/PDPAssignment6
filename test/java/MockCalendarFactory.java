import controller.CalendarController;
import controller.ICommandFactory;
import controller.command.calendar.CalendarCommandFactory;
import controller.command.event.CommandFactory;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.factory.CalendarFactory;
import view.ICalendarView;
import view.TextView;

/**
 * Mock implementation of CalendarFactory for testing.
 * This class overrides the createView method to return a MockGUIView
 * when GUI mode is requested, avoiding HeadlessExceptions.
 */
public class MockCalendarFactory extends CalendarFactory {

  private boolean testMode = true;

  /**
   * Constructor that initializes with testMode = true
   */
  public MockCalendarFactory() {
    super();
  }

  /**
   * Constructor that allows setting test mode
   * 
   * @param testMode whether to run in test mode (true) or normal mode (false)
   */
  public MockCalendarFactory(boolean testMode) {
    super();
    this.testMode = testMode;
  }

  /**
   * Sets whether the factory should operate in test mode.
   * In test mode, a MockGUIView is returned instead of real GUIView.
   * 
   * @param testMode true to use mocks, false to use real components
   */
  public void setTestMode(boolean testMode) {
    this.testMode = testMode;
  }

  /**
   * Creates a view based on the specified mode.
   * Overrides the parent method to use MockGUIView when in GUI mode.
   *
   * @param mode       the mode to create the view for
   * @param controller the controller to use
   * @return the created view
   */
  public static ICalendarView createView(String mode, CalendarController controller) {
    if (mode == null || mode.trim().isEmpty()) {
      throw new IllegalArgumentException("Mode cannot be null or empty");
    }

    switch (mode.toLowerCase()) {
      case "gui":
        // Return our MockGUIView in test mode to avoid HeadlessException
        return (ICalendarView) new MockGUIView(controller);
      case "text":
        return new TextView(controller);
      default:
        throw new IllegalArgumentException("Invalid mode: " + mode);
    }
  }

  /**
   * Creates a command factory for event commands.
   * This version does additional validation and creates mocks when in test mode.
   *
   * @param calendar the calendar to operate on
   * @param view     the view to interact with
   * @return an ICommandFactory for event commands
   */
  @Override
  public ICommandFactory createEventCommandFactory(ICalendar calendar, ICalendarView view) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }
    return new CommandFactory(calendar, view);
  }

  /**
   * Creates a command factory for calendar management commands.
   * This version does additional validation and creates mocks when in test mode.
   *
   * @param calendarManager the calendar manager to operate on
   * @param view            the view to interact with
   * @return an ICommandFactory for calendar commands
   */
  @Override
  public ICommandFactory createCalendarCommandFactory(CalendarManager calendarManager, 
                                                    ICalendarView view) {
    if (calendarManager == null) {
      throw new IllegalArgumentException("Calendar manager cannot be null");
    }
    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }
    return new CalendarCommandFactory(calendarManager, view);
  }

  /**
   * Creates a calendar controller with all its dependencies.
   * This version is more tolerant of null values for testing purposes.
   *
   * @param eventCommandFactory    the command factory for event commands
   * @param calendarCommandFactory the command factory for calendar commands
   * @param calendarManager        the calendar manager
   * @param view                   the view to interact with
   * @return a CalendarController instance
   */
  @Override
  public CalendarController createController(ICommandFactory eventCommandFactory,
                                           ICommandFactory calendarCommandFactory,
                                           CalendarManager calendarManager, 
                                           ICalendarView view) {
    if (calendarManager == null) {
      throw new IllegalArgumentException("Calendar manager cannot be null");
    }

    return new CalendarController(eventCommandFactory, calendarCommandFactory, 
                                calendarManager, view);
  }
} 