import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import controller.CalendarController;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import view.GUICalendarPanel;
import view.GUICalendarSelectorPanel;
import view.GUIEventPanel;
import view.GUIExportImportPanel;
import view.ICalendarView;
import viewmodel.CalendarViewModel;
import viewmodel.EventViewModel;
import viewmodel.ExportImportViewModel;

/**
 * Mock implementation of ICalendarView for testing purposes.
 * This implementation avoids creating actual Swing components to prevent HeadlessException errors.
 */
public class MockGUIView implements ICalendarView {
  private LocalDate selectedDate = LocalDate.now();
  private String selectedCalendar = "";
  private List<String> calendarNames = new ArrayList<>();
  private List<Event> events = new ArrayList<>();
  private List<RecurringEvent> recurringEvents = new ArrayList<>();
  private Event currentEvent;
  private ICalendar currentCalendar;
  
  // View models - initialized with default or null values
  private final CalendarViewModel calendarViewModel = new CalendarViewModel();
  private EventViewModel eventViewModel = null;
  private final ExportImportViewModel exportImportViewModel = new ExportImportViewModel();
  
  // Mock panels - we don't actually create Swing components to avoid HeadlessException
  private final GUICalendarPanel calendarPanel = null;
  private final GUIEventPanel eventPanel = null;
  private final GUICalendarSelectorPanel calendarSelectorPanel = null;
  private final GUIExportImportPanel exportImportPanel = null;
  
  // Capture messages for verification in tests
  private String lastMessage = "";
  private String lastError = "";
  private String lastCommand = "";
  
  /**
   * Default constructor
   */
  public MockGUIView() {
    // No initialization required
  }
  
  /**
   * Constructor that accepts a controller for EventViewModel initialization
   */
  public MockGUIView(CalendarController controller) {
    if (controller != null) {
      this.eventViewModel = new EventViewModel(controller);
    }
  }
  
  /**
   * Display the GUI (mock implementation does nothing to avoid HeadlessException)
   */
  public void displayGUI() {
    // Do nothing, since we don't want to display actual GUI
  }

  /**
   * @return the calendar panel (null in mock implementation)
   */
  public GUICalendarPanel getCalendarPanel() {
    return calendarPanel;
  }

  /**
   * @return the event panel (null in mock implementation)
   */
  public GUIEventPanel getEventPanel() {
    return eventPanel;
  }

  /**
   * @return the calendar selector panel (null in mock implementation)
   */
  public GUICalendarSelectorPanel getCalendarSelectorPanel() {
    return calendarSelectorPanel;
  }

  /**
   * @return the export/import panel (null in mock implementation)
   */
  public GUIExportImportPanel getExportImportPanel() {
    return exportImportPanel;
  }

  /**
   * @return the calendar view model
   */
  public CalendarViewModel getCalendarViewModel() {
    return calendarViewModel;
  }

  /**
   * @return the event view model
   */
  public EventViewModel getEventViewModel() {
    return eventViewModel;
  }

  /**
   * @return the export/import view model
   */
  public ExportImportViewModel getExportImportViewModel() {
    return exportImportViewModel;
  }

  @Override
  public void updateCalendarView(ICalendar calendar) {
    this.currentCalendar = calendar;
  }

  @Override
  public void updateEventList(List<Event> events) {
    this.events = new ArrayList<>(events);
  }

  @Override
  public void updateRecurringEventList(List<RecurringEvent> recurringEvents) {
    this.recurringEvents = new ArrayList<>(recurringEvents);
  }

  @Override
  public void showEventDetails(Event event) {
    this.currentEvent = event;
  }

  @Override
  public void clearEventDetails() {
    this.currentEvent = null;
  }

  @Override
  public void updateCalendarList(List<String> calendarNames) {
    this.calendarNames = new ArrayList<>(calendarNames);
  }

  @Override
  public String getSelectedCalendar() {
    return selectedCalendar;
  }

  @Override
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  @Override
  public void setSelectedDate(LocalDate date) {
    this.selectedDate = date;
  }

  @Override
  public void refreshView() {
    // Do nothing in the mock
  }

  @Override
  public void displayMessage(String message) {
    this.lastMessage = message;
  }

  @Override
  public void displayError(String error) {
    this.lastError = error;
  }
  
  @Override
  public void setSelectedCalendar(String calendarName) {
    this.selectedCalendar = calendarName;
  }
  
  /**
   * Required by ICalendarView. For the mock implementation, this does the same 
   * as setSelectedDate.
   */
  @Override
  public void updateSelectedDate(LocalDate date) {
    setSelectedDate(date);
  }
  
  /**
   * Required by ICalendarView. For testing, this returns a fixed command.
   * Call setNextCommand() to change what this returns.
   */
  @Override
  public String readCommand() {
    return lastCommand;
  }
  
  /**
   * Set the command that will be returned by readCommand()
   * 
   * @param command the command to return on next readCommand call
   */
  public void setNextCommand(String command) {
    this.lastCommand = command;
  }
  
  // Additional methods for testing
  
  public String getLastMessage() {
    return lastMessage;
  }
  
  public String getLastError() {
    return lastError;
  }
  
  public List<Event> getEvents() {
    return events;
  }
  
  public List<RecurringEvent> getRecurringEvents() {
    return recurringEvents;
  }
  
  public Event getCurrentEvent() {
    return currentEvent;
  }
  
  public ICalendar getCurrentCalendar() {
    return currentCalendar;
  }
} 