package org.hango.cloud.common.infra.base.wrapper;

import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * 包装Request
 *
 * @date 2019/11/19 下午2:59.
 */
public class MultiReadRequestWrapper extends HttpServletRequestWrapper {

    private volatile byte[] cachedBytes;

    private HttpServletRequest originalRequest;

    public MultiReadRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        originalRequest = request;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (null == cachedBytes) {
            cachedBytes = cacheInputStreamContent();
        }

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBytes);
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new RuntimeException("Not implemented");
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (null == cachedBytes) {
            cachedBytes = cacheInputStreamContent();
        }
        return new BufferedReader(new InputStreamReader(this.getInputStream(), Charset.defaultCharset()));
    }

    private byte[] cacheInputStreamContent() throws IOException {
        InputStream inputStream = originalRequest.getInputStream();
        return StreamUtils.copyToByteArray(inputStream);
    }

}
