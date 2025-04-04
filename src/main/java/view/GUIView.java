package view;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.*;

import controller.CalendarController;
import controller.GUIController;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import viewmodel.CalendarViewModel;
import viewmodel.EventViewModel;
import viewmodel.ExportImportViewModel;

/**
 * Main GUI view class that integrates all GUI components and implements both ICalendarView and IGUIView interfaces.
 * This class provides the main window and layout for the calendar application.
 */
public class GUIView extends JFrame implements ICalendarView, IGUIView {
  private static final Color THEME_COLOR = new Color(0x4a86e8);
  private static final Color BORDER_COLOR = new Color(0xcccccc);
  private static final int SIDEBAR_WIDTH = 180;

  private final GUICalendarPanel calendarPanel;
  private final GUIEventPanel eventPanel;
  private final GUICalendarSelectorPanel calendarSelectorPanel;
  private final GUIExportImportPanel exportImportPanel;
  private final JTextArea messageArea;
  private final CalendarController controller;
  private CalendarViewModel calendarViewModel;
  private EventViewModel eventViewModel;
  private ExportImportViewModel exportImportViewModel;
  private GUIController guiController;

  /**
   * Constructs a new GUIView.
   *
   * @param controller the calendar controller
   */
  public GUIView(CalendarController controller) {
    this.controller = controller;
    System.out.println("Creating GUIView...");
    
    // Status bar removed
    
    // Initialize view models
    calendarViewModel = new CalendarViewModel();
    eventViewModel = new EventViewModel(controller);
    exportImportViewModel = new ExportImportViewModel();

    // Set up the main window
    setTitle("Calendar Application");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setPreferredSize(new Dimension(800, 600));

    // Initialize components
    calendarPanel = new GUICalendarPanel();
    eventPanel = new GUIEventPanel();
    calendarSelectorPanel = new GUICalendarSelectorPanel();
    exportImportPanel = new GUIExportImportPanel();
    messageArea = new JTextArea(3, 40);
    messageArea.setEditable(false);
    messageArea.setLineWrap(true);
    messageArea.setWrapStyleWord(true);

    // Set up layout
    setupLayout();

    // Set up listeners
    setupListeners();

    pack();
    setLocationRelativeTo(null);
    System.out.println("GUIView created successfully.");
  }

  /**
   * Shows the GUI window.
   */
  public void displayGUI() {
    System.out.println("Displaying GUI window...");
    pack();
    setVisible(true);
  }

  /**
   * Sets up the listeners for all ViewModels.
   */
  private void setupListeners() {
    // Set up calendar selector listener for calendar selection and creation
    calendarSelectorPanel.addCalendarSelectorListener(new GUICalendarSelectorPanel.CalendarSelectorListener() {
      @Override
      public void onCalendarSelected(ICalendar calendar) {
        System.out.println("[DEBUG] Calendar selected: " + (calendar != null ? calendar.toString() : "null"));
        if (calendar != null) {
          controller.setSelectedCalendar(calendar);
          eventPanel.clearForm();
          refreshView();
        }
      }

      @Override
      public void onCalendarSelected(String calendarName) {
        System.out.println("[DEBUG] Calendar selected by name: " + calendarName);
        if (calendarName != null) {
          controller.setSelectedCalendarByName(calendarName);
          eventPanel.clearForm();
          refreshView();
        }
      }

      @Override
      public void onCalendarCreated(String name, String timezone) {
        System.out.println("[DEBUG] Attempting to create calendar: " + name + " with timezone: " + timezone);
        try {
          boolean success = controller.createCalendar(name, timezone);
          System.out.println("[DEBUG] Calendar creation result: " + (success ? "success" : "failed"));
          if (success) {
            controller.updateCalendarList();
            controller.setSelectedCalendarByName(name);
            displayMessage("Calendar created successfully: " + name);
            refreshView();
          } else {
            showErrorMessage("Could not create calendar");
          }
        } catch (Exception ex) {
          System.out.println("[DEBUG] Calendar creation error: " + ex.getMessage());
          showErrorMessage("Could not create calendar: " + ex.getMessage());
        }
      }
    });

    // Set up event panel listener
    eventPanel.addEventPanelListener(new GUIEventPanel.EventPanelListener() {
      @Override
      public void onEventSaved(EventFormData formData) {
        System.out.println("[DEBUG] Attempting to save event. Recurring: " + formData.isRecurring());
        
        try {
          ICalendar currentCalendar = controller.getCurrentCalendar();
          System.out.println("[DEBUG] Current calendar: " + (currentCalendar != null ? currentCalendar.toString() : "null"));

          if (currentCalendar == null) {
            showErrorMessage("Please select a calendar first");
            return;
          }

          // Pass the form data to the GUI controller
          if (guiController != null) {
            guiController.onEventSaved(formData);
          } else {
            showErrorMessage("GUI Controller not initialized");
          }
        } catch (Exception e) {
          showErrorMessage("Error creating event: " + e.getMessage());
          e.printStackTrace();
        }
      }

      @Override
      public void onEventCancelled() {
        System.out.println("[DEBUG] Event creation cancelled");
        eventPanel.clearForm();
      }
      
      @Override
      public void onEventCopied(String targetCalendarName, LocalDateTime targetStartDateTime, LocalDateTime targetEndDateTime) {
        System.out.println("[DEBUG] Attempting to copy event to calendar: " + targetCalendarName);
        
        try {
          // Get the current event from the event panel
          Event currentEvent = eventPanel.getCurrentEvent();
          if (currentEvent == null) {
            showErrorMessage("No event selected to copy");
            return;
          }
          
          // Call the controller to handle the copy operation
          if (guiController != null) {
            boolean success = guiController.copyEvent(currentEvent, targetCalendarName, targetStartDateTime, targetEndDateTime);
            if (success) {
              showInfoMessage("Event copied successfully");
              eventPanel.clearForm();
              refreshView(); // Refresh to show the copied event
            } else {
              showErrorMessage("Failed to copy event");
            }
          } else {
            showErrorMessage("Controller not initialized");
          }
        } catch (Exception e) {
          System.out.println("[DEBUG] Event copy error: " + e.getMessage());
          showErrorMessage("Error copying event: " + e.getMessage());
        }
      }

      @Override
      public void onEventUpdated(EventFormData formData) {
        System.out.println("[DEBUG] Attempting to update event. Recurring: " + formData.isRecurring());
        
        try {
          ICalendar currentCalendar = controller.getCurrentCalendar();
          if (currentCalendar == null) {
            showErrorMessage("Please select a calendar first");
            return;
          }

          // Pass the form data to the GUI controller
          if (guiController != null) {
            guiController.onEventUpdated(formData);
          } else {
            showErrorMessage("GUI Controller not initialized");
          }
        } catch (Exception e) {
          System.out.println("[DEBUG] Event update error: " + e.getMessage());
          showErrorMessage("Error updating event: " + e.getMessage());
          e.printStackTrace();
        }
      }

      private void handleEventResult(String result) {
        System.out.println("[DEBUG] Handling event result: " + result);
        if (result.startsWith("Error")) {
          showErrorMessage(result);
        } else {
          displayMessage(result);
          refreshView();
          // Update the event list for the selected date
          LocalDate selectedDate = calendarPanel.getSelectedDate();
          System.out.println("[DEBUG] Selected date for refresh: " + selectedDate);
          if (selectedDate != null) {
            try {
              ICalendar currentCalendar = controller.getCurrentCalendar();
              System.out.println("[DEBUG] Current calendar for refresh: " + (currentCalendar != null ? currentCalendar.toString() : "null"));
              if (currentCalendar != null) {
                List<Event> events = currentCalendar.getEventsOnDate(selectedDate);
                System.out.println("[DEBUG] Events found for date: " + events.size());
                calendarPanel.updateEvents(currentCalendar.getAllEvents());
                calendarPanel.updateRecurringEvents(currentCalendar.getAllRecurringEvents());
                calendarPanel.updateEventList(selectedDate);
                System.out.println("[DEBUG] View updated with events");
              }
            } catch (Exception e) {
              System.out.println("[DEBUG] Error updating event list: " + e.getMessage());
              showErrorMessage("Error updating event list: " + e.getMessage());
            }
          }
        }
      }
    });

    // Set up event listeners for calendar panel
    calendarPanel.addCalendarPanelListener(new GUICalendarPanel.CalendarPanelListener() {
      @Override
      public void onDateSelected(LocalDate date) {
        if (date != null) {
          eventPanel.setDate(date);
        }
      }

      @Override
      public void onEventSelected(Event event) {
        if (event != null) {
          eventPanel.displayEvent(event);
        }
      }

      @Override
      public void onRecurringEventSelected(RecurringEvent event) {
        if (event != null) {
          eventPanel.displayRecurringEvent(event);
        }
      }

      @Override
      public void onStatusRequested(LocalDate date) {
        // Status handling is done in the panel itself
      }

      @Override
      public void onEventsListRequested(LocalDate date) {
        calendarPanel.updateEventList(date);
      }

      @Override
      public void onDateRangeSelected(LocalDate startDate, LocalDate endDate) {
        calendarPanel.updateEventListRange(startDate, endDate, null);
      }

      @Override
      public void onEditEvent(Event event) {
        System.out.println("[DEBUG] Edit event requested in view: " + event.getSubject());
        if (guiController != null) {
          guiController.editEvent(event);
        }
      }

      @Override
      public void onCopyEvent(Event event) {
        System.out.println("[DEBUG] Copy event requested in view: " + event.getSubject());
        if (guiController != null) {
          guiController.showCopyEventDialog(event);
        }
      }

      @Override
      public void onPrintEvent(Event event) {
        System.out.println("[DEBUG] Print event requested in view: " + event.getSubject());
        if (guiController != null) {
          guiController.printEvent(event);
        }
      }
    });

    // Set up export/import listeners
    exportImportPanel.addExportImportListener(new GUIExportImportPanel.ExportImportListener() {
      @Override
      public void onImport(File file) {
        if (file != null) {
          try {
            controller.importCalendarFromCSV(file.getAbsolutePath());
            // Success message will be shown by the ExportImportViewModel
          } catch (Exception ex) {
            exportImportPanel.showErrorMessage("Import failed: " + ex.getMessage());
          }
        }
      }

      @Override
      public void onExport(File file) {
        if (file != null) {
          try {
            controller.exportCalendarToCSV(file.getAbsolutePath());
            exportImportPanel.showExportSuccess();
          } catch (Exception ex) {
            exportImportPanel.showErrorMessage("Export failed: " + ex.getMessage());
          }
        }
      }
    });
  }

  /**
   * Sets up the layout of the GUI components.
   */
  private void setupLayout() {
    // Custom title bar panel with consistent styling
    JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
    titleBar.setBackground(THEME_COLOR);
    titleBar.setPreferredSize(new Dimension(getWidth(), 40));
    titleBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    JLabel titleLabel = new JLabel("Calendar Application");
    titleLabel.setForeground(Color.WHITE);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));
    titleBar.add(titleLabel);

    // Main content panel with proper spacing
    JPanel contentPanel = new JPanel();
    contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    contentPanel.setLayout(new BorderLayout(10, 10));

    // Create the split pane with proper settings
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    mainSplitPane.setBorder(null);
    mainSplitPane.setDividerSize(5);
    mainSplitPane.setResizeWeight(0.0); // Fix left panel width
    mainSplitPane.setMinimumSize(new Dimension(800, 500));

    // Left sidebar with proper constraints
    JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
    leftPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
    leftPanel.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 0));
    leftPanel.setBackground(new Color(0xf8f8f8));
    leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

    // Calendar selector at top of left panel
    calendarSelectorPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 200));
    calendarSelectorPanel.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 150));

    // Import/Export in the center of left panel (moved up from bottom)
    exportImportPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 200));
    exportImportPanel.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 150));

    // Create a vertical panel to hold components in the left sidebar
    JPanel leftComponentsPanel = new JPanel();
    leftComponentsPanel.setLayout(new BoxLayout(leftComponentsPanel, BoxLayout.Y_AXIS));
    leftComponentsPanel.setBackground(new Color(0xf8f8f8));

    // Add components to left panel in vertical order
    leftComponentsPanel.add(calendarSelectorPanel);
    leftComponentsPanel.add(Box.createVerticalStrut(10)); // Add spacing
    leftComponentsPanel.add(exportImportPanel);
    leftComponentsPanel.add(Box.createVerticalGlue()); // Push everything up

    leftPanel.add(leftComponentsPanel, BorderLayout.CENTER);

    // Right side with a horizontal split between calendar and event panel
    JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

    // Create a secondary split pane to separate calendar and event panel horizontally
    JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    rightSplitPane.setBorder(null);
    rightSplitPane.setDividerSize(5);
    rightSplitPane.setResizeWeight(0.7); // Give more weight to calendar

    // Calendar panel with proper size
    JPanel calendarContainer = new JPanel(new BorderLayout());
    calendarContainer.add(calendarPanel, BorderLayout.CENTER);
    calendarContainer.setPreferredSize(new Dimension(550, 0));

    // Event panel with fixed width
    eventPanel.setPreferredSize(new Dimension(350, 0));
    JScrollPane eventScrollPane = new JScrollPane(eventPanel);
    eventScrollPane.setBorder(null);

    // Add components to right split pane
    rightSplitPane.setLeftComponent(calendarContainer);
    rightSplitPane.setRightComponent(eventScrollPane);

    rightPanel.add(rightSplitPane, BorderLayout.CENTER);

    // Set up main split pane
    mainSplitPane.setLeftComponent(leftPanel);
    mainSplitPane.setRightComponent(rightPanel);

    // Add components to main window
    contentPanel.add(mainSplitPane, BorderLayout.CENTER);
    add(titleBar, BorderLayout.NORTH);
    add(contentPanel, BorderLayout.CENTER);

    // Add window resize listener
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        SwingUtilities.invokeLater(() -> {
          revalidate();
          repaint();
        });
      }
    });
  }

  /**
   * Gets the calendar panel.
   *
   * @return the calendar panel
   */
  public GUICalendarPanel getCalendarPanel() {
    return calendarPanel;
  }

  /**
   * Sets the controller for this view.
   *
   * @param controller the controller to set
   */
  public void setController(CalendarController controller) {
    // No-op since we already have the controller from the constructor
  }

  /**
   * Sets the GUI controller for this view.
   *
   * @param guiController the GUI controller to set
   */
  public void setGUIController(GUIController guiController) {
    this.guiController = guiController;
  }

  /**
   * Shows an event edit dialog.
   *
   * @param event       the event to edit
   * @param isRecurring whether the event is recurring
   */
  public void showEventEditDialog(Event event, boolean isRecurring) {
    EventEditDialog dialog = new EventEditDialog(this, event, isRecurring);
    boolean confirmed = dialog.showDialog();

    if (confirmed) {
      // Update the event with the new values
      String subject = dialog.getSubject();
      String location = dialog.getEventLocation();
      String description = dialog.getDescription();

      // Create a copy of the event with the new values
      Event updatedEvent = event;
      updatedEvent.setSubject(subject);
      updatedEvent.setLocation(location);
      updatedEvent.setDescription(description);

      // Notify the controller that the event has been updated
      if (guiController != null) {
        guiController.onEventUpdated(updatedEvent);
      }
    }
  }

  /**
   * Shows an event copy dialog.
   *
   * @param event     the event to copy
   * @param calendars the list of available calendars
   */
  public void showEventCopyDialog(Event event, List<ICalendar> calendars) {
    EventCopyDialog dialog = new EventCopyDialog(this, event, calendars);
    boolean confirmed = dialog.showDialog();

    if (confirmed) {
      // Get the target calendar name
      String targetCalendarName = dialog.getTargetCalendarName();

      // Notify the controller that the event should be copied
      if (guiController != null) {
        String result = guiController.executeCopyEvent(event, targetCalendarName);
        showInfoMessage(result);
      }
    }
  }

  /**
   * Gets the event panel.
   *
   * @return the event panel
   */
  public GUIEventPanel getEventPanel() {
    return eventPanel;
  }

  /**
   * Gets the calendar selector panel.
   *
   * @return the calendar selector panel
   */
  public GUICalendarSelectorPanel getCalendarSelectorPanel() {
    return calendarSelectorPanel;
  }

  /**
   * Gets the export/import panel.
   *
   * @return the export/import panel
   */
  public GUIExportImportPanel getExportImportPanel() {
    return exportImportPanel;
  }

  /**
   * Gets the calendar view model.
   *
   * @return the calendar view model
   */
  public CalendarViewModel getCalendarViewModel() {
    return calendarViewModel;
  }

  /**
   * Gets the event view model.
   *
   * @return the event view model
   */
  public EventViewModel getEventViewModel() {
    return eventViewModel;
  }

  /**
   * Gets the export/import view model.
   *
   * @return the export/import view model
   */
  public ExportImportViewModel getExportImportViewModel() {
    return exportImportViewModel;
  }

  /**
   * Updates the calendar view.
   *
   * @param calendar the calendar to display
   */
  @Override
  public void updateCalendarView(ICalendar calendar) {
    if (calendar != null) {
      calendarPanel.updateCalendar(calendar);
      calendarSelectorPanel.setSelectedCalendar(calendar.toString());
    }
  }

  @Override
  public void updateEventList(List<Event> events) {
    if (events != null) {
      calendarPanel.updateEvents(events);
      calendarPanel.updateEventList(LocalDate.now());
    }
  }

  @Override
  public void updateRecurringEventList(List<RecurringEvent> recurringEvents) {
    if (recurringEvents != null) {
      calendarPanel.updateRecurringEvents(recurringEvents);
    }
  }

  @Override
  public void showEventDetails(Event event) {
    System.out.println("[DEBUG] GUIView.showEventDetails called for event: " + (event != null ? event.getSubject() : "null"));
    eventPanel.displayEvent(event);
  }

  @Override
  public void clearEventDetails() {
    eventPanel.clearForm();
  }

  @Override
  public void updateCalendarList(List<String> calendarNames) {
    calendarSelectorPanel.updateCalendarList(calendarNames);
  }

  @Override
  public void setSelectedCalendar(String calendarName) {
    calendarSelectorPanel.setSelectedCalendar(calendarName);
  }

  @Override
  public String getSelectedCalendar() {
    return calendarSelectorPanel.getSelectedCalendarName();
  }

  @Override
  public LocalDate getSelectedDate() {
    return calendarPanel.getSelectedDate();
  }

  @Override
  public void setSelectedDate(LocalDate date) {
    calendarPanel.setSelectedDate(date);
  }

  @Override
  public void refreshView() {
    SwingUtilities.invokeLater(() -> {
      calendarPanel.refresh();
      eventPanel.refresh();
      calendarSelectorPanel.refresh();
      revalidate();
      repaint();
    });
  }

  @Override
  public String readCommand() {
    // Not used in GUI mode
    return null;
  }

  @Override
  public void displayMessage(String message) {
    messageArea.setText(message);
  }

  @Override
  public void displayError(String error) {
    JOptionPane.showMessageDialog(
            this,
            error,
            "Error",
            JOptionPane.ERROR_MESSAGE
    );
  }
  
  /**
   * Shows an error message dialog.
   *
   * @param message the error message to display
   */
  public void showErrorMessage(String message) {
    displayError(message);
  }
  
  /**
   * Shows an information message dialog.
   *
   * @param message the information message to display
   */
  public void showInfoMessage(String message) {
    JOptionPane.showMessageDialog(
            this,
            message,
            "Information",
            JOptionPane.INFORMATION_MESSAGE
    );
  }

  // CalendarViewModelListener implementation
  public void onCalendarChanged(ICalendar calendar) {
    calendarPanel.updateCalendar(calendar);
    exportImportViewModel.setCurrentCalendar(calendar);
  }

  public void onDateSelected(LocalDate date) {
    calendarPanel.setSelectedDate(date);
  }

  public void onEventsUpdated(List<Event> events) {
    calendarPanel.updateEvents(events);
  }

  public void onRecurringEventsUpdated(List<RecurringEvent> recurringEvents) {
    calendarPanel.updateRecurringEvents(recurringEvents);
  }

  public void onCalendarListUpdated(List<String> calendarNames) {
    calendarSelectorPanel.updateCalendarList(calendarNames);
  }

  public void onError(String error) {
    displayError(error);
  }

  // EventViewModelListener implementation
  public void onEventSelected(Event event) {
    eventPanel.displayEvent(event);
  }

  public void onRecurringEventSelected(RecurringEvent event) {
    eventPanel.displayRecurringEvent(event);
  }

  public void onEventCreated(Event event) {
    displayMessage("Event created successfully");
  }

  public void onRecurringEventCreated(RecurringEvent event) {
    displayMessage("Recurring event created successfully");
  }

  public void onEventUpdated(Event event) {
    displayMessage("Event updated successfully");
  }

  public void onRecurringEventUpdated(RecurringEvent event) {
    displayMessage("Recurring event updated successfully");
  }

  public void onImportSuccess(String message) {
    System.out.println("[DEBUG] GUIView.onImportSuccess called with message: " + message);
    displayMessage(message);

    // Show success popup
    System.out.println("[DEBUG] Showing import success popup");
    exportImportPanel.showImportSuccess(message);

    // Refresh the calendar view to show the imported events
    LocalDate currentDate = calendarPanel.getSelectedDate();
    System.out.println("[DEBUG] Refreshing calendar view after import for date: " + currentDate);

    // Get the current calendar's events
    if (calendarViewModel.getCurrentCalendar() != null) {
      // Get all events for the current calendar
      List<Event> events = calendarViewModel.getCurrentCalendar().getAllEvents();
      System.out.println("[DEBUG] Found " + events.size() + " events in calendar after import");

      // Update the calendar panel with all events
      calendarPanel.updateEvents(events);

      // Force a complete refresh of the calendar display
      calendarPanel.updateCalendar(calendarViewModel.getCurrentCalendar());
      System.out.println("[DEBUG] Calendar panel updated with imported events");

      // Refresh the entire view
      refreshView();
    }
  }

  /**
   * Updates the selected date in the view.
   *
   * @param date the date to update to
   */
  @Override
  public void updateSelectedDate(LocalDate date) {
    if (date != null) {
      calendarPanel.setSelectedDate(date);
      eventPanel.setDate(date);
    }
  }

  /**
   * Updates the calendar display.
   */
  public void updateCalendarDisplay() {
    calendarPanel.refresh();
  }
} 