/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.test.cloudstackclient;

import static org.test.cloudstackclient.CloudStackUtils.parseNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * query_string
 * :apiKey=2zqJCi57S2pNTu_GP8k51LEgz70ewmPuffqtRLFmtnVAXal_sh1zqiFq3ioJrfsMdDtDgs1-7LHK8jBS2Oabjw&command=listZones
 * S86sMgTuUh9Z7h5XOMnc/miJMEY=
 *
 * @author Administrator
 */
public class CloudStack {

    private CloudStackUtils.KeyStore keyStore = new CloudStackUtils.KeyStore(
            "qEf_jCXmDDep7LSKLJqdJ2tGMbN0TevfuvikQnPjmp1FnBOAKn0Z4PNGiVqEZ86P7adJMk46YU0Y179x57n4AA",
            "CQdFkYMPguw9zgoG9MNASJX93KirBByBNiFvOEF12fnBMbdYk34s5pWW6u1CPTy-UKGPAR1Hwps4GUK6FV90cg");
    private String url = "http://192.168.1.151:8080/client/api/?";

    public CloudStack(String url, String name, String password) {
        this.url = url;
        try {
            this.keyStore = CloudStackUtils.getCloudStackApiKey(url, name, password);
        } catch (MalformedURLException ex) {
            Logger.getLogger(CloudStack.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CloudStack.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CloudStack.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(CloudStack.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public CloudStack(CloudStackUtils.KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public CloudStack() {
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, IOException, ParserConfigurationException, SAXException {
        CloudStack cloudStack = new CloudStack("http://192.168.31.100:8080/client/api?", "admin", "password");
        List<Zone> zones = cloudStack.listZones(null);
        System.out.println(CloudStackUtils.printObject(zones.get(0)));
        List<Network> networks = cloudStack.listNetworks(null);
        for (Network network : networks) {
            System.out.println(CloudStackUtils.printObject(network));
        }
    }

    public Document listHosts(HashMap<String, String> parameter) {
        return doRequest("listHosts", parameter);
    }

    public List<Network> listNetworks(HashMap<String, String> parameter) {
        Document network = doRequest("listNetworks", parameter);
        network.normalizeDocument();
        NodeList list = network.getElementsByTagName("network");
        List<Network> networks = new ArrayList<Network>();
        for (int i = 0; i < list.getLength(); i++) {
            Network objh = parseNode(list.item(i), Network.class);
            networks.add(objh);
        }
        return networks;
    }

    public Document listAsyncJobs() {
        return doRequest("listAsyncJobs", null);
    }

    /**
     * http://192.168.31.100:8080/client/api?command=createNetwork&</br>
     * zoneId=7649d2a6-7119-435f-9179-c4e03dc33c47&</br>
     * networkOfferingId=d9c6117d-8c14-4d85-9684-96cfdf74891b&</br>
     * physicalnetworkid=f1d17655-856d-4328-a2d5-63a6f2006392&</br>
     * name=createNetwork&</br>
     * displayText=createNetwork&</br>
     * vlan=3&</br>acltype=domain&</br>
     * gateway=192.168.1.1&netmask=255.255.255.0&startip=192.168.1.1&endip=192.168.1.100&</br>
     * response=json&sessionkey=t4CxYfVB90Cx%2BhZMxFSY4GP63nE%3D&_=1421114033604
     *
     * @param parameter
     * @return
     */
    public Document createNetwork() {
        String zoneID = "7649d2a6-7119-435f-9179-c4e03dc33c47", networkOfferingId = "d9c6117d-8c14-4d85-9684-96cfdf74891b",
                physicalnetworkid = "f1d17655-856d-4328-a2d5-63a6f2006392", name = "createNetwork", displayText = "displayText", vlan = "3",
                acltype = "domain", gateway = "192.168.1.1", netmask = "255.255.255.0", startip = "192.168.1.1", endip = "192.168.1.100";
        return createNetwork(zoneID, networkOfferingId, physicalnetworkid, name, displayText, vlan, acltype, gateway, netmask, startip, endip);
    }

    public List<Physicalnetwork> listPhysicalNetworks(HashMap<String, String> parameter) {
        Document phynetwork = doRequest("listPhysicalNetworks", parameter);
        phynetwork.normalizeDocument();
        NodeList list = phynetwork.getElementsByTagName("physicalnetwork");
        List<Physicalnetwork> networks = new ArrayList<Physicalnetwork>();
        for (int i = 0; i < list.getLength(); i++) {
            Physicalnetwork objh = parseNode(list.item(i), Physicalnetwork.class);
            networks.add(objh);
        }
        return networks;
    }

    public Document restartNetwork(String id) {
        HashMap<String, String> parameter = new HashMap<String, String>();
        parameter.put("id", id);
        return doRequest("restartNetwork", parameter);
    }

    public Document createNetwork(String zoneID, String networkOfferingId, String physicalnetworkid, String name, String displayText, String vlan, String acltype,
            String gateway, String netmask, String startIP, String endip) {
        HashMap<String, String> parameter = new HashMap<String, String>();
        parameter.put("zoneId", zoneID);
        parameter.put("networkOfferingId", networkOfferingId);
        parameter.put("physicalnetworkid", physicalnetworkid);
        parameter.put("name", name);
        parameter.put("displayText", displayText);
        parameter.put("vlan", vlan);
        parameter.put("acltype", acltype);
        parameter.put("gateway", gateway);
        parameter.put("netmask", netmask);
        parameter.put("startip", startIP);
        parameter.put("endip", endip);
        return createNetwork(parameter);
    }

    public Document createNetwork(HashMap<String, String> parameter) {
        return doRequest("createNetwork", parameter);
    }

    public List<Networkoffering> listNetworkOfferings(HashMap<String, String> parameter) {
        Document off = doRequest("listNetworkOfferings", parameter);
        off.normalizeDocument();
        NodeList list = off.getElementsByTagName("networkoffering");
        List<Networkoffering> networkofferings = new ArrayList<Networkoffering>();
        for (int i = 0; i < list.getLength(); i++) {
            Networkoffering objh = parseNode(list.item(i), Networkoffering.class);
            networkofferings.add(objh);
        }
        return networkofferings;
    }

    protected Document doRequest(String cmd, HashMap<String, String> parameter) {
        if (parameter == null) {
            parameter = new HashMap<String, String>();
        }
        URL url;
        URLConnection connect;
        InputStream inputStream = null;
        try {
            String requestUrl = request(createRequestPairs(cmd, parameter));
            url = new URL(requestUrl);
            System.out.println(requestUrl);
            connect = url.openConnection();
            connect.connect();
            inputStream = connect.getInputStream();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            return doc;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CloudStack.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(CloudStack.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CloudStack.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(CloudStack.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CloudStack.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CloudStack.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(CloudStack.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(CloudStack.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }

    public Document listRouters(HashMap<String, String> parameter) {
        return doRequest("listRouters", parameter);
    }

    public List<Zone> listZones(HashMap<String, String> parameter) {
        Document zones = doRequest("listZones", parameter);
        zones.normalizeDocument();
        NodeList list = zones.getElementsByTagName("zone");
        List<Zone> zoneList = new ArrayList<Zone>();
        for (int i = 0; i < list.getLength(); i++) {
            zoneList.add(parseNode(list.item(i), Zone.class));
        }
        return zoneList;
    }

    protected LinkedList<NameValuePair> createRequestPairs(String command, HashMap<String, String> optional) {
        LinkedList<NameValuePair> queryValues = new LinkedList<NameValuePair>();
        queryValues.add(new NameValuePair("command", command));
        queryValues.add(new NameValuePair("apiKey", keyStore.apiKey));
        if (optional != null) {
            Iterator optional_it = optional.entrySet().iterator();
            while (optional_it.hasNext()) {
                Map.Entry pairs = (Map.Entry) optional_it.next();
                queryValues.add(new NameValuePair((String) pairs.getKey(), (String) pairs.getValue()));
            }
        }
        return queryValues;
    }

    protected String request(LinkedList<NameValuePair> pairs) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        pairs.add(new NameValuePair("signature", signRequest(pairs)));
        String requestURL = url + URLEncoder.encode(pairs.get(0).getName(), "UTF-8") + "=" + URLEncoder.encode(pairs.get(0).getValue(), "UTF-8");
        for (int i = 1; i < pairs.size(); i++) {
            requestURL = requestURL + "&" + URLEncoder.encode(pairs.get(i).getName(), "UTF-8") + "=" + URLEncoder.encode(pairs.get(i).getValue(), "UTF-8");
        }
        return requestURL;
    }

    protected String signRequest(List<NameValuePair> pairs) throws NoSuchAlgorithmException, InvalidKeyException {
        Collections.sort(pairs, new Comparator<NameValuePair>() {
            public int compare(NameValuePair o1, NameValuePair o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        String queryString = pairs.get(0).getName() + "=" + pairs.get(0).getValue();
        for (int i = 1; i < pairs.size(); i++) {
            queryString = queryString + "&" + pairs.get(i).getName() + "=" + pairs.get(i).getValue();
        }
//        System.out.println("queryString :" + queryString);
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec secret_key = new SecretKeySpec(keyStore.secretkey.getBytes(), "HmacSHA1");
        mac.init(secret_key);
        byte[] digest = mac.doFinal(queryString.toLowerCase().getBytes());
        String result = Base64.getEncoder().encodeToString(digest);
        return result;
    }

    class NameValuePair implements Serializable {

        public NameValuePair() {
            this(null, null);
        }

        public NameValuePair(String name, String value) {
            this.name = name;
            this.value = value;
        }
        private String name = null;
        private String value = null;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String toString() {
            return ("name=" + name + ", " + "value=" + value);
        }

    }
}
