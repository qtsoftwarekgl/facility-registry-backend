package com.frpr.controlller;

import com.frpr.repo.*;
import com.frpr.response.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class LocationController {

    @Autowired
    CellsRepository cellsRepository;

    @Autowired
    DistrictsRepository districtsRepository;

    @Autowired
    ProvincesRepository provincesRepository;

    @Autowired
    SectorsRepository sectorsRepository;

    @Autowired
    VillagesRepository villagesRepository;


    @GetMapping("/provinces/activeList")
    public ResponseEntity<CommonResponse> getProvinces() {
        //     user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(provincesRepository.findAllByStatus("ACTIVE")), HttpStatus.ACCEPTED);
    }

    @GetMapping("/districts/activeList")
    public ResponseEntity<CommonResponse> getDistricts(@RequestParam String provinceId) {
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(districtsRepository.findAllByProvinceIdAndStatus(provinceId, "ACTIVE")), HttpStatus.ACCEPTED);
    }

    @GetMapping("/sectors/activeList")
    public ResponseEntity<CommonResponse> getSectors(@RequestParam String districtId) {
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(sectorsRepository.findAllByDistrictIdAndStatus(districtId, "ACTIVE")), HttpStatus.ACCEPTED);
    }

    @GetMapping("/cells/activeList")
    public ResponseEntity<CommonResponse> getCells(@RequestParam String sectorId) {
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(cellsRepository.findAllBySectorIdAndStatus(sectorId, "ACTIVE")), HttpStatus.ACCEPTED);
    }

    @GetMapping("/villages/activeList")
    public ResponseEntity<CommonResponse> getVillages(@RequestParam String cellId) {
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(villagesRepository.findAllByCellIdAndStatus(cellId, "ACTIVE")), HttpStatus.ACCEPTED);
    }

}
