package view;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel class that handles calendar selection and creation.
 */
public class GUICalendarSelectorPanel extends JPanel {
    private final JList<String> calendarList;
    private final JTextField newCalendarNameField;
    private final JComboBox<String> timezoneComboBox;
    private final JButton createButton;
    private final DefaultListModel<String> listModel;
    private CalendarSelectionListener listener;

    /**
     * Interface for calendar selection events.
     */
    public interface CalendarSelectionListener {
        void onCalendarSelected(String calendarName);
        void onCalendarCreated(String calendarName, String timezone);
    }

    /**
     * Constructs a new GUICalendarSelectorPanel.
     */
    public GUICalendarSelectorPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Calendars"));

        // Initialize components
        listModel = new DefaultListModel<>();
        calendarList = new JList<>(listModel);
        newCalendarNameField = new JTextField();
        timezoneComboBox = new JComboBox<>(java.util.TimeZone.getAvailableIDs());
        createButton = new JButton("Create Calendar");

        // Set up layout
        setupLayout();
        setupListeners();
    }

    /**
     * Sets up the layout of the calendar selector panel.
     */
    private void setupLayout() {
        // Calendar list panel
        JPanel listPanel = new JPanel(new BorderLayout(5, 5));
        listPanel.add(new JLabel("Available Calendars:"), BorderLayout.NORTH);
        listPanel.add(new JScrollPane(calendarList), BorderLayout.CENTER);

        // New calendar panel
        JPanel newCalendarPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        newCalendarPanel.setBorder(BorderFactory.createTitledBorder("Create New Calendar"));
        newCalendarPanel.add(new JLabel("Calendar Name:"));
        newCalendarPanel.add(newCalendarNameField);
        newCalendarPanel.add(new JLabel("Timezone:"));
        newCalendarPanel.add(timezoneComboBox);
        newCalendarPanel.add(createButton);

        // Add components to panel
        add(listPanel, BorderLayout.CENTER);
        add(newCalendarPanel, BorderLayout.SOUTH);
    }

    /**
     * Sets up event listeners.
     */
    private void setupListeners() {
        calendarList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listener != null) {
                String selected = calendarList.getSelectedValue();
                if (selected != null) {
                    listener.onCalendarSelected(selected);
                }
            }
        });

        createButton.addActionListener(e -> {
            if (listener != null) {
                String name = newCalendarNameField.getText().trim();
                String timezone = (String) timezoneComboBox.getSelectedItem();
                if (!name.isEmpty()) {
                    listener.onCalendarCreated(name, timezone);
                    newCalendarNameField.setText("");
                }
            }
        });
    }

    /**
     * Updates the list of calendars.
     *
     * @param calendarNames the list of calendar names
     */
    public void updateCalendars(List<String> calendarNames) {
        listModel.clear();
        for (String name : calendarNames) {
            listModel.addElement(name);
        }
    }

    /**
     * Adds a new calendar to the list.
     *
     * @param name the name of the calendar
     * @param timezone the timezone of the calendar
     */
    public void addCalendar(String name, String timezone) {
        if (!listModel.contains(name)) {
            listModel.addElement(name);
        }
    }

    /**
     * Gets the currently selected calendar.
     *
     * @return the selected calendar name
     */
    public String getSelectedCalendar() {
        return calendarList.getSelectedValue();
    }

    /**
     * Sets the selected calendar.
     *
     * @param calendarName the name of the calendar to select
     */
    public void setSelectedCalendar(String calendarName) {
        int index = listModel.indexOf(calendarName);
        if (index >= 0) {
            calendarList.setSelectedIndex(index);
        }
    }

    /**
     * Adds a listener for calendar selection events.
     *
     * @param listener the listener to add
     */
    public void addCalendarSelectionListener(CalendarSelectionListener listener) {
        this.listener = listener;
    }

    /**
     * Refreshes the calendar selector panel.
     */
    public void refresh() {
        calendarList.revalidate();
        calendarList.repaint();
    }
} 