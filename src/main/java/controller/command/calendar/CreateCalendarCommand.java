package controller.command.calendar;

import controller.command.ICommand;
import model.calendar.CalendarManager;
import model.exceptions.ConflictingEventException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import model.exceptions.InvalidTimezoneException;
import view.ICalendarView;

/**
 * Command for creating a new calendar.
 */
public class CreateCalendarCommand implements ICommand {
  private final CalendarManager calendarManager;
  private final ICalendarView view;

  /**
   * Creates a new CreateCalendarCommand.
   *
   * @param calendarManager the calendar manager
   * @param view            the view
   */
  public CreateCalendarCommand(CalendarManager calendarManager, ICalendarView view) {
    this.calendarManager = calendarManager;
    this.view = view;
  }

  @Override
  public String execute(String[] args) throws ConflictingEventException, InvalidEventException,
          EventNotFoundException {
    if (args.length < 5) {
      return "Error: Invalid arguments for create calendar command";
    }

    if (!"--name".equals(args[1])) {
      return "Error: Expected --name flag";
    }

    if (!"--timezone".equals(args[3])) {
      return "Error: Expected --timezone flag";
    }

    String name = args[2];
    String timezone = args[4];

    try {
      calendarManager.createCalendar(name, timezone);
      return "Calendar created: " + name;
    } catch (DuplicateCalendarException e) {
      return "Error: " + e.getMessage();
    } catch (InvalidTimezoneException e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return "create-calendar";
  }
} 