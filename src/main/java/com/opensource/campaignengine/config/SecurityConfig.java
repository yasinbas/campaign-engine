package com.opensource.campaignengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import static org.springframework.security.config.Customizer.withDefaults;


@EnableMethodSecurity
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
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                // Giriş formu ayarı aynı kalıyor
                .formLogin(withDefaults())
                // YENİ EKLENEN KISIM: Çıkış ayarları
                .logout(logout -> logout
                        // Çıkış işlemi başarılı olduğunda yönlendirilecek sayfa
                        .logoutSuccessUrl("/login?logout")
                        // Herkesin çıkış yapabilmesine izin ver
                        .permitAll()
                );
        return http.build();
    }

    // 3. Hafızadaki kullanıcıyı tanımlayan eski userDetailsService metodu SİLİNDİ.
    // Artık CustomUserDetailsService kullanılıyor.
}