package pavlik.pokladna.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
public class WebSecurityConfig {


    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(configurer ->
                        configurer
                                .requestMatchers("/js/**", "/css/**").permitAll()
                                .requestMatchers(
                                        "/",
                                        "/sales/index/**", "/sales/show/**", "/sales/create/**",
                                        "/transactions/index/**", "/transactions/show/**", "/transactions/currentBalance"
                                ).hasAnyRole("EMPLOYEE")
                                .requestMatchers(
                                        "/admin",
                                        "/sales/delete/**", "/sales/deleteAll/**",
                                        "/transactions/deposit", "/transactions/withdraw", "/transactions/delete/**", "/transactions/deleteAll/**",
                                        "users/**"
                                ).hasAnyRole("ADMIN")

                                .anyRequest().authenticated()
                )
                .formLogin(form ->
                        form
                                .loginPage("/authentication/login")
                                .loginProcessingUrl("/authenticateTheUser")
                                .permitAll()
                )
                .logout(logout -> logout.permitAll()
                )
                .exceptionHandling(configurer ->
                        configurer.accessDeniedPage("/authentication/accesDenied")
                );


        return http.build();
    }

}
