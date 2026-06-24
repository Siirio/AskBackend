package kz.ask.business.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import kz.ask.business.domain.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {

    Optional<City> findByNameIgnoreCase(String name);
}
