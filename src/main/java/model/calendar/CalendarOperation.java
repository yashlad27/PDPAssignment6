package model.calendar;

import model.exceptions.CalendarExceptions.CalendarNotFoundException;
import model.exceptions.CalendarExceptions.ConflictingEventException;
import model.exceptions.CalendarExceptions.EventNotFoundException;
import model.exceptions.CalendarExceptions.InvalidEventException;

/**
 * Functional interface for performing operations on calendars.
 * This interface allows for executing operations on a Calendar instance
 * and returning a result of a generic type.
 *
 * @param <T> the type of the result returned by the operation
 */
@FunctionalInterface
public interface CalendarOperation<T> {

  /**
   * Executes an operation on the provided calendar and returns a result.
   *
   * @param calendar the calendar to operate on
   * @return the result of the operation
   * @throws CalendarNotFoundException if the calendar is not found
   * @throws EventNotFoundException    if an event is not found
   * @throws ConflictingEventException if an event conflict occurs
   * @throws InvalidEventException     if event parameters are invalid
   */
  T execute(Calendar calendar) throws CalendarNotFoundException, EventNotFoundException,
          ConflictingEventException, InvalidEventException;
}
