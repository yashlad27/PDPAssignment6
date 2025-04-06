package view;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

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
            Date.from(initialDateTime.atZone(ZoneId.systemDefault()).toInstant()),
            null, null, java.util.Calendar.DAY_OF_MONTH));
    timeSpinner = new JSpinner(new SpinnerDateModel(
            Date.from(initialDateTime.atZone(ZoneId.systemDefault()).toInstant()),
            null, null, Calendar.MINUTE));

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
      Date date = (Date) dateSpinner.getValue();
      Date time = (Date) timeSpinner.getValue();
      selectedDateTime = LocalDateTime.of(
              date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
              time.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
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