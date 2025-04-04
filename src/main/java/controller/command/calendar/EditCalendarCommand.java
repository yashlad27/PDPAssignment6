package controller.command.calendar;

import controller.command.ICommand;
import model.calendar.CalendarManager;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import model.exceptions.InvalidTimezoneException;
import view.ICalendarView;

/**
 * Command for editing an existing calendar.
 */
public class EditCalendarCommand implements ICommand {
  private final CalendarManager calendarManager;
  private final ICalendarView view;

  /**
   * Creates a new EditCalendarCommand.
   *
   * @param calendarManager the calendar manager
   * @param view            the view
   */
  public EditCalendarCommand(CalendarManager calendarManager, ICalendarView view) {
    this.calendarManager = calendarManager;
    this.view = view;
  }

  @Override
  public String execute(String[] args) throws ConflictingEventException, InvalidEventException,
          EventNotFoundException {
    if (args.length < 6) {
      return "Error: Invalid arguments for edit calendar command";
    }

    if (!"--name".equals(args[1])) {
      return "Error: Expected --name flag";
    }

    if (!"--property".equals(args[3])) {
      return "Error: Expected --property flag";
    }

    String name = args[2];
    String property = args[4];
    String value = args[5];

    try {
      if ("timezone".equals(property)) {
        calendarManager.editCalendarTimezone(name, value);
        return "Calendar timezone updated: " + name;
      } else {
        return "Error: Unsupported property: " + property;
      }
    } catch (CalendarNotFoundException e) {
      return "Error: " + e.getMessage();
    } catch (InvalidTimezoneException e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return "edit-calendar";
  }
}