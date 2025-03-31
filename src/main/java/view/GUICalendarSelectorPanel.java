package view;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import model.calendar.ICalendar;

/**
 * Panel class that handles calendar selection and creation.
 */
public class GUICalendarSelectorPanel extends JPanel {
  private static final Color THEME_COLOR = new Color(0x4a86e8);
  private static final Color THEME_LIGHT = new Color(0xe6f2ff);
  private static final Color BORDER_COLOR = new Color(0xcccccc);

  private final JList<String> calendarList;
  private final List<CalendarItem> calendarItems;
  private CalendarSelectorListener listener;
  private ICalendar selectedCalendar;

  private final DefaultListModel<String> calendarListModel;
  private final JButton addCalendarButton;
  private final JButton removeCalendarButton;
  private final JButton selectCalendarButton;
  private final JLabel timezoneLabel;

  /**
   * Interface for calendar selection events.
   */
  public interface CalendarSelectorListener {
    void onCalendarSelected(ICalendar calendar);

    /**
     * Called when a calendar is selected by name.
     *
     * @param calendarName The name of the selected calendar
     */
    default void onCalendarSelected(String calendarName) {
      // Default implementation does nothing
    }

    /**
     * Called when a new calendar is created.
     *
     * @param name     The name of the new calendar
     * @param timezone The timezone of the new calendar
     */
    default void onCalendarCreated(String name, String timezone) {
      // Default implementation does nothing
    }
  }

  private class CalendarItem extends JPanel {
    private final ICalendar calendar;
    private final JLabel nameLabel;
    private final JLabel timezoneLabel;
    private boolean isSelected;

    public CalendarItem(ICalendar calendar) {
      this.calendar = calendar;
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      setBackground(Color.WHITE);

      JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 2));
      contentPanel.setOpaque(false);

      nameLabel = new JLabel(calendar.toString());
      nameLabel.setFont(nameLabel.getFont().deriveFont(11f));

      timezoneLabel = new JLabel("(" + calendar.getTimeZone().getID() + ")");
      timezoneLabel.setFont(timezoneLabel.getFont().deriveFont(11f));
      timezoneLabel.setForeground(Color.GRAY);

      contentPanel.add(nameLabel);
      contentPanel.add(timezoneLabel);

      add(contentPanel, BorderLayout.CENTER);

      addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
          setSelected(true);
          if (listener != null) {
            listener.onCalendarSelected(calendar);
          }
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
          if (!isSelected) {
            setBackground(new Color(0xf8f8f8));
          }
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
          if (!isSelected) {
            setBackground(Color.WHITE);
          }
        }
      });
    }

    public void setSelected(boolean selected) {
      isSelected = selected;
      if (selected) {
        setBackground(THEME_LIGHT);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(THEME_COLOR),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        nameLabel.setForeground(THEME_COLOR);
      } else {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        nameLabel.setForeground(Color.BLACK);
      }
    }

    public ICalendar getCalendar() {
      return calendar;
    }
  }

  /**
   * Constructs a new GUICalendarSelectorPanel.
   */
  public GUICalendarSelectorPanel() {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setBackground(new Color(0xf8f8f8));

    // Title
    JLabel titleLabel = new JLabel("Calendars");
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

    // Calendar list
    calendarListModel = new DefaultListModel<>();
    calendarList = new JList<>(calendarListModel);
    calendarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    calendarList.setVisibleRowCount(5);
    calendarList.setBorder(BorderFactory.createTitledBorder("Calendars"));

    // Add Calendar button
    addCalendarButton = new JButton("Add Calendar");
    removeCalendarButton = new JButton("Remove Calendar");
    selectCalendarButton = new JButton("Select Calendar");
    timezoneLabel = new JLabel("Timezone: ");

    // Scroll pane for calendar list
    JScrollPane scrollPane = new JScrollPane(calendarList);
    scrollPane.setBorder(null);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    add(titleLabel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);
    add(addCalendarButton, BorderLayout.SOUTH);

    calendarItems = new ArrayList<>();

    setupLayout();
    setupListeners();
  }

  /**
   * Sets the list of calendars.
   *
   * @param calendars the list of calendars
   */
  public void setCalendars(List<ICalendar> calendars) {
    calendarList.removeAll();
    calendarItems.clear();

    for (ICalendar calendar : calendars) {
      CalendarItem item = new CalendarItem(calendar);
      if (calendar.equals(selectedCalendar)) {
        item.setSelected(true);
      }
      calendarItems.add(item);
      calendarList.add(item);
      calendarList.add(Box.createVerticalStrut(5));
    }

    revalidate();
    repaint();
  }

  /**
   * Sets the selected calendar.
   *
   * @param calendar the selected calendar
   */
  public void setSelectedCalendar(ICalendar calendar) {
    selectedCalendar = calendar;
    for (CalendarItem item : calendarItems) {
      item.setSelected(item.getCalendar().equals(calendar));
    }
  }

  /**
   * Adds a listener for calendar selection events.
   *
   * @param listener the listener to add
   */
  public void addCalendarSelectorListener(CalendarSelectorListener listener) {
    this.listener = listener;
  }

  /**
   * Updates the list of calendars.
   *
   * @param calendarNames the list of calendar names
   */
  public void updateCalendarList(List<String> calendarNames) {
    calendarListModel.clear();
    for (String name : calendarNames) {
      calendarListModel.addElement(name);
    }
  }

  /**
   * Sets the selected calendar by name.
   *
   * @param calendarName the name of the calendar to select
   */
  public void setSelectedCalendar(String calendarName) {
    if (calendarName != null) {
      int index = calendarListModel.indexOf(calendarName);
      if (index >= 0) {
        calendarList.setSelectedIndex(index);
      }
    }
  }

  /**
   * Gets the selected calendar.
   *
   * @return the selected calendar, or null if no calendar is selected
   */
  public ICalendar getSelectedCalendar() {
    return selectedCalendar;
  }

  /**
   * Gets the name of the selected calendar.
   *
   * @return the name of the selected calendar, or null if no calendar is selected
   */
  public String getSelectedCalendarName() {
    if (selectedCalendar != null) {
      return selectedCalendar.toString();
    }
    return null;
  }

  /**
   * Refreshes the calendar selector panel.
   */
  public void refresh() {
    revalidate();
    repaint();
  }

  private void setupLayout() {
    // Implementation of setupLayout method
  }

  private void setupListeners() {
    calendarList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        int index = calendarList.getSelectedIndex();
        if (index >= 0) {
          String calendarName = calendarListModel.getElementAt(index);
          // Notify listener
          if (listener != null) {
            // Assuming listener has a method to handle selection by name
            listener.onCalendarSelected(calendarName);
          }
        }
      }
    });

    addCalendarButton.addActionListener(e -> {
      // Show dialog to create a new calendar
      showAddCalendarDialog();
    });
  }

  /**
   * Shows a dialog for adding a new calendar with name and timezone.
   */
  private void showAddCalendarDialog() {
    // Create the dialog
    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Calendar", true);
    dialog.setLayout(new BorderLayout());
    dialog.setResizable(false);

    // Create form panel
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Calendar name field
    JLabel nameLabel = new JLabel("Calendar Name:");
    formPanel.add(nameLabel, gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    JTextField nameField = new JTextField(20);
    formPanel.add(nameField, gbc);

    // Timezone selector
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    JLabel timezoneLabel = new JLabel("Timezone:");
    formPanel.add(timezoneLabel, gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    // Get all available timezone IDs
    String[] availableTimezones = java.util.TimeZone.getAvailableIDs();
    // Sort timezones alphabetically
    java.util.Arrays.sort(availableTimezones);

    JComboBox<String> timezoneComboBox = new JComboBox<>(availableTimezones);
    // Set default to system timezone
    timezoneComboBox.setSelectedItem(java.util.TimeZone.getDefault().getID());
    formPanel.add(timezoneComboBox, gbc);

    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton addButton = new JButton("Add");
    JButton cancelButton = new JButton("Cancel");

    // Style buttons
    addButton.setBackground(THEME_COLOR);
    addButton.setForeground(Color.WHITE);
    addButton.setFont(new Font("Arial", Font.BOLD, 12));
    addButton.setFocusPainted(false);
    addButton.setOpaque(true);
    addButton.setContentAreaFilled(true);
    addButton.setBorderPainted(true);
    addButton.setBorder(new RoundRectBorder(THEME_COLOR, 8));
    addButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            addButton.setBackground(new Color(41, 98, 255));
            addButton.setBorder(new RoundRectBorder(new Color(41, 98, 255), 8));
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            addButton.setBackground(THEME_COLOR);
            addButton.setBorder(new RoundRectBorder(THEME_COLOR, 8));
        }
    });

    // Style cancel button
    cancelButton.setBackground(new Color(0xf0f0f0));
    cancelButton.setForeground(new Color(0x333333));
    cancelButton.setFocusPainted(false);
    cancelButton.setOpaque(true);
    cancelButton.setContentAreaFilled(true);
    cancelButton.setBorderPainted(true);
    cancelButton.setBorder(new RoundRectBorder(new Color(0xcccccc), 8));

    // Center the buttons with some space between them
    buttonPanel.add(addButton);
    buttonPanel.add(Box.createHorizontalStrut(10));
    buttonPanel.add(cancelButton);

    // Event handlers
    addButton.addActionListener(ae -> {
      String calendarName = nameField.getText().trim();
      String selectedTimezone = (String) timezoneComboBox.getSelectedItem();

      if (calendarName.isEmpty()) {
        JOptionPane.showMessageDialog(dialog,
                "Calendar name cannot be empty.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Notify listener about the new calendar
      if (listener != null) {
        // Assuming listener has a method to handle calendar creation
        listener.onCalendarCreated(calendarName, selectedTimezone);
      }

      dialog.dispose();
    });

    cancelButton.addActionListener(ae -> dialog.dispose());

    // Add components to dialog
    dialog.add(formPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    // Set dialog properties
    dialog.pack();
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }
} 