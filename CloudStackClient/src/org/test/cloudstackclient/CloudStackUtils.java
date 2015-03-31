/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.test.cloudstackclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Administrator
 */
public class CloudStackUtils {

    public static void main(String[] args) throws Throwable {
        CloudStack cloudStack = new CloudStack("http://192.168.31.100:8080/client/api?", "admin", "password");
       

    }

    public static String printObject(Object obj) {
        Field[] fs = obj.getClass().getFields();
        StringBuffer buffer = new StringBuffer();
        for (Field f : fs) {
            try {
                buffer.append(" " + f.getName() + "=" + f.get(obj).toString() + "\n");
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(CloudStackUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(CloudStackUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return buffer.toString();
    }

    public static <T> T parseNode(Node node, Class<T> t) {
        try {
            HashMap<String, String> values = new HashMap<String, String>();
            NodeList nl = node.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node item = nl.item(i);
                values.put(item.getNodeName(), item.getChildNodes().item(0).getNodeValue());
            }
            T obj = t.newInstance();
            Field[] fs = t.getFields();
            for (Field f : fs) {
                f.set(obj, values.get(f.getName()));
            }
            return obj;
        } catch (InstantiationException ex) {
            Logger.getLogger(CloudStackUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CloudStackUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static KeyStore getCloudStackApiKey(String url, String name, String password) throws UnsupportedEncodingException, MalformedURLException, IOException, ParserConfigurationException, SAXException {
        String urlPath = url + "command=login&response=xml&" + "username=" + URLEncoder.encode(name, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
        URL u = new URL(urlPath);
        HttpURLConnection connect = (HttpURLConnection) u.openConnection();
        connect.connect();
        CookieManager cookieManager = new CookieManager();
        cookieManager.storeCookies(connect);
        InputStream inputStream = (InputStream) connect.getContent();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
        doc.getDocumentElement().normalize();
        String sessionkey = doc.getElementsByTagName("sessionkey").item(0).getChildNodes().item(0).getNodeValue();
        String userid = doc.getElementsByTagName("userid").item(0).getChildNodes().item(0).getNodeValue();
        urlPath = url + "command=registerUserKeys&response=xml&" + "sessionkey=" + URLEncoder.encode(sessionkey, "UTF-8") + "&id=" + userid;
        u = new URL(urlPath);
        connect = (HttpURLConnection) u.openConnection();
        cookieManager.setCookies(connect);
        connect.connect();
        inputStream = (InputStream) connect.getContent();
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
        doc.getDocumentElement().normalize();
        String apikey = doc.getElementsByTagName("apikey").item(0).getChildNodes().item(0).getNodeValue();
        String secretkey = doc.getElementsByTagName("secretkey").item(0).getChildNodes().item(0).getNodeValue();
        return new KeyStore(apikey, secretkey);
    }

    static class CookieManager {

        private Map store;

        private static final String SET_COOKIE = "Set-Cookie";
        private static final String COOKIE_VALUE_DELIMITER = ";";
        private static final String PATH = "path";
        private static final String EXPIRES = "expires";
        private static final String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";
        private static final String SET_COOKIE_SEPARATOR = "; ";
        private static final String COOKIE = "Cookie";

        private static final char NAME_VALUE_SEPARATOR = '=';
        private static final char DOT = '.';

        private DateFormat dateFormat;

        public CookieManager() {

            store = new HashMap();
            dateFormat = new SimpleDateFormat(DATE_FORMAT);
        }

        /**
         * Retrieves and stores cookies returned by the host on the other side
         * of the the open java.net.URLConnection.
         *
         * The connection MUST have been opened using the connect() method or a
         * IOException will be thrown.
         *
         * @param conn a java.net.URLConnection - must be open, or IOException
         * will be thrown
         * @throws java.io.IOException Thrown if conn is not open.
         */
        public void storeCookies(URLConnection conn) throws IOException {

            // let's determine the domain from where these cookies are being sent
            String domain = getDomainFromHost(conn.getURL().getHost());

            Map domainStore; // this is where we will store cookies for this domain

            // now let's check the store to see if we have an entry for this domain
            if (store.containsKey(domain)) {
                // we do, so lets retrieve it from the store
                domainStore = (Map) store.get(domain);
            } else {
                // we don't, so let's create it and put it in the store
                domainStore = new HashMap();
                store.put(domain, domainStore);
            }

            // OK, now we are ready to get the cookies out of the URLConnection
            String headerName = null;
            for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
                if (headerName.equalsIgnoreCase(SET_COOKIE)) {
                    Map cookie = new HashMap();
                    StringTokenizer st = new StringTokenizer(conn.getHeaderField(i), COOKIE_VALUE_DELIMITER);

                    // the specification dictates that the first name/value pair
                    // in the string is the cookie name and value, so let's handle
                    // them as a special case: 
                    if (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        String name = token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR));
                        String value = token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
                        domainStore.put(name, cookie);
                        cookie.put(name, value);
                    }

                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        cookie.put(token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR)).toLowerCase(),
                                token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length()));
                    }
                }
            }
        }

        /**
         * Prior to opening a URLConnection, calling this method will set all
         * unexpired cookies that match the path or subpaths for thi underlying
         * URL
         *
         * The connection MUST NOT have been opened method or an IOException
         * will be thrown.
         *
         * @param conn a java.net.URLConnection - must NOT be open, or
         * IOException will be thrown
         * @throws java.io.IOException Thrown if conn has already been opened.
         */
        public void setCookies(URLConnection conn) throws IOException {

            // let's determine the domain and path to retrieve the appropriate cookies
            URL url = conn.getURL();
            String domain = getDomainFromHost(url.getHost());
            String path = url.getPath();

            Map domainStore = (Map) store.get(domain);
            if (domainStore == null) {
                return;
            }
            StringBuffer cookieStringBuffer = new StringBuffer();

            Iterator cookieNames = domainStore.keySet().iterator();
            while (cookieNames.hasNext()) {
                String cookieName = (String) cookieNames.next();
                Map cookie = (Map) domainStore.get(cookieName);
                // check cookie to ensure path matches  and cookie is not expired
                // if all is cool, add cookie to header string 
                if (comparePaths((String) cookie.get(PATH), path) && isNotExpired((String) cookie.get(EXPIRES))) {
                    cookieStringBuffer.append(cookieName);
                    cookieStringBuffer.append("=");
                    cookieStringBuffer.append((String) cookie.get(cookieName));
                    if (cookieNames.hasNext()) {
                        cookieStringBuffer.append(SET_COOKIE_SEPARATOR);
                    }
                }
            }
            try {
                conn.setRequestProperty(COOKIE, cookieStringBuffer.toString());
            } catch (java.lang.IllegalStateException ise) {
                IOException ioe = new IOException("Illegal State! Cookies cannot be set on a URLConnection that is already connected. "
                        + "Only call setCookies(java.net.URLConnection) AFTER calling java.net.URLConnection.connect().");
                throw ioe;
            }
        }

        private String getDomainFromHost(String host) {
            if (host.indexOf(DOT) != host.lastIndexOf(DOT)) {
                return host.substring(host.indexOf(DOT) + 1);
            } else {
                return host;
            }
        }

        private boolean isNotExpired(String cookieExpires) {
            if (cookieExpires == null) {
                return true;
            }
            Date now = new Date();
            try {
                return (now.compareTo(dateFormat.parse(cookieExpires))) <= 0;
            } catch (java.text.ParseException pe) {
                pe.printStackTrace();
                return false;
            }
        }

        private boolean comparePaths(String cookiePath, String targetPath) {
            if (cookiePath == null) {
                return true;
            } else if (cookiePath.equals("/")) {
                return true;
            } else if (targetPath.regionMatches(0, cookiePath, 0, cookiePath.length())) {
                return true;
            } else {
                return false;
            }

        }

        /**
         * Returns a string representation of stored cookies organized by
         * domain.
         */
        public String toString() {
            return store.toString();
        }
    }

    public static class KeyStore {

        public String apiKey, secretkey;

        public KeyStore(String apiKey, String secretkey) {
            this.apiKey = apiKey;
            this.secretkey = secretkey;
        }
    }
}
