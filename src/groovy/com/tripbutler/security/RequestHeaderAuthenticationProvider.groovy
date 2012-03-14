package com.tripbutler.security

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication
import org.springframework.security.authentication.AuthenticationProvider
import com.tripbutler.user.ApiAuthentication
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import com.tripbutler.user.User

class RequestHeaderAuthenticationProvider implements AuthenticationProvider {

	def authenticationCache
	def signedRequestHeaderUtil
	def userDetailsService

	Authentication authenticate(Authentication authentication) {
        if (!supports(authentication.class)) {
            return null;
        }
		
		RequestHeaderAuthenticationToken auth = authentication as RequestHeaderAuthenticationToken
		// check if request is outdated, possible a replay-attack
		def minDateTime = new Date()
		minDateTime.minutes -= 10
		if(auth.signedRequestHeader.date.after(minDateTime)) {
			// find api authentication from cache or db
			def apiAuth = authenticationCache.getApiAuthenticationFromCache( auth.accessKey )
			if(!apiAuth) {
				def users = User.findByAccessKey( auth.accessKey ).list()
				apiAuth = !users.empty ? users.first().apiAuthentication : null
			}
			if(apiAuth) {
				authenticationCache.putApiAuthenticationInCache(apiAuth)
				auth.signedRequestHeader.secretKey = apiAuth.secretKey
				def headerSignature = signedRequestHeaderUtil.createSignature( auth.signedRequestHeader )
				if(auth.signature == headerSignature) {
					def userDetails = userDetailsService.loadUserByUsername(auth.principal)
					auth.setDetails(userDetails)
					return authentication
				}
				else
					throw new BadCredentialsException('Bad Credentials - Signature does not match')
			}
			throw new BadCredentialsException('Bad Credentials - No Credentials found')
		}
		throw new BadCredentialsException('Bad Credentials - Request is not longer valid')
	}

	boolean supports(Class authentication) {
		return RequestHeaderAuthenticationToken.class.isAssignableFrom(authentication)
	}
}