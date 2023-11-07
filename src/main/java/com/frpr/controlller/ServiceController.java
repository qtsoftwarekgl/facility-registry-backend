package com.frpr.controlller;

import com.frpr.mapper.FacilityRegistryMapper;
import com.frpr.model.*;
import com.frpr.repo.ServiceRepository;
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
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/v1/service")
public class ServiceController {

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    private AuditService auditService;

    @Autowired
    protected FacilityRegistryMapper facilityRegistryMapper;

    private String getLoggedInUser() {
        return ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
    }

    @PostMapping
    public ResponseEntity<CommonResponse> save(@RequestBody Service service, HttpServletRequest request) {

        if (StringUtils.isBlank(service.getName())) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please enter valid name"), HttpStatus.ACCEPTED);
        }

        if (service.getStatus() == null) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please select valid status"), HttpStatus.ACCEPTED);
        }

        if(isNotValidRecord(service)){
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Same category and name already exist"), HttpStatus.ACCEPTED);
        }

        service.setCreatedBy(getLoggedInUser());
        service.setChangedBy(getLoggedInUser());
        service.setDateCreated(new Date());
        service.setDateChanged(new Date());
        Service u = serviceRepository.save(service);

        auditService.saveAuditLog(getLoggedInUser(), Service.class.getSimpleName(), Action.ADD, service, null, service, request, null);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(u), HttpStatus.ACCEPTED);
    }

    private boolean isNotValidRecord(Service service){
        Service existingRecord = serviceRepository.findAllByName(service.getName());
        if(existingRecord == null)
            return false;
        return service.get_id() == null || !service.get_id().equals(existingRecord.get_id());
    }

    @PutMapping
    public ResponseEntity<CommonResponse> update(@RequestBody Service service, HttpServletRequest request) {

        if (StringUtils.isBlank(service.getName())) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please enter valid name"), HttpStatus.ACCEPTED);
        }

        if (service.getStatus() == null) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please select valid status"), HttpStatus.ACCEPTED);
        }

        if(isNotValidRecord(service)){
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Same category and name already exist"), HttpStatus.ACCEPTED);
        }

        Optional<Service> oldData = serviceRepository.findById(service.get_id());
        if (!oldData.isPresent()) {
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.no_data_with_id: " + service.get_id()), HttpStatus.ACCEPTED);
        }

        service.setCreatedBy(oldData.get().getCreatedBy());
        service.setChangedBy(getLoggedInUser());
        service.setDateCreated(oldData.get().getDateCreated());
        service.setDateChanged(new Date());
        Service u = serviceRepository.save(service);

        auditService.saveAuditLog(getLoggedInUser(), Service.class.getSimpleName(), Action.UPDATE, service, oldData.get(), service, request, null);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(u), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse> getOnce(@PathVariable String id) {
        Optional<Service> u = serviceRepository.findById(id);

        return u.map(service -> new ResponseEntity<>(new CommonResponse("ok", null).setData(facilityRegistryMapper.toServicePojo(service)), HttpStatus.ACCEPTED))
                .orElseGet(() -> new ResponseEntity<>(new CommonResponse("error", "Given Service id is not found"), HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<CommonResponse> getAll(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int limit,
                                                 @RequestParam(required = false) String name,
                                                 @RequestParam(required = false) StatusEnum status) {

        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("dateCreated").descending());
        Query query = new Query().with(PageRequest.of(page - 1, limit));

        if (StringUtils.isNotBlank(name))
            query.addCriteria(Criteria.where("name").regex(name, "i"));

        if (status != null)
            query.addCriteria(Criteria.where("status").is(status.name()));

        Page<Service> services = PageableExecutionUtils.getPage(
                mongoTemplate.find(query, Service.class),
                pageRequest,
                () -> mongoTemplate.count(query.skip(0).limit(0), Service.class));

        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(facilityRegistryMapper.toServicePojoList(services.getContent())).setCount(services.getTotalElements()), HttpStatus.ACCEPTED);
    }

    @GetMapping("/activate/{id}/{status}")
    public ResponseEntity<CommonResponse> activeInActive(@PathVariable String id, @PathVariable StatusEnum status) {
        Optional<Service> u = serviceRepository.findById(id);
        return u.map(service -> {
                    service.setStatus(status);
                    return new ResponseEntity<>(new CommonResponse("ok", null).setData(serviceRepository.save(service)), HttpStatus.ACCEPTED);
                })
                .orElseGet(() -> new ResponseEntity<>(new CommonResponse("error", "Given Service id is not found"), HttpStatus.NOT_FOUND));
    }

    @GetMapping("/delete/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable String id) {
        Optional<Service> u = serviceRepository.findById(id);
        return u.map(service -> {
                    service.setStatus(StatusEnum.DELETED);
                    return new ResponseEntity<>(new CommonResponse("ok", null).setData(serviceRepository.save(service)), HttpStatus.ACCEPTED);
                })
                .orElseGet(() -> new ResponseEntity<>(new CommonResponse("error", "Given Service id is not found"), HttpStatus.NOT_FOUND));

    }
}