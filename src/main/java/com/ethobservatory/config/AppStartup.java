package com.ethobservatory.config;

import com.ethobservatory.service.BlockchainMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AppStartup {

    private static final Logger log = LoggerFactory.getLogger(AppStartup.class);

    private final BlockchainMonitorService blockchainMonitorService;

    public AppStartup(BlockchainMonitorService blockchainMonitorService) {
        this.blockchainMonitorService = blockchainMonitorService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("Application ready; starting blockchain initialization.");
        blockchainMonitorService.initialize();
    }
}
