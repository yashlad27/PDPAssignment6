package controller.command.calendar;

import controller.command.ICommand;
import model.calendar.CalendarManager;
import view.ICalendarView;

/**
 * Command for copying an existing calendar.
 */
public class CopyCalendarCommand implements ICommand {
    private final CalendarManager calendarManager;

    /**
     * Creates a new CopyCalendarCommand.
     *
     * @param calendarManager the calendar manager
     */
    public CopyCalendarCommand(CalendarManager calendarManager, ICalendarView view) {
        this.calendarManager = calendarManager;
        // View parameter is kept for interface consistency but not used in this implementation
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 3) {
            return "Error: Invalid arguments for copy calendar command";
        }

        String sourceName = args[0];
        String targetName = args[2];

        try {
            // Use the calendar registry to copy the calendar
            calendarManager.getCalendarRegistry().updateCalendarName(sourceName, targetName);
            return "Calendar copied: " + sourceName + " -> " + targetName;
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Override
    public String getName() {
        return "copy-calendar";
    }
} 