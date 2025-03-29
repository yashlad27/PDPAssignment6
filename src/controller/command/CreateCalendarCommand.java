package controller.command;

import model.calendar.CalendarManager;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;

/**
 * Command for creating a new calendar.
 */
public class CreateCalendarCommand implements Command {
    private final CalendarManager calendarManager;

    public CreateCalendarCommand(CalendarManager calendarManager) {
        this.calendarManager = calendarManager;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 5) {
            return "Error: Insufficient arguments for create calendar command";
        }
        String calendarName = args[2];
        String timezone = args[4];

        if (calendarName.length() > 100) {
            return "Error: Calendar name cannot exceed 100 characters";
        }

        try {
            calendarManager.createCalendar(calendarName, timezone);
            return "Calendar '" + calendarName + "' created successfully with timezone " + timezone;
        } catch (DuplicateCalendarException | InvalidTimezoneException e) {
            return e.getMessage();
        }
    }
} 