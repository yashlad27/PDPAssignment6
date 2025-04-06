package view;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

import model.event.Event;

/**
 * Dialog for editing events.
 */
public class EventEditDialog extends JDialog {
  private JTextField subjectField;
  private JTextField locationField;
  private JTextArea descriptionArea;
  private JButton saveButton;
  private JButton cancelButton;
  private final Event event;
  private final boolean isRecurring;
  private boolean editConfirmed = false;

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /**
   * Constructs a new EventEditDialog.
   *
   * @param parent      the parent frame
   * @param event       the event to edit
   * @param isRecurring whether the event is recurring
   */
  public EventEditDialog(JFrame parent, Event event, boolean isRecurring) {
    super(parent, "Edit Event", true);
    this.event = event;
    this.isRecurring = isRecurring;

    initComponents();
    layoutComponents();

    setSize(500, 400);
    setLocationRelativeTo(parent);
  }

  /**
   * Initializes the dialog components.
   */
  private void initComponents() {
    subjectField = new JTextField(event.getSubject(), 30);
    locationField = new JTextField(event.getLocation(), 30);
    descriptionArea = new JTextArea(event.getDescription(), 5, 30);

    saveButton = new JButton("Save");
    saveButton.addActionListener(e -> {
      editConfirmed = true;
      dispose();
    });

    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());
  }

  /**
   * Lays out the dialog components.
   */
  private void layoutComponents() {
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Event type
    mainPanel.add(new JLabel("Event Type:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    mainPanel.add(new JLabel(isRecurring ? "Recurring Event" : "Single Event"), gbc);

    // Subject
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    mainPanel.add(new JLabel("Subject:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    mainPanel.add(subjectField, gbc);

    // Date and time (read-only)
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    mainPanel.add(new JLabel("Date & Time:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    LocalDateTime startDateTime = event.getStartDateTime();
    LocalDateTime endDateTime = event.getEndDateTime();
    String dateTimeText = startDateTime.format(DATE_TIME_FORMATTER) + " - " +
            endDateTime.format(DATE_TIME_FORMATTER);
    mainPanel.add(new JLabel(dateTimeText), gbc);

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
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    mainPanel.add(descriptionArea, gbc);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);

    setLayout(new BorderLayout());
    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
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
   * Gets the edited subject.
   *
   * @return the edited subject
   */
  public String getSubject() {
    return subjectField.getText();
  }

  /**
   * Gets the edited event location.
   *
   * @return the edited event location
   */
  public String getEventLocation() {
    return locationField.getText();
  }

  /**
   * Gets the edited description.
   *
   * @return the edited description
   */
  public String getDescription() {
    return descriptionArea.getText();
  }
}
