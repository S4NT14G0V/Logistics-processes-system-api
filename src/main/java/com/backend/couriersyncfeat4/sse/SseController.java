package com.backend.couriersyncfeat4.sse;

import com.backend.couriersyncfeat4.entity.UserEntity;
import com.backend.couriersyncfeat4.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {

    private final SseEmitterService sseEmitterService;
    private final UserService userService;

    public SseController(SseEmitterService sseEmitterService, UserService userService) {
        this.sseEmitterService = sseEmitterService;
        this.userService = userService;
    }

    @GetMapping("/events")
    public SseEmitter subscribe() {
        UserEntity user = userService.getCurrentUser();
        return sseEmitterService.subscribe(user.getId());
    }
}
