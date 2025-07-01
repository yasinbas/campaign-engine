package com.opensource.campaignengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Kendi UserDetailsService'imizi buraya enjekte etmemize gerek yok.
    // Spring Boot, context'te bu tipten bir Bean bulduğunda otomatik olarak kullanır.

    // 1. Güvenli şifreleme için PasswordEncoder Bean'i oluşturuyoruz.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Güvenlik kurallarını tanımlayan ana metodumuz.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // "/admin/" ile başlayan tüm adresler için kimlik doğrulaması iste
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Sadece ADMIN rolü olanlar erişebilir!
                        // Geriye kalan tüm adreslere herkesin erişmesine izin ver
                        .anyRequest().permitAll()
                )
                .formLogin(withDefaults());
        return http.build();
    }

    // 3. Hafızadaki kullanıcıyı tanımlayan eski userDetailsService metodu SİLİNDİ.
    // Artık CustomUserDetailsService kullanılıyor.
}