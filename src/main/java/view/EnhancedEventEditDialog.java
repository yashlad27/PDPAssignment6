package view;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import model.event.Event;

/**
 * Enhanced dialog for editing events with more comprehensive options.
 */
public class EnhancedEventEditDialog extends JDialog {
  private static final Color THEME_COLOR = new Color(0x4a86e8);
  private static final Color HEADER_COLOR = new Color(0x4a86e8);

  private final Event event;
  private final boolean isRecurring;
  private boolean editConfirmed = false;

  // Form components
  private JTextField subjectField;
  private JTextField locationField;
  private JTextArea descriptionArea;
  private JSpinner dateSpinner;
  private JSpinner startTimeSpinner;
  private JSpinner endTimeSpinner;
  private JCheckBox allDayCheckBox;
  private JCheckBox privateCheckBox;
  private JCheckBox recurringCheckBox;
  private JCheckBox autoDeclineCheckBox;

  // Updated event data
  private String updatedSubject;
  private String updatedLocation;
  private String updatedDescription;
  private LocalDateTime updatedStartDateTime;
  private LocalDateTime updatedEndDateTime;
  private boolean updatedAllDay;
  private boolean updatedPrivate;
  private boolean updatedRecurring;


  /**
   * Constructs a new EnhancedEventEditDialog.
   *
   * @param parent      the parent frame
   * @param event       the event to edit
   * @param isRecurring whether the event is recurring
   */
  public EnhancedEventEditDialog(Frame parent, Event event, boolean isRecurring) {
    super(parent, "Edit Event", true);
    this.event = event;
    this.isRecurring = isRecurring;

    initComponents();
    layoutComponents();

    // Set initial values
    populateFieldsFromEvent();

    pack();
    setLocationRelativeTo(parent);
    setResizable(true);
    setMinimumSize(new Dimension(500, 500));
  }

  /**
   * Initializes the dialog components.
   */
  private void initComponents() {
    // Text fields
    subjectField = new JTextField(30);
    locationField = new JTextField(30);
    descriptionArea = new JTextArea(5, 30);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    // No need for a separate scrollpane reference as we're using it directly

    // Date and time components
    dateSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
    dateSpinner.setEditor(dateEditor);

    startTimeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
    startTimeSpinner.setEditor(startTimeEditor);

    endTimeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
    endTimeSpinner.setEditor(endTimeEditor);

    // Checkboxes
    allDayCheckBox = new JCheckBox("All Day Event");
    privateCheckBox = new JCheckBox("Private Event");
    recurringCheckBox = new JCheckBox("Recurring Event");
    autoDeclineCheckBox = new JCheckBox("Auto-decline");

    // Make the recurring checkbox disabled for existing events
    recurringCheckBox.setEnabled(false);

    // We'll add the scrollpane in the layout method

    // Buttons
    JButton saveButton = new JButton("Save");
    saveButton.setBackground(HEADER_COLOR);
    saveButton.setForeground(Color.WHITE);
    saveButton.addActionListener(e -> {
      if (validateForm()) {
        gatherUpdatedData();
        editConfirmed = true;
        dispose();
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    // Add checkbox listeners
    allDayCheckBox.addActionListener(e -> {
      boolean isAllDay = allDayCheckBox.isSelected();
      startTimeSpinner.setEnabled(!isAllDay);
      endTimeSpinner.setEnabled(!isAllDay);

      if (isAllDay) {
        // Set default all-day times (00:00 to 23:59)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        startTimeSpinner.setValue(cal.getTime());

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        endTimeSpinner.setValue(cal.getTime());
      }
    });
  }

  /**
   * Populates the form fields with data from the event.
   */
  private void populateFieldsFromEvent() {
    // Set basic fields
    subjectField.setText(event.getSubject());
    locationField.setText(event.getLocation() != null ? event.getLocation() : "");
    descriptionArea.setText(event.getDescription() != null ? event.getDescription() : "");

    // Set date and time
    LocalDateTime startDateTime = event.getStartDateTime();
    LocalDateTime endDateTime = event.getEndDateTime();

    // Convert to java.util.Date for the spinners
    Calendar cal = Calendar.getInstance();

    // Set date
    cal.set(startDateTime.getYear(), startDateTime.getMonthValue() - 1, startDateTime.getDayOfMonth());
    dateSpinner.setValue(cal.getTime());

    // Set start time
    cal.set(Calendar.HOUR_OF_DAY, startDateTime.getHour());
    cal.set(Calendar.MINUTE, startDateTime.getMinute());
    startTimeSpinner.setValue(cal.getTime());

    // Set end time
    cal.set(Calendar.HOUR_OF_DAY, endDateTime.getHour());
    cal.set(Calendar.MINUTE, endDateTime.getMinute());
    endTimeSpinner.setValue(cal.getTime());

    // Set checkboxes
    allDayCheckBox.setSelected(event.isAllDay());
    startTimeSpinner.setEnabled(!event.isAllDay());
    endTimeSpinner.setEnabled(!event.isAllDay());

    privateCheckBox.setSelected(!event.isPublic());
    recurringCheckBox.setSelected(isRecurring);
    // Auto-decline option - only set if the method exists on the event
    try {
      autoDeclineCheckBox.setSelected(false); // Default to false
    } catch (Exception e) {
      // If method doesn't exist, just leave the checkbox unchecked
      autoDeclineCheckBox.setEnabled(false);
    }
  }

  /**
   * Lays out the dialog components.
   */
  private void layoutComponents() {
    // Main panel with padding
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Title
    JLabel titleLabel = new JLabel("Edit Event");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
    titleLabel.setForeground(THEME_COLOR);
    gbc.gridwidth = 2;
    mainPanel.add(titleLabel, gbc);

    // Add separator
    gbc.gridy++;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(new JSeparator(), gbc);

    // Subject
    gbc.gridy++;
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridwidth = 1;
    mainPanel.add(createLabel("Subject:"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    mainPanel.add(subjectField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    mainPanel.add(createLabel("Location:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    mainPanel.add(locationField, gbc);

    // Date
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.NONE;
    mainPanel.add(createLabel("Date:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(dateSpinner, gbc);

    // Time range
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.fill = GridBagConstraints.NONE;
    mainPanel.add(createLabel("Start Time:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(startTimeSpinner, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.fill = GridBagConstraints.NONE;
    mainPanel.add(createLabel("End Time:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(endTimeSpinner, gbc);

    // Checkboxes in a panel
    JPanel checkboxPanel = new JPanel(new GridLayout(2, 2, 10, 5));
    checkboxPanel.add(allDayCheckBox);
    checkboxPanel.add(privateCheckBox);
    checkboxPanel.add(recurringCheckBox);
    checkboxPanel.add(autoDeclineCheckBox);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(checkboxPanel, gbc);

    // Description
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.NONE;
    mainPanel.add(createLabel("Description:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    JScrollPane descScrollPane = new JScrollPane(descriptionArea);
    descScrollPane.setPreferredSize(new Dimension(400, 150));
    mainPanel.add(descScrollPane, gbc);

    // Buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton saveButton = new JButton("Save");
    saveButton.setBackground(HEADER_COLOR);
    saveButton.setForeground(Color.WHITE);
    saveButton.addActionListener(e -> {
      if (validateForm()) {
        gatherUpdatedData();
        editConfirmed = true;
        dispose();
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);

    // Add conflict info panel
    JPanel infoPanel = new JPanel(new BorderLayout());
    infoPanel.setBorder(new EmptyBorder(10, 15, 0, 15));
    JTextArea infoText = new JTextArea(
            "Note: Events cannot conflict with each other. Editing that would create " +
                    "a conflict with another existing event is not allowed.");
    infoText.setEditable(false);
    infoText.setLineWrap(true);
    infoText.setWrapStyleWord(true);
    infoText.setBackground(new Color(0xFFFAE6));
    infoText.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFFCC80), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    infoPanel.add(infoText, BorderLayout.CENTER);

    // Main layout
    setLayout(new BorderLayout());
    add(infoPanel, BorderLayout.NORTH);
    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
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
    // Auto-decline is handled separately in the controller if needed

    // Convert date and time to LocalDateTime
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
  }

  /**
   * Shows the dialog and returns whether the edit was confirmed.
   *
   * @return true if the edit was confirmed, false otherwise
   */
  public boolean showDialog() {
    setVisible(true);
    return editConfirmed;
  }

  /**
   * Gets the updated subject.
   *
   * @return the updated subject
   */
  /**
   * Gets the updated subject.
   *
   * @return the updated subject
   */
  public String getSubject() {
    return updatedSubject;
  }

  /**
   * Gets the updated event location.
   *
   * @return the updated event location
   */
  public String getEventLocation() {
    return updatedLocation;
  }

  /**
   * Gets the updated description.
   *
   * @return the updated description
   */
  public String getDescription() {
    return updatedDescription;
  }

  /**
   * Gets the updated start date and time.
   *
   * @return the updated start date and time
   */
  public LocalDateTime getStartDateTime() {
    return updatedStartDateTime;
  }

  /**
   * Gets the updated end date and time.
   *
   * @return the updated end date and time
   */
  public LocalDateTime getEndDateTime() {
    return updatedEndDateTime;
  }

  /**
   * Gets whether the event is all day.
   *
   * @return true if the event is all day, false otherwise
   */
  public boolean isAllDay() {
    return updatedAllDay;
  }

  /**
   * Gets whether the event is private.
   *
   * @return true if the event is private, false otherwise
   */
  public boolean isPrivate() {
    return updatedPrivate;
  }

  /**
   * Gets whether the event is recurring.
   *
   * @return true if the event is recurring, false otherwise
   */
  public boolean isRecurring() {
    return updatedRecurring;
  }

  /**
   * Creates a styled label.
   *
   * @param text the label text
   * @return the styled label
   */
  private JLabel createLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("Arial", Font.PLAIN, 12));
    return label;
  }

  /**
   * Validates the form input.
   *
   * @return true if the form is valid, false otherwise
   */
  private boolean validateForm() {
    // Check required fields
    if (subjectField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this,
              "Subject is required",
              "Validation Error",
              JOptionPane.ERROR_MESSAGE);
      subjectField.requestFocus();
      return false;
    }

    // Validate times
    Date startDate = (Date) startTimeSpinner.getValue();
    Date endDate = (Date) endTimeSpinner.getValue();

    Calendar startCal = Calendar.getInstance();
    startCal.setTime(startDate);

    Calendar endCal = Calendar.getInstance();
    endCal.setTime(endDate);

    if (!allDayCheckBox.isSelected() && endCal.before(startCal)) {
      JOptionPane.showMessageDialog(this,
              "End time must be after start time",
              "Validation Error",
              JOptionPane.ERROR_MESSAGE);
      endTimeSpinner.requestFocus();
      return false;
    }

    return true;
  }
}
