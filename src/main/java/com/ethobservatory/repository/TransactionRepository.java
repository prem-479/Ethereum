package com.ethobservatory.repository;

import com.ethobservatory.model.TransactionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {

    Optional<TransactionRecord> findByTransactionHash(String transactionHash);

    Page<TransactionRecord> findByFromAddressOrToAddress(String fromAddress, String toAddress, Pageable pageable);

    Page<TransactionRecord> findByBlockNumber(Long blockNumber, Pageable pageable);

    Page<TransactionRecord> findByFromAddress(String fromAddress, Pageable pageable);

    Page<TransactionRecord> findByToAddress(String toAddress, Pageable pageable);

        List<TransactionRecord> findByTransactionHashContainingIgnoreCaseOrFromAddressContainingIgnoreCaseOrToAddressContainingIgnoreCase(
            String transactionHash, String fromAddress, String toAddress);

        List<TransactionRecord> findByBlockNumber(Long blockNumber);

        @Query("select count(distinct case when t.fromAddress = :address then t.toAddress else t.fromAddress end) from TransactionRecord t where t.fromAddress = :address or t.toAddress = :address")
        int countDistinctCounterparties(String address);

    @Query("select count(t) from TransactionRecord t")
    long countTransactions();

    @Query("select count(distinct t.fromAddress) + count(distinct t.toAddress) from TransactionRecord t")
    Long countDistinctWallets();
}
