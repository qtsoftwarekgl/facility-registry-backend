package com.frpr.config;

import com.frpr.response.CommonResponse;
import com.frpr.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@RequiredArgsConstructor
public class Handler {

    private final AuditService auditService;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse> handle(Exception ex,
                                                 HttpServletRequest request) {
        auditService.saveAuditLogForErrors(ex, request);
        return new ResponseEntity<>(new CommonResponse("error", "Something went wrong!! Please contact your administrator").setData(ex.getMessage()), HttpStatus.ACCEPTED);
    }
}