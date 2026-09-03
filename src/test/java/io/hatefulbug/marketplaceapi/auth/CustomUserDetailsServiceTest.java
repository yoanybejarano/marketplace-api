
package io.hatefulbug.marketplaceapi.auth;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import io.hatefulbug.marketplaceapi.entity.Permission;
import io.hatefulbug.marketplaceapi.entity.Role;
import io.hatefulbug.marketplaceapi.entity.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        Permission readPermission = new Permission();
        readPermission.setName("READ_INVENTORY");

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        adminRole.setPermissions(Set.of(readPermission));

        sampleUser = new User();
        sampleUser.setId(1);
        sampleUser.setEmail("admin@example.com");
        sampleUser.setPassword("hashed_password");
        sampleUser.setActive(true);
        sampleUser.setRoles(Set.of(adminRole));
    }

    @Test
    @DisplayName("Should load UserDetails successfully when user exists")
    void loadUserByUsername_Success() {
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(sampleUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("hashed_password");
        assertThat(userDetails.isEnabled()).isTrue();

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_ADMIN", "READ_INVENTORY");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user is not found")
    void loadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: unknown@example.com");
    }
}
