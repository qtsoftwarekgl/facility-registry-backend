package com.frpr.controlller;

import com.frpr.mapper.FacilityRegistryMapper;
import com.frpr.model.*;
import com.frpr.model.Package;
import com.frpr.pojo.PackagePojo;
import com.frpr.repo.MetaDataRepository;
import com.frpr.repo.PackageRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/package")
public class PackageController {

    @Autowired
    PackageRepository packageRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MetaDataRepository metaDataRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    protected FacilityRegistryMapper facilityRegistryMapper;

    private String getLoggedInUser() {
        return ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
    }

    @PostMapping
    public ResponseEntity<CommonResponse> save(@RequestBody Package aPackage, HttpServletRequest request) {


        if (StringUtils.isBlank(aPackage.getName())) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please enter valid name"), HttpStatus.ACCEPTED);
        }

        if (StringUtils.isBlank(aPackage.getCategoryId())) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please select the category"), HttpStatus.ACCEPTED);
        }

        if (aPackage.getServices() == null || aPackage.getServices().isEmpty()) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please select at least one service"), HttpStatus.ACCEPTED);
        }

        if (aPackage.getStatus() == null) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please select valid status"), HttpStatus.ACCEPTED);
        }

        if(isNotValidRecord(aPackage)){
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Same category and name already exist"), HttpStatus.ACCEPTED);
        }

        aPackage.setCreatedBy(getLoggedInUser());
        aPackage.setChangedBy(getLoggedInUser());
        aPackage.setDateCreated(new Date());
        aPackage.setDateChanged(new Date());
        Package u = packageRepository.save(aPackage);

        auditService.saveAuditLog(getLoggedInUser(), Package.class.getSimpleName(), Action.ADD, aPackage, null, aPackage, request, null);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(u), HttpStatus.ACCEPTED);
    }


    private boolean isNotValidRecord(Package aPackage){
        Package existingRecord = packageRepository.findAllByNameAndCategoryId(aPackage.getName(), aPackage.getCategoryId());
        if(existingRecord == null)
            return false;
        return aPackage.get_id() == null || !aPackage.get_id().equals(existingRecord.get_id());
    }

    @PutMapping
    public ResponseEntity<CommonResponse> update(@RequestBody Package aPackage, HttpServletRequest request) {

        if (StringUtils.isBlank(aPackage.getName())) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please enter valid name"), HttpStatus.ACCEPTED);
        }

        if (StringUtils.isBlank(aPackage.getCategoryId())) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please select the category"), HttpStatus.ACCEPTED);
        }

        if (aPackage.getServices() == null || aPackage.getServices().isEmpty()) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please select at least one service"), HttpStatus.ACCEPTED);
        }

        if (aPackage.getStatus() == null) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Please select valid status"), HttpStatus.ACCEPTED);
        }

        if(isNotValidRecord(aPackage)){
            return new ResponseEntity<>(new CommonResponse("error", "server_error.Same category and name already exist"), HttpStatus.ACCEPTED);
        }

        Optional<Package> oldData = packageRepository.findById(aPackage.get_id());
        if (!oldData.isPresent()) {
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.no_data_with_id: " + aPackage.get_id()), HttpStatus.ACCEPTED);
        }

        aPackage.setCreatedBy(oldData.get().getCreatedBy());
        aPackage.setChangedBy(getLoggedInUser());
        aPackage.setDateCreated(oldData.get().getDateCreated());
        aPackage.setDateChanged(new Date());
        packageRepository.save(aPackage);

        auditService.saveAuditLog(getLoggedInUser(), Package.class.getSimpleName(), Action.UPDATE, aPackage, oldData.get(), aPackage, request, null);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(aPackage), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse> getOnce(@PathVariable String id) {
        Optional<Package> u = packageRepository.findById(id);

        return u.map(aPackage -> new ResponseEntity<>(new CommonResponse("ok", null).setData(facilityRegistryMapper.toPackagePojo(aPackage)), HttpStatus.ACCEPTED))
                .orElseGet(() -> new ResponseEntity<>(new CommonResponse("error", "Given package id is not found"), HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<CommonResponse> getAll(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int limit,
                                                 @RequestParam(required = false) String name,
                                                 @RequestParam(required = false) String categoryId,
                                                 @RequestParam(required = false) StatusEnum status) {

        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("dateCreated").descending());
        Query query = new Query().with(PageRequest.of(page - 1, limit));

        if (StringUtils.isNotBlank(name))
            query.addCriteria(Criteria.where("name").regex(name, "i"));

        if (status != null)
            query.addCriteria(Criteria.where("status").is(status.name()));

        if (StringUtils.isNotBlank(categoryId))
            query.addCriteria(Criteria.where("categoryId").is(categoryId));

        Page<Package> packagePage = PageableExecutionUtils.getPage(
                mongoTemplate.find(query, Package.class),
                pageRequest,
                () -> mongoTemplate.count(query.skip(0).limit(0), Package.class));
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null)
                .setData(setCategoryName(facilityRegistryMapper.toPackagePojoList(packagePage.getContent())))
                .setCount(packagePage.getTotalElements()), HttpStatus.ACCEPTED);
    }

    private List<PackagePojo> setCategoryName(List<PackagePojo> packages){
        return packages.stream().peek(f -> {
            if(f.getCategoryId()!= null){
                f.setCategoryName(metaDataRepository.findById(f.getCategoryId()).map(MetaData::getName).orElse(""));
            }
        }).collect(Collectors.toList());
    }

    @GetMapping("/activate/{id}/{status}")
    public ResponseEntity<CommonResponse> activeInActive(@PathVariable String id, @PathVariable StatusEnum status,  HttpServletRequest request) {
        Optional<Package> oldData = packageRepository.findById(id);
        return oldData.map(service -> {
                    service.setStatus(status);
                    auditService.saveAuditLog(getLoggedInUser(), Package.class.getSimpleName(), Action.UPDATE, id, oldData.orElse(null), oldData.orElse(null), request, null);
                    return new ResponseEntity<>(new CommonResponse("ok", null).setData(packageRepository.save(service)), HttpStatus.ACCEPTED);
                })
                .orElseGet(() -> new ResponseEntity<>(new CommonResponse("error", "Given package id is not found"), HttpStatus.NOT_FOUND));
    }

    @GetMapping("/delete/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable String id, HttpServletRequest request) {

        Optional<Package> oldData = packageRepository.findById(id);
        if (!oldData.isPresent()) {
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "Given package id is not found"), HttpStatus.ACCEPTED);
        }

        packageRepository.deleteById(id);
        auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.DELETE, id, oldData.orElse(null), null, request, null);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(oldData.get()), HttpStatus.ACCEPTED);

    }
}