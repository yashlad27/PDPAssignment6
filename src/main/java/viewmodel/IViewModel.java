package viewmodel;

/**
 * Base interface for all ViewModels in the MVVM architecture.
 * This interface defines common functionality that all ViewModels should implement.
 */
public interface IViewModel {
  /**
   * Initializes the ViewModel with necessary data and setup.
   */
  void initialize();

  /**
   * Cleans up any resources used by the ViewModel.
   */
  void dispose();

  /**
   * Refreshes the ViewModel's state.
   */
  void refresh();
} 