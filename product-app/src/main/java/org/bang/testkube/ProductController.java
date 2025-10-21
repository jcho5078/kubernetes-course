package org.bang.testkube;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    /*@GetMapping
    public String test1(HttpServletRequest request
        , @Value("${spring.application.mode}") String mode
        , @Value("${spring.application.version}") String version ){

        return "Hello :" + request.getRemoteAddr() + "-" + request.getRemotePort() + "   " + mode + "   " + version;
    }*/

    @GetMapping
    public String testProduct(HttpServletRequest request){

        String productId = request.getParameter("product_id");

        return "product info - " + productId;
    }

    @GetMapping("/product")
    public String getProductInfo(@RequestParam("product_id") String productId) {
        // user-app으로부터 받은 product_id를 포함한 응답
        return "Product App Info for Product ID: " + productId;
    }
}
