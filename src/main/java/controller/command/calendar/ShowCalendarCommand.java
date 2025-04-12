package controller.command.calendar;

import controller.command.ICommand;
import model.calendar.CalendarManager;
import model.exceptions.CalendarExceptions.ConflictingEventException;
import model.exceptions.CalendarExceptions.EventNotFoundException;
import model.exceptions.CalendarExceptions.InvalidEventException;
import view.ICalendarView;

/**
 * Command for showing calendar information.
 */
public class ShowCalendarCommand implements ICommand {
  private final CalendarManager calendarManager;

  /**
   * Creates a new ShowCalendarCommand.
   *
   * @param calendarManager the calendar manager
   * @param view            the view
   */
  public ShowCalendarCommand(CalendarManager calendarManager, ICalendarView view) {
    this.calendarManager = calendarManager;
    // View parameter is kept for interface consistency but not used in this implementation
  }

  @Override
  public String execute(String[] args) throws ConflictingEventException, InvalidEventException,
          EventNotFoundException {
    String activeCalendarName = calendarManager.getCalendarRegistry().getActiveCalendarName();
    if (activeCalendarName == null) {
      return "No active calendar";
    }

    StringBuilder builder = new StringBuilder();
    builder.append("Active calendar: ").append(activeCalendarName).append("\n");
    builder.append("Available calendars:\n");

    for (String name : calendarManager.getCalendarRegistry().getCalendarNames()) {
      builder.append("  ").append(name);
      if (name.equals(activeCalendarName)) {
        builder.append(" (active)");
      }
      builder.append("\n");
    }

    return builder.toString();
  }

  @Override
  public String getName() {
    return "show calendar";
  }
}