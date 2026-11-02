package dev.workhard.entry;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

public class EntryController {

    // 본문 없이 302 + Location만 반환. @RestController여도 REST API가 아니라 리다이렉트 진입점이다.
    @RestController
    public static class Redirecting {
        @GetMapping("/enter")
        public ResponseEntity<Void> enter() {
            return ResponseEntity.status(302).header("Location", "/pages/home").build();
        }
    }

    // @Controller + String 반환 = 뷰 이름으로 해석된다.
    @Controller
    public static class Page {
        @GetMapping("/home")
        public String home() {
            return "pages";
        }
    }

    // @RestController + String 반환 = 응답 본문 그대로.
    @RestController
    public static class Text {
        @GetMapping("/text")
        public String text() {
            return "pages";
        }
    }

    // @Controller여도 ResponseEntity를 반환하면 상태/헤더가 그대로 나간다.
    @Controller
    public static class PlainControllerRedirect {
        @GetMapping("/enter2")
        public ResponseEntity<Void> enter() {
            return ResponseEntity.status(302).header("Location", "/pages/home").build();
        }
    }
}
