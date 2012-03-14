package com.tripbutler.security.filter;

import org.apache.commons.httpclient.util.DateUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResponseSigningFilter implements Filter {

	static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZ", Locale.US);

	public void init(FilterConfig filterConfig) throws ServletException {

	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		String authentication = ((HttpServletRequest)servletRequest).getHeader("Authorization");
		if(authentication != null && authentication.startsWith("TB ")) {
			SignedResponseWrapper response = new SignedResponseWrapper((HttpServletResponse) servletResponse);
			//System.out.println("ResponseSigningFilter::doFilter");
			filterChain.doFilter(servletRequest, response);
			String signDate = dateFormat.format( new Date() );
			response.addHeader("X-Tb-Date", signDate);
			response.addHeader("X-Tb-Signature", response.getSignature(signDate));
		}
		else if(filterChain != null) {
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}

	public void destroy() {

	}
}
