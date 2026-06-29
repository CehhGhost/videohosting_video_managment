package cehhghost.videohosting.video_managment.configs;

import cehhghost.videohosting.video_managment.handlers.RestAccessDeniedHandler;
import cehhghost.videohosting.video_managment.handlers.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/internal/video-storage/videos/*/playback-url").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }
}
