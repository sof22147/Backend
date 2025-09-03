package com.room911.room911.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .cors(Customizer.withDefaults())     // usa tu CorsConfig (WebMvcConfigurer)
      .csrf(csrf -> csrf.disable())
      .requestCache(rc -> rc.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // preflight CORS
        .anyRequest().permitAll()           // TODO abierto
      )
      .anonymous(Customizer.withDefaults())
      .httpBasic(httpBasic -> httpBasic.disable())
      .formLogin(form -> form.disable())
      .logout(logout -> logout.disable());

    return http.build();
  }
}
