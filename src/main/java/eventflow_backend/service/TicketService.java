package eventflow_backend.service;

import eventflow_backend.exception.RegistrationException;
import eventflow_backend.exception.ResourceNotFoundException;
import eventflow_backend.model.Registration;
import eventflow_backend.model.Ticket;
import eventflow_backend.model.User;
import eventflow_backend.repository.RegistrationRepository;
import eventflow_backend.repository.TicketRepository;
import eventflow_backend.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;

    public TicketService(
            TicketRepository ticketRepository,
            RegistrationRepository registrationRepository,
            UserRepository userRepository,
            QrCodeService qrCodeService) {

        this.ticketRepository = ticketRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.qrCodeService = qrCodeService;
    }

    @Transactional
    public Ticket generateTicket(
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

        if (!"CONFIRMED".equals(
                registration.getStatus())) {

            throw new RegistrationException(
                    "Ticket can only be generated for a confirmed registration"
            );
        }

        if (ticketRepository
                .findByRegistrationId(registrationId)
                .isPresent()) {

            throw new RegistrationException(
                    "Ticket already generated for this registration"
            );
        }

        Ticket ticket = new Ticket();

        ticket.setRegistration(registration);

        ticket.setTicketNumber(
                "EVF-" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase()
        );

        ticket.setQrToken(
                UUID.randomUUID().toString()
        );

        ticket.setStatus(
                "VALID"
        );

        ticket.setIssuedAt(
                LocalDateTime.now()
        );

        return ticketRepository.save(ticket);
    }

    public byte[] generateTicketQrCode(
            String email,
            Long ticketId) throws Exception {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found"
                        ));

        String ticketOwnerEmail =
                ticket.getRegistration()
                        .getUser()
                        .getEmail();

        if (!ticketOwnerEmail.equals(email)) {

            throw new ResourceNotFoundException(
                    "Ticket not found"
            );
        }

        if (!"VALID".equals(
                ticket.getStatus())) {

            throw new RegistrationException(
                    "QR code is not available for this ticket"
            );
        }

        return qrCodeService.generateQrCodeImage(
                ticket.getQrToken(),
                300,
                300
        );
    }

    public Ticket verifyQrToken(
            String email,
            String qrToken) {

        Ticket ticket = ticketRepository
                .findByQrToken(qrToken)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invalid QR ticket"
                        ));

        // Only organizer who owns the event can verify this ticket
        if (ticket.getRegistration()
                .getEvent()
                .getOrganizer() == null ||
                !ticket.getRegistration()
                        .getEvent()
                        .getOrganizer()
                        .getEmail()
                        .equals(email)) {

            throw new AccessDeniedException(
                    "You can only verify tickets for your own events"
            );
        }

        if (!"VALID".equals(
                ticket.getStatus())) {

            throw new RegistrationException(
                    "Ticket is not valid"
            );
        }

        if (!"CONFIRMED".equals(
                ticket.getRegistration()
                        .getStatus())) {

            throw new RegistrationException(
                    "Registration is not confirmed"
            );
        }

        return ticket;
    }

    public Ticket getTicketByRegistration(
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

        return ticketRepository
                .findByRegistrationId(
                        registration.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found"
                        ));
    }

    @Transactional
    public Ticket checkInTicket(
            String email,
            String qrToken) {

        Ticket ticket = ticketRepository
                .findByQrToken(qrToken)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invalid QR ticket"
                        ));

        // Only organizer who owns the event can check in this ticket
        if (ticket.getRegistration()
                .getEvent()
                .getOrganizer() == null ||
                !ticket.getRegistration()
                        .getEvent()
                        .getOrganizer()
                        .getEmail()
                        .equals(email)) {

            throw new AccessDeniedException(
                    "You can only check in attendees for your own events"
            );
        }

        if ("USED".equals(
                ticket.getStatus())) {

            throw new RegistrationException(
                    "Ticket has already been used"
            );
        }

        if (!"VALID".equals(
                ticket.getStatus())) {

            throw new RegistrationException(
                    "Ticket is not valid"
            );
        }

        Registration registration =
                ticket.getRegistration();

        if (!"CONFIRMED".equals(
                registration.getStatus())) {

            throw new RegistrationException(
                    "Registration is not confirmed"
            );
        }

        ticket.setStatus(
                "USED"
        );

        ticket.setCheckedInAt(
                LocalDateTime.now()
        );

        registration.setStatus(
                "CHECKED_IN"
        );

        registrationRepository.save(
                registration
        );

        return ticketRepository.save(
                ticket
        );
    }

    public List<Ticket> getMyTickets(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        return ticketRepository
                .findByRegistrationUserId(
                        user.getId()
                );
    }
}