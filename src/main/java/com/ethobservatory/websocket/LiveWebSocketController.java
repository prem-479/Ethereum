package com.ethobservatory.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class LiveWebSocketController {

    @MessageMapping("/ping")
    @SendTo("/topic/status")
    public LiveEvent ping() {
        return new LiveEvent("PONG", "heartbeat");
    }
}
