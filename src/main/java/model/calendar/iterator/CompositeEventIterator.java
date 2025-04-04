package model.calendar.iterator;

import model.event.Event;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * A composite iterator that combines multiple event iterators.
 * This allows for unified iteration over different types of events.
 */
public class CompositeEventIterator implements EventIterator {
    private final List<EventIterator> iterators;
    private int currentIteratorIndex = 0;
    
    /**
     * Creates a new CompositeEventIterator.
     *
     * @param iterators the list of iterators to combine
     */
    public CompositeEventIterator(List<EventIterator> iterators) {
        this.iterators = iterators;
    }
    
    @Override
    public boolean hasNext() {
        // If current iterator has next, return true
        if (currentIteratorIndex < iterators.size() && 
            iterators.get(currentIteratorIndex).hasNext()) {
            return true;
        }
        
        // Otherwise, find the next iterator that has elements
        for (int i = currentIteratorIndex + 1; i < iterators.size(); i++) {
            if (iterators.get(i).hasNext()) {
                currentIteratorIndex = i;
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Event next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more events to iterate over");
        }
        
        return iterators.get(currentIteratorIndex).next();
    }
    
    @Override
    public void reset() {
        currentIteratorIndex = 0;
        for (EventIterator iterator : iterators) {
            iterator.reset();
        }
    }
}
