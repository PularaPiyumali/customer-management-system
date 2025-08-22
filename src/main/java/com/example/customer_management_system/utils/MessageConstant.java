package com.example.customer_management_system.utils;

public class MessageConstant {

  public static String FAILED = "FAILED";
  public static String EMPTY_FILE_MESSAGE = "File is required and cannot be empty.";
  public static String PROCESSING = "PROCESSING";
  public static String BULK_UPLOAD_SUCCESS_MESSAGE = "Bulk upload started successfully. Use jobId to check status.";
  public static String BULK_UPLOAD_FAILED_MESSAGE = "Failed to start bulk upload.";
  public static String JOB_NOT_FOUND = "Job not found with id: ";
  public static String PROCESSING_FAILED = "Processing failed: " ;
  public static String PROCESSING_COMPLETED_SUCCESSFULLY = "Processing completed successfully";
  public static String PROCESSING_IN_PROGRESS = "Processing in progress...";
  public static String FILE_FORMAT_MESSAGE = "File must be an Excel file (.xlsx or .xls)";
  public static String FILE_SIZE_EXCEEDED_MESSAGE = "File size exceeds 100MB limit";
  public static String ERROR_MESSAGE = "Error processing row {}: {}";
  public static String FAILED_TO_PARSE_ROW = "Failed to parse row: ";
  public static String UNABLE_TO_PARSE_ROW = "Unable to parse date: ";
  public static String INVALID_DATE_FORMAT = "Invalid date format: ";
  public static String CITY_NOT_FOUND = "City not found";
  public static String CUSTOMER_CREATION_SUCCESS_RESPONSE = "Customer created with NIC {}";
  public static String CUSTOMER_NOT_FOUND = "Customer not found with id: ";
  public static String DESC = "desc";
  public static String CUSTOMER_WITH_NIC = "Customer with NIC " ;
  public static String ALREADY_EXISTS = " already exists";
  public static String DUPLICATE_NIC_MESSAGE = "Duplicate NICs found within family members";
  public static String DUPLICATE_NIC_EXCEPTION_MESSAGE = "Family member cannot have the same NIC as the parent customer: ";
  public static String FAMILY_MEMBER_WITH_NIC = "Family member with NIC " ;
}
