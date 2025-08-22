package com.example.customer_management_system.application;

import com.example.customer_management_system.model.BulkUploadResponse;
import com.example.customer_management_system.domain.entities.BulkProcessing;
import com.example.customer_management_system.domain.entities.Customer;
import com.example.customer_management_system.domain.repository.BulkProcessingRepository;
import com.example.customer_management_system.domain.repository.CustomerRepository;
import com.example.customer_management_system.utils.MessageConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkCustomerService {

  private final CustomerRepository customerRepository;
  private final BulkProcessingRepository bulkProcessingJobRepository;

  @Value("${bulk.processing.batch-size:1000}")
  private int batchSize;

  /** Handles validation + job creation + async processing kickoff. */
  public BulkUploadResponse handleBulkUpload(MultipartFile file) {

    if (file == null || file.isEmpty()) {
      BulkUploadResponse response = new BulkUploadResponse();
      response.setStatus(MessageConstant.FAILED);
      response.setMessage(MessageConstant.EMPTY_FILE_MESSAGE);
      return response;
    }

    String jobId = UUID.randomUUID().toString();
    try {
      // Validate file
      validateFile(file);

      // Create job record
      BulkProcessing job = new BulkProcessing(jobId);
      bulkProcessingJobRepository.save(job);

      // Start async processing
      processBulkUploadAsync(file, jobId);

      return new BulkUploadResponse(
          jobId, MessageConstant.PROCESSING, MessageConstant.BULK_UPLOAD_SUCCESS_MESSAGE);

    } catch (Exception e) {
      return new BulkUploadResponse(
          jobId,
          MessageConstant.FAILED,
          MessageConstant.BULK_UPLOAD_FAILED_MESSAGE + e.getMessage());
    }
  }

  /**
   * Process bulk upload async completable future.
   *
   * @param file the file
   * @param jobId the job id
   * @return the completable future
   */
@Async
  @Transactional
  public CompletableFuture<Void> processBulkUploadAsync(MultipartFile file, String jobId) {
    BulkProcessing job = bulkProcessingJobRepository.findByJobId(jobId).orElse(null);
    if (job == null) {
      return CompletableFuture.completedFuture(null);
    }

    try {
      List<Customer> customers = parseExcelFile(file);
      job.setTotalRecords(customers.size());
      bulkProcessingJobRepository.save(job);

      int successCount = 0;
      int failedCount = 0;
      List<Customer> batch = new ArrayList<>();

      for (int i = 0; i < customers.size(); i++) {
        Customer customer = customers.get(i);

        try {
          // Check if NIC already exists
          if (!customerRepository.existsByNicNumber(customer.getNicNumber())) {
            batch.add(customer);
            successCount++;
          } else {
            failedCount++; // Skip duplicates
          }

          // Process batch when it reaches batch size or at the end
          if (batch.size() == batchSize || i == customers.size() - 1) {
            if (!batch.isEmpty()) {
              customerRepository.saveAll(batch);
              batch.clear();
            }
          }

          // Update job progress
          job.setProcessedRecords(i + 1);
          job.setSuccessRecords(successCount);
          job.setFailedRecords(failedCount);

          if ((i + 1) % 100 == 0) { // Update DB every 100 records
            bulkProcessingJobRepository.save(job);
          }

        } catch (Exception e) {
          failedCount++;
          job.setFailedRecords(failedCount);
        }
      }

      job.setStatus(BulkProcessing.JobStatus.COMPLETED);
      job.setSuccessRecords(successCount);
      job.setFailedRecords(failedCount);
      bulkProcessingJobRepository.save(job);

    } catch (Exception e) {
      job.setStatus(BulkProcessing.JobStatus.FAILED);
      job.setErrorMessage(e.getMessage());
      bulkProcessingJobRepository.save(job);
    }

    return CompletableFuture.completedFuture(null);
  }

  /**
   * Gets bulk upload status.
   *
   * @param jobId the job id
   * @return the bulk upload status
   */
public BulkUploadResponse getBulkUploadStatus(String jobId) {
    BulkProcessing job =
        bulkProcessingJobRepository
            .findByJobId(jobId)
            .orElseThrow(() -> new RuntimeException(MessageConstant.JOB_NOT_FOUND + jobId));

    BulkUploadResponse response = new BulkUploadResponse();
    response.setJobId(job.getJobId());
    response.setStatus(job.getStatus().toString());
    response.setTotalRecords(job.getTotalRecords());
    response.setProcessedRecords(job.getProcessedRecords());
    response.setSuccessRecords(job.getSuccessRecords());
    response.setFailedRecords(job.getFailedRecords());

    if (job.getStatus() == BulkProcessing.JobStatus.FAILED) {
      response.setMessage(MessageConstant.PROCESSING_FAILED + job.getErrorMessage());
    } else if (job.getStatus() == BulkProcessing.JobStatus.COMPLETED) {
      response.setMessage(MessageConstant.PROCESSING_COMPLETED_SUCCESSFULLY);
    } else {
      response.setMessage(MessageConstant.PROCESSING_IN_PROGRESS);
    }

    return response;
  }

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException(MessageConstant.EMPTY_FILE_MESSAGE);
    }

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null
        || (!originalFilename.toLowerCase().endsWith(".xlsx")
            && !originalFilename.toLowerCase().endsWith(".xls"))) {
      throw new IllegalArgumentException(MessageConstant.FILE_FORMAT_MESSAGE);
    }

    if (file.getSize() > 100 * 1024 * 1024) { // 100MB limit
      throw new IllegalArgumentException(MessageConstant.FILE_SIZE_EXCEEDED_MESSAGE);
    }
  }

  private List<Customer> parseExcelFile(MultipartFile file) throws IOException {
    List<Customer> customers = new ArrayList<>();

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);

      // Skip header row
      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;

        try {
          Customer customer = parseRowToCustomer(row);
          if (customer != null) {
            customers.add(customer);
          }
        } catch (Exception e) {
          // Log error but continue processing other rows
          log.info(MessageConstant.ERROR_MESSAGE, i, e.getMessage());
        }
      }
    }

    return customers;
  }

  private Customer parseRowToCustomer(Row row) {
    try {
      String name = getCellValueAsString(row.getCell(0));
      String dobString = getCellValueAsString(row.getCell(1));
      String nicNumber = getCellValueAsString(row.getCell(2));

      // Validate mandatory fields
      if (name == null
          || name.trim().isEmpty()
          || dobString == null
          || dobString.trim().isEmpty()
          || nicNumber == null
          || nicNumber.trim().isEmpty()) {
        return null; // Skip invalid rows
      }

      LocalDate dateOfBirth = parseDate(dobString);

      Customer customer = new Customer();
      customer.setName(name.trim());
      customer.setDateOfBirth(dateOfBirth);
      customer.setNicNumber(nicNumber.trim());

      return customer;

    } catch (Exception e) {
      throw new RuntimeException(MessageConstant.FAILED_TO_PARSE_ROW + e.getMessage(), e);
    }
  }

  private String getCellValueAsString(Cell cell) {
    if (cell == null) return null;

    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          return cell.getDateCellValue().toString();
        } else {
          return String.valueOf((long) cell.getNumericCellValue());
        }
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      case FORMULA:
        return cell.getCellFormula();
      default:
        return null;
    }
  }

  private LocalDate parseDate(String dateString) {
    try {
      // Try different date formats
      String[] formats = {"yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MM-yyyy"};

      for (String format : formats) {
        try {
          return LocalDate.parse(dateString, java.time.format.DateTimeFormatter.ofPattern(format));
        } catch (DateTimeParseException e) {
          // Try next format
        }
      }

      throw new RuntimeException(MessageConstant.UNABLE_TO_PARSE_ROW + dateString);

    } catch (Exception e) {
      throw new RuntimeException(MessageConstant.INVALID_DATE_FORMAT + dateString, e);
    }
  }
}
