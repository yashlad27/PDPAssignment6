package controller.command;

import java.util.HashMap;
import java.util.Map;

import controller.ICommandFactory;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import utilities.TimeZoneHandler;
import view.ICalendarView;

/**
 * Unified factory for creating and managing all calendar and event commands.
 * This factory handles operations for both calendar management and event management.
 */
public class UnifiedCommandFactory implements ICommandFactory {

    private final Map<String, Command> commands;
    private final ICalendar calendar;
    private final CalendarManager calendarManager;
    private final ICalendarView view;

    /**
     * Creates a UnifiedCommandFactory with required dependencies.
     *
     * @param calendar the current calendar model
     * @param calendarManager the calendar manager
     * @param view the view for user interaction
     * @throws IllegalArgumentException if any parameter is null
     */
    public UnifiedCommandFactory(ICalendar calendar, CalendarManager calendarManager, ICalendarView view) {
        if (calendar == null) {
            throw new IllegalArgumentException("Calendar cannot be null");
        }
        if (calendarManager == null) {
            throw new IllegalArgumentException("CalendarManager cannot be null");
        }
        if (view == null) {
            throw new IllegalArgumentException("View cannot be null");
        }

        this.commands = new HashMap<>();
        this.calendar = calendar;
        this.calendarManager = calendarManager;
        this.view = view;

        registerCommands();
    }

    /**
     * Registers all available commands.
     */
    private void registerCommands() {
        // Event commands
        registerEventCommands();
        
        // Calendar commands
        registerCalendarCommands();
    }

    /**
     * Registers event-related commands.
     */
    private void registerEventCommands() {
        // Create event command
        commands.put("create", new CreateEventCommand(calendar));

        // Edit event command
        commands.put("edit", new EditEventCommand(calendar));

        // Print events command
        commands.put("print", new PrintEventsCommand(calendar));

        // Show status command
        commands.put("show", new ShowStatusCommand(calendar));

        // Export calendar command
        commands.put("export", new ExportCalendarCommand(calendar));
    }

    /**
     * Registers calendar-related commands.
     */
    private void registerCalendarCommands() {
        // Create calendar command
        commands.put("create", new CreateCalendarCommand(calendarManager));

        // Edit calendar command
        commands.put("edit", new EditCalendarCommand(calendarManager));

        // Use calendar command
        commands.put("use", new UseCalendarCommand(calendarManager));

        // Copy command
        commands.put("copy", new CopyEventCommand(calendarManager, calendarManager.getTimezoneHandler()));
    }

    @Override
    public boolean hasCommand(String commandName) {
        return commands.containsKey(commandName);
    }

    @Override
    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }

    /**
     * Gets the current calendar instance.
     *
     * @return the calendar instance
     */
    public ICalendar getCalendar() {
        return calendar;
    }

    /**
     * Gets the calendar manager instance.
     *
     * @return the calendar manager instance
     */
    public CalendarManager getCalendarManager() {
        return calendarManager;
    }

    /**
     * Gets the view instance.
     *
     * @return the view instance
     */
    public ICalendarView getView() {
        return view;
    }
} 