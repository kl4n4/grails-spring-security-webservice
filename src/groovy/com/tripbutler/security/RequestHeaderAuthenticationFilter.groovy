package com.tripbutler.security

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.web.filter.GenericFilterBean

import org.springframework.security.authentication.InsufficientAuthenticationException

import org.springframework.security.authentication.AnonymousAuthenticationToken

class RequestHeaderAuthenticationFilter extends GenericFilterBean implements ApplicationEventPublisherAware {
	
    def authenticationManager
    def eventPublisher
    def rememberMeServices
    def springSecurityService
    def authenticationEntryPoint
	def signedRequestHeaderUtil

	def anonymousAuthenticationFilter

    AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler()
    AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler()

    void afterPropertiesSet() {
        assert authenticationManager != null, 'authenticationManager must be specified'
        assert rememberMeServices != null, 'rememberMeServices must be specified'
        assert authenticationEntryPoint != null, 'An AuthenticationEntryPoint is required'
        assert anonymousAuthenticationFilter != null, 'anonymousAuthenticationFilter must be specified'
    }

    void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req
        HttpServletResponse response = (HttpServletResponse) res

        //if (SecurityContextHolder.getContext().getAuthentication() == null && (request.getHeader('Authorization') || req.getParameter('auth') == 'anonymous')) {
        if (request.getHeader('Authorization') || req.getParameter('auth') == 'anonymous') {

			//logger.trace 'RequestHeaderAuthenticationFilter::attemptAuthentication => ' + request.getHeader('Authorization')

			def signedRequestHeader = signedRequestHeaderUtil.parseHeader(request)

			if ( signedRequestHeader?.accessKey == 'anonymous' || req.getParameter('auth') == 'anonymous' ) {
				anonymousAuthentication(request, response)
			}
			else if ( signedRequestHeader ) {
				Authentication auth
				RequestHeaderAuthenticationToken authenticationToken
                try {
                    authenticationToken = new RequestHeaderAuthenticationToken(signedRequestHeader)
					auth = authenticationManager.authenticate(authenticationToken)

					if (logger.isDebugEnabled())
						logger.debug("Authentication success: " + auth)

					onSuccessfulAuthentication(request, response, auth)
                } catch (AuthenticationException authenticationException) {
                    onUnsuccessfulAuthentication(request, response, authenticationException)
					return
                }
			}
			else {
				onUnsuccessfulAuthentication(request, response, new InsufficientAuthenticationException('No authorization for this operation granted.'))
				return
			}
        }
        chain.doFilter(req, res)
    }

	protected void anonymousAuthentication(HttpServletRequest request, HttpServletResponse response) {
		def authToken = new AnonymousAuthenticationToken(anonymousAuthenticationFilter.key, anonymousAuthenticationFilter.userAttribute.getPassword(), anonymousAuthenticationFilter.userAttribute.getAuthorities())
		Authentication anonymousAuth = authenticationManager.authenticate(authToken)
		logger.debug("Anonymous Authentication success: " + anonymousAuth)
		onSuccessfulAuthentication(request, response, anonymousAuth)
	}

	protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) {
        SecurityContextHolder.getContext().setAuthentication(authResult)
        rememberMeServices.onLoginSuccess(request, response, authResult)
    }

    protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
		logger.warn("Authentication failed: " + failed.message)
        SecurityContextHolder.clearContext();
        rememberMeServices.loginFail(request, response)
		authenticationEntryPoint.commence(request, response, failed)
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher
    }
}
