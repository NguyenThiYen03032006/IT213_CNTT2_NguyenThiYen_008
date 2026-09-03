package vn.rikkei.exam.clinicappointment.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.rikkei.exam.clinicappointment.model.ResourceInventory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ResourceInventoryRepository extends JpaRepository<ResourceInventory, Long> {
    List<ResourceInventory> findByResourceType_ResourceCodeAndAvailableDateBetween(String resourceCode, LocalDate startDate, LocalDate endDate);
    Optional<ResourceInventory> findByResourceType_ResourceCodeAndAvailableDate(String resourceCode, LocalDate availableDate);
    List<ResourceInventory> findByResourceType_ResourceCode(String resourceCode);
}
