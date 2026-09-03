
package io.hatefulbug.marketplaceapi.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import io.hatefulbug.marketplaceapi.payload.AuthenticationRequest;
import io.hatefulbug.marketplaceapi.payload.AuthenticationResponse;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserDetailsService userDetailsService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 1. Verify credentials using BCrypt & AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // 2. Load UserDetails after successful authentication
        var user = userDetailsService.loadUserByUsername(request.email());

        // 3. Generate token signed with your internal JwtService secret
        var jwtToken = jwtService.generateToken(user);

        return new AuthenticationResponse(jwtToken);
    }
}
