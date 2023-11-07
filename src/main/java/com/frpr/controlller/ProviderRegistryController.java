package com.frpr.controlller;

import com.frpr.model.ProviderRegistry;
import com.frpr.repo.FacilityRepository;
import com.frpr.repo.ProviderRepository;
import com.frpr.response.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

//@RestController
@RequestMapping("/v1/provider-registry")
public class ProviderRegistryController {

    @Autowired
    ProviderRepository repository;

    @Autowired
    FacilityRepository facilityRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping
    public ResponseEntity<CommonResponse> save(@RequestBody ProviderRegistry user) {
        //     user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        ProviderRegistry u = repository.save(user);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(u), HttpStatus.ACCEPTED);
    }

    @PutMapping
    public ResponseEntity<CommonResponse> update(@RequestBody ProviderRegistry user) {
        ProviderRegistry u = repository.save(user);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(u), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse> getOnce(@PathVariable String id) {
        Optional<ProviderRegistry> u = repository.findById(id);
        u.get().setFacilities(facilityRepository.findAllById(u.get().getFacilityId()));
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(u.get()), HttpStatus.ACCEPTED);
    }

    @GetMapping
    public ResponseEntity<CommonResponse> getAll(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int limit) {
        Page<ProviderRegistry> users = repository.findAll(PageRequest.of(page - 1, limit, Sort.by("surName")));
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(users.getContent()).setCount(users.getTotalElements()), HttpStatus.ACCEPTED);
    }

    @GetMapping("/activate/{id}/{status}")
    public ResponseEntity<CommonResponse> activeInActive(@PathVariable String id, @PathVariable String status) {
        Optional<ProviderRegistry> u = repository.findById(id);
        u.get().setStatus(status);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(repository.save(u.get())), HttpStatus.ACCEPTED);
    }

    @GetMapping("/delete/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable String id) {
        Optional<ProviderRegistry> u = repository.findById(id);
        u.get().setStatus("DELETED");
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(repository.save(u.get())), HttpStatus.ACCEPTED);
    }
}
