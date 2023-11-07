package com.frpr.controlller;

import com.frpr.model.*;
import com.frpr.repo.MetaDataRepository;
import com.frpr.response.CommonResponse;
import com.frpr.service.AuditService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/meta-data")
public class MetaDataController {

    @Autowired
    MetaDataRepository metaDataRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    private AuditService auditService;

    private String getLoggedInUser() {
        try {
            return ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        } catch (Exception ignored) {

        }
        return "";
    }

    @PostMapping
    public ResponseEntity<CommonResponse> save(@RequestBody MetaData data, HttpServletRequest request) {

        if (StringUtils.isBlank(data.getName())) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please enter valid name"), HttpStatus.ACCEPTED);
        }

        if (data.getType() == null) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please select valid type"), HttpStatus.ACCEPTED);
        }

        if (data.getStatus() == null) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please select valid status"), HttpStatus.ACCEPTED);
        }

        if (isAlreadyExist(null, data.getName(), data.getType())) {
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.Metadata_exist_with_name_and_type: " + data.getName()), HttpStatus.ACCEPTED);
        }
        auditService.saveAuditLog(getLoggedInUser(), MetaData.class.getSimpleName(), Action.ADD, data, null, null, request);
        MetaData u = metaDataRepository.save(data);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(u), HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse> update(@PathVariable String id, @RequestBody MetaData data, HttpServletRequest request) {


        try {
            if (StringUtils.isBlank(data.getName())) {
                return new ResponseEntity<>(new CommonResponse("error", "server_error.Please enter valid name"), HttpStatus.ACCEPTED);
            }

            if (data.getType() == null) {
                return new ResponseEntity<>(new CommonResponse("error", "server_error.Please select valid type"), HttpStatus.ACCEPTED);
            }

            if (data.getStatus() == null) {
                return new ResponseEntity<>(new CommonResponse("error", "server_error.Please select valid status"), HttpStatus.ACCEPTED);
            }

            if (isAlreadyExist(id, data.getName(), data.getType())) {
                return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.Metadata_exist_with_name_and_type: " + data.getName()), HttpStatus.ACCEPTED);
            }
            data.set_id(id);
            Optional<MetaData> oldData = metaDataRepository.findById(id);
            if (!oldData.isPresent()) {
                return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.no_data_with_id: " + data.getName()), HttpStatus.ACCEPTED);
            }
            data.setCreatedAt(oldData.get().getCreatedAt());
            //Optional<MetaData> auditData = metaDataRepository.findById(id);

            auditService.saveAuditLog(getLoggedInUser(), MetaData.class.getSimpleName(), Action.UPDATE, data, oldData.orElse(null), data, request);
            return new ResponseEntity<CommonResponse>(new CommonResponse("ok", "server_success.updated_successfully").setData(metaDataRepository.save(data)), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.update_error"), HttpStatus.ACCEPTED);
        }

    }


    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse> getOnce(@PathVariable String id, HttpServletRequest request) {
        Optional<MetaData> oldData = metaDataRepository.findById(id);
        return oldData.map(data -> new ResponseEntity<>(new CommonResponse("ok", null).setData(data), HttpStatus.ACCEPTED))
                .orElseGet(() -> new ResponseEntity<>(new CommonResponse("error", "server_error.no_data_with_id: " + id), HttpStatus.ACCEPTED));


    }

    @GetMapping
    public ResponseEntity<CommonResponse> getAll(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int limit,
                                                 @RequestParam(value = "name", required = false) String name,
                                                 @RequestParam(value = "type", required = false) MetaDataTypeEnum type,
                                                 @RequestParam(value = "status", required = false) StatusEnum status,
                                                 @RequestParam(value = "skipPagination", required = false) Boolean skipPagination, HttpServletRequest request) {
        auditService.saveAuditLog(getLoggedInUser(), MetaData.class.getSimpleName(), Action.GET, request.getQueryString(), null, request, "LIST");
        Query query = new Query();
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        if (skipPagination != null && skipPagination) {
            query = new Query();
        } else {
            query = new Query().with(PageRequest.of(page - 1, limit));
        }

        if (name != null && !name.isEmpty())
            query.addCriteria(Criteria.where("name").regex(name, "i"));

        if (type != null)
            query.addCriteria(Criteria.where("type").is(type));

        if (status != null)
            query.addCriteria(Criteria.where("status").is(status.name()));

        Query finalQuery = query;
        Page<MetaData> metaDataPage = PageableExecutionUtils.getPage(
                mongoTemplate.find(query, MetaData.class),
                pageRequest,
                () -> mongoTemplate.count(finalQuery.skip(0).limit(0), AuditLog.class));
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(metaDataPage.get()).setCount(metaDataPage.getTotalElements()), HttpStatus.ACCEPTED);
    }

    @GetMapping("/activate/{id}/{status}")
    public ResponseEntity<CommonResponse> activeInActive(@PathVariable String id, @PathVariable StatusEnum status, HttpServletRequest request) {

        Optional<MetaData> oldData = metaDataRepository.findById(id);
        if (!oldData.isPresent()) {
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.no_data_with_id: " + id), HttpStatus.ACCEPTED);
        }
        oldData.get().setStatus(status);
        auditService.saveAuditLog(getLoggedInUser(), MetaData.class.getSimpleName(), Action.UPDATE, id, oldData.orElse(null), oldData.orElse(null), request, null);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(metaDataRepository.save(oldData.get())), HttpStatus.ACCEPTED);
    }

    @GetMapping("/delete/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable String id, HttpServletRequest request) {
        Optional<MetaData> oldData = metaDataRepository.findById(id);
        if (!oldData.isPresent()) {
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.no_data_with_id: " + id), HttpStatus.ACCEPTED);
        }
        metaDataRepository.deleteById(id);
        auditService.saveAuditLog(getLoggedInUser(), MetaData.class.getSimpleName(), Action.DELETE, id, oldData.orElse(null), null, request, null);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(oldData.get()), HttpStatus.ACCEPTED);
    }


    private boolean isAlreadyExist(String Id, String name, MetaDataTypeEnum type) {
        List<MetaData> list = metaDataRepository.findAllByNameAndType(name, type);
        return isSameData(list, Id);
    }

    private boolean isSameData(List<MetaData> list, String exstingId) {

        if (list.isEmpty())
            return false;


        if (list.size() > 1) return true;

        MetaData registry = list.get(0);

        if (exstingId != null) {
            return !(registry.get_id().equalsIgnoreCase(exstingId));
        } else {
            return true;
        }
    }
}
