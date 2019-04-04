package org.monkeynuthead.webfluxsample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@EnableWebFluxSecurity
class SecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Bean
    SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http,
                                                  final AuthenticationWebFilter authenticationWebFilter) {
        return http.authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    ReactiveAuthenticationManager authenticationManager() {
        return new AuthenticationManager();
    }

    @Bean
    ServerSecurityContextRepository serverSecurityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    AuthenticationWebFilter authenticationWebFilter(final ReactiveAuthenticationManager authenticationManager,
                                                    final ServerSecurityContextRepository serverSecurityContextRepository) {
        final AuthenticationWebFilter filter = new AuthenticationWebFilter(authenticationManager);
        filter.setServerAuthenticationConverter(new AuthenticationConvertor(serverSecurityContextRepository));
        filter.setSecurityContextRepository(serverSecurityContextRepository);
        return filter;
    }

    private static final class AuthenticationManager implements ReactiveAuthenticationManager {

        @Override
        public Mono<Authentication> authenticate(final Authentication authentication) {
            log.info("Authentication: {}", authentication);

            //Use the given authentication, if it has been authenticated, otherwise check it (and load again)
            return Mono.just(authentication)
                    .filter(Authentication::isAuthenticated)
                    .switchIfEmpty(Mono.defer(() -> checkAuthentication(authentication)));
        }

        private Mono<Authentication> checkAuthentication(final Authentication authentication) {
            log.info("Call Authentication Service: {}", authentication);
            return Mono.just(new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    authentication.getCredentials(),
                    authorities("ROLE_ADMIN")));
        }

        private Collection<? extends GrantedAuthority> authorities(final String... authorities) {
            return Arrays.stream(authorities)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

    }

    private static final class AuthenticationConvertor implements ServerAuthenticationConverter {

        private final ServerSecurityContextRepository serverSecurityContextRepository;

        private AuthenticationConvertor(ServerSecurityContextRepository serverSecurityContextRepository) {
            this.serverSecurityContextRepository = serverSecurityContextRepository;
        }

        @Override
        public Mono<Authentication> convert(final ServerWebExchange exchange) {
            //Use the context if there is already one in the session - otherwise create a new one
            return serverSecurityContextRepository.load(exchange)
                    .map(SecurityContext::getAuthentication)
                    .switchIfEmpty(Mono.defer(() -> constructAuthentication(exchange)));

        }

        private Mono<Authentication> constructAuthentication(final ServerWebExchange exchange) {
            final Object principal = exchange.getRequest().getHeaders().getFirst("x-user");
            final Object credential = exchange.getRequest().getHeaders().getFirst("x-token");
            log.info("Construct Authentication: {}, {}", principal, credential);
            if (principal == null || credential == null) {
                return Mono.empty();
            }
            return Mono.just(new UsernamePasswordAuthenticationToken(principal, credential));
        }

    }

}
