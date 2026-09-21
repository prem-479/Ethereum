package com.ethobservatory.repository;

import com.ethobservatory.model.ObservationSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObservationSessionRepository extends JpaRepository<ObservationSession, Long> {
}
