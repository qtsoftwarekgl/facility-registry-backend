package com.frpr.controlller;

import com.frpr.pojo.CitizenReq;
import com.frpr.pojo.RolesDto;
import com.frpr.response.CommonResponse;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1")
public class CommonController {

    @Value("${openhim.baseUrl}")
    private String openHimUrl;

    @PostMapping("/citizens/getCitizen")
    public ResponseEntity<?> getCitizen(@RequestBody CitizenReq req) {
        RestTemplate restTemplate = new RestTemplate();

        final String baseUrl = openHimUrl + "/api/v1/citizens/getCitizen";
        //URI uri = new URI(baseUrl);


        ResponseEntity<String> result = restTemplate.postForEntity(baseUrl, req, String.class);


        return new ResponseEntity<>(new Gson().fromJson(result.getBody(), Object.class), HttpStatus.ACCEPTED);
    }

    @GetMapping("/roles/activeList")
    public ResponseEntity<?> getStaticRole() {
        List<RolesDto> roles = new ArrayList<>();
        roles.add(RolesDto.builder().role("MOH").name("Viewer").role("VIEWER").status("ACTIVE").value("VIEWER").build());
        roles.add(RolesDto.builder().role("MOH").name("Admin").role("ADMIN").status("ACTIVE").value("ADMIN").build());

        return new ResponseEntity<>(new CommonResponse("ok", "").setCount((long) roles.size()).setData(roles), HttpStatus.ACCEPTED);
    }
}
