package view.dialog;

/**
 * Base interface for all event dialog implementations.
 * This follows the Interface Segregation Principle by defining
 * a minimal interface that all dialogs must implement.
 */
public interface IEventDialog {

  /**
   * Shows the dialog and returns whether the action was confirmed.
   *
   * @return true if the action was confirmed, false otherwise
   */
  boolean showDialog();
}
