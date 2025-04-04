package controller.command.create.strategy;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.event.validation.IEventValidationService;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;

/**
 * Base class for event creators that provides common functionality.
 */
public abstract class BaseEventCreator implements EventCreator {
    protected final IEventValidationService validationService;
    protected final String[] args;

    /**
     * Creates a new BaseEventCreator.
     *
     * @param validationService the validation service to use
     * @param args the arguments for event creation
     */
    protected BaseEventCreator(IEventValidationService validationService, String[] args) {
        if (validationService == null) {
            throw new IllegalArgumentException("Validation service cannot be null");
        }
        this.validationService = validationService;
        this.args = args;
    }

    /**
     * Validates the arguments for event creation.
     *
     * @throws InvalidEventException if the arguments are invalid
     */
    protected void validateArguments() throws InvalidEventException {
        if (args == null || args.length == 0) {
            throw new InvalidEventException("No arguments provided for event creation");
        }
    }

    @Override
    public String executeCreation(ICalendar calendar) throws ConflictingEventException, InvalidEventException {
        Event event = createEventInternal();
        calendar.addEvent(event, true);
        return "Successfully created event: " + event.getSubject();
    }

    /**
     * Executes the event creation process.
     *
     * @return success message
     * @throws InvalidEventException if the event is invalid
     * @throws ConflictingEventException if the event conflicts with existing events
     */
    public String execute() throws InvalidEventException, ConflictingEventException {
        Event event = createEvent();
        return "Event ready for creation: " + event.getSubject();
    }

    /**
     * Creates an event.
     *
     * @return the created event
     * @throws InvalidEventException if the event parameters are invalid
     */
    public Event createEvent() throws InvalidEventException {
        validateArguments();
        return createEventInternal();
    }

    /**
     * Creates a recurring event.
     *
     * @return the created recurring event
     * @throws InvalidEventException if the event parameters are invalid
     */
    public RecurringEvent createRecurringEvent() throws InvalidEventException {
        validateArguments();
        return createRecurringEventInternal();
    }

    /**
     * Creates an event internally.
     *
     * @return the created event
     * @throws InvalidEventException if the event creation fails
     */
    protected abstract Event createEventInternal() throws InvalidEventException;

    /**
     * Creates a recurring event internally.
     *
     * @return the created recurring event
     * @throws InvalidEventException if the event creation fails
     */
    protected RecurringEvent createRecurringEventInternal() throws InvalidEventException {
        throw new UnsupportedOperationException("This creator does not support recurring events");
    }
} 