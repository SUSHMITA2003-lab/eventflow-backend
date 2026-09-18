package eventflow_backend.controller;

import eventflow_backend.model.Registration;
import eventflow_backend.service.RegistrationService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(
            RegistrationService registrationService) {

        this.registrationService = registrationService;
    }

    @PostMapping("/events/{eventId}")
    public Registration registerForEvent(
            @PathVariable Long eventId,
            Authentication authentication) {

        String email = authentication.getName();

        return registrationService.registerForEvent(
                email,
                eventId
        );
    }

    @GetMapping("/my")
    public List<Registration> getMyRegistrations(
            Authentication authentication) {

        String email = authentication.getName();

        return registrationService.getMyRegistrations(
                email
        );
    }

    @PutMapping("/{registrationId}/cancel")
    public Registration cancelRegistration(
            @PathVariable Long registrationId,
            Authentication authentication) {

        String email = authentication.getName();

        return registrationService.cancelRegistration(
                email,
                registrationId
        );
    }

    @GetMapping("/event/{eventId}")
    public List<Registration> getEventRegistrations(
            @PathVariable Long eventId,
            Authentication authentication) {

        String email = authentication.getName();

        return registrationService.getEventRegistrations(
                email,
                eventId
        );
    }
}