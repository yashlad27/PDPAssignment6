package view.dialog;

import java.time.LocalDateTime;

/**
 * Interface for event copy dialogs.
 * This follows the Interface Segregation Principle by defining 
 * only methods specific to event copying operations.
 */
public interface IEventCopyDialog extends IEventDialog {
    
    /**
     * Gets the name of the target calendar.
     *
     * @return the name of the target calendar
     */
    String getTargetCalendarName();
    
    /**
     * Gets the target start date and time.
     *
     * @return the target start date and time
     */
    LocalDateTime getTargetStartDateTime();
    
    /**
     * Gets the target end date and time.
     *
     * @return the target end date and time
     */
    LocalDateTime getTargetEndDateTime();
}
