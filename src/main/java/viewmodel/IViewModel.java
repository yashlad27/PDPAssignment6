package viewmodel;

/**
 * Base interface for all ViewModels in the Model-View-ViewModel (MVVM) architecture.
 *
 * <p> This interface defines common functionality that all ViewModels should implement,
 * establishing a standard contract for MVVM implementation. ViewModels serve as an intermediary
 * between the View and Model layers, providing data binding capabilities and separation of
 * concerns.</p>
 *
 * <p> In the MVVM pattern, ViewModels:</p>
 * <ul>
 *   <li>Expose data and commands from the Model to the View</li>
 *   <li>Handle user interactions from the View</li>
 *   <li>Manage the application's state relevant to a specific view</li>
 *   <li>Provide data validation and transformation logic</li>
 * </ul>
 *
 * <p> Implementing classes should be stateful and maintain the UI state
 * throughout the lifecycle of the associated view.</p>
 */
public interface IViewModel {

  /**
   * Initializes the ViewModel with necessary data and setup.
   *
   * <p> This method should be called when the associated View is created or activated.
   * Implementation should include:</p>
   * <ul>
   *   <li>Loading initial data from the Model layer</li>
   *   <li>Setting up data bindings</li>
   *   <li>Registering event listeners or observers</li>
   *   <li>Initializing any resources needed for the ViewModel operation</li>
   * </ul>
   *
   * <p> This method may be called multiple times during the application lifecycle,
   * so implementations should handle repeated initializations appropriately.</p>
   */
  void initialize();

  /**
   * Cleans up any resources used by the ViewModel.
   *
   * <p> This method should be called when the associated View is being destroyed
   * or deactivated. Implementation should include:</p>
   * <ul>
   *   <li>Unregistering event listeners or observers</li>
   *   <li>Releasing any held references to avoid memory leaks</li>
   *   <li>Cancelling any ongoing asynchronous operations</li>
   *   <li>Closing or disposing of any system resources</li>
   * </ul>
   *
   * <p> After this method is called, the ViewModel instance should not be used
   * unless {@link #initialize()} is called again.</p>
   */
  void dispose();

  /**
   * Refreshes the ViewModel's state.
   *
   * <p> This method should update the ViewModel with the latest data from the Model layer.
   * It differs from {@link #initialize()} in that it doesn't perform a complete setup but rather
   * updates existing data. Implementation should include:</p>
   * <ul>
   *   <li>Reloading data from the underlying Model</li>
   *   <li>Updating any calculated or derived properties</li>
   *   <li>Notifying the View of changes (through data binding or other mechanisms)</li>
   * </ul>
   *
   * <p> This method may be called in response to user actions, system events,
   * or when the underlying data has changed.</p>
   */
  void refresh();
}