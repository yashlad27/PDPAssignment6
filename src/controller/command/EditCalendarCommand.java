package controller.command;

import model.calendar.CalendarManager;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.InvalidTimezoneException;

/**
 * Command for editing a calendar's properties.
 */
public class EditCalendarCommand implements Command {
    private final CalendarManager calendarManager;

    public EditCalendarCommand(CalendarManager calendarManager) {
        this.calendarManager = calendarManager;
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 6) {
            return "Error: Insufficient arguments for edit calendar command";
        }
        String calendarName = args[2];
        String property = args[4];
        String value = args[5];

        try {
            if (property.equals("timezone")) {
                calendarManager.editCalendarTimezone(calendarName, value);
                return "Timezone updated to " + value + " for calendar '" + calendarName + "'";
            }
            return "Error: Invalid property '" + property + "' for calendar edit";
        } catch (CalendarNotFoundException | InvalidTimezoneException e) {
            return e.getMessage();
        }
    }
} 