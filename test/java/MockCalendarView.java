import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import view.GUICalendarPanel.CalendarPanelListener;
import view.GUICalendarSelectorPanel.CalendarSelectorListener;
import view.GUIEventPanel.EventPanelListener;
import view.GUIExportImportPanel.ExportImportListener;
import view.ICalendarView;
import viewmodel.CalendarViewModel;
import viewmodel.EventViewModel;
import viewmodel.ExportImportViewModel;

/**
 * Mock implementation of ICalendarView for testing purposes.
 * This implementation does not depend on any Swing/AWT components,
 * making it suitable for headless testing environments.
 */
public class MockCalendarView implements ICalendarView {
    private String lastMessage;
    private String lastError;
    private String selectedCalendar;
    private LocalDate selectedDate;
    private Event currentEvent;
    private List<String> calendarNames = new ArrayList<>();
    private List<Event> events = new ArrayList<>();
    private List<RecurringEvent> recurringEvents = new ArrayList<>();
    private List<CalendarSelectorListener> calendarSelectorListeners = new ArrayList<>();
    private List<EventPanelListener> eventPanelListeners = new ArrayList<>();
    private List<CalendarPanelListener> calendarPanelListeners = new ArrayList<>();
    private List<ExportImportListener> exportImportListeners = new ArrayList<>();
    
    // Models for testing
    private CalendarViewModel calendarViewModel = new CalendarViewModel();
    private EventViewModel eventViewModel = new EventViewModel(null);
    private ExportImportViewModel exportImportViewModel = new ExportImportViewModel();
    
    // Tracking flags for method calls
    private boolean updateEventsCalled;
    private boolean clearEventDetailsCalled;
    private boolean showEventDetailsCalled;
    private boolean updateCalendarListCalled;
    private boolean updateCalendarViewCalled;
    private boolean displayEventCalled;
    private boolean displayRecurringEventCalled;
    private boolean updateStatusCalled;
    private boolean refreshCalendarViewCalled;
    private boolean refreshEventViewCalled;

    public MockCalendarView() {
        this.selectedDate = LocalDate.now();
    }

    @Override
    public String readCommand() {
        return "";
    }

    @Override
    public void displayMessage(String message) {
        this.lastMessage = message;
        System.out.println("MockCalendarView.displayMessage: " + message);
    }

    @Override
    public void displayError(String errorMessage) {
        this.lastError = errorMessage;
        System.out.println("MockCalendarView.displayError: " + errorMessage);
    }

    @Override
    public void updateCalendarView(ICalendar calendar) {
        updateCalendarViewCalled = true;
    }

    @Override
    public void updateEventList(List<Event> events) {
        this.events = new ArrayList<>(events);
        updateEventsCalled = true;
    }

    @Override
    public void updateRecurringEventList(List<RecurringEvent> recurringEvents) {
        this.recurringEvents = new ArrayList<>(recurringEvents);
    }

    @Override
    public void showEventDetails(Event event) {
        this.currentEvent = event;
        showEventDetailsCalled = true;
    }

    @Override
    public void clearEventDetails() {
        this.currentEvent = null;
        clearEventDetailsCalled = true;
    }

    @Override
    public void updateCalendarList(List<String> calendarNames) {
        this.calendarNames = new ArrayList<>(calendarNames);
        updateCalendarListCalled = true;
    }

    @Override
    public void setSelectedCalendar(String calendarName) {
        this.selectedCalendar = calendarName;
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
        // Nothing to do in mock
    }

    @Override
    public void updateSelectedDate(LocalDate date) {
        this.selectedDate = date;
    }

    @Override
    public void showErrorMessage(String message) {
        this.lastError = message;
        System.out.println("MockCalendarView.showErrorMessage: " + message);
    }

    @Override
    public void showInfoMessage(String message) {
        this.lastMessage = message;
        System.out.println("MockCalendarView.showInfoMessage: " + message);
    }

    @Override
    public void addCalendarSelectorListener(CalendarSelectorListener listener) {
        calendarSelectorListeners.add(listener);
    }

    @Override
    public void addEventPanelListener(EventPanelListener listener) {
        eventPanelListeners.add(listener);
    }

    @Override
    public void addCalendarPanelListener(CalendarPanelListener listener) {
        calendarPanelListeners.add(listener);
    }

    @Override
    public void addExportImportListener(ExportImportListener listener) {
        exportImportListeners.add(listener);
    }

    @Override
    public void updateCalendarName(String name) {
        // Mock implementation
    }

    @Override
    public void clearEvents() {
        this.events.clear();
        this.recurringEvents.clear();
    }

    @Override
    public Event getCurrentEvent() {
        return currentEvent;
    }

    @Override
    public void displayEvent(Event event) {
        this.currentEvent = event;
        displayEventCalled = true;
    }

    @Override
    public void displayRecurringEvent(RecurringEvent event) {
        displayRecurringEventCalled = true;
    }

    @Override
    public void clearForm() {
        this.currentEvent = null;
    }

    @Override
    public void updateEventList(LocalDate date) {
        updateEventsCalled = true;
    }

    @Override
    public void updateEventListRange(LocalDate startDate, LocalDate endDate, String filter) {
        updateEventsCalled = true;
    }
    
    @Override
    public void updateEvents(LocalDate date, List<Event> events) {
        this.events = new ArrayList<>(events);
        updateEventsCalled = true;
    }
    
    @Override
    public void updateStatus(LocalDate date, boolean isBusy, int eventCount) {
        updateStatusCalled = true;
    }
    
    @Override
    public void refreshCalendarView() {
        refreshCalendarViewCalled = true;
    }
    
    @Override
    public void refreshEventView() {
        refreshEventViewCalled = true;
    }
    
    @Override
    public CalendarViewModel getCalendarViewModel() {
        return calendarViewModel;
    }
    
    @Override
    public EventViewModel getEventViewModel() {
        return eventViewModel;
    }
    
    @Override
    public ExportImportViewModel getExportImportViewModel() {
        return exportImportViewModel;
    }
    
    // Helper methods for testing
    
    /**
     * Get the last message displayed.
     * 
     * @return the last message
     */
    public String getLastMessage() {
        return lastMessage;
    }
    
    /**
     * Get the last error message displayed.
     * 
     * @return the last error message
     */
    public String getLastError() {
        return lastError;
    }
    
    /**
     * Check if updateEvents was called.
     * 
     * @return true if the method was called
     */
    public boolean wasUpdateEventsCalled() {
        return updateEventsCalled;
    }
    
    /**
     * Check if showEventDetails was called.
     * 
     * @return true if the method was called
     */
    public boolean wasShowEventDetailsCalled() {
        return showEventDetailsCalled;
    }
    
    /**
     * Check if updateCalendarList was called.
     * 
     * @return true if the method was called
     */
    public boolean wasUpdateCalendarListCalled() {
        return updateCalendarListCalled;
    }
    
    /**
     * Check if updateCalendarView was called.
     * 
     * @return true if the method was called
     */
    public boolean wasUpdateCalendarViewCalled() {
        return updateCalendarViewCalled;
    }
    
    /**
     * Check if updateStatus was called.
     * 
     * @return true if the method was called
     */
    public boolean wasUpdateStatusCalled() {
        return updateStatusCalled;
    }
    
    /**
     * Check if refreshCalendarView was called.
     * 
     * @return true if the method was called
     */
    public boolean wasRefreshCalendarViewCalled() {
        return refreshCalendarViewCalled;
    }
    
    /**
     * Check if refreshEventView was called.
     * 
     * @return true if the method was called
     */
    public boolean wasRefreshEventViewCalled() {
        return refreshEventViewCalled;
    }
    
    /**
     * Reset all tracking flags.
     */
    public void resetTracking() {
        updateEventsCalled = false;
        clearEventDetailsCalled = false;
        showEventDetailsCalled = false;
        updateCalendarListCalled = false;
        updateCalendarViewCalled = false;
        displayEventCalled = false;
        displayRecurringEventCalled = false;
        updateStatusCalled = false;
        refreshCalendarViewCalled = false;
        refreshEventViewCalled = false;
        lastError = null;
        lastMessage = null;
    }
    
    /**
     * Clear the last error message.
     */
    public void clearError() {
        lastError = null;
    }
    
    /**
     * Trigger a file import from the mock view.
     * 
     * @param file the file to import
     */
    public void triggerImport(File file) {
        for (ExportImportListener listener : exportImportListeners) {
            listener.onImport(file);
        }
    }
    
    /**
     * Trigger a file export from the mock view.
     * 
     * @param file the file to export to
     */
    public void triggerExport(File file) {
        for (ExportImportListener listener : exportImportListeners) {
            listener.onExport(file);
        }
    }
    
    /**
     * Trigger a calendar selection event.
     * 
     * @param calendarName the name of the calendar to select
     */
    public void triggerCalendarSelected(String calendarName) {
        for (CalendarSelectorListener listener : calendarSelectorListeners) {
            listener.onCalendarSelected(calendarName);
        }
    }
    
    /**
     * Trigger a date selection event.
     * 
     * @param date the date to select
     */
    public void triggerDateSelected(LocalDate date) {
        for (CalendarPanelListener listener : calendarPanelListeners) {
            listener.onDateSelected(date);
        }
    }
    
    /**
     * Trigger an event selection event.
     * 
     * @param event the event to select
     */
    public void triggerEventSelected(Event event) {
        for (CalendarPanelListener listener : calendarPanelListeners) {
            listener.onEventSelected(event);
        }
    }
}