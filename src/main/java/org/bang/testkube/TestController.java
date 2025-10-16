package org.bang.testkube;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping
    public String test1(HttpServletRequest request
        , @Value("${spring.application.mode}") String mode
        , @Value("${spring.application.version}") String version ){

        return "Hello v4:" + request.getRemoteAddr() + "-" + request.getRemotePort() + "   " + mode + "   " + version;
    }
}
