package dev.workhard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 같은 /main이지만 serverError 키가 있을 때만 이쪽으로 온다. */
@RestController
public class ErrorPageController {

    // params = "serverError": 값과 상관없이 키 존재만 조건으로 건다
    @GetMapping(path = "/main", params = "serverError")
    // required = false: "?serverError"처럼 값이 비면 필수 파라미터는 400이 되므로 키 존재는 params에 맡긴다
    public ResponseEntity<String> page(@RequestParam(name = "serverError", required = false) String value) {
        if ("1".equals(value)) {
            return ResponseEntity.ok("server-error");
        }
        return ResponseEntity.status(404).body("invalid"); // 값 검증은 메서드 안에서
    }
}
