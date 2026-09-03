package vn.rikkei.exam.clinicappointment.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.rikkei.exam.clinicappointment.model.AppUser;
public interface AppUserRepository extends JpaRepository<AppUser, String> { }
