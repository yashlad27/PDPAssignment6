package view;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * A panel that displays a month view of the calendar.
 * This component shows the days of the current month in a grid layout.
 */
public class GUICalendarPanel extends JPanel {
    private final JLabel monthLabel;
    private final JPanel calendarGrid;
    private final JButton prevMonthButton;
    private final JButton nextMonthButton;
    private YearMonth currentMonth;
    private static final String[] DAYS_OF_WEEK = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    /**
     * Constructs a new GUICalendarPanel.
     * Initializes the calendar display with the current month.
     */
    public GUICalendarPanel() {
        setLayout(new BorderLayout());
        currentMonth = YearMonth.now();

        // Create month navigation panel
        JPanel navigationPanel = new JPanel(new BorderLayout());
        prevMonthButton = new JButton("←");
        nextMonthButton = new JButton("→");
        monthLabel = new JLabel(currentMonth.format(MONTH_FORMATTER));
        monthLabel.setHorizontalAlignment(SwingConstants.CENTER);

        navigationPanel.add(prevMonthButton, BorderLayout.WEST);
        navigationPanel.add(monthLabel, BorderLayout.CENTER);
        navigationPanel.add(nextMonthButton, BorderLayout.EAST);

        // Create calendar grid
        calendarGrid = new JPanel(new GridLayout(7, 7, 5, 5));
        calendarGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add day headers
        for (String day : DAYS_OF_WEEK) {
            JLabel label = new JLabel(day);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            calendarGrid.add(label);
        }

        // Add navigation listeners
        prevMonthButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendarDisplay();
        });

        nextMonthButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendarDisplay();
        });

        // Add components to panel
        add(navigationPanel, BorderLayout.NORTH);
        add(calendarGrid, BorderLayout.CENTER);

        // Initial calendar display
        updateCalendarDisplay();
    }

    /**
     * Updates the calendar display to show the current month.
     */
    private void updateCalendarDisplay() {
        // Update month label
        monthLabel.setText(currentMonth.format(MONTH_FORMATTER));

        // Clear existing day cells (keep day headers)
        for (int i = 7; i < calendarGrid.getComponentCount(); i++) {
            calendarGrid.remove(i);
        }

        // Get the first day of the month and its day of week (0 = Sunday)
        LocalDate firstDay = currentMonth.atDay(1);
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;

        // Add empty cells for days before the first of the month
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarGrid.add(new JLabel(""));
        }

        // Add cells for each day of the month
        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setHorizontalAlignment(SwingConstants.CENTER);
            calendarGrid.add(dayButton);
        }

        // Revalidate and repaint the panel
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    /**
     * Gets the currently displayed month.
     *
     * @return the current YearMonth
     */
    public YearMonth getCurrentMonth() {
        return currentMonth;
    }
} 