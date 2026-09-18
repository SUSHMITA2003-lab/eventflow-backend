package eventflow_backend.repository;

import eventflow_backend.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository
        extends JpaRepository<Registration, Long> {

    boolean existsByUserIdAndEventId(
            Long userId,
            Long eventId
    );

    Optional<Registration> findByUserIdAndEventId(
            Long userId,
            Long eventId
    );

    List<Registration> findByUserId(Long userId);

    Optional<Registration> findByIdAndUserId(
            Long id,
            Long userId
    );

    List<Registration> findByEventId(Long eventId);

    List<Registration> findByEventIdAndStatusNot(
            Long eventId,
            String status
    );
}