package vn.rikkei.exam.clinicappointment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IngestResponse {
    private final int chunksIngested;
    private final String message;
}