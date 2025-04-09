package view.dialog;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import model.event.Event;

/**
 * Enhanced dialog for editing events with improved UI and validation.
 * This implementation follows the SOLID principles:
 * - Single Responsibility: Only handles event editing UI
 * - Open/Closed: Extends AbstractEventDialog and implements IEventEditDialog
 * - Liskov Substitution: Can be used anywhere an IEventEditDialog is needed
 * - Interface Segregation: Implements only methods relevant to event editing
 * - Dependency Inversion: GUI components depend on dialog interfaces, not implementations
 */
public class EnhancedEventEditDialog extends AbstractEventDialog implements IEventEditDialog {

  private final boolean isRecurring;

  private JTextField subjectField;
  private JTextField locationField;
  private JTextArea descriptionArea;
  private JSpinner dateSpinner;
  private JSpinner startTimeSpinner;
  private JSpinner endTimeSpinner;
  private JCheckBox allDayCheckBox;
  private JCheckBox privateCheckBox;
  private JCheckBox recurringCheckBox;

  private String updatedSubject;
  private String updatedLocation;
  private String updatedDescription;
  private boolean updatedAllDay;
  private boolean updatedPrivate;
  private boolean updatedRecurring;
  private LocalDateTime updatedStartDateTime;
  private LocalDateTime updatedEndDateTime;

  /**
   * Constructs a new EnhancedEventEditDialog.
   *
   * @param parent      the parent component
   * @param event       the event to edit
   * @param isRecurring whether the event is recurring
   */
  public EnhancedEventEditDialog(Component parent, Event event, boolean isRecurring) {
    super(parent, "Edit Event", event);
    this.isRecurring = isRecurring;
  }

  @Override
  protected void initComponents() {
    subjectField = new JTextField(30);
    locationField = new JTextField(30);
    descriptionArea = new JTextArea(5, 30);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);

    dateSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner,
            "yyyy-MM-dd");
    dateSpinner.setEditor(dateEditor);

    startTimeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner,
            "HH:mm");
    startTimeSpinner.setEditor(startTimeEditor);

    endTimeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(endTimeSpinner,
            "HH:mm");
    endTimeSpinner.setEditor(endTimeEditor);

    allDayCheckBox = new JCheckBox("All Day Event");
    privateCheckBox = new JCheckBox("Private Event");
    recurringCheckBox = new JCheckBox("Recurring Event");

    recurringCheckBox.setSelected(isRecurring);
    recurringCheckBox.setEnabled(false);
  }

  @Override
  protected void layoutComponents() {
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

    GridBagConstraints gbc = createGBC();

    // Add header
    JLabel headerLabel = new JLabel("Edit Event Details");
    headerLabel.setFont(headerLabel.getFont().deriveFont(java.awt.Font.BOLD, 16f));
    headerLabel.setForeground(HEADER_COLOR);
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(headerLabel, gbc);

    // Reset to default width
    gbc.gridwidth = 1;
    gbc.gridy++;

    // Subject
    mainPanel.add(new JLabel("Subject:*"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    mainPanel.add(subjectField, gbc);

    // Location
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    mainPanel.add(new JLabel("Location:"), gbc);
    gbc.gridx = 1;
    gbc.weightx = 1.0;
    mainPanel.add(locationField, gbc);

    // Description
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    mainPanel.add(new JLabel("Description:"), gbc);
    gbc.gridx = 1;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1.0;
    JScrollPane scrollPane = new JScrollPane(descriptionArea);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    mainPanel.add(scrollPane, gbc);

    // Date
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weighty = 0.0;
    gbc.fill = GridBagConstraints.NONE;
    mainPanel.add(new JLabel("Date:*"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(dateSpinner, gbc);

    // Time panel
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;

    JPanel timePanel = new JPanel(new GridBagLayout());
    timePanel.setBorder(BorderFactory.createTitledBorder("Time"));

    GridBagConstraints timegbc = createGBC();

    // Start time
    timePanel.add(new JLabel("Start Time:*"), timegbc);
    timegbc.gridx = 1;
    timegbc.fill = GridBagConstraints.HORIZONTAL;
    timegbc.weightx = 1.0;
    timePanel.add(startTimeSpinner, timegbc);

    // End time
    timegbc.gridx = 0;
    timegbc.gridy++;
    timegbc.weightx = 0.0;
    timePanel.add(new JLabel("End Time:*"), timegbc);
    timegbc.gridx = 1;
    timegbc.weightx = 1.0;
    timePanel.add(endTimeSpinner, timegbc);

    // All-day checkbox
    timegbc.gridx = 0;
    timegbc.gridy++;
    timegbc.gridwidth = 2;
    timePanel.add(allDayCheckBox, timegbc);

    mainPanel.add(timePanel, gbc);

    // Options panel
    gbc.gridy++;

    JPanel optionsPanel = new JPanel(new GridBagLayout());
    optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

    GridBagConstraints optgbc = createGBC();
    optgbc.gridwidth = 2;

    // Private checkbox
    optionsPanel.add(privateCheckBox, optgbc);

    // Recurring checkbox
    optgbc.gridy++;
    optionsPanel.add(recurringCheckBox, optgbc);

    mainPanel.add(optionsPanel, gbc);

    // Create buttons
    JButton saveButton = new JButton("Save");
    saveButton.setBackground(HEADER_COLOR);
    saveButton.setForeground(Color.WHITE);
    saveButton.addActionListener(e -> {
      if (validateForm()) {
        gatherUpdatedData();
        actionConfirmed = true;
        dispose();
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    // Create button panel
    JPanel buttonPanel = createButtonPanel(saveButton, cancelButton);

    // Create info panel with conflict warning
    JPanel infoPanel = createInfoPanel(
            "Note: Events cannot conflict with each other. Editing that would create " +
                    "a conflict with another existing event is not allowed.");

    // Main layout
    setLayout(new BorderLayout());
    add(infoPanel, BorderLayout.NORTH);
    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  @Override
  protected void setupListeners() {
    allDayCheckBox.addActionListener(e -> {
      boolean isAllDay = allDayCheckBox.isSelected();
      startTimeSpinner.setEnabled(!isAllDay);
      endTimeSpinner.setEnabled(!isAllDay);

      if (isAllDay) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        startTimeSpinner.setValue(cal.getTime());

        // Set end time to 23:59
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        endTimeSpinner.setValue(cal.getTime());
      }
    });
  }

  @Override
  protected void loadEventData() {
    subjectField.setText(event.getSubject());
    locationField.setText(event.getLocation() != null ? event.getLocation() : "");
    descriptionArea.setText(event.getDescription() != null ? event.getDescription() : "");

    LocalDateTime startDateTime = event.getStartDateTime();
    LocalDateTime endDateTime = event.getEndDateTime();

    boolean isAllDay = startDateTime.toLocalTime().equals(LocalTime.of(0, 0)) &&
            (endDateTime.toLocalTime().equals(LocalTime.of(23, 59)) ||
                    endDateTime.toLocalTime().equals(LocalTime.of(23, 59, 59)));

    allDayCheckBox.setSelected(isAllDay);

    Calendar cal = Calendar.getInstance();
    cal.set(startDateTime.getYear(), startDateTime.getMonthValue() - 1,
            startDateTime.getDayOfMonth());
    dateSpinner.setValue(cal.getTime());

    cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, startDateTime.getHour());
    cal.set(Calendar.MINUTE, startDateTime.getMinute());
    startTimeSpinner.setValue(cal.getTime());

    cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, endDateTime.getHour());
    cal.set(Calendar.MINUTE, endDateTime.getMinute());
    endTimeSpinner.setValue(cal.getTime());

    startTimeSpinner.setEnabled(!isAllDay);
    endTimeSpinner.setEnabled(!isAllDay);

    privateCheckBox.setSelected(!event.isPublic());
  }

  /**
   * Validates the form data.
   *
   * @return true if the form data is valid, false otherwise
   */
  private boolean validateForm() {
    if (subjectField.getText().trim().isEmpty()) {
      showValidationError(subjectField, "Subject is required");
      return false;
    }

    Date startTime = (Date) startTimeSpinner.getValue();
    Date endTime = (Date) endTimeSpinner.getValue();

    if (startTime.after(endTime)) {
      showValidationError(endTimeSpinner, "End time must be after start time");
      return false;
    }

    return true;
  }

  /**
   * Shows a validation error for a component.
   *
   * @param component the component with the error
   * @param message   the error message
   */
  private void showValidationError(Component component, String message) {
    JOptionPane.showMessageDialog(this, message,
            "Validation Error", JOptionPane.ERROR_MESSAGE);
    if (component instanceof JTextComponent) {
      ((JTextComponent) component).selectAll();
    }
    component.requestFocus();
  }

  /**
   * Gathers the updated data from the form fields.
   */
  private void gatherUpdatedData() {
    updatedSubject = subjectField.getText().trim();
    updatedLocation = locationField.getText().trim();
    updatedDescription = descriptionArea.getText().trim();
    updatedAllDay = allDayCheckBox.isSelected();
    updatedPrivate = privateCheckBox.isSelected();
    updatedRecurring = recurringCheckBox.isSelected();

    Date dateValue = (Date) dateSpinner.getValue();
    Date startTimeValue = (Date) startTimeSpinner.getValue();
    Date endTimeValue = (Date) endTimeSpinner.getValue();

    Calendar dateCal = Calendar.getInstance();
    dateCal.setTime(dateValue);

    Calendar startTimeCal = Calendar.getInstance();
    startTimeCal.setTime(startTimeValue);

    Calendar endTimeCal = Calendar.getInstance();
    endTimeCal.setTime(endTimeValue);

    LocalDate date = LocalDate.of(
            dateCal.get(Calendar.YEAR),
            dateCal.get(Calendar.MONTH) + 1,
            dateCal.get(Calendar.DAY_OF_MONTH)
    );

    LocalTime startTime = LocalTime.of(
            startTimeCal.get(Calendar.HOUR_OF_DAY),
            startTimeCal.get(Calendar.MINUTE)
    );

    LocalTime endTime = LocalTime.of(
            endTimeCal.get(Calendar.HOUR_OF_DAY),
            endTimeCal.get(Calendar.MINUTE)
    );

    updatedStartDateTime = LocalDateTime.of(date, startTime);
    updatedEndDateTime = LocalDateTime.of(date, endTime);

    if (updatedAllDay) {
      updatedStartDateTime = LocalDateTime.of(date, LocalTime.of(0, 0));
      updatedEndDateTime = LocalDateTime.of(date, LocalTime.of(23, 59));
    }
  }

  @Override
  public String getSubject() {
    return updatedSubject;
  }

  @Override
  public String getEventLocation() {
    return updatedLocation;
  }

  @Override
  public String getDescription() {
    return updatedDescription;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return updatedStartDateTime;
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return updatedEndDateTime;
  }

  @Override
  public boolean isAllDay() {
    return updatedAllDay;
  }

  @Override
  public boolean isPrivate() {
    return updatedPrivate;
  }

  @Override
  public boolean isRecurring() {
    return updatedRecurring;
  }
}
