# Calendar Application - User Guide

## Table of Contents
1. [Getting Started](#getting-started)
2. [Overview](#overview)
3. [Calendar Navigation](#calendar-navigation)
4. [Calendar Management](#calendar-management)
5. [Event Management](#event-management)
6. [Import/Export Features](#importexport-features)
7. [Additional Features](#additional-features)

## Getting Started

### Building the Application
Before running the application, you need to build it using Maven. From the project root directory (not src/), run either:
```bash
mvn clean package
```
or to skip tests:
```bash
mvn clean package -DskipTests
```

### Running the Application
The application can be run in three different modes:

1. **GUI Mode** (Default):
   ```bash
   java -jar target/PDPAssignment6-1.0-SNAPSHOT.jar
   ```
   This launches the graphical user interface.

2. **Interactive Mode**:
   ```bash
   java -jar target/PDPAssignment6-1.0-SNAPSHOT.jar --mode interactive
   ```
   This starts the application in command-line interactive mode.

3. **Headless Mode**:
   ```bash
   java -jar target/PDPAssignment6-1.0-SNAPSHOT.jar --mode headless <path-to-commands-file>
   ```
   This runs the application using commands from a text file.
   - Default commands file location: `src/resources/headlesscmd.txt`
   - You can specify your own commands file path

### Running the Pre-built JAR
A pre-built version of the application is available in the resources folder. You can run it using these commands:

1. **GUI Mode** (Default):
   ```bash
   java -jar resources/PDPAssignment6-1.0-SNAPSHOT.jar
   ```

2. **Interactive Mode**:
   ```bash
   java -jar resources/PDPAssignment6-1.0-SNAPSHOT.jar --mode interactive
   ```

3. **Headless Mode**:
   ```bash
   java -jar resources/PDPAssignment6-1.0-SNAPSHOT.jar --mode headless resources/headlessCmd.txt
   ```
   - Default commands file is available at: `resources/headlessCmd.txt`

Note: After building the application with Maven, use the commands in the "Running the Application" section above to run your newly built jar file from the target directory.

## Overview
The Calendar Application is a feature-rich scheduling tool that allows you to:
- Manage multiple calendars with different time zones
- Create and organize events
- Import and export calendar data
- View your schedule in a monthly format

## Calendar Navigation

### Monthly View
- Navigate between months using left/right arrows
- Click "Today" to return to the current date
- Currently selected day is highlighted with a blue outline
- Days with events are visually indicated

### Date Selection
- Click any date to view its events
- Selected date's events appear in the event list below
- Current date is always accessible via the "Today" button

## Calendar Management

### Calendar List
- View all calendars in the left sidebar
- Currently active calendar is shown at bottom ("Current Calendar: [name]")
- Calendars are displayed with their names and time zones

### Creating a Calendar
1. Click "Add Calendar" in the left sidebar
2. Enter a unique calendar name
3. Select a time zone from the dropdown menu
4. Click "Add" to create the calendar

### Using a Calendar
1. Select a calendar from the list
2. Click "Use" to make it active
3. A confirmation message will appear
4. The current calendar indicator will update

### Editing a Calendar
1. Select a calendar from the list
2. Click "Edit" button
3. Modify the calendar name
4. Click "Save" to apply changes
5. Changes will be reflected in the calendar list

## Event Management

### Creating Events
1. Select a target date in the calendar
2. In the Event Details panel:
   - Enter Subject (required)
   - Verify or modify Date
   - Set Start Time and End Time
   - Add Location (optional)
   - Enter Description (optional)
   - Configure event options:
     * All Day Event: For full-day events
     * Recurring Event: For repeating events
     * Private Event: For personal events
     * Auto-decline: For automatic conflict management
3. Click "Save" to create the event

### Editing Events
1. Select the event from the event list
2. Modify details in the Event Details panel
3. Click "Edit" to save changes
4. Click "Cancel" to discard changes

### Viewing Events
- Events for selected date appear in Event List Results
- Each event shows:
  * Time
  * Subject
  * Location (if specified)
- Click an event to view/edit its details

## Import/Export Features

### Importing Calendar Data
1. In the Import/Export section:
   - Click "Import from CSV"
   - Use "Choose File..." to select your CSV file
   - Follow the import wizard
2. Imported events will be added to the current calendar

### Exporting Calendar Data
1. Select the calendar to export
2. In the Import/Export section:
   - Click "Export to CSV"
   - Choose save location
   - Confirm the export
3. Your calendar data will be saved as a CSV file

## Additional Features

### Calendar Status
- Click "Check Status" to view:
  * Current calendar information
  * Number of events
  * Time zone settings

### Time Zone Management
- Each calendar can have its own time zone
- Events automatically adjust when viewing in different time zones
- Time zone is displayed with calendar name
