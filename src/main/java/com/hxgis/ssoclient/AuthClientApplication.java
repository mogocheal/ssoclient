package com.hxgis.ssoclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@EnableAutoConfiguration
@Configuration
@EnableOAuth2Sso
@RestController
public class AuthClientApplication extends WebSecurityConfigurerAdapter {

    @Autowired
    private OAuth2RestTemplate template;

    @RequestMapping("/test")
    public String test() throws URISyntaxException {
        return "hello";
    }

    @RequestMapping("/hello")
    public @ResponseBody ResponseEntity<HashMap> hello() throws URISyntaxException {
        return template.getForEntity(new URI("http://localhost:8000/api/station/greeting"), HashMap.class);
    }

    /*@RequestMapping("/")
    public String home(Principal user) {
        return "Hello " + user.getName();
    }*/

    @RequestMapping({"/user", "/me"})
    public Map<String, String> user(Principal principal) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name", principal.getName());
        return map;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**","/img/**","/js/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
             http
                .antMatcher("/**").authorizeRequests()
                .antMatchers("/", "/login**", "/webjars/**").permitAll()
                .anyRequest().authenticated()
                .and().logout().logoutSuccessUrl("/").permitAll().and().csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        // @formatter:on
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthClientApplication.class, args);
    }

    protected static class Stuff {
        @Autowired
        OAuth2ClientContext oauth2ClientContext;

        @Bean
        @ConfigurationProperties("security.oauth2.client")
        public AuthorizationCodeResourceDetails resource() {
            return new AuthorizationCodeResourceDetails();
        }

        @Bean
        public OAuth2RestTemplate umeteoRestTemplate() {
            OAuth2RestTemplate template = new OAuth2RestTemplate(resource(), oauth2ClientContext);
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
            converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON,
                    MediaType.valueOf("text/javascript")));
            template.setMessageConverters(Arrays.<HttpMessageConverter<?>>asList(converter));
            return template;
        }
    }
}
