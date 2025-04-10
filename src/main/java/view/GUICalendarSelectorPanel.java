package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

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
  private final JButton useCalendarButton;

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

      JLabel timezoneLabel = new JLabel("(" + calendar.getTimeZone().getID() + ")");
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
    useCalendarButton = new JButton("Use");
    ButtonStyler.applyPrimaryStyle(useCalendarButton);

    // Set preferred size for buttons to ensure they fit
    addCalendarButton.setPreferredSize(new Dimension(120, 25));
    useCalendarButton.setPreferredSize(new Dimension(80, 25));

    // Create a panel for buttons with horizontal layout
    JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
    buttonPanel.setOpaque(false);
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    buttonPanel.add(addCalendarButton);
    buttonPanel.add(useCalendarButton);

    // Scroll pane for calendar list
    JScrollPane scrollPane = new JScrollPane(calendarList);
    scrollPane.setBorder(null);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    add(titleLabel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);

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
          if (listener != null) {
            listener.onCalendarSelected(calendarName);
          }
        }
      }
    });

    addCalendarButton.addActionListener(e -> {
      showAddCalendarDialog();
    });

    useCalendarButton.addActionListener(e -> {
      System.out.println("[DEBUG] Use Calendar button clicked");

      int selectedIndex = calendarList.getSelectedIndex();
      if (selectedIndex >= 0) {
        String calendarName = calendarListModel.getElementAt(selectedIndex);
        System.out.println("[DEBUG] Selected calendar from list: " + calendarName);

        if (listener != null) {
          try {
            System.out.println("[DEBUG] Notifying listener to switch to calendar: " + calendarName);
            listener.onCalendarSelected(calendarName);

            JOptionPane.showMessageDialog(
                    this,
                    "Switched to calendar: " + calendarName,
                    "Calendar Selected",
                    JOptionPane.INFORMATION_MESSAGE);
          } catch (Exception ex) {
            System.err.println("[ERROR] Failed to switch calendar: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to switch calendar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
          }
        } else {
          System.out.println("[WARNING] No calendar selection listener registered");
        }
      } else {
        System.out.println("[DEBUG] No calendar selected in the list");
        JOptionPane.showMessageDialog(
                this,
                "Please select a calendar from the list first",
                "No Calendar Selected",
                JOptionPane.WARNING_MESSAGE);
      }
    });
  }

  /**
   * Shows a dialog for adding a new calendar with name and timezone.
   */
  private void showAddCalendarDialog() {
    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Add Calendar", true);
    dialog.setLayout(new BorderLayout());
    dialog.setResizable(false);

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);

    JLabel nameLabel = new JLabel("Calendar Name:");
    formPanel.add(nameLabel, gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    JTextField nameField = new JTextField(20);
    formPanel.add(nameField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    JLabel timezoneLabel = new JLabel("Timezone:");
    formPanel.add(timezoneLabel, gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    String[] availableTimezones = TimeZone.getAvailableIDs();
    Arrays.sort(availableTimezones);

    JComboBox<String> timezoneComboBox = new JComboBox<>(availableTimezones);
    timezoneComboBox.setSelectedItem(TimeZone.getDefault().getID());
    formPanel.add(timezoneComboBox, gbc);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

    JButton addButton = new JButton("Add");
    JButton cancelButton = new JButton("Cancel");

    ButtonStyler.applyPrimaryStyle(addButton);
    ButtonStyler.applySecondaryStyle(cancelButton);

    Dimension buttonSize = new Dimension(100, 30);
    addButton.setPreferredSize(buttonSize);
    cancelButton.setPreferredSize(buttonSize);
    addButton.setMinimumSize(buttonSize);
    cancelButton.setMinimumSize(buttonSize);

    buttonPanel.add(addButton);
    buttonPanel.add(Box.createHorizontalStrut(10));
    buttonPanel.add(cancelButton);

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

      if (listener != null) {
        listener.onCalendarCreated(calendarName, selectedTimezone);
      }

      dialog.dispose();
    });

    cancelButton.addActionListener(ae -> dialog.dispose());

    dialog.add(formPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    dialog.pack();
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }
} 