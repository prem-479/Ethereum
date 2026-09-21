package com.ethobservatory.controller;

import com.ethobservatory.dto.AddressProfileResponse;
import com.ethobservatory.model.AddressActivity;
import com.ethobservatory.service.AddressActivityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AddressController {

    private final AddressActivityService addressActivityService;

    public AddressController(AddressActivityService addressActivityService) {
        this.addressActivityService = addressActivityService;
    }

    @GetMapping("/addresses/{address}")
    public AddressProfileResponse profile(@PathVariable String address) {
        Optional<AddressActivity> optional = addressActivityService.findByAddress(address);
        if (optional.isEmpty()) {
            return new AddressProfileResponse(address, 0, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, null, null);
        }

        AddressActivity activity = optional.get();
        return new AddressProfileResponse(
                activity.getAddress(),
                activity.getTransactionCount(),
                activity.getSentTransactions(),
                activity.getReceivedTransactions(),
                activity.getUniqueCounterparties(),
                activity.getEthSent(),
                activity.getEthReceived(),
                activity.getFirstSeen(),
                activity.getLastSeen()
        );
    }
}
