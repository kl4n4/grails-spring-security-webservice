package com.tripbutler.security.signing;

import com.tripbutler.security.RequestHeaderAuthenticationUserDetails;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.Date;

public class HmacSha1OutputStream extends ServletOutputStream {

    /** The default encoding to use when URL encoding */
    private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private final ServletOutputStream output;
	private final Mac mac;
	private final StringBuffer buffer = new StringBuffer();

    {
		//System.out.println("HmacSha1OutputStream: static");
        try {
            mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public HmacSha1OutputStream(ServletOutputStream output) {
		//System.out.println("HmacSha1OutputStream: construct");
        this.output = output;
    }

    public void write(int i) throws IOException {
        byte[] b = { (byte) i };
		String character = new String(b);
		//System.out.println("HmacSha1OutputStream: " + character);
		buffer.append(character);
        output.write(b, 0, 1);
    }

    public String getSignature(String signDate) {
		//System.out.println("HmacSha1OutputStream::getSignature");
		RequestHeaderAuthenticationUserDetails userDetails = (RequestHeaderAuthenticationUserDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
		String key = userDetails.getApiAuthentication().getSecretKey();
        try {
			mac.init(new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM));
			//System.out.println("HmacSha1OutputStream::getSignature - body = " + buffer.toString());
			String stringToSign = signDate + "\n" +
				buffer.toString();
			//System.out.println("HmacSha1OutputStream::getSignature - stringToSign = " + stringToSign);
			byte[] signature = Base64.encodeBase64( mac.doFinal(stringToSign.getBytes(DEFAULT_ENCODING)) );
			return new String(signature);
        } catch (Exception e) {
			System.out.println("HmacSha1OutputStream::getSignature -  EXCEPTION");
        }
		return null;
    }

}