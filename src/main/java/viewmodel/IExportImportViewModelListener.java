package viewmodel;

/**
 * Interface for listeners that want to be notified of changes in the ExportImportViewModel.
 * This interface defines the contract for objects that need to respond to calendar
 * import/export operations in the application's view model layer.
 *
 * <p>The listener methods are called when:
 * <ul>
 *   <li>Calendar data is successfully imported</li>
 *   <li>Calendar data is successfully exported</li>
 *   <li>Errors occur during import/export operations</li>
 * </ul>
 *
 * <p>Implementing classes should handle these notifications appropriately to maintain
 * UI consistency and provide feedback to users about the success or failure of
 * import/export operations.
 */
public interface IExportImportViewModelListener {

  /**
   * Called when calendar data is successfully imported.
   * Implementers should update their UI or state to reflect the newly imported data
   * and provide appropriate feedback to the user.
   *
   * @param message A success message describing the import operation result
   */
  void onImportSuccess(String message);

  /**
   * Called when calendar data is successfully exported.
   * Implementers should provide appropriate feedback to the user about the
   * successful completion of the export operation.
   */
  void onExportSuccess();

  /**
   * Called when an error occurs during import/export operations.
   * Implementers should handle the error appropriately, typically by displaying
   * the error message to the user or logging it.
   *
   * @param error A string describing the error that occurred
   */
  void onError(String error);
}