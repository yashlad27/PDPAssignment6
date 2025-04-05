package utilities;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.format.DateTimeFormatter;

import model.event.Event;

/**
 * Utility class for printing event details.
 */
public class EventPrinter implements Printable {
  private final Event event;
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /**
   * Constructs a new EventPrinter.
   *
   * @param event the event to print
   */
  public EventPrinter(Event event) {
    this.event = event;
  }

  /**
   * Prints the event.
   *
   * @return true if printing was successful, false otherwise
   */
  public boolean print() {
    PrinterJob job = PrinterJob.getPrinterJob();
    job.setPrintable(this);
    boolean doPrint = job.printDialog();
    if (doPrint) {
      try {
        job.print();
        return true;
      } catch (PrinterException e) {
        System.err.println("Printing failed: " + e.getMessage());
        return false;
      }
    }
    return false;
  }

  @Override
  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
    if (pageIndex > 0) {
      return NO_SUCH_PAGE;
    }

    Graphics2D g2d = (Graphics2D) graphics;
    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

    // Draw event details
    int y = 50;
    int lineHeight = 20;

    g2d.drawString("Event Details", 50, y);
    y += lineHeight * 2;

    g2d.drawString("Subject: " + event.getSubject(), 50, y);
    y += lineHeight;

    g2d.drawString("Date: " + event.getStartDateTime().toLocalDate(), 50, y);
    y += lineHeight;

    g2d.drawString("Time: " + event.getStartDateTime().toLocalTime() + " - " + event.getEndDateTime().toLocalTime(), 50, y);
    y += lineHeight;

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      g2d.drawString("Location: " + event.getLocation(), 50, y);
      y += lineHeight;
    }

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      g2d.drawString("Description: " + event.getDescription(), 50, y);
    }

    return PAGE_EXISTS;
  }
}
