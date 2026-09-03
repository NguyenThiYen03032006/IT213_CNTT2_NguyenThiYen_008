package vn.rikkei.exam.clinicappointment.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.rikkei.exam.clinicappointment.model.ResourceType;
import java.util.Optional;

public interface ResourceTypeRepository extends JpaRepository<ResourceType, String> {
    Optional<ResourceType> findByResourceCode(String resourceCode);
}
