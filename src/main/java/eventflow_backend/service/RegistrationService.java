package eventflow_backend.service;

import eventflow_backend.exception.RegistrationException;
import eventflow_backend.exception.ResourceNotFoundException;
import eventflow_backend.model.Event;
import eventflow_backend.model.Registration;
import eventflow_backend.model.User;
import eventflow_backend.repository.EventRepository;
import eventflow_backend.repository.RegistrationRepository;
import eventflow_backend.repository.TicketRepository;
import eventflow_backend.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;

    public RegistrationService(
            RegistrationRepository registrationRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            TicketRepository ticketRepository) {

        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Registration registerForEvent(
            String email,
            Long eventId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event not found"
                        ));

        Registration existingRegistration =
                registrationRepository
                        .findByUserIdAndEventId(
                                user.getId(),
                                event.getId()
                        )
                        .orElse(null);

        if (existingRegistration != null) {

            if (!"CANCELLED".equals(
                    existingRegistration.getStatus())) {

                throw new RegistrationException(
                        "You have already registered for this event"
                );
            }

            if (event.getAvailableSeats() <= 0) {

                throw new RegistrationException(
                        "No seats available for this event"
                );
            }

            existingRegistration.setStatus(
                    "CONFIRMED"
            );

            existingRegistration.setRegistrationDate(
                    LocalDateTime.now()
            );

            event.setAvailableSeats(
                    event.getAvailableSeats() - 1
            );

            eventRepository.save(event);

            return registrationRepository.save(
                    existingRegistration
            );
        }

        if (event.getAvailableSeats() <= 0) {

            throw new RegistrationException(
                    "No seats available for this event"
            );
        }

        Registration registration =
                new Registration();

        registration.setUser(user);
        registration.setEvent(event);

        registration.setRegistrationDate(
                LocalDateTime.now()
        );

        registration.setStatus(
                "CONFIRMED"
        );

        event.setAvailableSeats(
                event.getAvailableSeats() - 1
        );

        eventRepository.save(event);

        return registrationRepository.save(
                registration
        );
    }

    public List<Registration> getMyRegistrations(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        return registrationRepository.findByUserId(
                user.getId()
        );
    }

    @Transactional
    public Registration cancelRegistration(
            String email,
            Long registrationId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        Registration registration =
                registrationRepository
                        .findByIdAndUserId(
                                registrationId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Registration not found"
                                ));

        if ("CANCELLED".equals(
                registration.getStatus())) {

            throw new RegistrationException(
                    "Registration is already cancelled"
            );
        }

        if ("CHECKED_IN".equals(
                registration.getStatus())) {

            throw new RegistrationException(
                    "Checked-in registration cannot be cancelled"
            );
        }

        Event event =
                registration.getEvent();

        event.setAvailableSeats(
                Math.min(
                        event.getCapacity(),
                        event.getAvailableSeats() + 1
                )
        );

        // Delete old ticket if one exists
        ticketRepository
                .findByRegistrationId(
                        registration.getId()
                )
                .ifPresent(
                        ticketRepository::delete
                );

        registration.setStatus(
                "CANCELLED"
        );

        eventRepository.save(event);

        return registrationRepository.save(
                registration
        );
    }

    public List<Registration> getEventRegistrations(
            String email,
            Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event not found"
                        ));

        // Only the organizer who created the event
        // can view its attendees
        if (event.getOrganizer() == null ||
                !event.getOrganizer()
                        .getEmail()
                        .equals(email)) {

            throw new AccessDeniedException(
                    "You can only view attendees of your own events"
            );
        }

        return registrationRepository
                .findByEventIdAndStatusNot(
                        event.getId(),
                        "CANCELLED"
                );
    }
}