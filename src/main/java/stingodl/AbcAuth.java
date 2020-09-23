/*******************************************************************************
 * Copyright (c) 2020 StingoDL.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package stingodl;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.LineNumberReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbcAuth {

    static final Logger LOGGER = Logger.getLogger(AbcAuth.class.getName());
    static final String HMAC_SHA256 = "HmacSHA256";
    static final byte[] HMAC_KEY = "android.content.res.Resources".getBytes(StandardCharsets.UTF_8);
    static final String HEXES = "0123456789abcdef";
    String token = "";

    public AbcAuth(String programId, HttpInput httpInput) {
        Mac mac;
        StringBuilder s = new StringBuilder("/auth/hls/sign?ts=");
        s.append(Long.toString((System.currentTimeMillis() / 1000)));
        s.append("&hn=");
        s.append(programId);
        s.append("&d=android-tablet");
        String path = s.toString();
        try {
            mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(HMAC_KEY, HMAC_SHA256);
            mac.init(keySpec);
            String hmac = bytesToHex(mac.doFinal(path.getBytes(StandardCharsets.UTF_8)));
            URI uri = new URI("https://iview.abc.net.au" + path + "&sig=" + hmac);
            LOGGER.fine("Token URI: " + uri.toString());
            LineNumberReader reader = new LineNumberReader(httpInput.getReader(uri));
            token = reader.readLine();
            LOGGER.fine("Token: " + token);
            reader.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "ABC Auth failed to initialize", e);
        }
    }
    public String bytesToHex( byte [] raw ) {
        final StringBuilder hex = new StringBuilder( 2 * raw.length );
        for ( final byte b : raw ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    /**
     * Sets token as URI query.
     * @param uriStr the uri to be amended
     * @return URI with query
     */
    public String setQueryToken(String uriStr) {
        return uriStr + "?hdnea=" + token;
    }

    /**
     * Appends token to existing query.
     * @param uriStr the url to be appended to
     * @return appended url
     */
    public String appendToken(String uriStr) {
        return uriStr + "&hdnea=" + token;
    }
}
