package view;

/**
 * Interface for notification display functionality. This interface defines methods for displaying
 * various types of messages to the user.
 */
public interface INotificationDisplay {

  /**
   * Displays a message to the user.
   *
   * @param message the message to display to the user
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param errorMessage the error message to display to the user
   */
  void displayError(String errorMessage);

} 