package vn.rikkei.exam.clinicappointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.rikkei.exam.clinicappointment.model.ReservationRequest;
import vn.rikkei.exam.clinicappointment.model.ResourceType;

import java.time.LocalDate;
import java.util.List;

import vn.rikkei.exam.clinicappointment.model.ReservationStatus;

public interface ReservationRequestRepository extends JpaRepository<ReservationRequest, String> {
    List<ReservationRequest> findByStatus(ReservationStatus status);
    List<ReservationRequest> findByRequester_UserId(String userId);
}
