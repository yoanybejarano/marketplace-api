
package io.hatefulbug.marketplaceapi.service;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hatefulbug.marketplaceapi.dto.CustomerDto;
import io.hatefulbug.marketplaceapi.entity.Customer;
import io.hatefulbug.marketplaceapi.exception.ResourceNotFoundException;
import io.hatefulbug.marketplaceapi.repository.CustomerRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCustomer = new Customer();
        sampleCustomer.setId(1);
        sampleCustomer.setFirstName("John");
        sampleCustomer.setLastName("Doe");
        sampleCustomer.setEmail("john.doe@example.com");
        sampleCustomer.setPhone("1234567890");
        sampleCustomer.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should return CustomerDto when customer exists")
    void getCustomerById_WhenCustomerExists_ReturnsCustomerDto() {
        // Given
        Integer customerId = 1;
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(sampleCustomer));

        // When
        CustomerDto result = customerService.getCustomerById(customerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(sampleCustomer.getId());
        assertThat(result.firstName()).isEqualTo(sampleCustomer.getFirstName());
        assertThat(result.lastName()).isEqualTo(sampleCustomer.getLastName());
        assertThat(result.email()).isEqualTo(sampleCustomer.getEmail());
        assertThat(result.phone()).isEqualTo(sampleCustomer.getPhone());
        assertThat(result.createdAt()).isEqualTo(sampleCustomer.getCreatedAt());

        verify(customerRepository).findById(customerId);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when customer does not exist")
    void getCustomerById_WhenCustomerDoesNotExist_ThrowsResourceNotFoundException() {
        // Given
        Integer customerId = 99;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> customerService.getCustomerById(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with id: " + customerId);

        verify(customerRepository).findById(customerId);
        verifyNoMoreInteractions(customerRepository);
    }
}
