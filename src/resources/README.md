# Calendar Application

A comprehensive calendar management system that provides both command-line and graphical user interface (GUI) capabilities. The application enables users to:

- **Multiple Calendar Management**: Create and manage multiple calendars with different timezones, allowing for personal, work, and other specialized calendars
- **Event Management**: Create, edit, and manage various types of events including:
  - Single events with specific start and end times
  - All-day events
  - Recurring events with customizable patterns
  - Events with auto-decline for conflicting schedules
  - Private events with restricted visibility

- **Timezone Support**: 
  - Create calendars in different timezones
  - Convert events between timezones
  - Handle daylight saving time automatically
  - View events in local time regardless of calendar timezone

- **User Interface Options**:
  - **Command-Line Interface (CLI)**:
    - Interactive mode for direct command input
    - Headless mode for batch processing of commands
    - Comprehensive command set for all operations
    - Detailed error messages and validation

  - **Graphical User Interface (GUI)**:
    - Modern Swing-based interface
    - Calendar grid view with color-coded events
    - Intuitive event creation and editing forms
    - Drag-and-drop support for event rescheduling
    - Timezone selection and management
    - Calendar switching with visual indicators
    - Event filtering and search capabilities
    - Export/import functionality with visual feedback

- **Advanced Features**:
  - Event conflict detection and resolution
  - Recurring event pattern customization
  - Event copying between calendars
  - CSV export/import for calendar data
  - Event status checking at specific times
  - Private event support
  - Auto-decline for important events

## Design Changes

### 1. Multi-Calendar Support

- **Added `CalendarManager` class**: Serves as the central coordinator for managing multiple
  calendars
- **Added `CalendarRegistry` class**: Stores and indexes calendars by name, maintaining the active
  calendar reference
- **Added `ICalendar` interface**: Provides an abstraction for calendar operations to support
  polymorphism
- **Justification**: These changes create a clear separation of concerns where `CalendarRegistry`
  handles storage while `CalendarManager` manages high-level operations, following Single
  Responsibility Principle

### 2. Timezone Support

- **Added `TimeZoneHandler` class**: Validates timezones and converts times between different
  timezones
- **Added `TimezoneConverter` interface**: Provides a functional way to convert times between
  timezones
- **Enhanced `Calendar` class**: Added timezone property and methods to handle timezone-specific
  operations
- **Justification**: This approach encapsulates timezone logic and makes it reusable across the
  application

### 3. Command System Enhancement

- **Created `CalendarCommandFactory`**: Dedicated factory for calendar-specific commands
- **Added `CalendarCommandHandler` interface**: Functional interface for calendar commands
- **Updated `CalendarController`**: Now routes commands to either event or calendar command
  factories
- **Justification**: This design allows for independent extension of both event and calendar
  commands

### 4. Copy Functionality

- **Added `CopyEventCommand` class**: Unified command for all copy operations
- **Implemented Strategy Pattern**: Created `CopyStrategy` interface with implementations:
    - `SingleEventCopyStrategy`
    - `DayEventsCopyStrategy`
    - `RangeEventsCopyStrategy`
- **Added `CopyStrategyFactory`**: Selects appropriate copy strategy based on command
- **Justification**: Strategy pattern allows for extensible and maintainable copy operations with
  different behaviors

### 5. Builder Pattern for Recurring Events

- **Implemented Builder in `RecurringEvent`**: Allows for step-by-step construction with validation
- **Moved validation to build time**: Ensures objects are created in a valid state
- **Added fluent interface**: Makes code more readable when creating recurring events
- **Justification**: Builder pattern improves the construction of complex objects with many optional
  parameters

### 6. Additional Exception Types

- **Added `CalendarNotFoundException`**: For operations on non-existent calendars
- **Added `DuplicateCalendarException`**: For attempts to create calendars with duplicate names
- **Added `InvalidTimezoneException`**: For operations with invalid timezone specifications
- **Justification**: Specialized exceptions improve error handling and provide clearer feedback

### 7. Enhanced Validation

- **Added `CalendarNameValidator`**: Centralizes calendar name validation logic
- **Improved command validation**: More robust parameter checking in command handlers
- **Enhanced error reporting**: More descriptive error messages
- **Justification**: Better validation improves reliability and user experience

### 8. GUI Implementation

- **Added `GUIView` class**: Provides a modern Swing-based graphical user interface
- **Added `GUIController` class**: Handles user interactions and updates the view
- **Added `EventFormData` class**: Manages form data for event creation and editing
- **Added `DateTimeSelectionDialog`**: Custom dialog for date and time selection
- **Added `EnhancedEventEditDialog`**: Advanced dialog for editing event properties
- **Added `CalendarDisplayManager`**: Manages calendar display and navigation
- **Justification**: The GUI implementation provides a more user-friendly interface while maintaining the core functionality of the command-line version

## Features

### Calendar Management

- Create multiple calendars with different timezones
- Switch between calendars using the `use calendar` command or GUI calendar selector
- Edit calendar properties (e.g., timezone)
- Export calendar events to CSV files
- View calendars in a modern graphical interface

### Event Management

- Create one-time and recurring events through GUI forms
- Support for different repeat patterns (MWF, TR, SU)
- Edit event properties (subject, location, description) through intuitive dialogs
- Copy events between calendars
- Show event status at specific times
- View events in a calendar grid with color coding
- Drag and drop support for event rescheduling

### Command Modes

- Interactive Mode: User can input commands directly
- Headless Mode: Execute commands from a file
- GUI Mode: Use the graphical user interface

## Command Syntax

### Calendar Commands

```
# Create a new calendar with a unique name and timezone
# Example: create calendar --name "Work" --timezone "America/New_York"
create calendar --name <name> --timezone <timezone>

# Switch to a different calendar by specifying its name
# Example: use calendar --name "Personal"
use calendar --name <name>

# Edit calendar properties such as timezone
# Example: edit calendar --name "Work" --property timezone "Europe/London"
edit calendar --name <name> --property <property> <value>

# Export calendar events to a CSV file
# Example: export cal "work_events.csv"
export cal <filename>.csv
```

### Event Commands

```
# Create a single event with optional description, location, and privacy settings
# Example: create event "Team Meeting" from 2024-04-10T10:00 to 2024-04-10T11:00 desc "Weekly sync" at "Conference Room A" private
create event "<subject>" from <start-time> to <end-time> [desc "<description>"] [at "<location>"] [private]

# Create an event that automatically declines conflicting events
# Example: create event --autoDecline "Important Meeting" from 2024-04-10T14:00 to 2024-04-10T15:00
create event --autoDecline "<subject>" from <start-time> to <end-time> [desc "<description>"] [at "<location>"] [private]

# Create an all-day event (spans entire day)
# Example: create event "Holiday" on 2024-12-25 desc "Christmas Day"
create event "<subject>" on <date> [desc "<description>"] [at "<location>"] [private]

# Create a recurring event that repeats a specific number of times
# Example: create event "Standup" from 2024-04-10T09:00 to 2024-04-10T09:30 repeats MWF for 10 times
create event "<subject>" from <start-time> to <end-time> repeats <pattern> for <count> times [desc "<description>"] [at "<location>"]

# Create a recurring event that repeats until a specific end date
# Example: create event "Team Lunch" from 2024-04-10T12:00 to 2024-04-10T13:00 repeats F until 2024-12-31
create event "<subject>" from <start-time> to <end-time> repeats <pattern> until <end-date> [desc "<description>"] [at "<location>"]

# Create an all-day recurring event that repeats a specific number of times
# Example: create event "Weekend" on 2024-04-13 repeats SU for 52 times
create event "<subject>" on <date> repeats <pattern> for <count> times [desc "<description>"] [at "<location>"]

# Create an all-day recurring event that repeats until a specific end date
# Example: create event "Monthly Review" on 2024-04-01 repeats M until 2024-12-31
create event "<subject>" on <date> repeats <pattern> until <end-date> [desc "<description>"] [at "<location>"]

# Edit the subject/title of an existing event
# Example: edit event subject "Team Meeting" from 2024-04-10T10:00 with "Team Sync"
edit event subject "<subject>" from <date-time> with "<new-subject>"

# Edit the location of an existing event
# Example: edit event location "Team Meeting" from 2024-04-10T10:00 with "Virtual Meeting"
edit event location "<subject>" from <date-time> with "<new-location>"

# Edit the time of an existing event
# Example: edit event time "Team Meeting" from 2024-04-10T10:00 to 2024-04-10T11:30
edit event time "<subject>" from <date-time> to <new-date-time>

# Print all events in the current calendar
# Example: print events
print events

# Print events occurring on a specific date
# Example: print events on 2024-04-10
print events on <date>

# Print events occurring within a date range
# Example: print events from 2024-04-01 to 2024-04-30
print events from <start-date> to <end-date>

# Copy a single event to another calendar with a new time
# Example: copy event "Team Meeting" on 2024-04-10T10:00 --target "Personal" to 2024-04-10T15:00
copy event "<subject>" on <date-time> --target <calendar> to <target-date-time>

# Copy all events from a specific date to another calendar
# Example: copy events on 2024-04-10 --target "Work" to 2024-04-11
copy events on <date> --target <calendar> to <target-date>

# Copy all events between two dates to another calendar
# Example: copy events between 2024-04-01 and 2024-04-30 --target "Archive" to 2024-05-01
copy events between <start-date> and <end-date> --target <calendar> to <target-date>

# Show the status of events at a specific time
# Example: show status on 2024-04-10T10:00
show status on <date-time>
```

### Date/Time Format

- Date format: YYYY-MM-DD (e.g., 2024-04-10)
- Time format: hh:mm (e.g., 14:30)
- Combined format: YYYY-MM-DDThh:mm (e.g., 2024-04-10T14:30)

### Repeat Patterns

- MWF: Monday, Wednesday, Friday
- TR: Tuesday, Thursday
- SU: Sunday
- Any combination of M,T,W,R,F,S,U (e.g., MTWRFSU for daily, MWF for Monday/Wednesday/Friday)

### Exit Command

```
# Exit the application
exit
```

## Running the Application

### Application using .jar

- please move .jar file to root directory before executing the following commands.

### Interactive Mode

To run the application in interactive mode:

```bash
java -jar resources/PDPAssignment6-1.0-SNAPSHOT.jar --mode interactive
```

### Headless Mode

To run the application in headless mode with a command file:

```bash
java -jar resources/PDPAssignment6-1.0-SNAPSHOT.jar headless src/resources/headlessCmd.txt
```

### GUI Mode

To run the application with the graphical user interface:

```bash
java -jar resources/PDPAssignment6-1.0-SNAPSHOT.jar
```

## Example Command Files

- `resources/headlessCmd.txt`: Contains valid commands for testing
- `resources/invalidCommands.txt`: Contains invalid commands for error handling testing

## Error Handling

The application handles various error cases including:

- Invalid date/time formats
- Missing required parameters
- Non-existent calendars
- Invalid repeat patterns
- Invalid timezones
- End time before start time

## Dependencies

- Java 11 or higher
- JUnit 4 for testing

## Project Structure

```
src/
├── controller/
│   ├── command/
│   │   ├── calendar/
│   │   └── event/
│   └── parser/
├── model/
│   ├── calendar/
│   └── exceptions/
├── view/
│   ├── dialog/
│   ├── display/
│   └── GUI components
└── CalendarApp.java
```

## Feature Status

### Working Features

- Multiple calendar creation and management
- Timezone support for calendars
- Event creation (single, recurring, all-day)
- Event editing and querying
- Copy events between calendars (single event, day, range)
- Interactive and headless modes
- CSV export
- Graphical User Interface with:
  - Calendar grid view
  - Event creation/editing forms
  - Timezone selection
  - Calendar switching
  - Event filtering
  - Drag and drop support

## Team Contribution

- Calendar Management & Timezone Support: [Gaurav Bidani]
- Copy Functionality & Command System: [Yash Lad]
- UI Improvements & Testing: [Gaurav Bidani]
- Documentation & Bug Fixes: [Yash Lad]

## Exit

To exit the application, use the `exit` command.
