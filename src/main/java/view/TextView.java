package view;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;

/**
 * Text-based view implementation for the calendar application.
 */
public class TextView implements ICalendarView {
  private final Scanner scanner;

  public TextView() {
    this.scanner = new Scanner(System.in);
  }

  @Override
  public String readCommand() {
    return scanner.nextLine();
  }

  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  @Override
  public void displayError(String error) {
    System.err.println("Error: " + error);
  }

  @Override
  public void updateCalendarView(ICalendar calendar) {
    // Not used in text mode
  }

  @Override
  public void updateEventList(List<Event> events) {
    // Not used in text mode
  }

  @Override
  public void updateRecurringEventList(List<RecurringEvent> recurringEvents) {
    // Not used in text mode
  }

  @Override
  public void showEventDetails(Event event) {
    // Not used in text mode
  }

  @Override
  public void clearEventDetails() {
    // Not used in text mode
  }

  @Override
  public void updateCalendarList(List<String> calendarNames) {
    // Not used in text mode
  }

  @Override
  public void setSelectedCalendar(String calendarName) {
    // Not used in text mode
  }

  @Override
  public String getSelectedCalendar() {
    // Not used in text mode
    return null;
  }

  @Override
  public LocalDate getSelectedDate() {
    // Not used in text mode
    return null;
  }

  @Override
  public void setSelectedDate(LocalDate date) {
    // Not used in text mode
  }

  @Override
  public void updateSelectedDate(LocalDate date) {
    // Not used in text mode
  }

  @Override
  public void refreshView() {
    // Not used in text mode
  }
} 