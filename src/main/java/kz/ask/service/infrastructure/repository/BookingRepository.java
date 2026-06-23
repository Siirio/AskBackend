package kz.ask.service.infrastructure.repository;

import kz.ask.service.domain.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByBranch_Id(UUID branchId);
    Optional<Booking> findByIdAndBranch_Id(UUID id, UUID branchId);
}
