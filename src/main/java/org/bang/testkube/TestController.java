package org.bang.testkube;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping
    public String test1(HttpServletRequest request) {

        return "Hello v3:" + request.getRemoteAddr() + "-" + request.getRemotePort();
    }
}
