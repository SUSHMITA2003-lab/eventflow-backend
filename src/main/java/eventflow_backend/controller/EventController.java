package eventflow_backend.controller;

import eventflow_backend.model.Event;
import eventflow_backend.service.EventService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(
            EventService eventService) {

        this.eventService = eventService;
    }

    @PostMapping
    public Event createEvent(
            @Valid @RequestBody Event event,
            Authentication authentication) {

        String email = authentication.getName();

        return eventService.createEvent(
                email,
                event
        );
    }

    @GetMapping
    public List<Event> getAllEvents() {

        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public Event getEventById(
            @PathVariable Long id) {

        return eventService.getEventById(id);
    }

    @PutMapping("/{id}")
    public Event updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody Event event,
            Authentication authentication) {

        String email = authentication.getName();

        return eventService.updateEvent(
                email,
                id,
                event
        );
    }

    @DeleteMapping("/{id}")
    public String deleteEvent(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        eventService.deleteEvent(
                email,
                id
        );

        return "Event deleted successfully";
    }
}