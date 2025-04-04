package controller.command.create.strategy;

import model.exceptions.InvalidEventException;

/**
 * Implementation of IEventCreatorFactory that creates event creators.
 */
public class EventCreatorFactory implements IEventCreatorFactory {

  /**
   * Creates a new EventCreatorFactory.
   */
  public EventCreatorFactory() {
    // No initialization needed
  }

  @Override
  public EventCreator createForType(String type, String[] args) throws InvalidEventException {
    if (type == null || type.trim().isEmpty()) {
      throw new InvalidEventException("Event type cannot be empty");
    }

    // Implement the factory pattern to create the appropriate event creator using the consolidated class
    switch (type.toLowerCase()) {
      case "single":
        return ConsolidatedEventCreator.createSingleEvent(args);
      case "recurring":
        return ConsolidatedEventCreator.createRecurringEvent(args);
      case "allday":
        return ConsolidatedEventCreator.createAllDayEvent(args);
      case "recurring-until":
        return ConsolidatedEventCreator.createRecurringUntilEvent(args);
      case "allday-recurring":
        return ConsolidatedEventCreator.createAllDayRecurringEvent(args);
      case "allday-recurring-until":
        return ConsolidatedEventCreator.createAllDayRecurringUntilEvent(args);
      default:
        throw new InvalidEventException("Unknown event type: " + type);
    }
  }
} 