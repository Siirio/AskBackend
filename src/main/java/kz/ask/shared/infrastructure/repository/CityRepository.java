package kz.ask.shared.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.shared.domain.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {

    Optional<City> findByNameIgnoreCase(String name);

    List<City> findAllByOrderByNameAsc();
}
