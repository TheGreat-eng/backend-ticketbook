package com.example.App.security;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.example.App.config.RsaKeyConfig;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final RsaKeyConfig rsaKeyConfig;

    public String generateToken(String email, Collection<? extends GrantedAuthority> authorities) throws JOSEException {
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer("ticket-pro")
                .issueTime(new Date())
                .expirationTime(new Date(new Date().getTime() + 3600000))
                .claim("roles", roles)
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).build();
        JWSObject jwsObject = new JWSObject(header, payload);

        JWSSigner signer = new RSASSASigner(rsaKeyConfig.getPrivateKey());
        jwsObject.sign(signer);

        return jwsObject.serialize();
    }
}
