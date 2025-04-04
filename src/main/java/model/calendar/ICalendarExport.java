package model.calendar;

import model.export.IDataExporter;
import java.io.IOException;

/**
 * Interface for calendar export operations.
 */
public interface ICalendarExport {
    /**
     * Exports calendar data to a file.
     *
     * @param filePath the path where the file should be created
     * @param exporter the exporter to use for formatting the data
     * @return the path of the exported file
     * @throws IOException if an I/O error occurs
     */
    String exportData(String filePath, IDataExporter exporter) throws IOException;
} 