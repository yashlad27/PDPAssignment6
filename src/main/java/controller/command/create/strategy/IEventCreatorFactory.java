package controller.command.create.strategy;

import model.exceptions.InvalidEventException;

/**
 * Factory interface for creating event creators.
 */
public interface IEventCreatorFactory {
  /**
   * Creates an event creator for the specified type.
   *
   * @param type the type of event creator to create
   * @param args the arguments for event creation
   * @return the appropriate event creator
   * @throws InvalidEventException if the type is unknown or arguments are invalid
   */
  EventCreator createForType(String type, String[] args) throws InvalidEventException;
} 