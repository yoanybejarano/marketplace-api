package io.hatefulbug.marketplaceapi.service;

import org.springframework.stereotype.Service;

import io.hatefulbug.marketplaceapi.dto.CustomerDto;
import io.hatefulbug.marketplaceapi.entity.Customer;
import io.hatefulbug.marketplaceapi.exception.ResourceNotFoundException;
import io.hatefulbug.marketplaceapi.metric.CustomerMetrics;
import io.hatefulbug.marketplaceapi.repository.CustomerRepository;
import io.hatefulbug.marketplaceapi.util.DtoMapperUtil;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMetrics customerMetrics;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerMetrics customerMetrics
    ) {
        this.customerRepository = customerRepository;
        this.customerMetrics = customerMetrics;
    }

    public CustomerDto getCustomerById(Integer id) {
        return customerMetrics.recordGetCustomer(() -> {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> {
                        customerMetrics.recordCustomerNotFound();
                        return new ResourceNotFoundException("Customer not found with id: " + id);
                    });
            return DtoMapperUtil.toCustomerDto(customer);
        });
    }
}
