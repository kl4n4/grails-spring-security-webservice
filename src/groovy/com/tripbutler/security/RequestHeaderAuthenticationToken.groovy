package com.tripbutler.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import com.tripbutler.security.signing.SignedRequestHeader

class RequestHeaderAuthenticationToken extends AbstractAuthenticationToken {

	private SignedRequestHeader signedRequestHeader

	def RequestHeaderAuthenticationToken(SignedRequestHeader signedRequestHeader) {
		super(null)
		this.signedRequestHeader = signedRequestHeader
		setAuthenticated(true)
	}

	SignedRequestHeader getSignedRequestHeader() {
		signedRequestHeader
	}

	String getAccessKey() {
		signedRequestHeader.accessKey
	}

	String getSignature() {
		signedRequestHeader.signature
	}

	Object getPrincipal() {
		return signedRequestHeader.accessKey
	}

	Object getCredentials() {
		return signedRequestHeader.signature
	}

	@Override
	String toString() {
		accessKey + ' : ' + signedRequestHeader.signature
	}


}
