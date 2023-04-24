package com.project.alfa.common.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    
    private final AuthenticationProvider       authenticationProvider;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final UserDetailsService           userDetailsService;
    private final DataSource                   dataSource;
    private final OAuth2UserService            oAuth2UserService;
    
    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }
    
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/img/**", "/js/**", "/vendor/**");
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
            .disable()
            .authorizeRequests()
            .antMatchers("/login", "/register", "/forgot-password", "/confirm-email")
            .permitAll()
            .antMatchers("/posts/write?notice=true")
            .access("hasRole('ROLE_ADMIN')")
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic()
            .and()
            .formLogin()
            .loginPage("/login")
            .loginProcessingUrl("/login-proc")
            .defaultSuccessUrl("/", true)
            .failureHandler(authenticationFailureHandler)
            .and()
            .rememberMe()
            .rememberMeParameter("remember-me")
            .tokenValiditySeconds(86400 * 14)
            .alwaysRemember(false)
            .userDetailsService(userDetailsService)
            .tokenRepository(tokenRepository())
            .and()
            .logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID", "remember-me")
            .and()
            .oauth2Login()
            .loginPage("/login")
            .userInfoEndpoint()
            .userService(oAuth2UserService);
    }
    
}
