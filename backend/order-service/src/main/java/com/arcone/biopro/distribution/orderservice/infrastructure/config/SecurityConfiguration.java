package com.arcone.biopro.distribution.orderservice.infrastructure.config;

import com.arcone.biopro.distribution.orderservice.infrastructure.config.security.AuthoritiesConstants;
import com.arcone.biopro.distribution.orderservice.infrastructure.config.security.SecurityUtils;
import com.arcone.biopro.distribution.orderservice.infrastructure.config.security.oauth2.AudienceValidator;
import com.arcone.biopro.distribution.orderservice.infrastructure.config.security.oauth2.JwtGrantedAuthorityConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.PREFERRED_USERNAME;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final ApplicationProperties applicationProperties;

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;
    @Value("${application.security.disabled:true}")
    private boolean securityDisabled;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        if (securityDisabled) {
            return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
        }

        http
            .securityMatcher(
                new NegatedServerWebExchangeMatcher(
                    new OrServerWebExchangeMatcher(pathMatchers("/app/**", "/i18n/**", "/content/**", "/swagger-ui/**", "/management/health/**"))
                )
            )
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .headers(
                headers ->
                    headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(applicationProperties.getSecurity().getContentSecurityPolicy()))
                        .frameOptions(frameOptions -> frameOptions.mode(Mode.DENY))
                        .referrerPolicy(
                            referrer ->
                                referrer.policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                        .permissionsPolicy(
                            permissions ->
                                permissions.policy(
                                    "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"
                                )
                        )
            )
            .requestCache(cache -> cache.requestCache(NoOpServerRequestCache.getInstance()))
            .authorizeExchange(
                authz ->
                    // prettier-ignore
                    authz
                        .pathMatchers("/graphql").permitAll()
                        .pathMatchers("/v1/**").permitAll()
                        .pathMatchers("/api/authenticate").permitAll()
                        .pathMatchers("/api/auth-info").permitAll()
                        .pathMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                        .pathMatchers("/api/**").authenticated()
                        .pathMatchers("/v3/api-docs/**").hasAuthority(AuthoritiesConstants.ADMIN)
                        .pathMatchers("/management/health").permitAll()
                        .pathMatchers("/management/health/**").permitAll()
                        .pathMatchers("/management/info").permitAll()
                        .pathMatchers("/management/prometheus").permitAll()
                        .pathMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
            )
            .oauth2Client(withDefaults())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthorityConverter());
        jwtAuthenticationConverter.setPrincipalClaimName(PREFERRED_USERNAME);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    /**
     * Map authorities from "groups" or "roles" claim in ID Token.
     *
     * @return a {@link ReactiveOAuth2UserService} that has the groups from the IdP.
     */
    @Bean
    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();

        return userRequest -> {
            // Delegate to the default implementation for loading a user
            return delegate
                .loadUser(userRequest)
                .map(user -> {
                    Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

                    user
                        .getAuthorities()
                        .forEach(authority -> {
                            if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                                mappedAuthorities.addAll(
                                    SecurityUtils.extractAuthorityFromClaims(oidcUserAuthority.getUserInfo().getClaims())
                                );
                            }
                        });

                    return new DefaultOidcUser(mappedAuthorities, user.getIdToken(), user.getUserInfo(), PREFERRED_USERNAME);
                });
        };
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder() {
        NimbusReactiveJwtDecoder jwtDecoder = (NimbusReactiveJwtDecoder) ReactiveJwtDecoders.fromOidcIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(applicationProperties.getSecurity().getOauth2().getAudience());
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }
}
