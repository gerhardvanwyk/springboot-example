package org.wyk.application.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wyk.application.dto.Stuff;

import java.util.ArrayList;
import java.util.List;

/**
 * see @RestController
 * Response is Wrapped in a Response Body
 */
@RestController(value = "/api")
public class DataRestController {

    @GetMapping( value = "stuff", produces = {"application/json"})
    public List<Stuff> getStuff(){
        List<Stuff> stuffs = new ArrayList<>();
        Stuff one = new Stuff();
        one.setImportance(1);
        one.setName("Some Stuff");
        one.setType("Moveable Stuff");
        stuffs.add(one);
        Stuff two = new Stuff();
        two.setType("Simelar Stuff");
        two.setName("Some more Stuff");
        two.setImportance(1);
        stuffs.add(two);

        return stuffs;
    }
}
