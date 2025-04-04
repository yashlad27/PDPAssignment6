package controller.command;

import java.util.function.Function;

import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

/**
 * Interface for command pattern implementation. Each command represents a specific operation in the
 * calendar application.
 */
public interface ICommand {

  /**
   * Executes the command with the given arguments.
   *
   * @param args the command arguments
   * @return a string representing the result of command execution
   */
  String execute(String[] args) throws ConflictingEventException, InvalidEventException,
          EventNotFoundException;

  /**
   * Gets the name of the command.
   *
   * @return the command name
   */
  String getName();

  /**
   * Creates an ICommand from a function that takes String[] and returns String.
   * This factory method allows for easy creation of command objects from lambda expressions.
   *
   * @param name     the name of the command
   * @param executor the function that executes the command
   * @return an ICommand implementation
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