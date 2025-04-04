package controller.command.calendar;

import controller.command.ICommand;
import model.calendar.CalendarManager;
import model.exceptions.CalendarNotFoundException;
import view.ICalendarView;

/**
 * Command for setting the active calendar.
 */
public class UseCalendarCommand implements ICommand {
  private final CalendarManager calendarManager;
  private final ICalendarView view;

  /**
   * Creates a new UseCalendarCommand.
   *
   * @param calendarManager the calendar manager
   * @param view            the view
   */
  public UseCalendarCommand(CalendarManager calendarManager, ICalendarView view) {
    this.calendarManager = calendarManager;
    this.view = view;
  }

  @Override
  public String getName() {
    return "use";
  }

  @Override
  public String execute(String[] args) {
    if (args.length < 3) {
      return "Error: Invalid arguments for use calendar command";
    }

    if (!"--name".equals(args[1])) {
      return "Error: Expected --name flag";
    }

    String name = args[2];

    try {
      calendarManager.setActiveCalendar(name);
      return "Now using calendar: " + name;
    } catch (CalendarNotFoundException e) {
      return "Error: " + e.getMessage();
    }
  }
} 