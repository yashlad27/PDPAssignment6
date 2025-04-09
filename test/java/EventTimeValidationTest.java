import model.event.Event;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * Test class to validate the event time logic.
 * This tests the core business logic related to event time validation
 * without the complexity of controller-view interaction.
 */
public class EventTimeValidationTest {

    /**
     * Test that an event with end time after start time is valid
     */
    @Test
    public void testValidEventTime() {
        // Create event with end time after start time
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = new Event(
                "Test Event",
                startTime,
                endTime,
                "Test Description",
                "Test Location",
                false
        );
        
        // Verify the event has valid times
        assertTrue("End time should be after start time", 
                event.getEndDateTime().isAfter(event.getStartDateTime()));
    }
    
    /**
     * Test that an event with equal start and end time is invalid
     */
    @Test
    public void testEqualStartEndTime() {
        // Create times that are equal
        LocalDateTime sameTime = LocalDateTime.now();
        
        try {
            Event event = new Event(
                    "Test Event",
                    sameTime,
                    sameTime,
                    "Test Description",
                    "Test Location",
                    false
            );
            
            // If we get here without exception, we need to verify that
            // there's proper validation in the Event class - this is a hint
            // that validation might need to be added
            assertFalse("Equal start and end times should be invalid",
                    event.getEndDateTime().equals(event.getStartDateTime()));
        } catch (IllegalArgumentException e) {
            // If the Event constructor properly validates and throws an exception,
            // this is the expected behavior
            assertTrue("Exception should mention time validation", 
                    e.getMessage().toLowerCase().contains("time"));
        }
    }
    
    /**
     * Test that an event with end time before start time is invalid
     */
    @Test
    public void testEndTimeBeforeStartTime() {
        // Create end time that's before start time
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.minusHours(1);
        
        try {
            Event event = new Event(
                    "Test Event",
                    startTime,
                    endTime,
                    "Test Description",
                    "Test Location",
                    false
            );
            
            // If we get here without exception, we need to verify that
            // validation is happening properly
            assertFalse("End time before start time should be invalid",
                    event.getEndDateTime().isBefore(event.getStartDateTime()));
        } catch (IllegalArgumentException e) {
            // If the Event constructor properly validates and throws an exception,
            // this is the expected behavior
            assertTrue("Exception should mention time validation", 
                    e.getMessage().toLowerCase().contains("time"));
        }
    }
}
