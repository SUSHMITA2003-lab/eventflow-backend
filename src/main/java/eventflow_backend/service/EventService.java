package eventflow_backend.service;

import eventflow_backend.exception.RegistrationException;
import eventflow_backend.exception.ResourceNotFoundException;
import eventflow_backend.model.Event;
import eventflow_backend.model.User;
import eventflow_backend.repository.EventRepository;
import eventflow_backend.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(
            EventRepository eventRepository,
            UserRepository userRepository) {

        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public Event createEvent(
            String email,
            Event event) {

        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Organizer not found"
                        ));

        event.setOrganizer(organizer);

        event.setAvailableSeats(
                event.getCapacity()
        );

        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {

        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {

        return eventRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event not found"
                        ));
    }

    public Event updateEvent(
            String email,
            Long id,
            Event updatedEvent) {

        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event not found"
                        ));

        // Only the organizer who created the event can update it
        if (existingEvent.getOrganizer() == null ||
                !existingEvent.getOrganizer()
                        .getEmail()
                        .equals(email)) {

            throw new AccessDeniedException(
                    "You can only update your own events"
            );
        }

        /*
         * Calculate how many seats are already occupied.
         *
         * Example:
         * capacity = 100
         * availableSeats = 90
         *
         * registeredSeats = 10
         */
        int registeredSeats =
                existingEvent.getCapacity()
                        - existingEvent.getAvailableSeats();

        int newCapacity =
                updatedEvent.getCapacity();

        // Do not allow capacity below already registered attendees
        if (newCapacity < registeredSeats) {

            throw new RegistrationException(
                    "Capacity cannot be less than the number of registered attendees"
            );
        }

        // Update event details
        existingEvent.setTitle(
                updatedEvent.getTitle()
        );

        existingEvent.setDescription(
                updatedEvent.getDescription()
        );

        existingEvent.setLocation(
                updatedEvent.getLocation()
        );

        existingEvent.setEventDate(
                updatedEvent.getEventDate()
        );

        existingEvent.setStartTime(
                updatedEvent.getStartTime()
        );

        existingEvent.setCapacity(
                newCapacity
        );

        // Recalculate available seats
        existingEvent.setAvailableSeats(
                newCapacity - registeredSeats
        );

        return eventRepository.save(
                existingEvent
        );
    }

    public void deleteEvent(
            String email,
            Long id) {

        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event not found"
                        ));

        // Only the organizer who created the event can delete it
        if (existingEvent.getOrganizer() == null ||
                !existingEvent.getOrganizer()
                        .getEmail()
                        .equals(email)) {

            throw new AccessDeniedException(
                    "You can only delete your own events"
            );
        }

        eventRepository.delete(
                existingEvent
        );
    }
}