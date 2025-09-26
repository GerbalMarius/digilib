package org.digilib.library.home;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//only for testing if api works, to be removed later
@RestController
public class HomeController {

    @GetMapping("")
    public String index() {
        return "Hello World!";
    }
}
