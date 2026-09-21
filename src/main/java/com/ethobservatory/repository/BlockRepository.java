package com.ethobservatory.repository;

import com.ethobservatory.model.BlockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BlockRepository extends JpaRepository<BlockRecord, Long> {

    Optional<BlockRecord> findByBlockNumber(Long blockNumber);

    Optional<BlockRecord> findByBlockHash(String blockHash);

    @Query("select max(b.blockNumber) from BlockRecord b")
    Long findMaxBlockNumber();

    Optional<BlockRecord> findTopByOrderByBlockNumberDesc();
}
