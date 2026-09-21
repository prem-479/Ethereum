package com.ethobservatory.service;

import com.ethobservatory.model.AddressActivity;
import com.ethobservatory.repository.AddressActivityRepository;
import com.ethobservatory.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
public class AddressActivityService {

    private final AddressActivityRepository addressActivityRepository;
    private final TransactionRepository transactionRepository;

    public AddressActivityService(AddressActivityRepository addressActivityRepository,
                                  TransactionRepository transactionRepository) {
        this.addressActivityRepository = addressActivityRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AddressActivity recordActivity(String address, boolean sent, BigDecimal value, Instant timestamp) {
        return recordActivity(address, sent, value, timestamp, null);
    }

    @Transactional
    public AddressActivity recordActivity(String address, boolean sent, BigDecimal value, Instant timestamp, String counterparty) {
        String normalized = normalize(address);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("Address must be valid.");
        }

        AddressActivity activity = addressActivityRepository.findByAddress(normalized)
                .orElseGet(() -> {
                    AddressActivity newActivity = new AddressActivity();
                    newActivity.setAddress(normalized);
                    newActivity.setFirstSeen(timestamp);
                    newActivity.setLastSeen(timestamp);
                    newActivity.setTransactionCount(0);
                    newActivity.setSentTransactions(0);
                    newActivity.setReceivedTransactions(0);
                    newActivity.setUniqueCounterparties(0);
                    newActivity.setEthSent(BigDecimal.ZERO);
                    newActivity.setEthReceived(BigDecimal.ZERO);
                    return newActivity;
                });

        activity.setTransactionCount(activity.getTransactionCount() + 1);
        if (sent) {
            activity.setSentTransactions(activity.getSentTransactions() + 1);
            activity.setEthSent(activity.getEthSent().add(value));
        } else {
            activity.setReceivedTransactions(activity.getReceivedTransactions() + 1);
            activity.setEthReceived(activity.getEthReceived().add(value));
        }

        activity.setLastSeen(timestamp);
        if (activity.getFirstSeen() == null || timestamp.isBefore(activity.getFirstSeen())) {
            activity.setFirstSeen(timestamp);
        }

        if (counterparty != null && !counterparty.isBlank()) {
            activity.setUniqueCounterparties(transactionRepository.countDistinctCounterparties(normalized));
        }

        return addressActivityRepository.save(activity);
    }

    public Optional<AddressActivity> findByAddress(String address) {
        return addressActivityRepository.findByAddress(normalize(address));
    }

    private String normalize(String address) {
        return address == null ? null : address.trim();
    }
}
