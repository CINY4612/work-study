package dev.workhard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 조건이 없는 일반 진입 매핑. */
@RestController
public class EntryController {

    @GetMapping("/main")
    public String entry() {
        return "entry";
    }
}
