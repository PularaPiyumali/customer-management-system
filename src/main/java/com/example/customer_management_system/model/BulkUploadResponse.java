package com.example.customer_management_system.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BulkUploadResponse {
    private String jobId;
    private String status;
    private String message;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer successRecords;
    private Integer failedRecords;

    public BulkUploadResponse(String jobId, String status, String message) {
        this.jobId = jobId;
        this.status = status;
        this.message = message;
    }
}
