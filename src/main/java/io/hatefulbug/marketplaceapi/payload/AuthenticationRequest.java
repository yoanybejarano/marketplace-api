
package io.hatefulbug.marketplaceapi.payload;

public record AuthenticationRequest(
        String email,
        String password
) {
}

