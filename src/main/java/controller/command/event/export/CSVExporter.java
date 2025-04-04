package controller.command.event.export;

import model.event.Event;
import model.event.RecurringEvent;
import model.calendar.ICalendar;
import model.export.IDataExporter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports calendar events to CSV format.
 */
public class CSVExporter implements IDataExporter {
    private static final String CSV_HEADER = "Name,Start Time,End Time,Location,Description,Is Recurring,Repeat Days,Repeat Count,Until Date";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Exports events from a calendar to a CSV file.
     *
     * @param calendar the calendar containing events to export
     * @param filePath the path to save the CSV file
     * @throws IOException if there is an error writing the file
     */
    public void exportToCSV(ICalendar calendar, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(CSV_HEADER + "\n");
            
            List<Event> events = calendar.getAllEvents();
            for (Event event : events) {
                writer.write(formatEvent(event) + "\n");
            }
        }
    }

    @Override
    public String export(String filePath, List<Event> events) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(CSV_HEADER + "\n");
            
            for (Event event : events) {
                writer.write(formatEvent(event) + "\n");
            }
        }
        return filePath;
    }
    
    @Override
    public String formatForDisplay(List<Event> events, boolean showDetails) {
        StringBuilder sb = new StringBuilder();
        for (Event event : events) {
            sb.append(event.getSubject()).append(" - ");
            sb.append(event.getStartDateTime().format(DATE_TIME_FORMATTER)).append(" to ");
            sb.append(event.getEndDateTime().format(DATE_TIME_FORMATTER));
            
            if (showDetails) {
                sb.append("\n  Location: ").append(event.getLocation());
                sb.append("\n  Description: ").append(event.getDescription());
                if (event instanceof RecurringEvent) {
                    RecurringEvent recurringEvent = (RecurringEvent) event;
                    sb.append("\n  Recurring: Yes (").append(recurringEvent.getRepeatDays()).append(")");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatEvent(Event event) {
        StringBuilder sb = new StringBuilder();
        sb.append(escapeCsv(event.getSubject())).append(",");
        sb.append(event.getStartDateTime().format(DATE_TIME_FORMATTER)).append(",");
        sb.append(event.getEndDateTime().format(DATE_TIME_FORMATTER)).append(",");
        sb.append(escapeCsv(event.getLocation())).append(",");
        sb.append(escapeCsv(event.getDescription())).append(",");
        
        if (event instanceof RecurringEvent) {
            RecurringEvent recurringEvent = (RecurringEvent) event;
            sb.append("true,");
            sb.append(recurringEvent.getRepeatDays()).append(",");
            sb.append(recurringEvent.getOccurrences()).append(",");
            sb.append(recurringEvent.getEndDate() != null ? 
                     recurringEvent.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
        } else {
            sb.append("false,,,");
        }
        
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
} 