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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Spring Security configuration
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig
  extends WebSecurityConfigurerAdapter {
//  @Autowired
//  private AccessDeniedHandler accessDeniedHandler;

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
        .formLogin()
          .loginPage("/login")
          .defaultSuccessUrl("/")
          .permitAll()
          .and()
        .logout()
          .permitAll();
//          .and()
//        .exceptionHandling().accessDeniedHandler(accessDeniedHandler);
  }
}
