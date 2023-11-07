package com.frpr.controlller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frpr.mapper.FacilityRegistryMapper;
import com.frpr.model.*;
import com.frpr.model.Package;
import com.frpr.pojo.FRCount;
import com.frpr.pojo.FRPojo;
import com.frpr.repo.*;
import com.frpr.response.CommonResponse;
import com.frpr.service.AuditService;
import com.frpr.utils.DateFormatEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.text.SimpleDateFormat;

import static com.frpr.utils.CommonUtils.formatDate;
import static com.frpr.utils.CommonUtils.setEndOfDay;

@RestController
@RequestMapping("/v1/facility-registry")
public class FacilityRegistryController {

    @Autowired
    protected FacilityRegistryMapper facilityRegistryMapper;

    @Autowired
    FacilityRepository repository;

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

    @Autowired
    MetaDataRepository metaDataRepository;

    @Autowired
    PackageRepository packageRepository;


    @Autowired
    MongoTemplate mongoTemplate;


    @Autowired
    RestTemplate restTemplate;

    @Value("${pr.url}")
    private String prBaseUrl;

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
    public ResponseEntity<CommonResponse> save(@RequestBody FacilityRegistry facilityRegistry, HttpServletRequest request) {
        //     user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        facilityRegistry.setCode(getNextCode());
        if (isAlreadyExist(null, facilityRegistry.getCode(), facilityRegistry.getLocationCode())) {
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.Facility_already_exist_with_code: " + facilityRegistry.getCode()), HttpStatus.ACCEPTED);
        }
        auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.ADD, facilityRegistry, null, null, request, facilityRegistry.getCode());
        FacilityRegistry u = repository.save(facilityRegistry);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(u), HttpStatus.ACCEPTED);
    }

    @PutMapping("/{frId}")
    public ResponseEntity<CommonResponse> update(@PathVariable String frId, @RequestBody FacilityRegistry registry, HttpServletRequest request) {


        try {
            FacilityRegistry facilityRegistry = repository.findBy_id(frId);
            FacilityRegistry oldData = repository.findBy_id(frId);

            if (registry.getEmail() == null) {
                facilityRegistry.set_id(frId);
                facilityRegistry.setStatus(registry.getStatus());
                facilityRegistry.setDeactivateReason(registry.getDeactivateReason());
                facilityRegistry = repository.save(facilityRegistry);
                auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.UPDATE, registry, oldData, facilityRegistry, request, registry.getCode());
                return new ResponseEntity<CommonResponse>(new CommonResponse("ok", "server_success.updated_facility").setData(facilityRegistry), HttpStatus.ACCEPTED);
            } else if (registry.getCode() == null) {

                facilityRegistry.setStatus(registry.getStatus());
                if (facilityRegistry.getCode() == null || facilityRegistry.getCode().isEmpty()) {
                    registry.setCode(getNextCode());
                } else {
                    registry.setCode(facilityRegistry.getCode());
                }
                registry.set_id(frId);
                facilityRegistry = repository.save(registry);
                auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.UPDATE, registry, oldData, facilityRegistry, request, registry.getCode());
                return new ResponseEntity<CommonResponse>(new CommonResponse("ok", "server_success.updated_facility").setData(facilityRegistry), HttpStatus.ACCEPTED);
            } else {
                if (isAlreadyExist(frId, registry.getCode(), registry.getLocationCode())) {
                    return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.Facility_already_exist_with_code"), HttpStatus.ACCEPTED);
                }
                registry.set_id(frId);
                facilityRegistry = repository.save(registry);
                auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.UPDATE, registry, oldData, facilityRegistry, request, registry.getCode());
                return new ResponseEntity<CommonResponse>(new CommonResponse("ok", "server_success.updated_users").setData(facilityRegistry), HttpStatus.ACCEPTED);
            }
        } catch (Exception e) {
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.update_error"), HttpStatus.ACCEPTED);
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse> getOnce(@PathVariable String id, HttpServletRequest request) {
        Optional<FacilityRegistry> u = repository.findById(id);

        if (!u.isPresent()) {
            return new ResponseEntity<>(new CommonResponse("error", "server_error.no_data_with_id: " + id), HttpStatus.ACCEPTED);
        }

        FRPojo pojo = facilityRegistryMapper.toFRPojo(u.get());
        auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.GET, id, u.orElse(null), request, pojo.getCode());
        setLocationData(u.get(), pojo);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(pojo), HttpStatus.ACCEPTED);
    }

    @GetMapping
    public ResponseEntity<CommonResponse> getAll(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int limit, @RequestParam(value = "facilityId", required = false) String facilityId,
                                                 @RequestParam(value = "code", required = false) String code,
                                                 @RequestParam(value = "locationCode", required = false) String locationCode,
                                                 @RequestParam(value = "status", required = false) String status,
                                                 @RequestParam(value = "name", required = false) String name,
                                                 @RequestParam(value = "type", required = false) String type,
                                                 @RequestParam(value = "province", required = false) String province,
                                                 @RequestParam(value = "district", required = false) String district,
                                                 @RequestParam(value = "sector", required = false) String sector,
                                                 @RequestParam(value = "cell", required = false) String cell,
                                                 @RequestParam(value = "village", required = false) String village,
                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date opening_from_date,
                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date opening_to_date,
                                                 @RequestParam(value = "skipPagination", required = false) Boolean skipPagination,
                                                 HttpServletRequest request) {
        auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.GET, request.getQueryString(), null, request, "LIST");
        if (facilityId == null) {
            Query query = new Query();
            PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("createdAt"));
            if (skipPagination != null && skipPagination) {
                query = new Query();
            } else {
                query = new Query().with(PageRequest.of(page - 1, limit));
            }

            if (code != null && !code.isEmpty())
                query.addCriteria(Criteria.where("code").regex(code, "i"));

            if (name != null && !name.isEmpty())
                query.addCriteria(Criteria.where("name").regex(name, "i"));

            if (locationCode != null && !locationCode.isEmpty())
                query.addCriteria(Criteria.where("locationCode").regex(locationCode, "i"));

            if (status != null && !status.isEmpty())
                query.addCriteria(Criteria.where("status").is(status));

            if (type != null && !type.isEmpty())
                query.addCriteria(Criteria.where("type").is(type));

            if (province != null && !province.isEmpty())
                query.addCriteria(Criteria.where("province").is(province));

            if (district != null && !district.isEmpty())
                query.addCriteria(Criteria.where("district").is(district));

            if (sector != null && !sector.isEmpty())
                query.addCriteria(Criteria.where("sector").is(sector));

            if (cell != null && !cell.isEmpty())
                query.addCriteria(Criteria.where("cell").is(cell));

            if (village != null && !village.isEmpty())
                query.addCriteria(Criteria.where("village").is(village));


            if (opening_from_date != null && opening_to_date != null)
                query.addCriteria(Criteria.where("facilityOpeningDate").gte(opening_from_date).lt(setEndOfDay(opening_to_date)));

            Query finalQuery = query;

            Page<FacilityRegistry> fr = PageableExecutionUtils.getPage(
                    mongoTemplate.find(finalQuery, FacilityRegistry.class),
                    pageRequest,
                    () -> mongoTemplate.count(finalQuery.skip(0).limit(0), FacilityRegistry.class));

            List<FRPojo> frPojoList = new ArrayList<>();
            for (FacilityRegistry facilityRegistry : fr) {
                FRPojo pojo = facilityRegistryMapper.toFRPojo(facilityRegistry);
                setLocationData(facilityRegistry, pojo);
                // pojo.setServices(serviceRepository.findAllById(facilityRegistry.getServices()));
                frPojoList.add(pojo);
            }


            return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(frPojoList).setCount(fr.getTotalElements()), HttpStatus.ACCEPTED);
        } else {
            FacilityRegistry registry = repository.findBy_id(facilityId);

            FRCount count = new FRCount();
            count.setFacility(registry);

            return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(count), HttpStatus.ACCEPTED);
        }
    }


    private void setLocationData(FacilityRegistry facilityRegistry, FRPojo pojo) {
        if (facilityRegistry.getCell() != null)
            pojo.setCell(cellsRepository.findById(facilityRegistry.getCell()).orElse(null));

        if (facilityRegistry.getDistrict() != null)
            pojo.setDistrict(districtsRepository.findById(facilityRegistry.getDistrict()).orElse(null));

        if (facilityRegistry.getProvince() != null)
            pojo.setProvince(provincesRepository.findById(facilityRegistry.getProvince()).orElse(null));

        if (facilityRegistry.getSector() != null)
            pojo.setSector(sectorsRepository.findById(facilityRegistry.getSector()).orElse(null));

        if (facilityRegistry.getVillage() != null)
            pojo.setVillage(villagesRepository.findById(facilityRegistry.getVillage()).orElse(null));
    }

    @GetMapping("/activate/{id}/{status}")
    public ResponseEntity<CommonResponse> activeInActive(@PathVariable String id, @PathVariable String status, HttpServletRequest request) {

        Optional<FacilityRegistry> u = repository.findById(id);
        u.get().setStatus(status);
        FacilityRegistry res = repository.save(u.get());
        auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.UPDATE, id, u.orElse(null), res, request, u.get().getCode());
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(res), HttpStatus.ACCEPTED);
    }

    @GetMapping("/delete/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable String id, HttpServletRequest request) {
        Optional<FacilityRegistry> u = repository.findById(id);
        u.get().setStatus("DELETED");
        FacilityRegistry res = repository.save(u.get());
        auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.UPDATE, id, u.orElse(null), res, request, u.get().getCode());
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(res), HttpStatus.ACCEPTED);
    }

    @GetMapping("/getAllProviderByFacility/{id}")
    public ResponseEntity<CommonResponse> getAllProviderByFacility(@PathVariable String id, HttpServletRequest request) {
        FacilityRegistry u = repository.findAllByCode(id);
        if (u == null)
            return new ResponseEntity<>(new CommonResponse("error", null).setData(new ArrayList<>()), HttpStatus.ACCEPTED);
        auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.GET, id, u, request, u.getCode());

        return new ResponseEntity<>(new CommonResponse("ok", u.getStatus().equalsIgnoreCase("ACTIVE") ? "1" : "0").setData(getPRs(u)), HttpStatus.ACCEPTED);
    }

    private List<ProviderRegistry> getPRs(FacilityRegistry facilityRegistry) {
        String url = prBaseUrl + "/api/v1/provider-registry/getProviderByFacilityId/" + facilityRegistry.get_id();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<ProviderRegistry> entity = new HttpEntity<ProviderRegistry>(headers);

        return Objects.requireNonNull(restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<ProviderRegistry>>() {
        }).getBody());

    }

    @PostMapping("/byids")
    public ResponseEntity<?> byIdes(@RequestBody ProviderRegistry registry, HttpServletRequest request) {
        //     user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.GET, registry, null, request, "ALL");
        if (registry.getFacilityId() == null || registry.getFacilityId().isEmpty()) {
            Query query = new Query();
            query.addCriteria(Criteria.where("name").regex(registry.getCountryOfBirth(), "i"));
            List<FacilityRegistry> facilityRegistries = mongoTemplate.find(query, FacilityRegistry.class);
            return new ResponseEntity<>(facilityRegistries, HttpStatus.ACCEPTED);
        }

        if (registry.getFacilityId().contains("1234")) {
            Iterable<FacilityRegistry> u = repository.findAll();
            return new ResponseEntity<>(u, HttpStatus.ACCEPTED);
        } else if (registry.getFacilityId().get(0).length() == 4) {
            FacilityRegistry u = repository.findAllByCode(registry.getFacilityId().get(0));
            return new ResponseEntity<>(Collections.singletonList(u), HttpStatus.ACCEPTED);
        } else {
            Iterable<FacilityRegistry> u = repository.findAllById(registry.getFacilityId());
            return new ResponseEntity<>(u, HttpStatus.ACCEPTED);
        }

    }

    @PostMapping("/check_code/get_next")
    public ResponseEntity<?> checkCode(HttpServletRequest request) {
        auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.GET, null, null, request);
        return new ResponseEntity<>("Some Data are here " + getNextCode(), HttpStatus.ACCEPTED);
    }

    public String getNextCode() {
        try {

            //now select from the facility
            Query query = new Query();
            // query.addCriteria(Criteria.where("name").regex("After Date Compliance Test", "i"));
            query.with(Sort.by(Order.desc("_id")));

            List<FacilityRegistry> facilityRegistries = mongoTemplate.find(query, FacilityRegistry.class);
            // List<FacilityRegistry> facilityRegistries = repository.findAll(Sort.by("_id").descending());

            if (facilityRegistries.size() == 0) {
                return "0001";
            }
            String out_code = "";
            int maximum_code = 0;
            for (int i = 0; i < facilityRegistries.size(); i++) {
                FacilityRegistry fr = facilityRegistries.get(i);
                String old_code = fr.getCode();
                int new_code = 0;
                //now create the operation of incrementing the code
                try {
                    new_code = old_code.isEmpty() ? 0 : Integer.parseInt(old_code);
                } catch (Exception ignored) {
                }


                if (maximum_code < new_code) {
                    maximum_code = new_code;
                }

            }

            maximum_code++;

            out_code = maximum_code + "";
            while (out_code.length() < 4) {
                out_code = "0" + out_code;
            }
            //System.out.print(out_code + ", ");

            return out_code;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private boolean isAlreadyExist(String exstingId, String code, String locationCode) {


        if (exstingId != null || code != null || locationCode != null) {
            Query query = new Query();
            if (code != null && !code.isEmpty()) {
                query.addCriteria(Criteria.where("code").regex(code, "i"));
                List<FacilityRegistry> list = new ArrayList<>(mongoTemplate.find(query, FacilityRegistry.class));
                return isUserIdSame(list, exstingId);
            }

            /*if (locationCode != null) {
                query.addCriteria(Criteria.where("locationCode").regex(locationCode, "i"));
                List<FacilityRegistry> list = new ArrayList<>(mongoTemplate.find(query, FacilityRegistry.class));
                return isUserIdSame(list, exstingId);
            }*/


            /*List<FacilityRegistry> list = new ArrayList<>(mongoTemplate.find(query, FacilityRegistry.class));

            return isUserIdSame(list, exstingId);*/
        }

        return false;
    }

    private boolean isUserIdSame(List<FacilityRegistry> list, String exstingId) {

        if (list.isEmpty())
            return false;


        if (list.size() > 1) return true;

        FacilityRegistry registry = list.get(0);

        if (exstingId != null) {
            return !(registry.get_id().equalsIgnoreCase(exstingId));
        } else {
            return true;
        }
    }

    private boolean facilityByNameByLocation(String name, String locationCode) {
        Query query = new Query();
        if (name != null && !name.isEmpty()) {
            query.addCriteria(Criteria.where("name").is(name));
            query.addCriteria(Criteria.where("locationCode").is(locationCode));
            List<FacilityRegistry> list = new ArrayList<>(mongoTemplate.find(query, FacilityRegistry.class));
            if (list.size() > 0) {
                return true;
            }
        }
        return false;
    }

    @PostMapping(value = "/upload", consumes = "application/json")
    public ResponseEntity<CommonResponse> uploadFacilities(@RequestBody String data, HttpServletRequest request) {

        //Here make sure to get the list of available data to be processed
        ObjectMapper objectMapper1 = new ObjectMapper();

        try {
            JsonNode jsonNode = objectMapper1.readTree(data);

            Iterator<JsonNode> facilities = jsonNode.elements();

            List<String> savedCode = new ArrayList<>();
            List<String> unsavedCode = new ArrayList<>();


            List<List<String>> uploadResult = new ArrayList<>();

            //Loop all comming comming data and try to process those info
            while (facilities.hasNext()) {
                JsonNode facility = facilities.next();

                //System.out.println(facility.get("name").toString().replace("\"", ""));
                

                FacilityRegistry facilityRegistry = new FacilityRegistry();
                facilityRegistry.setName(facility.get("name").toString().trim().replace("\"", ""));
                facilityRegistry.setCode(getNextCode());
                facilityRegistry.setStatus(facility.get("status").toString().trim().replace("\"", ""));

                Provinces p = provincesRepository.findOneByName(facility.get("province").toString().trim().replace("\"", ""));
                if (p != null) {
                    facilityRegistry.setProvince(p.get_id());

                    Districts districts = districtsRepository.findOneByProvinceIdAndName(p.get_id(), facility.get("district").toString().trim().replace("\"", ""));
                    if (districts != null) {
                        facilityRegistry.setDistrict(districts.get_id());

                        Sectors sectors = sectorsRepository.findOneByDistrictIdAndName(districts.get_id(), facility.get("sector").toString().trim().replace("\"", ""));
                        if (sectors != null) {
                            facilityRegistry.setSector(sectors.get_id());

                            Cell cell = cellsRepository.findOneBySectorIdAndName(sectors.get_id(), facility.get("cell").toString().trim().replace("\"", ""));
                            if (cell != null) {
                                facilityRegistry.setCell(cell.get_id());

                                Villages villages = villagesRepository.findOneByCellIdAndName(cell.get_id(), facility.get("village").toString().trim().replace("\"", ""));
                                // System.out.println(cell.get_id() + " " + facility.get("village").toString().trim().replace("\"", ""));;
                                if (villages != null) {
                                    facilityRegistry.setVillage(villages.get_id());
                                    facilityRegistry.setLocationCode(villages.getCode()); //The remove the ambiguity on the users' side for the location as it is predefined encoded string on every village
                                } else {
                                    unsavedCode.add(facility.get("name").toString().trim().replace("\"", "") + "(" + facility.get("locationCode").toString().trim().replace("\"", "") + ") has Invalid village " + facility.get("village").toString().trim().replace("\"", ""));
                                    continue;
                                }
                            } else {
                                unsavedCode.add(facility.get("name").toString().trim().replace("\"", "") + "(" + facility.get("locationCode").toString().trim().replace("\"", "") + ") has Invalid cell " + facility.get("cell").toString().trim().replace("\"", ""));
                                continue;
                            }
                        } else {
                            unsavedCode.add(facility.get("name").toString().trim().replace("\"", "") + "(" + facility.get("locationCode").toString().trim().replace("\"", "") + ") has Invalid sector " + facility.get("sector").toString().trim().replace("\"", ""));
                            continue;
                        }
                    } else {
                        unsavedCode.add(facility.get("name").toString().trim().replace("\"", "") + "(" + facility.get("locationCode").toString().trim().replace("\"", "") + ") has Invalid district " + facility.get("district").toString().trim().replace("\"", ""));
                        continue;
                    }
                } else {
                    unsavedCode.add(facility.get("name").toString().trim().replace("\"", "") + "(" + facility.get("locationCode").toString().trim().replace("\"", "") + ") has Invalid province " + facility.get("province").toString().trim().replace("\"", ""));
                    continue;
                }

                //Here check if the facility exist in the database before
                if (facilityByNameByLocation(facilityRegistry.getName(), facilityRegistry.getLocationCode())) {
                    unsavedCode.add(facilityRegistry.getName() + " Exists with " + facilityRegistry.getLocationCode() + " as location code" );
                    continue;
                }

                //facilityRegistry.setLocationCode(facility.get("locationCode").toString().trim().replace("\"", ""));
                facilityRegistry.setCategory(facility.get("category").toString().trim().replace("\"", ""));

                //Make sure to find the category object in the database if not skipp the raw too
                MetaData metaData = metaDataRepository.findOneByName(facilityRegistry.getCategory());
                if(metaData == null){
                    unsavedCode.add(facilityRegistry.getName() + " has an invalid Category '" + facilityRegistry.getCategory() + "'");
                    continue;
                }
                facilityRegistry.setCategory(metaData.getName()); // Here reset the category for better using the same capitalization schema as the database
                //we have the metada now make sure to find corresponding packages
                List<Package> packages = packageRepository.findAllByCategoryId(metaData.get_id());

                if(packages == null){
                    unsavedCode.add(facilityRegistry.getName() + " has an invalid Category '" + facilityRegistry.getCategory() + "' no package was found.");
                    continue;
                }
                facilityRegistry.setPackages(packages);
                facilityRegistry.setType(facility.get("type").toString().trim().replace("\"", ""));
                facilityRegistry.setLongitude(facility.get("longitude").toString().trim().replace("\"", ""));
                facilityRegistry.setLatitude(facility.get("latitude").toString().trim().replace("\"", ""));
                facilityRegistry.setFacilityOpeningDate(formatDate(DateFormatEnum.YYYYMMDD, facility.get("facilityOpeningDate").toString().trim().replace("\"", "")));
                facilityRegistry.setPobox(facility.get("pobox").toString().trim().replace("\"", ""));
                facilityRegistry.setPhonenumber(facility.get("phonenumber").toString().trim().replace("\"", ""));
                facilityRegistry.setEmail(facility.get("email").toString().trim().replace("\"", ""));
                facilityRegistry.setStreetAddress(facility.get("streetAddress").toString().trim().replace("\"", ""));
                // facilityRegistry.set(facility.get("phonenumber").toString().trim());

                auditService.saveAuditLog(getLoggedInUser(), FacilityRegistry.class.getSimpleName(), Action.UPLOAD, facilityRegistry, null, null, request, facilityRegistry.getCode());
                FacilityRegistry u = repository.save(facilityRegistry);
                savedCode.add(u.getCode());
                // System.out.println(facilityRegistry.getFacilityOpeningDate());
                // System.out.println(facilityRegistry.getCode());
                // System.out.println("===============================================================");
            }

            uploadResult.add(0, savedCode);
            uploadResult.add(1, unsavedCode);

            if(unsavedCode.size() > 0){
                return new ResponseEntity<>(new CommonResponse("error", unsavedCode.size() + " record"+(unsavedCode.size()>1?"s":"")+" got an error").setData(unsavedCode), HttpStatus.NOT_ACCEPTABLE);
            }

            if(savedCode.size() > 0){
                return new ResponseEntity<CommonResponse>(new CommonResponse("ok", savedCode.size() + " Facilities are uploaded!").setData(uploadResult), HttpStatus.ACCEPTED);
            }
            return new ResponseEntity<CommonResponse>(new CommonResponse("error",  " No Records were uploaded"), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonResponse("error", e.getMessage()).setData(e), HttpStatus.NOT_ACCEPTABLE);
        }

        //return new ResponseEntity<>(new CommonResponse("error", null).setData(data), HttpStatus.NOT_ACCEPTABLE);
    }
}
