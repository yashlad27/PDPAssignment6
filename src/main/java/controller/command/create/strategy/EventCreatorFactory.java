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

        // Implement the factory pattern to create the appropriate event creator
        switch (type.toLowerCase()) {
            case "single":
                return new SingleEventCreator(args);
            case "recurring":
                return new RecurringEventCreator(args);
            case "allday":
                return new AllDayEventCreator(args);
            case "recurring-until":
                return new RecurringUntilEventCreator(args);
            case "allday-recurring":
                return new AllDayRecurringEventCreator(args);
            case "allday-recurring-until":
                return new AllDayRecurringUntilEventCreator(args);
            default:
                throw new InvalidEventException("Unknown event type: " + type);
        }
    }
} 