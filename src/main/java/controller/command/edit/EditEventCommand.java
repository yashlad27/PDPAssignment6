package controller.command.edit;

import controller.command.ICommand;
import controller.command.edit.strategy.ConsolidatedEventEditor;
import model.calendar.ICalendar;
import model.exceptions.CalendarExceptions.ConflictingEventException;
import model.exceptions.CalendarExceptions.EventNotFoundException;
import model.exceptions.CalendarExceptions.InvalidEventException;

/**
 * Command for editing calendar events using the Strategy pattern.
 */
public class EditEventCommand implements ICommand {

  private static final int MIN_REQUIRED_ARGS = 3;
  private final ICalendar calendar;

  /**
   * Constructor that creates an EditEventCommand with a calendar reference.
   *
   * @param calendar the calendar to use for editing events
   * @throws IllegalArgumentException if calendar is null
   */
  public EditEventCommand(ICalendar calendar) {
    validateCalendar(calendar);
    this.calendar = calendar;
  }

  /**
   * Validates that the calendar is not null.
   *
   * @param calendar the calendar to validate
   * @throws IllegalArgumentException if calendar is null
   */
  private void validateCalendar(ICalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
  }

  /**
   * Executes the edit event command with the provided arguments.
   * Uses the strategy pattern to delegate event editing to the appropriate editor.
   *
   * @param args command arguments, where args[0] is the edit type
   * @return a message indicating the result of the operation
   */
  @Override
  public String execute(String[] args) {
    if (!hasMinimumArgs(args, MIN_REQUIRED_ARGS)) {
      return "Error: Insufficient arguments for edit command";
    }

    String editType = args[0];

    try {
      if ("single".equalsIgnoreCase(editType)) {
        // Format from headlessCmd.txt: 
        // edit event subject "Gym" from 2024-03-26T18:00 with "Weightlifting Session"
        // args[] = ["single", "subject", "Gym", "2024-03-26T18:00", "Weightlifting Session"]
        String property = args[1]; // e.g., "subject"
        String subject = args[2];  // e.g., "Gym"
        String startDateTime = args[3]; // e.g., "2024-03-26T18:00"
        String newValue = args[4]; // e.g., "Weightlifting Session"

        try {
          ConsolidatedEventEditor editor = new ConsolidatedEventEditor(calendar, subject,
                  startDateTime, property, newValue);
          return editor.editEvent(subject, startDateTime, property, newValue);
        } catch (EventNotFoundException | InvalidEventException | ConflictingEventException e) {
          return String.format("Failed to edit event: %s", e.getMessage());
        }
      }

      else {
        return "Error: Unknown edit type: " + editType;
      }
    } catch (IllegalArgumentException e) {
      return String.format("Error in command arguments: %s", e.getMessage());
    } catch (Exception e) {
      return String.format("Error editing event: %s", e.getMessage());
    }
  }

  /**
   * Checks if the command has the required minimum number of arguments.
   *
   * @param args            the command arguments
   * @param minimumRequired the minimum required number of arguments
   * @return true if the command has the minimum required arguments, false otherwise
   */
  private boolean hasMinimumArgs(String[] args, int minimumRequired) {
    return args != null && args.length >= minimumRequired;
  }

  /**
   * Returns the name of this command.
   *
   * @return the string "edit" which identifies this command to the command factory
   */
  @Override
  public String getName() {
    return "edit";
  }
}