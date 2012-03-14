package com.tripbutler.security

import org.codehaus.groovy.grails.plugins.springsecurity.GormUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.GrantedAuthority
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class RequestHeaderAuthenticationUserDetailsService extends GormUserDetailsService {
	
	@Override
	protected UserDetails createUserDetails(Object user, Collection<GrantedAuthority> authorities) {

		def conf = SpringSecurityUtils.securityConfig

		String usernamePropertyName = conf.userLookup.usernamePropertyName
		String passwordPropertyName = conf.userLookup.passwordPropertyName
		String enabledPropertyName = conf.userLookup.enabledPropertyName
		String accountExpiredPropertyName = conf.userLookup.accountExpiredPropertyName
		String accountLockedPropertyName = conf.userLookup.accountLockedPropertyName
		String passwordExpiredPropertyName = conf.userLookup.passwordExpiredPropertyName

		String username = user."$usernamePropertyName"
		String password = user."$passwordPropertyName"
		boolean enabled = enabledPropertyName ? user."$enabledPropertyName" : true
		boolean accountExpired = accountExpiredPropertyName ? user."$accountExpiredPropertyName" : false
		boolean accountLocked = accountLockedPropertyName ? user."$accountLockedPropertyName" : false
		boolean passwordExpired = passwordExpiredPropertyName ? user."$passwordExpiredPropertyName" : false

		return new RequestHeaderAuthenticationUserDetails(username, password, user.apiAuthentication,
			enabled, !accountExpired, !passwordExpired, !accountLocked, authorities, user)
	}
}
