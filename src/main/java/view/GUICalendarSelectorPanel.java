package view;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.util.TimeZone;

/**
 * Panel class that handles calendar selection and creation.
 */
public class GUICalendarSelectorPanel extends JPanel {
    private final JList<String> calendarList;
    private final JTextField newCalendarNameField;
    private final JComboBox<String> timezoneComboBox;
    private final JButton createButton;
    private final JLabel currentCalendarLabel;
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
        setBorder(BorderFactory.createTitledBorder("Calendar Selection"));

        // Initialize components
        DefaultListModel<String> listModel = new DefaultListModel<>();
        calendarList = new JList<>(listModel);
        calendarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        currentCalendarLabel = new JLabel("Currently using: None");
        currentCalendarLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        newCalendarNameField = new JTextField(20);
        createButton = new JButton("Create Calendar");
        
        // Initialize timezone combo box
        String[] availableZoneIds = TimeZone.getAvailableIDs();
        Arrays.sort(availableZoneIds);
        timezoneComboBox = new JComboBox<>(availableZoneIds);
        timezoneComboBox.setSelectedItem(TimeZone.getDefault().getID());

        // Set up layout
        setupLayout();
        setupListeners();
    }

    /**
     * Sets up the layout of the calendar selector panel.
     */
    private void setupLayout() {
        // Current calendar indicator
        JPanel currentCalendarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        currentCalendarPanel.add(currentCalendarLabel);
        currentCalendarPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        // Calendar list
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Available Calendars"));
        listPanel.add(new JScrollPane(calendarList), BorderLayout.CENTER);

        // Calendar creation panel
        JPanel createPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        createPanel.setBorder(BorderFactory.createTitledBorder("Create New Calendar"));
        
        // Add instruction label
        JLabel instructionLabel = new JLabel("Enter a name and select timezone to create a new calendar");
        instructionLabel.setForeground(Color.GRAY);
        createPanel.add(instructionLabel, "span 2");

        createPanel.add(new JLabel("Calendar Name:"));
        createPanel.add(newCalendarNameField);
        createPanel.add(new JLabel("Timezone:"));
        createPanel.add(timezoneComboBox);
        createPanel.add(new JLabel(""));
        createPanel.add(createButton);

        // Add all components to the main panel
        add(currentCalendarPanel, BorderLayout.NORTH);
        add(listPanel, BorderLayout.CENTER);
        add(createPanel, BorderLayout.SOUTH);
    }

    /**
     * Sets up event listeners.
     */
    private void setupListeners() {
        calendarList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listener != null) {
                String selectedCalendar = calendarList.getSelectedValue();
                if (selectedCalendar != null) {
                    listener.onCalendarSelected(selectedCalendar);
                }
            }
        });

        createButton.addActionListener(e -> {
            if (listener != null) {
                String calendarName = newCalendarNameField.getText().trim();
                if (!calendarName.isEmpty()) {
                    String timezone = (String) timezoneComboBox.getSelectedItem();
                    listener.onCalendarCreated(calendarName, timezone);
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
        DefaultListModel<String> model = (DefaultListModel<String>) calendarList.getModel();
        model.clear();
        for (String name : calendarNames) {
            model.addElement(name);
        }
    }

    /**
     * Adds a new calendar to the list.
     *
     * @param name the name of the calendar
     * @param timezone the timezone of the calendar
     */
    public void addCalendar(String name, String timezone) {
        DefaultListModel<String> model = (DefaultListModel<String>) calendarList.getModel();
        if (!model.contains(name)) {
            model.addElement(name);
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
        if (calendarName != null) {
            currentCalendarLabel.setText("Currently using: " + calendarName);
            // Find and select the calendar in the list
            DefaultListModel<String> model = (DefaultListModel<String>) calendarList.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i).equals(calendarName)) {
                    calendarList.setSelectedIndex(i);
                    calendarList.ensureIndexIsVisible(i);
                    break;
                }
            }
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