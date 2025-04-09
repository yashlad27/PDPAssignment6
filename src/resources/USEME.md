# Calendar Application User Guide

This document provides instructions on how to use the Calendar Application's graphical user interface (GUI) to perform various operations.

## Table of Contents
- [Calendar Management](#calendar-management)
- [Event Management](#event-management)
- [Date Navigation](#date-navigation)
- [Import and Export](#import-and-export)
- [Status and Information](#status-and-information)

## Calendar Management

### Creating a New Calendar
- Locate the "Calendar" section in the left sidebar
- Enter a name for your new calendar in the text field
- Select a timezone from the dropdown menu (e.g., "America/New_York")
- Click the "Add" button
- Your new calendar will be created and automatically selected

### Switching Between Calendars
- Locate the "Calendar" section in the left sidebar
- Select a calendar from the dropdown list
- Click the "Use" button
- The application will switch to the selected calendar and display its events

## Event Management

### Creating a New Event
- Select a date in the calendar grid by clicking on it
- Fill in the event details in the form on the right:
  - Subject (required): Enter a name for your event
  - Date: The selected date (can be changed using the date picker)
  - Start Time: Set the event start time
  - End Time: Set the event end time
  - Location: Enter the event location (optional)
  - Description: Add details about the event (optional)
  - All Day: Check this box if the event lasts all day
  - Private: Check this box if the event is private
- Click the "Save" button to create the event
- The event will appear in the event list for the selected date

### Viewing Events
- Click on a date in the calendar grid
- All events for that date will be displayed in the event list below the calendar
- Dates with events are highlighted in the calendar grid
- Select an event from the list to view its details in the form on the right

### Editing an Event
- Select the date containing the event you want to edit
- Click on the event in the event list to select it
- The event details will be displayed in the form on the right
- Click the "Edit" button to enable editing mode
- Modify the event details as needed
- Click the "Save" button to save your changes

### Copying an Event
- Select the date containing the event you want to copy
- Click on the event in the event list to select it
- Click the "Copy" button
- In the copy dialog that appears:
  - Select the target calendar from the dropdown list
  - Adjust the date and time if needed
  - Click "Copy" to create a copy of the event

## Date Navigation

### Changing Months
- Use the arrow buttons at the top of the calendar to navigate between months
- Click "<<" to go to the previous month
- Click ">>" to go to the next month

### Jumping to a Specific Date
- Use the date picker in the event form to select a specific date
- The calendar will automatically navigate to the month containing that date

## Import and Export

### Importing Events from CSV
- Locate the "Import/Export" section in the left sidebar
- Click the "Choose File..." button
- Browse to and select a CSV file containing event data
- The file will be automatically imported, and events will be added to the current calendar
- A success message will be displayed showing the number of events imported

### Exporting Events to CSV
- Locate the "Import/Export" section in the left sidebar
- Click the "Export Calendar" button
- Choose a location and filename for the CSV file
- Click "Save"
- All events from the current calendar will be exported to the CSV file
- A success message will be displayed confirming the export

## Status and Information

### Checking Date Status
- Select a date in the calendar
- Click the "Check Status" button in the left sidebar
- The status bar will display whether the selected date is busy or available

### Listing Events for a Date
- Select a date in the calendar
- Click the "List Events" button in the left sidebar
- All events for the selected date will be displayed in the event list

### Viewing Events in a Date Range
- Select a start date in the calendar
- Click the "Show Range" button in the left sidebar
- Select an end date when prompted
- All events within the date range will be displayed in the event list

### Viewing Application Messages
- The status bar at the bottom of the application displays important messages
- Success messages are displayed in green
- Error messages are displayed in red
- Information messages are displayed in blue

## Additional Features

### Recurring Events
- When creating or editing an event, check the "Recurring" checkbox
- Select the days of the week on which the event should recur
- Optionally set an end date for the recurring event
- Click "Save" to create the recurring event

### All-Day Events
- When creating or editing an event, check the "All Day" checkbox
- The start and end times will be disabled
- The event will be displayed as an all-day event in the calendar

