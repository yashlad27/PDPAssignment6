package controller.command.copy;

import controller.command.ICommand;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;

/**
 * Command for directly copying an event to a target calendar.
 * This is a simplified version of CopyEventCommand for use with the GUI.
 */
public class DirectCopyEventCommand implements ICommand {

  private final ICalendar targetCalendar;
  private final Event eventToCopy;

  /**
   * Constructs a new DirectCopyEventCommand.
   *
   * @param targetCalendar the target calendar to copy the event to
   * @param eventToCopy the event to copy
   */
  public DirectCopyEventCommand(ICalendar targetCalendar, Event eventToCopy) {
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Target calendar cannot be null");
    }
    if (eventToCopy == null) {
      throw new IllegalArgumentException("Event to copy cannot be null");
    }

    this.targetCalendar = targetCalendar;
    this.eventToCopy = eventToCopy;
  }

  /**
   * Executes the copy command, adding the event to the target calendar.
   *
   * @return true if the copy was successful, false otherwise
   */
  public boolean execute() {
    try {
      return targetCalendar.addEvent(eventToCopy, false);
    } catch (ConflictingEventException e) {
      return false;
    }
  }

  @Override
  public String execute(String[] args) {
    boolean success = execute();
    if (success) {
      return "Event copied successfully";
    } else {
      return "Failed to copy event";
    }
  }

  @Override
  public String getName() {
    return "directcopy";
  }
}
