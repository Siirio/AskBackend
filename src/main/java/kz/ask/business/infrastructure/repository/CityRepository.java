package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.business.domain.entity.City;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {

    Optional<City> findByNameIgnoreCase(String name);

    List<City> findByStatusOrderByNameAsc(RecordStatus status);
}
