package view;

import java.awt.*;
import java.time.LocalDateTime;

import javax.swing.*;

public class DateTimeSelectionDialog extends JDialog {
  private final JSpinner dateSpinner;
  private final JSpinner timeSpinner;
  private LocalDateTime selectedDateTime;

  public DateTimeSelectionDialog(JFrame parent, LocalDateTime initialDateTime) {
    super(parent, "Select Date and Time", true);
    setLayout(new BorderLayout(5, 5));

    // Initialize components
    dateSpinner = new JSpinner(new SpinnerDateModel(
            java.util.Date.from(initialDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
            null, null, java.util.Calendar.DAY_OF_MONTH));
    timeSpinner = new JSpinner(new SpinnerDateModel(
            java.util.Date.from(initialDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
            null, null, java.util.Calendar.MINUTE));

    // Format spinners
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
    JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
    dateSpinner.setEditor(dateEditor);
    timeSpinner.setEditor(timeEditor);

    // Create panel for spinners
    JPanel spinnerPanel = new JPanel(new GridLayout(2, 2, 5, 5));
    spinnerPanel.add(new JLabel("Date:"));
    spinnerPanel.add(dateSpinner);
    spinnerPanel.add(new JLabel("Time:"));
    spinnerPanel.add(timeSpinner);

    // Create button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("Cancel");

    okButton.addActionListener(e -> {
      java.util.Date date = (java.util.Date) dateSpinner.getValue();
      java.util.Date time = (java.util.Date) timeSpinner.getValue();
      selectedDateTime = LocalDateTime.of(
              date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
              time.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime()
      );
      dispose();
    });

    cancelButton.addActionListener(e -> {
      selectedDateTime = null;
      dispose();
    });

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    // Add components to dialog
    add(spinnerPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);

    // Set dialog properties
    pack();
    setLocationRelativeTo(parent);
  }

  public LocalDateTime getSelectedDateTime() {
    return selectedDateTime;
  }
} 