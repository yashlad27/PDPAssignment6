package controller.command;

import java.util.function.Function;

import model.exceptions.CalendarExceptions.ConflictingEventException;
import model.exceptions.CalendarExceptions.EventNotFoundException;
import model.exceptions.CalendarExceptions.InvalidEventException;

/**
 * Interface for command pattern implementation in the calendar application.
 *
 * <p> The command pattern encapsulates a request as an object, thereby allowing for
 * parameterization of clients with different requests, queuing of requests, and logging of the
 * requests. Each command represents a specific operation that can be performed within the calendar
 * application.</p>
 *
 * <p> Commands in this application follow these principles:</p>
 * <ul>
 *   <li>Single Responsibility: Each command performs exactly one operation</li>
 *   <li>Encapsulation: Commands encapsulate all necessary information to execute an operation</li>
 *   <li>Separation of Concerns: Commands separate the request initiator from request handler</li>
 * </ul>
 *
 * <p> This interface facilitates both concrete command implementations and dynamic command
 * creation through the factory method.</p>
 */
public interface ICommand {

  /**
   * Executes the command with the given arguments.
   *
   * <p> This method performs the actual operation of the command using the provided arguments.
   * Commands should validate their arguments before performing operations, throwing appropriate
   * exceptions when validation fails.</p>
   *
   * <p> The execution may result in:</p>
   * <ul>
   *   <li>Successful operation with a result message</li>
   *   <li>ConflictingEventException if the operation would create a scheduling conflict</li>
   *   <li>InvalidEventException if any event parameters are invalid</li>
   *   <li>EventNotFoundException if the operation refers to a non-existent event</li>
   * </ul>
   *
   * @param args the command arguments as an array of strings, typically parsed from user input
   * @return a string representing the result of command execution, suitable for display to the user
   * @throws ConflictingEventException when the command would create an event that conflicts with
   *                                   existing events
   * @throws InvalidEventException     when the provided event details (date, time, duration, etc.)
   *                                   are invalid
   * @throws EventNotFoundException    when the command references an event that doesn't exist in
   *                                   the calendar
   */
  String execute(String[] args) throws ConflictingEventException, InvalidEventException,
          EventNotFoundException;

  /**
   * Gets the name of the command.
   *
   * <p>The command name is used for:</p>
   * <ul>
   *   <li>Command registration and lookup in command registries</li>
   *   <li>Display in help menus or command listings</li>
   *   <li>Command recognition in user interfaces</li>
   * </ul>
   *
   * <p>Command names should be unique within the application to avoid ambiguity.</p>
   *
   * @return the command name as a string, typically lowercase and without spaces
   */
  String getName();

  /**
   * Creates an ICommand from a function that takes String[] and returns String.
   *
   * <p>This factory method allows for easy creation of command objects from lambda expressions
   * or method references, reducing boilerplate code when implementing simple commands.</p>
   *
   *
   * <p>Note that commands created with this factory method must handle exceptions internally
   * since the Function interface doesn't support checked exceptions. For commands that need to
   * throw checked exceptions, implement the interface directly.</p>
   *
   * @param name     the name of the command, must be non-null and unique in the command registry
   * @param executor the function that executes the command logic, accepting arguments and returning
   *                 a result
   * @return an ICommand implementation that delegates to the provided function
   */
  static ICommand fromExecutor(String name, Function<String[], String> executor) {
    return new ICommand() {
      @Override
      public String execute(String[] args) throws ConflictingEventException,
              InvalidEventException, EventNotFoundException {
        return executor.apply(args);
      }

      @Override
      public String getName() {
        return name;
      }
    };
  }
}