/*  This file is part of the Rade project (https://github.com/mgimpel/rade).
 *  Copyright (C) 2018 Marc Gimpel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/* $Id$ */
package fr.aesn.rade.webapp.config;

import java.util.Arrays;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;

import fr.aesn.rade.habilitations.AesnCasUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;

/**
 * Spring Security configuration
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SpringSecurityConfig
extends WebSecurityConfigurerAdapter {
	private static final String CAS_ACTIVE = "cas.active";
	private static final String CAS_URL_LOGIN = "cas.service.login";
	private static final String CAS_URL_LOGOUT = "cas.service.logout";
	private static final String CAS_URL_PREFIX = "cas.url.prefix";
	private static final String CAS_SERVICE_URL = "app.service.security";
	private static final String APP_SERVICE_HOME = "app.service.home";

	@Inject
	private Environment env;

	/**
	 * Authentication Provider defined in Application Context configuration file.
	 */
	@Autowired
	private AuthenticationProvider authenticationProvider;

	/**
	 * Configure Security Access
	 * @param http Spring HttpSecurity.
	 */
	@Override
	protected void configure(final HttpSecurity http)
			throws Exception {

		http.csrf().disable()
		.authorizeRequests()
		// SOAP & REST WebServices (CXF) : no restrictions
		.antMatchers("/services/**").permitAll()
		// Actuators (application information pages)
		// - health & info Actuators : no restrictions
		// - all other Actuators (logfile and metrics) : authenticated user
		.antMatchers("/actuator/health", "/actuator/info").permitAll()
		// Static resources (CSS, Javascript, images, ...) : no restrictions
		.antMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
		// WebJars : no restrictions
		.antMatchers("/webjars/**").permitAll()
		// Search queries open to all
		.antMatchers("/referentiel/**").permitAll()
		// Admin files : require administrator role
		.antMatchers("/admin/**", "/batch/**").hasAuthority("RAD_ADMIN")
		// User files : require any role
		.antMatchers("/user/**").hasAnyAuthority("RAD_CONSULT", "RAD_GESTION", "RAD_ADMIN")
		// All other files : authenticated user
		.anyRequest().authenticated()
		.and()
		.logout()
		.permitAll();

		if(env.getRequiredProperty(CAS_ACTIVE)!=null && "true".compareTo(env.getRequiredProperty(CAS_ACTIVE)) == 0){
			http.httpBasic()
			.authenticationEntryPoint(casAuthenticationEntryPoint()).and().addFilter(casAuthenticationFilter())
			.addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class)
			.addFilterBefore(requestCasGlobalLogoutFilter(), LogoutFilter.class);
			http.logout().logoutUrl("/logout").logoutSuccessUrl("/").invalidateHttpSession(true)
			.deleteCookies("JSESSIONID");
		}else{
			http.formLogin()
			.loginPage("/login")
			.defaultSuccessUrl("/")
			.permitAll();
		}
	}

	@Bean
	public ServiceProperties serviceProperties() {
		ServiceProperties sp = new ServiceProperties();
		sp.setService(env.getRequiredProperty(CAS_SERVICE_URL));
		sp.setSendRenew(false);
		return sp;
	}

	@Bean
	public CasAuthenticationProvider casAuthenticationProvider() {
		CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
		casAuthenticationProvider.setAuthenticationUserDetailsService(customUserDetailsService());
		casAuthenticationProvider.setServiceProperties(serviceProperties());
		casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
		casAuthenticationProvider.setKey("an_id_for_this_auth_provider_only");
		return casAuthenticationProvider;
	}

	@Bean
	public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> customUserDetailsService() {
		return new AesnCasUserDetailsService();
	}

	@Bean
	public SessionAuthenticationStrategy sessionStrategy() {
		SessionAuthenticationStrategy sessionStrategy = new SessionFixationProtectionStrategy();
		return sessionStrategy;
	}

	@Bean
	public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
		return new Cas20ServiceTicketValidator(env.getRequiredProperty(CAS_URL_PREFIX));
	}

	@Bean
	public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setAuthenticationManager(authenticationManager());
		casAuthenticationFilter.setSessionAuthenticationStrategy(sessionStrategy());
		return casAuthenticationFilter;
	}

	@Bean
	public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
		CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
		casAuthenticationEntryPoint.setLoginUrl(env.getRequiredProperty(CAS_URL_LOGIN));
		casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
		return casAuthenticationEntryPoint;
	}

	@Bean
	public SingleSignOutFilter singleSignOutFilter() {
		SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
		singleSignOutFilter.setIgnoreInitConfiguration(true);
		singleSignOutFilter.setCasServerUrlPrefix(env.getRequiredProperty(CAS_URL_PREFIX));
		return singleSignOutFilter;
	}

	@Bean
	public LogoutFilter requestCasGlobalLogoutFilter() {
		LogoutFilter logoutFilter = new LogoutFilter(env.getRequiredProperty(CAS_URL_LOGOUT) + "?service="
				+ env.getRequiredProperty(APP_SERVICE_HOME), new SecurityContextLogoutHandler());
		logoutFilter.setLogoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"));
		return logoutFilter;
	}

	@Inject
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		if(env.getRequiredProperty(CAS_ACTIVE)!=null && "true".compareTo(env.getRequiredProperty(CAS_ACTIVE)) == 0){
			auth.authenticationProvider(casAuthenticationProvider());
		}else{
			auth.authenticationProvider(authenticationProvider);
		}
	}

}
