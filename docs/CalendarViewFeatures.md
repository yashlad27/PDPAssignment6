# Calendar View Features Interface

## Overview

The `CalendarViewFeatures` interface provides a loosely coupled way for controllers to interact with calendar views. It abstracts the specific implementation details of views, allowing for better separation of concerns, easier testing, and greater flexibility in the UI layer.

## Benefits

- **Loose Coupling**: Controllers don't depend on specific view implementations
- **Testability**: Easy to create mock views for testing controllers
- **Flexibility**: Supports multiple view implementations with the same controller logic
- **Maintainability**: Clearer separation of concerns between view and controller

## Implementation

The `CalendarViewFeatures` interface extends the `IGUIView` interface to maintain backward compatibility with existing code. It defines methods that controllers need to interact with views without knowing their specific implementation details.

### Key Methods

- `displayMessage(String message)`: Display an informational message to the user
- `displayError(String message)`: Display an error message to the user
- `updateCalendarView(ICalendar calendar)`: Update the view with a calendar's data
- `updateEventList(List<Event> events)`: Update the displayed list of events
- `showEventDetails(Event event)`: Show detailed information for a specific event
- `refreshView()`: Force a refresh of all view components

## Using with Controllers

To use the `CalendarViewFeatures` interface with controllers:

1. Declare controller dependencies using the interface rather than concrete classes:

```java
public class MyController {
    private final CalendarViewFeatures view;
    
    public MyController(CalendarViewFeatures view) {
        this.view = view;
    }
    
    // Controller methods that call view methods through the interface
}
```

2. Ensure that view implementations implement this interface:

```java
public class MyView extends JPanel implements CalendarViewFeatures {
    // Implement all required methods
}
```

## Example

Here's a simple example of how to use the interface:

```java
// Controller using the interface
public class EventController {
    private final CalendarViewFeatures view;
    private final EventService service;
    
    public EventController(CalendarViewFeatures view, EventService service) {
        this.view = view;
        this.service = service;
    }
    
    public void loadEvents() {
        try {
            List<Event> events = service.getAllEvents();
            view.updateEventList(events);
        } catch (Exception e) {
            view.displayError("Failed to load events: " + e.getMessage());
        }
    }
}

// Application initialization
CalendarViewFeatures view = new GUIView(/* parameters */);
EventService service = new EventService(/* parameters */);
EventController controller = new EventController(view, service);
```

## Testing

The interface makes it easy to test controllers by creating mock or stub implementations:

```java
class TestView implements CalendarViewFeatures {
    private String lastErrorMessage;
    private List<Event> lastEvents;
    
    // Implement methods to track calls and store values
    
    @Override
    public void displayError(String message) {
        this.lastErrorMessage = message;
    }
    
    @Override
    public void updateEventList(List<Event> events) {
        this.lastEvents = events;
    }
    
    // Methods to check test results
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}

// In a test
@Test
public void testControllerErrorHandling() {
    TestView testView = new TestView();
    EventService mockService = mock(EventService.class);
    when(mockService.getAllEvents()).thenThrow(new RuntimeException("Test exception"));
    
    EventController controller = new EventController(testView, mockService);
    controller.loadEvents();
    
    assertEquals("Failed to load events: Test exception", testView.getLastErrorMessage());
} 