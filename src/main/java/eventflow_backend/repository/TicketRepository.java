package eventflow_backend.repository;

import eventflow_backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByQrToken(String qrToken);

    Optional<Ticket> findByRegistrationId(Long registrationId);

    List<Ticket> findByRegistrationUserId(Long userId);
}