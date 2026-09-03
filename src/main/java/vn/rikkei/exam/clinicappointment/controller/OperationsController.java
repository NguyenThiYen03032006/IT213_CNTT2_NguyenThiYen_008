package vn.rikkei.exam.clinicappointment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.rikkei.exam.clinicappointment.dto.ApproveRequestDto;
import vn.rikkei.exam.clinicappointment.dto.ApproveResponseDto;
import vn.rikkei.exam.clinicappointment.service.ReservationService;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationsController {

    private final ReservationService reservationService;

    @PostMapping("/approve-request")
    public ResponseEntity<ApproveResponseDto> approveRequest(@Valid @RequestBody ApproveRequestDto request) {
        ApproveResponseDto response = reservationService.processApprovalRequest(request);
        return ResponseEntity.ok(response);
    }
}
