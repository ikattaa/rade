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
package fr.aesn.rade.habilitations;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom UserDetailsService for Spring Security that uses AESN CAS server
 * to authenticate users, and queries the AESN Active Directory to recover
 * User Roles.
 *
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Slf4j
public class AesnCasUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

	public AesnCasUserDetailsService() {
		super();
	}

	@Override
	public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
		String login = token.getPrincipal().toString();
		String lowercaseLogin = login.toLowerCase();

		log.info("User authenticated: {}", login);
		List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
    	grantedAuthorities.add(new SimpleGrantedAuthority("RAD_ADMIN"));
    	grantedAuthorities.add(new SimpleGrantedAuthority("RAD_GESTION"));
    	grantedAuthorities.add(new SimpleGrantedAuthority("RAD_CONSULT"));
    
		return new User(lowercaseLogin,"not_used", grantedAuthorities);
	}
}