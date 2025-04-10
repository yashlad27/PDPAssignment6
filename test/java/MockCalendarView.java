import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import view.ICalendarView;

/**
 * Mock implementation of ICalendarView for testing purposes. Tracks displayed messages and errors
 * for verification in tests.
 */
public class MockCalendarView implements ICalendarView {

  private final List<String> displayedMessages;
  private final List<String> displayedErrors;

  public MockCalendarView() {
    this.displayedMessages = new ArrayList<>();
    this.displayedErrors = new ArrayList<>();
  }

  @Override
  public String readCommand() {
    return "";
  }

  @Override
  public void displayMessage(String message) {
    displayedMessages.add(message);
  }

  @Override
  public void displayError(String error) {
    displayedErrors.add(error);
  }

  @Override
  public void updateCalendarView(ICalendar calendar) {

  }

  @Override
  public void updateEventList(List<Event> events) {

  }

  @Override
  public void updateRecurringEventList(List<RecurringEvent> recurringEvents) {

  }

  @Override
  public void showEventDetails(Event event) {

  }

  @Override
  public void clearEventDetails() {

  }

  @Override
  public void updateCalendarList(List<String> calendarNames) {

  }

  @Override
  public void setSelectedCalendar(String calendarName) {

  }

  @Override
  public String getSelectedCalendar() {
    return "";
  }

  @Override
  public LocalDate getSelectedDate() {
    return null;
  }

  @Override
  public void setSelectedDate(LocalDate date) {

  }

  @Override
  public void refreshView() {

  }

  @Override
  public void updateSelectedDate(LocalDate date) {

  }

  public List<String> getDisplayedMessages() {
    return new ArrayList<>(displayedMessages);
  }

  public List<String> getDisplayedErrors() {
    return new ArrayList<>(displayedErrors);
  }

  public void clear() {
    displayedMessages.clear();
    displayedErrors.clear();
  }
}