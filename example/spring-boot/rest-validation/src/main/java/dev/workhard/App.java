package dev.workhard;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class App {
    record TodoRequest(@NotBlank String title) {}

    @PostMapping("/todos")
    TodoRequest create(@Valid @RequestBody TodoRequest request) {
        return request;
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
