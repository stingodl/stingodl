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

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbcAuth {

    static final Logger LOGGER = Logger.getLogger(AbcAuth.class.getName());

    long exp = 0L;
    String server;
    String token = "";
    DocumentBuilder builder;

    public AbcAuth() {
        try {
            builder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"XML Builder failed", e);
        }
    }

    /**
     * Fixes server and appends token.
     * @param uriStr the uri to be fixed
     * @return fixed url
     */
    public String fixUrl(String uriStr) {
        if (System.currentTimeMillis() > exp) {
            acquireToken();
        }
        URI uri = null;
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException se) {
            LOGGER.log(Level.SEVERE, "ABC URI Invalid: " + uriStr, se);
        }
        String fixed = uriStr;
        if (uri != null) {
            if ("http".equals(uri.getScheme())) {
                fixed = fixed.replace("http","https");
            }
            fixed = fixed.replace(uri.getHost(), server);
            fixed = fixed + "?hdnea=" + token;
        }
        return fixed;
    }

    /**
     * Appends token only.
     * @param uriStr the url to be appended to
     * @return appended url
     */
    public String appendTokenOnly(String uriStr) {
        if (System.currentTimeMillis() > exp) {
            acquireToken();
        }
        return uriStr + "&hdnea=" + token;
    }

    private synchronized void acquireToken() {
        Document doc = null;
        try {
            doc = builder.parse("https://iview.abc.net.au/auth");
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE,"Unable to connect to ABC auth", ioe);
        } catch (SAXException saxe) {
            LOGGER.log(Level.SEVERE,"Invalid SAX for ABC auth", saxe);
        }
        if (doc != null) {
            NodeList list = doc.getElementsByTagName("server");
            if (list.getLength() == 1) {
                URI tokenUri = null;
                try {
                    tokenUri = new URI(list.item(0).getTextContent());
                } catch (URISyntaxException se) {
                    LOGGER.log(Level.SEVERE, "ABC URI Invalid: " + list.item(0).getTextContent(), se);
                }
                server = tokenUri.getHost();
                list = doc.getElementsByTagName("tokenhd");
                if (list.getLength() == 1) {
                    token = list.item(0).getTextContent();
                    LOGGER.fine("Server: " + server + " Token: " + token);
                    int stStart = token.indexOf("st=") + 3;
                    int stEnd = token.indexOf('~', stStart);
                    int expStart = token.indexOf("exp=") + 4;
                    int expEnd = token.indexOf('~', expStart);
                    long abcSt = Long.parseLong(token.substring(stStart, stEnd)) * 1000;
                    long abcExp = Long.parseLong(token.substring(expStart, expEnd)) * 1000;
                    // adjust for our clock and reduce by 20 sec for safety (duration usually 10000 sec)
                    exp = abcExp - abcSt + System.currentTimeMillis() - 20000;
                } else {
                    LOGGER.log(Level.SEVERE,"Invalid ABC auth response (token) " + list);
                }
            } else {
                LOGGER.log(Level.SEVERE,"Invalid ABC auth response (server) " + list);
            }
        }
    }
}
