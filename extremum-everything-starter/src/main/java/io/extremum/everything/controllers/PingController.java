package io.extremum.everything.controllers;

import io.extremum.sharedmodels.dto.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {
    @GetMapping(value = "/ping")
    public Response ping () {
        return Response.ok();
    }
}
