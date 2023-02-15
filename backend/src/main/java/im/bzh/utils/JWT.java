package im.bzh.utils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JWT {

    @Bean
    public Key keyBean() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }
}
