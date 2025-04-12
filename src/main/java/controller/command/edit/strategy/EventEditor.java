package controller.command.edit.strategy;

import model.calendar.ICalendar;
import model.exceptions.CalendarExceptions.ConflictingEventException;
import model.exceptions.CalendarExceptions.EventNotFoundException;
import model.exceptions.CalendarExceptions.InvalidEventException;

/**
 * Strategy interface for different event editing methods.
 * Each concrete implementation handles a specific type of event editing operation.
 */
public interface EventEditor {

  /**
   * Executes the edit operation on the calendar.
   *
   * @param calendar the calendar containing the events to edit
   * @return a result message indicating success or failure
   * @throws EventNotFoundException    if the event to edit cannot be found
   * @throws InvalidEventException     if the edit parameters are invalid
   * @throws ConflictingEventException if the edit would create a conflict
   */
  String executeEdit(ICalendar calendar) throws EventNotFoundException,
          InvalidEventException, ConflictingEventException;
}