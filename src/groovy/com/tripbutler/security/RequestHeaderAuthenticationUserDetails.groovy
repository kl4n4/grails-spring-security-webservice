package com.tripbutler.security

import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import org.springframework.security.core.GrantedAuthority
import com.tripbutler.user.ApiAuthentication
import com.tripbutler.user.User

class RequestHeaderAuthenticationUserDetails extends GrailsUser {

	private final ApiAuthentication apiAuthentication
	private final User domainModel

	RequestHeaderAuthenticationUserDetails(String username, String password, ApiAuthentication apiAuthentication,
										   boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
										   Collection<GrantedAuthority> authorities, User user) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, user.id)
		this.apiAuthentication = apiAuthentication
		this.domainModel = user
	}

	ApiAuthentication getApiAuthentication() {
		apiAuthentication
	}

	User getDomainModel() {
		domainModel
	}
}
