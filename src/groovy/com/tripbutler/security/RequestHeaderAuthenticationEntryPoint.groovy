package com.tripbutler.security

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import grails.converters.XML
import grails.converters.JSON

class RequestHeaderAuthenticationEntryPoint implements AuthenticationEntryPoint {
	
    private static final Log log = LogFactory.getLog(RequestHeaderAuthenticationEntryPoint.class)

	void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        HttpServletResponse httpResponse = (HttpServletResponse) response
		def result = [
		    error: [
				code: HttpServletResponse.SC_UNAUTHORIZED,
				message: authException.message
			]
		]
		log.info('Unauthorized: ' + result.error)

		def contentType = 'application/json'
		if(request.contentType == 'text/xml' || request.contentType == 'application/xml' || request.getParameter('format') == 'xml') {
			contentType = request.contentType
			result = result as XML
		}
		else {
			result = result as JSON
		}
		httpResponse.contentType = contentType
		httpResponse.writer.print result.toString()
		httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
	}
}
