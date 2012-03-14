package com.tripbutler.security.filter;

import com.tripbutler.security.signing.HmacSha1OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class SignedResponseWrapper extends HttpServletResponseWrapper {

    private final HmacSha1OutputStream output;
    private final PrintWriter writer;

    public SignedResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
        output = new HmacSha1OutputStream(response.getOutputStream());
        writer = new PrintWriter(output, true);
    }

    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return output;
    }

    public String getSignature(String signDate) {
        return output.getSignature(signDate);
    }

}