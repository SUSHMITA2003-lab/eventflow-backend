package eventflow_backend.controller;

import eventflow_backend.dto.QrVerificationRequest;
import eventflow_backend.model.Ticket;
import eventflow_backend.service.TicketService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(
            TicketService ticketService) {

        this.ticketService = ticketService;
    }

    @PostMapping("/generate/{registrationId}")
    public Ticket generateTicket(
            @PathVariable Long registrationId,
            Authentication authentication) {

        String email = authentication.getName();

        return ticketService.generateTicket(
                email,
                registrationId
        );
    }

    @GetMapping(
            value = "/{ticketId}/qr",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> getTicketQrCode(
            @PathVariable Long ticketId,
            Authentication authentication) throws Exception {

        String email = authentication.getName();

        byte[] qrImage =
                ticketService.generateTicketQrCode(
                        email,
                        ticketId
                );

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

    @PostMapping("/verify")
    public Ticket verifyTicket(
            @RequestBody QrVerificationRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        return ticketService.verifyQrToken(
                email,
                request.getQrToken()
        );
    }

    @GetMapping("/registration/{registrationId}")
    public Ticket getTicketByRegistration(
            @PathVariable Long registrationId,
            Authentication authentication) {

        String email = authentication.getName();

        return ticketService.getTicketByRegistration(
                email,
                registrationId
        );
    }

    @PostMapping("/check-in")
    public Ticket checkInTicket(
            @RequestBody QrVerificationRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        return ticketService.checkInTicket(
                email,
                request.getQrToken()
        );
    }

    @GetMapping("/my")
    public List<Ticket> getMyTickets(
            Authentication authentication) {

        String email = authentication.getName();

        return ticketService.getMyTickets(email);
    }
}