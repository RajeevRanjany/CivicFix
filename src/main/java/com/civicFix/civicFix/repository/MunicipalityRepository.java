package com.civicFix.civicFix.repository;

import com.civicFix.civicFix.entity.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MunicipalityRepository extends JpaRepository<Municipality, Long> {

    Optional<Municipality> findByMinLatitudeLessThanEqualAndMaxLatitudeGreaterThanEqualAndMinLongitudeLessThanEqualAndMaxLongitudeGreaterThanEqual(
            double lat1, double lat2, double lon1, double lon2
    );
}