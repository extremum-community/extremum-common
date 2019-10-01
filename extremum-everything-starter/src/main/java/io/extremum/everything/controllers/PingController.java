package io.extremum.everything.controllers;

import io.extremum.sharedmodels.dto.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api("Ping")
@RestController
@RequestMapping("/v1")
public class PingController {
    @ApiOperation(value = "Check a health status of a service")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    @GetMapping(value = "/v1/ping")
    public Response ping() {
        return Response.ok();
    }
}
