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

import java.rmi.RemoteException;
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

import fr.aesn.rade.habilitations.ws.HabilitationsUtilisateurSrv;
import fr.aesn.rade.habilitations.ws.RoleBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;

import lombok.Setter;
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
	@Autowired
	 HabilitationsUtilisateurSrv habilitationsService;
	public AesnCasUserDetailsService() {
		super();
	}

	@Override
	public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
		String login = token.getPrincipal().toString();
		String lowercaseLogin = login.toLowerCase();
		log.info("User authenticated lowercaseLogin: {}", lowercaseLogin);		
	    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
	    RoleBean[] roles;
	    
	    try {
	    	roles  = habilitationsService.getRolesDunPerimetre(lowercaseLogin, null);
	    	for(RoleBean role :roles) {
	    		String  code = role.getCode();
	    		if (code != null && code.startsWith("RAD_")) {
	    			log.info("Role found for user {}: {}", login, code);
	    			grantedAuthorities.add(new SimpleGrantedAuthority(code));
	    		} 
	    	}
	    }catch(RemoteException e) {
	        log.debug("Unable to list user roles for {}", login, e);
	        throw new AuthenticationServiceException("Unable to list user roles for "+ login, e);
	    }
	    
		return new User(lowercaseLogin,"not_used", grantedAuthorities);
	}
}