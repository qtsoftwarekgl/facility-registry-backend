package com.frpr.service;

import com.frpr.model.Action;
import com.frpr.model.AuditLog;
import com.frpr.repo.AuditLogRepository;
import com.google.gson.Gson;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class AuditService {

    @Autowired
    AuditLogRepository auditLogRepository;

    @Async
    public void saveAuditLogForErrors(Exception ex, HttpServletRequest request) {
        String errorMessage =  ExceptionUtils.getMessage(ex);
        String query= request.getQueryString();
        AuditLog auditLog = new AuditLog("ERROR-TRACKER", Exception.class.getSimpleName(), Action.ERROR, errorMessage, ExceptionUtils.getStackTrace(ex),
                request.getRemoteAddr(), request.getMethod(), request.getServletPath(), query);
        auditLogRepository.save(auditLog);
    }


    @Async
    public void saveAuditLog(String username, String entity, Action action, Object payload, Object oldData, HttpServletRequest request) {
        String requestData = "";
        if (payload != null) {
            if (payload instanceof String) {
                requestData = (String) payload;
            } else {
                requestData = new Gson().toJson(payload);
            }
        }
        AuditLog auditLog = new AuditLog(username, entity, action, requestData, oldData == null ? "" : new Gson().toJson(oldData),
                request.getRemoteAddr(), request.getMethod(), request.getServletPath(), "");
        auditLogRepository.save(auditLog);
    }

    @Async
    public void saveAuditLog(String username, String entity, Action action, Object payload, Object oldData, Object newData, HttpServletRequest request) {
        String requestData = "";
        if (payload != null) {
            if (payload instanceof String) {
                requestData = (String) payload;
            } else {
                requestData = new Gson().toJson(payload);
            }
        }
        AuditLog auditLog = new AuditLog(username, entity, action, requestData, oldData == null ? "" : new Gson().toJson(oldData),
                request.getRemoteAddr(), request.getMethod(), request.getServletPath(), newData == null ? "" : new Gson().toJson(newData));
        auditLogRepository.save(auditLog);
    }

    @Async
    public void saveAuditLog(String username, String entity, Action action, Object payload, Object oldData, HttpServletRequest request, String facilityCode) {
        String requestData = "";
        if (payload != null) {
            if (payload instanceof String) {
                requestData = (String) payload;
            } else {
                requestData = new Gson().toJson(payload);
            }
        }
        AuditLog auditLog = new AuditLog(username, entity, action, requestData, oldData == null ? "" : new Gson().toJson(oldData),
                request.getRemoteAddr(), request.getMethod(), request.getServletPath(), facilityCode);
        auditLogRepository.save(auditLog);
    }

    @Async
    public void saveAuditLog(String username, String entity, Action action, Object payload, Object oldData, Object newData, HttpServletRequest request, String facilityCode) {
        String requestData = "";
        if (payload != null) {
            if (payload instanceof String) {
                requestData = (String) payload;
            } else {
                requestData = new Gson().toJson(payload);
            }
        }
        AuditLog auditLog = new AuditLog(username, entity, action, requestData, oldData == null ? "" : new Gson().toJson(oldData),
                request.getRemoteAddr(), request.getMethod(), request.getServletPath(), facilityCode, newData == null ? "" : new Gson().toJson(newData));
        auditLogRepository.save(auditLog);
    }

}
