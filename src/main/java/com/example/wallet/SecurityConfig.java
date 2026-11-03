// package com.example.wallet;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// public class SecurityConfig {

//     private static final String GROUP_PREFIX = "wallet-";

//     @Bean
//     SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//             .csrf(csrf -> csrf.disable())
//             .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//             .authorizeHttpRequests(auth -> auth
//                 .requestMatchers("/actuator/health/**").permitAll()
//                 .requestMatchers(HttpMethod.POST, "/accounts/**").hasRole("CUSTOMER")
//                 .requestMatchers(HttpMethod.GET, "/accounts/**").hasAnyRole("CUSTOMER", "SUPPORT", "ADMIN")
//                 .anyRequest().denyAll())
//             .oauth2ResourceServer(oauth -> oauth
//                 .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
//         return http.build();
//     }

//     @Bean
//     JwtAuthenticationConverter jwtAuthenticationConverter() {
//         JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
//         converter.setJwtGrantedAuthoritiesConverter(jwt -> {
//             var groups = jwt.getClaimAsStringList("groups");
//             if (groups == null) {
//                 return java.util.List.<GrantedAuthority>of();
//             }
//             return groups.stream()
//                 .filter(g -> g.startsWith(GROUP_PREFIX))
//                 .<GrantedAuthority>map(g -> new SimpleGrantedAuthority(
//                     "ROLE_" + g.substring(GROUP_PREFIX.length()).toUpperCase()))
//                 .toList();
//         });
//         return converter;
//     }
// }