package com.ethobservatory.service;

import com.ethobservatory.config.ObservatoryProperties;
import com.ethobservatory.model.ObservationSession;
import com.ethobservatory.repository.ObservationSessionRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final ObservationSessionRepository observationSessionRepository;
    private final ObservatoryProperties observatoryProperties;
    private ObservationSession currentSession;

    public SessionService(ObservationSessionRepository observationSessionRepository,
                          ObservatoryProperties observatoryProperties) {
        this.observationSessionRepository = observationSessionRepository;
        this.observatoryProperties = observatoryProperties;
    }

    @PostConstruct
    public void init() {
        currentSession = observationSessionRepository.findAll().stream().findFirst().orElseGet(() -> {
            ObservationSession session = new ObservationSession();
            session.setSessionStart(Instant.now());
            session.setLastUpdated(Instant.now());
            session.setLatestBlock(0L);
            session.setBlocksObserved(0L);
            session.setTransactionsObserved(0L);
            session.setUniqueWallets(0L);
            session.setRepeatedAddresses(0L);
            session.setAverageRpcLatencyMs(0.0);
            session.setAverageBlockIntervalMs(0.0);
            return observationSessionRepository.save(session);
        });
        log.info("Initialized observation session with ID {}", currentSession.getId());
    }

    @Transactional
    public ObservationSession getOrCreateSession() {
        if (currentSession == null) {
            currentSession = observationSessionRepository.findAll().stream().findFirst().orElseGet(() -> {
                ObservationSession session = new ObservationSession();
                session.setSessionStart(Instant.now());
                session.setLastUpdated(Instant.now());
                return observationSessionRepository.save(session);
            });
        }
        return currentSession;
    }

    public ObservationSession current() {
        return getOrCreateSession();
    }

    @Transactional
    public void updateStats(Long latestBlock, Long observedBlocks, Long observedTransactions, Long uniqueWallets, Long repeatedAddresses, Double avgLatencyMs, Double avgBlockIntervalMs) {
        ObservationSession session = getOrCreateSession();
        session.setLatestBlock(latestBlock);
        session.setBlocksObserved(observedBlocks);
        session.setTransactionsObserved(observedTransactions);
        session.setUniqueWallets(uniqueWallets);
        session.setRepeatedAddresses(repeatedAddresses);
        session.setAverageRpcLatencyMs(avgLatencyMs);
        session.setAverageBlockIntervalMs(avgBlockIntervalMs);
        session.setLastUpdated(Instant.now());
        observationSessionRepository.save(session);
    }

    public long sessionUptimeSeconds() {
        ObservationSession session = current();
        Instant delta = Instant.now().minusSeconds(session.getSessionStart().getEpochSecond());
        return Math.max(0, delta.getEpochSecond());
    }
}
