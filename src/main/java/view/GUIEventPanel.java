package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import model.event.Event;
import model.event.RecurringEvent;
import utilities.TimeZoneHandler;

/**
 * Panel class that handles event creation, editing, and display.
 */
public class GUIEventPanel extends JPanel {

  /**
   * Public enum for mode.
   */
  public enum PanelMode {
    CREATE("Create Event"), EDIT("Edit Event"), VIEW("View Event");

    private final String displayName;

    PanelMode(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }

  private PanelMode currentMode = PanelMode.CREATE;
  private final JTextField subjectField;
  private final JTextArea descriptionArea;
  private final JSpinner dateSpinner;
  private final JSpinner startTimeSpinner;
  private final JSpinner endTimeSpinner;
  private final JTextField locationField;
  private final JCheckBox recurringCheckBox;
  private final JPanel recurringOptionsPanel;
  private final ArrayList<JCheckBox> weekdayCheckboxes;
  private final JSpinner occurrencesSpinner;
  private final JSpinner untilDateSpinner;
  private final JButton saveButton;
  private final JButton cancelButton;
  private final JButton editButton;

  // Mode indicator label
  private final JLabel modeLabel;

  // Copy options components
  private JPanel copyOptionsPanel;
  private JComboBox<String> targetCalendarCombo;
  private JSpinner targetDateSpinner;
  private JSpinner targetStartTimeSpinner;
  private JSpinner targetEndTimeSpinner;

  // Current event being edited or copied
  private Event currentEvent;
  private EventPanelListener listener;
  private final Map<JComponent, JLabel> errorLabels;

  private final JCheckBox allDayCheckBox;
  private JCheckBox privateEventCheckBox;
  private JCheckBox autoDeclineCheckBox;
  private final TimeZoneHandler timezoneHandler;

  // Define consistent colors
  private static final Color HEADER_COLOR = new Color(0x4a86e8);
  private static final Color HEADER_LIGHT_COLOR = new Color(0xe6f2ff);
  private static final Color TEXT_COLOR = new Color(0x333333);

  /**
   * Data class to hold event information.
   */
  public static class EventData {

    public String subject;
    public String location;
    public String description;
    public LocalTime startTime;
    public LocalTime endTime;
    public boolean isRecurring;
    public Set<DayOfWeek> weekdays;
    public int occurrences;
    public LocalDate untilDate;
    public LocalDate date;

    /**
     * Constructs and holds initial event data.
     */
    public EventData() {
      this.date = LocalDate.now();
      this.startTime = LocalTime.of(9, 0);
      this.endTime = LocalTime.of(10, 0);
      this.isRecurring = false;
      this.occurrences = 1;
    }
  }

  /**
   * Interface for event panel events. This interface defines callbacks for the controller to handle
   * UI events. It uses the EventFormData class to transfer data from the view to the controller
   * without exposing any business logic in the view layer.
   */
  public interface EventPanelListener {

    /**
     * Called when a new event is saved.
     *
     * @param formData the form data collected from UI components
     */
    void onEventSaved(EventFormData formData);

    /**
     * Called when event creation/editing is cancelled.
     */
    void onEventCancelled();

    List<String> getAvailableCalendarNames();

    /**
     * Called when an existing event is updated.
     *
     * @param formData the form data collected from UI components
     */
    void onEventUpdated(EventFormData formData);

    /**
     * Called when an event is copied.
     *
     * @param targetCalendarName  the name of the target calendar
     * @param targetStartDateTime the target start date/time
     * @param targetEndDateTime   the target end date/time
     */
    void onEventCopied(String targetCalendarName, LocalDateTime targetStartDateTime,
        LocalDateTime targetEndDateTime);
  }

  /**
   * Constructs a new GUIEventPanel.
   */
  public GUIEventPanel() {
    subjectField = new JTextField(20);
    dateSpinner = new JSpinner(new SpinnerDateModel());
    startTimeSpinner = new JSpinner(new SpinnerDateModel());
    endTimeSpinner = new JSpinner(new SpinnerDateModel());
    locationField = new JTextField(20);
    descriptionArea = new JTextArea(5, 20);
    allDayCheckBox = new JCheckBox("All Day Event");
    recurringCheckBox = new JCheckBox("Recurring Event");
    privateEventCheckBox = new JCheckBox("Private Event");
    autoDeclineCheckBox = new JCheckBox("Auto-decline");
    weekdayCheckboxes = new ArrayList<>();
    occurrencesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    untilDateSpinner = new JSpinner(new SpinnerDateModel());
    saveButton = new JButton("Save");
    cancelButton = new JButton("Cancel");
    editButton = new JButton("Edit");
    editButton.setVisible(false);

    saveButton.setVisible(true);
    cancelButton.setVisible(true);

    System.out.println("[DEBUG] All buttons created and set to visible");
    recurringOptionsPanel = new JPanel(new GridLayout(0, 1));

    errorLabels = new HashMap<>();
    timezoneHandler = new TimeZoneHandler();

    modeLabel = new JLabel(PanelMode.CREATE.getDisplayName());
    modeLabel.setFont(modeLabel.getFont().deriveFont(Font.BOLD, 14f));
    modeLabel.setForeground(HEADER_COLOR);

    setupCopyOptionsPanel();
    targetCalendarCombo = new JComboBox<>();
    targetDateSpinner = new JSpinner(new SpinnerDateModel());
    targetStartTimeSpinner = new JSpinner(new SpinnerDateModel());
    targetEndTimeSpinner = new JSpinner(new SpinnerDateModel());

    targetDateSpinner.setEditor(new JSpinner.DateEditor(targetDateSpinner, "MM/dd/yyyy"));
    targetStartTimeSpinner.setEditor(new JSpinner.DateEditor(targetStartTimeSpinner, "HH:mm"));
    targetEndTimeSpinner.setEditor(new JSpinner.DateEditor(targetEndTimeSpinner, "HH:mm"));

    copyOptionsPanel.setVisible(false);

    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
    startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));
    endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));

    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);

    setupComponents();
    setupLayout();
    setupListeners();
  }

  private void setupComponents() {
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
    ((JSpinner.DefaultEditor) dateSpinner.getEditor()).getTextField().setEditable(true);

    startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));
    endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));
    ((JSpinner.DefaultEditor) startTimeSpinner.getEditor()).getTextField().setEditable(true);
    ((JSpinner.DefaultEditor) endTimeSpinner.getEditor()).getTextField().setEditable(true);

    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);

    String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
        "Sunday"};
    for (String weekday : weekdays) {
      JCheckBox checkbox = new JCheckBox(weekday);
      weekdayCheckboxes.add(checkbox);
      recurringOptionsPanel.add(checkbox);
    }

    JPanel occurrencesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    occurrencesPanel.add(new JLabel("Number of occurrences:"));
    occurrencesPanel.add(occurrencesSpinner);
    recurringOptionsPanel.add(occurrencesPanel);

    JPanel untilPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    untilPanel.add(new JLabel("Until date:"));
    untilPanel.add(untilDateSpinner);
    recurringOptionsPanel.add(untilPanel);

    recurringOptionsPanel.setVisible(false);

    errorLabels.clear();
    JLabel subjectErrorLabel = new JLabel("");
    subjectErrorLabel.setForeground(Color.RED);
    subjectErrorLabel.setVisible(false);
    errorLabels.put(subjectField, subjectErrorLabel);

    JLabel timeErrorLabel = new JLabel("");
    timeErrorLabel.setForeground(Color.RED);
    timeErrorLabel.setVisible(false);
    errorLabels.put(endTimeSpinner, timeErrorLabel);

    JLabel weekdayErrorLabel = new JLabel("");
    weekdayErrorLabel.setForeground(Color.RED);
    weekdayErrorLabel.setVisible(false);
    errorLabels.put(weekdayCheckboxes.get(0), weekdayErrorLabel);

    JLabel untilDateErrorLabel = new JLabel("");
    untilDateErrorLabel.setForeground(Color.RED);
    untilDateErrorLabel.setVisible(false);
    errorLabels.put(untilDateSpinner, untilDateErrorLabel);
  }

  /**
   * This method is called during initialization to set up the copy options panel. Most
   * functionality has been moved to EnhancedEventCopyDialog as part of SOLID refactoring, but this
   * method is still referenced in setupComponents().
   */
  private void setupCopyOptionsPanel() {
    copyOptionsPanel = new JPanel(new GridBagLayout());
    copyOptionsPanel.setBorder(BorderFactory.createTitledBorder("Copy Options"));
    copyOptionsPanel.setVisible(false);
  }

  private void setupListeners() {
    editButton.addActionListener(e -> {
      System.out.println("[DEBUG] Edit button clicked for event: " + (currentEvent != null
          ? currentEvent.getSubject() : "null"));
      if (currentEvent != null) {
        setPanelMode(PanelMode.EDIT);
        enableFormEditing(true);

        subjectField.setText(currentEvent.getSubject());
        locationField.setText(currentEvent.getLocation());
        descriptionArea.setText(currentEvent.getDescription());

        LocalDateTime startDateTime = currentEvent.getStartDateTime();
        LocalDateTime endDateTime = currentEvent.getEndDateTime();

        Calendar cal = Calendar.getInstance();
        cal.set(startDateTime.getYear(), startDateTime.getMonthValue() - 1,
            startDateTime.getDayOfMonth());
        dateSpinner.setValue(cal.getTime());

        cal.set(Calendar.HOUR_OF_DAY, startDateTime.getHour());
        cal.set(Calendar.MINUTE, startDateTime.getMinute());
        startTimeSpinner.setValue(cal.getTime());

        cal.set(Calendar.HOUR_OF_DAY, endDateTime.getHour());
        cal.set(Calendar.MINUTE, endDateTime.getMinute());
        endTimeSpinner.setValue(cal.getTime());

        allDayCheckBox.setSelected(currentEvent.isAllDay());
        startTimeSpinner.setEnabled(!currentEvent.isAllDay());
        endTimeSpinner.setEnabled(!currentEvent.isAllDay());

        if (currentEvent instanceof RecurringEvent) {
          RecurringEvent recurringEvent = (RecurringEvent) currentEvent;
          recurringCheckBox.setSelected(true);
          recurringOptionsPanel.setVisible(true);

          Set<DayOfWeek> weekdays = recurringEvent.getRepeatDays();
          for (int i = 0; i < weekdayCheckboxes.size(); i++) {
            weekdayCheckboxes.get(i).setSelected(weekdays.contains(DayOfWeek.of((i + 1) % 7 + 1)));
          }

          occurrencesSpinner.setValue(recurringEvent.getOccurrences());
          Calendar untilCal = Calendar.getInstance();
          untilCal.set(recurringEvent.getEndDate().getYear(),
              recurringEvent.getEndDate().getMonthValue() - 1,
              recurringEvent.getEndDate().getDayOfMonth());
          untilDateSpinner.setValue(untilCal.getTime());
        } else {
          recurringCheckBox.setSelected(false);
          recurringOptionsPanel.setVisible(false);
        }

        System.out.println("[DEBUG] Event form populated for editing");
      } else {
        System.out.println("[DEBUG] Cannot edit: No event selected");
      }
    });

    subjectField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        checkSubject();
      }

      public void removeUpdate(DocumentEvent e) {
        checkSubject();
      }

      public void insertUpdate(DocumentEvent e) {
        checkSubject();
      }

      private void checkSubject() {
        System.out.println("[DEBUG] Subject field changed: " + subjectField.getText());
      }
    });

    locationField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        checkLocation();
      }

      public void removeUpdate(DocumentEvent e) {
        checkLocation();
      }

      public void insertUpdate(DocumentEvent e) {
        checkLocation();
      }

      private void checkLocation() {
        System.out.println("[DEBUG] Location field changed: " + locationField.getText());
      }
    });

    descriptionArea.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        checkDescription();
      }

      public void removeUpdate(DocumentEvent e) {
        checkDescription();
      }

      public void insertUpdate(DocumentEvent e) {
        checkDescription();
      }

      private void checkDescription() {
        System.out.println("[DEBUG] Description changed: " + descriptionArea.getText());
      }
    });

    // Add change listeners to spinners
    dateSpinner.addChangeListener(e -> {
      Date date = (Date) dateSpinner.getValue();
      System.out.println("[DEBUG] Date changed: " + date);
    });

    startTimeSpinner.addChangeListener(e -> {
      Date time = (Date) startTimeSpinner.getValue();
      System.out.println("[DEBUG] Start time changed: " + time);
    });

    endTimeSpinner.addChangeListener(e -> {
      Date time = (Date) endTimeSpinner.getValue();
      System.out.println("[DEBUG] End time changed: " + time);
    });

    recurringCheckBox.addActionListener(e -> {
      handleRecurringCheckbox();
      System.out.println("[DEBUG] Recurring checkbox changed: " + recurringCheckBox.isSelected());

      // Make sure the panel is visible in the layout
      if (recurringCheckBox.isSelected()) {
        // Force the weekday checkboxes to be visible and at least one selected
        weekdayCheckboxes.get(0).setSelected(true);

        // Set until date to be a month later to pass validation
        Calendar untilCal = Calendar.getInstance();
        untilCal.setTime((Date) dateSpinner.getValue());
        untilCal.add(Calendar.MONTH, 1);
        untilDateSpinner.setValue(untilCal.getTime());

        System.out.println(
            "[DEBUG] Set Monday checkbox to selected and until date to " + untilCal.getTime());
      }
    });

    privateEventCheckBox.addActionListener(e -> {
      boolean isPrivate = privateEventCheckBox.isSelected();
      System.out.println("[DEBUG] Private Event checkbox changed: " + isPrivate);
    });

    autoDeclineCheckBox.addActionListener(e -> {
      boolean autoDecline = autoDeclineCheckBox.isSelected();
      System.out.println("[DEBUG] Auto-decline checkbox changed: " + autoDecline);
    });

    occurrencesSpinner.addChangeListener(e -> {
      int occurrences = (Integer) occurrencesSpinner.getValue();
      System.out.println("[DEBUG] Occurrences changed: " + occurrences);
    });

    untilDateSpinner.addChangeListener(e -> {
      Date untilDate = (Date) untilDateSpinner.getValue();
      System.out.println("[DEBUG] Until date changed: " + untilDate);
    });

    saveButton.addActionListener(e -> {
      System.out.println("[DEBUG] Save button clicked");
      saveEvent();
    });

    cancelButton.addActionListener(e -> {
      System.out.println("[DEBUG] Cancel button clicked, clearing form");
      clearForm();
      if (listener != null) {
        listener.onEventCancelled();
      }
      System.out.println("[DEBUG] Form cleared");
    });
  }

  private boolean validateForm() {
    System.out.println("[DEBUG] Validating form");
    boolean isValid = true;
    clearErrorLabels();

    if (subjectField.getText().trim().isEmpty()) {
      System.out.println("[DEBUG] Validation error: Subject is required");
      showError(subjectField, "Subject is required");
      isValid = false;
    }

    Date selectedDate = (Date) dateSpinner.getValue();
    Date startTime = (Date) startTimeSpinner.getValue();
    Date endTime = (Date) endTimeSpinner.getValue();

    String systemTimezone = timezoneHandler.getSystemDefaultTimezone();

    LocalDateTime startDateTime = selectedDate.toInstant().atZone(ZoneId.of(systemTimezone))
        .toLocalDateTime()
        .withHour(startTime.toInstant().atZone(ZoneId.of(systemTimezone)).getHour())
        .withMinute(startTime.toInstant().atZone(ZoneId.of(systemTimezone)).getMinute());

    LocalDateTime endDateTime = selectedDate.toInstant().atZone(ZoneId.of(systemTimezone))
        .toLocalDateTime().withHour(endTime.toInstant().atZone(ZoneId.of(systemTimezone)).getHour())
        .withMinute(endTime.toInstant().atZone(ZoneId.of(systemTimezone)).getMinute());

    System.out.println("[DEBUG] Start time: " + startDateTime);
    System.out.println("[DEBUG] End time: " + endDateTime);

    if (endDateTime.isBefore(startDateTime) || endDateTime.equals(startDateTime)) {
      System.out.println("[DEBUG] Validation error: End time must be after start time");
      showError(endTimeSpinner, "End time must be after start time");
      isValid = false;
    }

    if (recurringCheckBox.isSelected()) {
      boolean anyWeekdaySelected = weekdayCheckboxes.stream().anyMatch(JCheckBox::isSelected);
      if (!anyWeekdaySelected) {
        System.out.println("[DEBUG] Validation error: Select at least one weekday");
        showError(weekdayCheckboxes.get(0), "Select at least one weekday");
        isValid = false;
      }

      Date untilDate = (Date) untilDateSpinner.getValue();
      if (untilDate.before(selectedDate)) {
        System.out.println("[DEBUG] Validation error: Until date must be after start date");
        showError(untilDateSpinner, "Until date must be after start date");
        isValid = false;
      }
    }

    return isValid;
  }

  private void showError(JComponent component, String message) {
    JLabel errorLabel = errorLabels.get(component);
    if (errorLabel != null) {
      errorLabel.setText(message);
      errorLabel.setVisible(true);
    }
  }

  private void clearErrorLabels() {
    errorLabels.values().forEach(label -> label.setVisible(false));
  }

  /**
   * Enables or disables form editing.
   *
   * @param enabled true to enable editing, false to disable
   */
  private void enableFormEditing(boolean enabled) {
    subjectField.setEditable(enabled);
    locationField.setEditable(enabled);
    descriptionArea.setEditable(enabled);
    dateSpinner.setEnabled(enabled);
    startTimeSpinner.setEnabled(enabled);
    endTimeSpinner.setEnabled(enabled);
    allDayCheckBox.setEnabled(enabled);
    recurringCheckBox.setEnabled(enabled);
    privateEventCheckBox.setEnabled(enabled);
    autoDeclineCheckBox.setEnabled(enabled);

    if (recurringOptionsPanel.isVisible()) {
      for (JCheckBox cb : weekdayCheckboxes) {
        cb.setEnabled(enabled);
      }
      occurrencesSpinner.setEnabled(enabled);
      untilDateSpinner.setEnabled(enabled);
    }
  }

  /**
   * Sets the panel mode.
   *
   * @param mode the mode to set
   */
  public void setPanelMode(PanelMode mode) {
    System.out.println("[DEBUG] Setting panel mode to: " + mode);
    this.currentMode = mode;
    modeLabel.setText(mode.getDisplayName());

    editButton.setVisible(true);
    saveButton.setVisible(true);
    cancelButton.setVisible(true);

    if (mode == PanelMode.VIEW) {
      enableFormEditing(false);
      saveButton.setText("Save");
    } else if (mode == PanelMode.CREATE) {
      saveButton.setText("Save");
      enableFormEditing(true);
    } else if (mode == PanelMode.EDIT) {
      saveButton.setText("Update");
      enableFormEditing(true);
    }

    System.out.println(
        "[DEBUG] After setPanelMode - Save button visibility: " + saveButton.isVisible());
    System.out.println(
        "[DEBUG] After setPanelMode - Cancel button visibility: " + cancelButton.isVisible());
    System.out.println(
        "[DEBUG] After setPanelMode - Edit button visibility: " + editButton.isVisible());

    revalidate();
    repaint();
  }

  /**
   * Gets the selected target calendar name.
   *
   * @return the selected target calendar name
   */
  public String getTargetCalendarName() {
    return (String) targetCalendarCombo.getSelectedItem();
  }

  /**
   * Gets the target date and time for the copied event.
   *
   * @return the target date and time
   */
  public LocalDateTime getTargetDateTime() {
    Date selectedDate = (Date) targetDateSpinner.getValue();
    Date startTime = (Date) targetStartTimeSpinner.getValue();

    return selectedDate.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime()
        .withHour(startTime.toInstant().atZone(ZoneOffset.UTC).getHour())
        .withMinute(startTime.toInstant().atZone(ZoneOffset.UTC).getMinute());
  }

  /**
   * Gets the target end date and time for the copied event.
   *
   * @return the target end date and time
   */
  public LocalDateTime getTargetEndDateTime() {
    Date selectedDate = (Date) targetDateSpinner.getValue();
    Date endTime = (Date) targetEndTimeSpinner.getValue();

    return selectedDate.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime()
        .withHour(endTime.toInstant().atZone(ZoneOffset.UTC).getHour())
        .withMinute(endTime.toInstant().atZone(ZoneOffset.UTC).getMinute());
  }

  /**
   * Gets the current event being edited or copied.
   *
   * @return the current event
   */
  public Event getCurrentEvent() {
    return currentEvent;
  }

  /**
   * Displays an event in the form.
   *
   * @param event the event to display
   */
  public void displayEvent(Event event) {
    System.out.println("[DEBUG] displayEvent called for event: " + event.getSubject());
    System.out.println("[DEBUG] Event details: Start=" + event.getStartDateTime() + ", End="
        + event.getEndDateTime() + ", Location=" + event.getLocation() + ", AllDay="
        + event.isAllDay() + ", Type=" + (event instanceof RecurringEvent ? "Recurring"
        : "Single"));

    // If this is a recurring event, delegate to the specialized method
    if (event instanceof RecurringEvent) {
      displayRecurringEvent((RecurringEvent) event);
      return;
    }

    setPanelMode(PanelMode.VIEW);

    System.out.println("[DEBUG] displayEvent - After setPanelMode - Edit button visibility: "
        + editButton.isVisible());
    System.out.println("[DEBUG] displayEvent - After setPanelMode - Save button visibility: "
        + saveButton.isVisible());

    revalidate();
    repaint();
    this.currentEvent = event;
    subjectField.setText(event.getSubject());
    locationField.setText(event.getLocation());
    descriptionArea.setText(event.getDescription());
    // Convert times from UTC to local timezone for display
    String systemTimezone = timezoneHandler.getSystemDefaultTimezone();
    LocalDateTime localStartDateTime = timezoneHandler.convertFromUTC(event.getStartDateTime(),
        systemTimezone);
    LocalDateTime localEndDateTime = timezoneHandler.convertFromUTC(event.getEndDateTime(),
        systemTimezone);

    // Create Date objects from the local times
    Date localStartDate = Date.from(
        localStartDateTime.atZone(ZoneId.of(systemTimezone)).toInstant());
    Date localEndDate = Date.from(localEndDateTime.atZone(ZoneId.of(systemTimezone)).toInstant());

    dateSpinner.setValue(localStartDate);
    startTimeSpinner.setValue(localStartDate);
    endTimeSpinner.setValue(localEndDate);
    recurringCheckBox.setSelected(false);
    recurringOptionsPanel.setVisible(false);
    allDayCheckBox.setSelected(event.isAllDay());
    startTimeSpinner.setEnabled(!event.isAllDay());
    endTimeSpinner.setEnabled(!event.isAllDay());

    targetDateSpinner.setValue(Date.from(event.getStartDateTime().toInstant(ZoneOffset.UTC)));
    targetStartTimeSpinner.setValue(Date.from(event.getStartDateTime().toInstant(ZoneOffset.UTC)));
    targetEndTimeSpinner.setValue(Date.from(event.getEndDateTime().toInstant(ZoneOffset.UTC)));
  }

  /**
   * Displays a recurring event in the form.
   *
   * @param event the recurring event to display
   */
  public void displayRecurringEvent(RecurringEvent event) {
    displayEvent(event);
    recurringCheckBox.setSelected(true);
    recurringOptionsPanel.setVisible(true);

    Set<DayOfWeek> weekdays = event.getRepeatDays();

    int[] indices = new int[weekdays.size()];
    int index = 0;
    for (int i = 0; i < weekdayCheckboxes.size(); i++) {
      if (weekdays.contains(DayOfWeek.of(i + 1))) {
        indices[index++] = i;
      }
    }

    weekdayCheckboxes.forEach(cb -> cb.setSelected(false));
    for (int i : indices) {
      weekdayCheckboxes.get(i).setSelected(true);
    }
    occurrencesSpinner.setValue(event.getOccurrences());
    untilDateSpinner.setValue(
        Date.from(event.getEndDate().atStartOfDay().toInstant(ZoneOffset.UTC)));
  }

  /**
   * Sets the current date.
   *
   * @param date the date to set
   */
  public void setDate(LocalDate date) {
    if (date != null) {
      dateSpinner.setValue(Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC)));
    }
  }

  /**
   * Clears the form.
   */
  public void clearForm() {
    System.out.println("[DEBUG] Clearing form");
    subjectField.setText("");
    locationField.setText("");
    descriptionArea.setText("");
    recurringCheckBox.setSelected(false);
    recurringOptionsPanel.setVisible(false);
    allDayCheckBox.setSelected(false);
    startTimeSpinner.setEnabled(true);
    endTimeSpinner.setEnabled(true);

    Calendar cal = Calendar.getInstance();
    dateSpinner.setValue(cal.getTime());
    cal.set(Calendar.MINUTE, (cal.get(Calendar.MINUTE) / 15) * 15);
    startTimeSpinner.setValue(cal.getTime());
    cal.add(Calendar.HOUR_OF_DAY, 1);
    endTimeSpinner.setValue(cal.getTime());
    System.out.println("[DEBUG] Form fields reset to default values");

    setPanelMode(PanelMode.CREATE);
    System.out.println("[DEBUG] clearForm - After setPanelMode - Save button visibility: "
        + saveButton.isVisible());
    System.out.println("[DEBUG] clearForm - After setPanelMode - Edit button visibility: "
        + editButton.isVisible());

    currentEvent = null;
    System.out.println("[DEBUG] Current event reset to null");

    revalidate();
    repaint();
  }

  /**
   * Adds a listener for event panel events.
   *
   * @param listener the listener to add
   */
  public void addEventPanelListener(EventPanelListener listener) {
    this.listener = listener;
  }

  /**
   * Refreshes the event panel.
   */
  public void refresh() {
    clearForm();
  }

  /**
   * Collects all data from the form fields into a data transfer object. This method does not
   * contain any business logic, only UI data collection.
   *
   * @return EventFormData containing all the form field values
   */
  public EventFormData collectFormData() {
    Date selectedDate = (Date) dateSpinner.getValue();
    Date startTime = (Date) startTimeSpinner.getValue();
    Date endTime = (Date) endTimeSpinner.getValue();

    String subject = subjectField.getText().trim();
    String location = locationField.getText().trim();
    String description = descriptionArea.getText().trim();
    boolean isRecurring = recurringCheckBox.isSelected();
    boolean isAllDay = allDayCheckBox.isSelected();
    boolean isPrivate = privateEventCheckBox.isSelected();
    boolean autoDecline = autoDeclineCheckBox.isSelected();

    Set<DayOfWeek> weekdays = null;
    int occurrences = 0;
    LocalDate untilDate = null;

    if (isRecurring) {
      weekdays = weekdayCheckboxes.stream().filter(JCheckBox::isSelected)
          .map(cb -> DayOfWeek.of((weekdayCheckboxes.indexOf(cb) + 1))).collect(Collectors.toSet());
      occurrences = (Integer) occurrencesSpinner.getValue();
      untilDate = ((Date) untilDateSpinner.getValue()).toInstant().atZone(ZoneOffset.UTC)
          .toLocalDate();
    }

    return new EventFormData(subject, selectedDate, startTime, endTime, location, description,
        isRecurring, isAllDay, weekdays, occurrences, untilDate, isPrivate, autoDecline);
  }

  /**
   * Updates the visibility and content of the recurring event panel based on checkbox state. This
   * method is called when setting up the UI and when the recurring checkbox is toggled.
   */
  private void handleRecurringCheckbox() {
    boolean isRecurring = recurringCheckBox.isSelected();
    recurringOptionsPanel.setVisible(isRecurring);

    if (isRecurring) {
      // Ensure at least one weekday is selected by default (Monday)
      if (weekdayCheckboxes.stream().noneMatch(JCheckBox::isSelected)) {
        weekdayCheckboxes.get(0).setSelected(true);  // Monday is the first checkbox
      }

      // Set the "until date" to at least one month in the future from the event date
      Date currentDate = (Date) dateSpinner.getValue();
      Calendar untilCal = Calendar.getInstance();
      untilCal.setTime(currentDate);
      untilCal.add(Calendar.MONTH, 1);  // One month in the future
      untilDateSpinner.setValue(untilCal.getTime());

      // Set a reasonable default for occurrences
      occurrencesSpinner.setValue(4);  // Default to 4 occurrences
    }

    revalidate();
    repaint();
  }

  /**
   * Refreshes the event panel view.
   */
  public void refreshView() {
    System.out.println("[DEBUG] Refreshing event panel view");
    revalidate();
    repaint();
  }

  /**
   * Updates the event list with the provided events.
   *
   * @param events the list of events to display
   */
  public void updateEventList(List<Event> events) {
    if (events == null || events.isEmpty()) {
      clearEventDisplay();
      return;
    }

    displayEvents(events);
  }

  /**
   * Displays a list of events in the event panel.
   *
   * @param events the list of events to display
   */
  private void displayEvents(List<Event> events) {
    clearEventDisplay();

    JPanel eventsContainer = new JPanel();
    eventsContainer.setLayout(new BoxLayout(eventsContainer, BoxLayout.Y_AXIS));
    eventsContainer.setBackground(Color.WHITE);

    for (Event event : events) {
      JPanel eventItem = createEventDisplayItem(event);
      eventsContainer.add(eventItem);
      eventsContainer.add(Box.createVerticalStrut(5));
    }

    JScrollPane scrollPane = new JScrollPane(eventsContainer);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    removeAll();
    setLayout(new BorderLayout());
    add(scrollPane, BorderLayout.CENTER);
    revalidate();
    repaint();
  }

  /**
   * Creates a display item for an event.
   *
   * @param event the event to create a display for
   * @return a panel containing the event information
   */
  private JPanel createEventDisplayItem(Event event) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(5, 5));
    panel.setBorder(
        BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    panel.setBackground(Color.WHITE);

    JLabel titleLabel = new JLabel(event.getSubject());
    titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
    panel.add(titleLabel, BorderLayout.NORTH);

    StringBuilder details = new StringBuilder();
    details.append("<html>");

    String startTime = event.getStartDateTime().toLocalTime().toString();
    String endTime = event.getEndDateTime().toLocalTime().toString();
    details.append(String.format("<b>Time:</b> %s - %s<br/>", startTime, endTime));

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      details.append(String.format("<b>Location:</b> %s<br/>", event.getLocation()));
    }

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      details.append(String.format("<b>Description:</b> %s<br/>", event.getDescription()));
    }

    details.append("</html>");

    JLabel detailsLabel = new JLabel(details.toString());
    panel.add(detailsLabel, BorderLayout.CENTER);

    return panel;
  }

  /**
   * Clears the event display area.
   */
  private void clearEventDisplay() {
    removeAll();
    setLayout(new BorderLayout());
    JLabel emptyLabel = new JLabel("No events to display");
    emptyLabel.setHorizontalAlignment(JLabel.CENTER);
    add(emptyLabel, BorderLayout.CENTER);
    revalidate();
    repaint();
  }

  /**
   * Saves the current event using the form data. This method is called by the save button's action
   * listener. Collects data from form fields and notifies the listener.
   */
  private void saveEvent() {
    try {
      System.out.println("[DEBUG] Save event button clicked, current mode: " + currentMode);
      if (!validateForm()) {
        System.out.println("[DEBUG] Form validation failed");
        return;
      }
      System.out.println("[DEBUG] Form validation successful");

      EventFormData formData = collectFormData();
      System.out.println(
          "[DEBUG] Form data collected: Subject=" + formData.getSubject() + ", Location="
              + formData.getLocation() + ", Description length=" + formData.getDescription()
              .length() + ", Recurring=" + formData.isRecurring() + ", AllDay="
              + formData.isAllDay());

      if (listener != null) {
        if (currentMode == PanelMode.EDIT && currentEvent != null) {
          System.out.println("[DEBUG] Editing existing event: " + currentEvent.getSubject());
          listener.onEventUpdated(formData);
        } else {
          System.out.println("[DEBUG] Creating new event");
          listener.onEventSaved(formData);
        }
      }

      clearForm();
      currentEvent = null;
      setPanelMode(PanelMode.CREATE);

    } catch (Exception e) {
      System.out.println("[ERROR] Failed to save event: " + e.getMessage());
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Error saving event: " + e.getMessage(), "Save Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void setupLayout() {
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Event Details"),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)));

    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(HEADER_LIGHT_COLOR);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    headerPanel.add(modeLabel, BorderLayout.WEST);
    add(headerPanel, BorderLayout.NORTH);

    JPanel formPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.EAST;
    formPanel.add(new JLabel("Subject:"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.WEST;
    formPanel.add(subjectField, gbc);
    gbc.gridy = 1;
    formPanel.add(errorLabels.get(subjectField), gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.EAST;
    formPanel.add(new JLabel("Date:"), gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.WEST;
    formPanel.add(dateSpinner, gbc);

    JPanel timePanel = new JPanel(new GridLayout(2, 1, 0, 5));
    timePanel.setOpaque(false);

    JPanel startTimeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    startTimeRow.setOpaque(false);
    JLabel startTimeLabel = new JLabel("Start Time:");
    startTimeLabel.setPreferredSize(new Dimension(80, startTimeLabel.getPreferredSize().height));
    startTimeRow.add(startTimeLabel);

    startTimeSpinner.setPreferredSize(
        new Dimension(100, startTimeSpinner.getPreferredSize().height));
    startTimeRow.add(startTimeSpinner);

    JPanel endTimeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    endTimeRow.setOpaque(false);
    JLabel endTimeLabel = new JLabel("End Time:");
    endTimeLabel.setPreferredSize(new Dimension(80, endTimeLabel.getPreferredSize().height));
    endTimeRow.add(endTimeLabel);

    endTimeSpinner.setPreferredSize(new Dimension(100, endTimeSpinner.getPreferredSize().height));
    endTimeRow.add(endTimeSpinner);

    timePanel.add(startTimeRow);
    timePanel.add(endTimeRow);

    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.gridwidth = 2;
    formPanel.add(timePanel, gbc);

    gbc.gridwidth = 1;

    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridwidth = 2;
    formPanel.add(errorLabels.get(endTimeSpinner), gbc);

    gbc.gridx = 0;
    gbc.gridy = 6;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("Location:"), gbc);

    gbc.gridx = 0;
    gbc.gridy = 7;
    gbc.gridwidth = 2;
    formPanel.add(locationField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 8;
    gbc.gridwidth = 2;
    JScrollPane descScrollPane = new JScrollPane(descriptionArea);
    descScrollPane.setPreferredSize(new Dimension(0, 60));
    formPanel.add(descScrollPane, gbc);

    gbc.gridx = 0;
    gbc.gridy = 9;
    gbc.gridwidth = 1;
    formPanel.add(styleCheckbox(allDayCheckBox), gbc);

    gbc.gridx = 1;
    formPanel.add(styleCheckbox(recurringCheckBox), gbc);

    gbc.gridx = 0;
    gbc.gridy = 10;
    formPanel.add(styleCheckbox(privateEventCheckBox), gbc);

    gbc.gridx = 1;
    formPanel.add(styleCheckbox(autoDeclineCheckBox), gbc);

    gbc.gridx = 0;
    gbc.gridy = 11;
    gbc.gridwidth = 2;
    formPanel.add(recurringOptionsPanel, gbc);
    recurringOptionsPanel.setVisible(false);

    gbc.gridx = 0;
    gbc.gridy = 12;
    formPanel.add(errorLabels.get(weekdayCheckboxes.get(0)), gbc);
    gbc.gridy = 13;
    formPanel.add(errorLabels.get(untilDateSpinner), gbc);

    GridBagConstraints copyGbc = new GridBagConstraints();
    copyGbc.fill = GridBagConstraints.HORIZONTAL;
    copyGbc.insets = new Insets(5, 5, 5, 5);

    copyGbc.gridx = 0;
    copyGbc.gridy = 0;
    copyGbc.anchor = GridBagConstraints.EAST;
    copyOptionsPanel.add(new JLabel("Target Calendar:"), copyGbc);

    copyGbc.gridx = 1;
    copyGbc.gridwidth = 2;
    copyGbc.anchor = GridBagConstraints.WEST;
    copyOptionsPanel.add(targetCalendarCombo, copyGbc);

    copyGbc.gridx = 0;
    copyGbc.gridy = 1;
    copyGbc.gridwidth = 1;
    copyGbc.anchor = GridBagConstraints.EAST;
    copyOptionsPanel.add(new JLabel("Target Date:"), copyGbc);

    copyGbc.gridx = 1;
    copyGbc.gridwidth = 2;
    copyGbc.anchor = GridBagConstraints.WEST;
    copyOptionsPanel.add(targetDateSpinner, copyGbc);

    copyGbc.gridx = 0;
    copyGbc.gridy = 2;
    copyGbc.gridwidth = 1;
    copyGbc.anchor = GridBagConstraints.EAST;
    copyOptionsPanel.add(new JLabel("Start Time:"), copyGbc);

    copyGbc.gridx = 1;
    copyGbc.anchor = GridBagConstraints.WEST;
    copyOptionsPanel.add(targetStartTimeSpinner, copyGbc);

    copyGbc.gridx = 0;
    copyGbc.gridy = 3;
    copyGbc.anchor = GridBagConstraints.EAST;
    copyOptionsPanel.add(new JLabel("End Time:"), copyGbc);

    copyGbc.gridx = 1;
    copyGbc.anchor = GridBagConstraints.WEST;
    copyOptionsPanel.add(targetEndTimeSpinner, copyGbc);

    gbc.gridx = 0;
    gbc.gridy = 14;
    gbc.gridwidth = 3;
    formPanel.add(copyOptionsPanel, gbc);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 10, 5));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    buttonPanel.add(saveButton);
    buttonPanel.add(editButton);
    buttonPanel.add(cancelButton);

    Dimension buttonSize = new Dimension(100, 30);
    editButton.setPreferredSize(buttonSize);
    saveButton.setPreferredSize(buttonSize);
    cancelButton.setPreferredSize(buttonSize);

    editButton.setVisible(true);
    saveButton.setVisible(true);
    cancelButton.setVisible(true);

    System.out.println("[DEBUG] setupLayout - Edit button visibility: " + editButton.isVisible());
    System.out.println("[DEBUG] setupLayout - Save button visibility: " + saveButton.isVisible());
    System.out.println(
        "[DEBUG] setupLayout - Cancel button visibility: " + cancelButton.isVisible());

    int fixedWidth = 400;
    formPanel.setPreferredSize(new Dimension(fixedWidth, formPanel.getPreferredSize().height));
    formPanel.setMinimumSize(new Dimension(fixedWidth, 100));
    formPanel.setMaximumSize(new Dimension(fixedWidth, Short.MAX_VALUE));

    JScrollPane scrollPane = new JScrollPane(formPanel);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.getViewport().setMinimumSize(new Dimension(fixedWidth, 100));
    scrollPane.getViewport()
        .setPreferredSize(new Dimension(fixedWidth, formPanel.getPreferredSize().height));

    scrollPane.getViewport().setView(formPanel);

    add(scrollPane, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  private JCheckBox styleCheckbox(JCheckBox checkbox) {
    checkbox.setFont(new Font("Arial", Font.PLAIN, 11));
    checkbox.setForeground(TEXT_COLOR);
    checkbox.setOpaque(false);
    return checkbox;
  }
}