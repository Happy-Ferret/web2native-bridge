/*
 *  Copyright 2006-2015 WebPKI.org (http://webpki.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.webpki.w2nb.webpayment.keyprovider;

import java.io.IOException;
import java.io.InputStream;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;

import java.security.PublicKey;

import java.security.cert.X509Certificate;

import java.util.Enumeration;
import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.webpki.crypto.CertificateUtil;
import org.webpki.crypto.CustomCryptoProvider;

import org.webpki.json.JSONDecoderCache;

import org.webpki.keygen2.CredentialDiscoveryResponseDecoder;
import org.webpki.keygen2.InvocationResponseDecoder;
import org.webpki.keygen2.KeyCreationResponseDecoder;
import org.webpki.keygen2.ProvisioningFinalizationResponseDecoder;
import org.webpki.keygen2.ProvisioningInitializationResponseDecoder;

import org.webpki.net.HTTPSWrapper;

import org.webpki.util.ArrayUtil;
import org.webpki.util.Base64;
import org.webpki.util.MIMETypedObject;

import org.webpki.w2nb.webpayment.common.KeyStoreEnumerator;
import org.webpki.w2nb.webpayment.common.PayerAccountTypes;

import org.webpki.webutil.InitPropertyReader;

public class KeyProviderService extends InitPropertyReader implements ServletContextListener {

    static Logger logger = Logger.getLogger(KeyProviderService.class.getCanonicalName());
    
    static final String LOGOTYPE              = "logotype";

    static final String VERSION_CHECK         = "version_check";

    static final String KEYSTORE_PASSWORD     = "key_password";

    static final String BANK_HOST             = "bank_host";
    
    static final String KEYPROV_HOST          = "keyprov_host";

    static final String KEYPROV_KMK           = "keyprov_kmk";
    
    static final String SERVER_PORT_MAP       = "server_port_map";
    
    static final String[] CREDENTIALS         = {"paycred1", "paycred2", "paycred3"};
    
    static KeyStoreEnumerator keyManagemenentKey;
    
    static String keygen2EnrollmentUrl;
    
    static String bankAuthorityUrl;
    
    static String successImageAndMessage;
    
    static Integer serverPortMapping;

    static JSONDecoderCache keygen2JSONCache;
    
    static X509Certificate tlsCertificate;

    static String grantedVersions[];

    static class PaymentCredential {
        KeyStoreEnumerator signatureKey;
        String accountType;
        String accountId;
        boolean cardFormatted;
        String authorityUrl;
        MIMETypedObject cardImage;
        PublicKey encryptionKey;
    }

    static Vector<PaymentCredential> paymentCredentials = new Vector<PaymentCredential>();

    public static String logotype;

    InputStream getResource(String name) throws IOException {
        InputStream is = this.getClass().getResourceAsStream(name);
        if (is == null) {
            throw new IOException("Resource fail for: " + name);
        }
        return is;
    }

    String getURL (String inUrl) throws IOException {
        URL url = new URL(inUrl);
        if (!url.getHost().equals("localhost")) {
            return inUrl;
        }
        String autoHost = null;
        Enumeration<NetworkInterface> network_interfaces = NetworkInterface.getNetworkInterfaces();
        int foundAddresses = 0;
        while (network_interfaces.hasMoreElements()) {
            NetworkInterface network_interface = network_interfaces.nextElement();
            if (network_interface.isUp() && !network_interface.isVirtual() && !network_interface.isLoopback() &&
                network_interface.getDisplayName().indexOf("VMware") < 0) {  // Well.... 
                Enumeration<InetAddress> inet_addresses = network_interface.getInetAddresses();
                while (inet_addresses.hasMoreElements()) {
                    InetAddress inet_address = inet_addresses.nextElement();
                    if (inet_address instanceof Inet4Address) {
                        foundAddresses++;
                        autoHost = inet_address.getHostAddress();
                    }
                }
            }
        }
        if (foundAddresses != 1) throw new IOException("Couldn't determine network interface");
        logger.info("Host automagically set to: " + autoHost);
        return new URL(url.getProtocol(),
                       autoHost,
                       url.getPort(),
                       url.getFile()).toExternalForm();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        initProperties (event);
        try {
            CustomCryptoProvider.forcedLoad (false);

            ////////////////////////////////////////////////////////////////////////////////////////////
            // Logotype
            ////////////////////////////////////////////////////////////////////////////////////////////
            logotype = new String(
                ArrayUtil.getByteArrayFromInputStream(getResource(getPropertyString(LOGOTYPE))), "UTF-8");

                ////////////////////////////////////////////////////////////////////////////////////////////
            // Optional check
            ////////////////////////////////////////////////////////////////////////////////////////////
            if (getPropertyString(VERSION_CHECK).length() != 0) {
                grantedVersions = getPropertyStringList(VERSION_CHECK);
            }
 
            ////////////////////////////////////////////////////////////////////////////////////////////
            // A common string for browser enrollments
            ////////////////////////////////////////////////////////////////////////////////////////////
            successImageAndMessage = new StringBuffer("<img src=\"data:image/png;base64,")
                .append(new Base64(false).getBase64StringFromBinary(
                    ArrayUtil.getByteArrayFromInputStream(
                        event.getServletContext().getResourceAsStream("/images/certandkey.png"))))
                .append("\" title=\"Certificate and Private Key\"><br>&nbsp;" +
                        "<br><b>Enrollment Succeeded!</b>").toString();

            ////////////////////////////////////////////////////////////////////////////////////////////
            // KeyGen2
            ////////////////////////////////////////////////////////////////////////////////////////////
            keygen2JSONCache = new JSONDecoderCache ();
            keygen2JSONCache.addToCache (InvocationResponseDecoder.class);
            keygen2JSONCache.addToCache (ProvisioningInitializationResponseDecoder.class);
            keygen2JSONCache.addToCache (CredentialDiscoveryResponseDecoder.class);
            keygen2JSONCache.addToCache (KeyCreationResponseDecoder.class);
            keygen2JSONCache.addToCache (ProvisioningFinalizationResponseDecoder.class);

            ////////////////////////////////////////////////////////////////////////////////////////////
            // Credentials
            ////////////////////////////////////////////////////////////////////////////////////////////
            for (String credentialEntry : CREDENTIALS) {
                final String[] arguments = getPropertyStringList(credentialEntry);
                PaymentCredential paymentCredential = new PaymentCredential();
                paymentCredentials.add(paymentCredential);
                paymentCredential.signatureKey =
                    new KeyStoreEnumerator(getResource(arguments[0]),
                                           getPropertyString(KEYSTORE_PASSWORD));
                paymentCredential.accountType = PayerAccountTypes.valueOf(arguments[1]).getTypeUri();
                boolean cardFormatted = true;
                if (arguments[2].charAt(0) == '!') {
                    cardFormatted = false;
                    arguments[2] = arguments[2].substring(1);
                }
                paymentCredential.accountId = arguments[2];
                paymentCredential.cardFormatted = cardFormatted;
                paymentCredential.cardImage = new MIMETypedObject() {
                    @Override
                    public byte[] getData() throws IOException {
                        return ArrayUtil.getByteArrayFromInputStream(getResource(arguments[3]));
                    }
                    @Override
                    public String getMimeType() throws IOException {
                        return "image/png";
                    }
                };
                paymentCredential.encryptionKey =
                    CertificateUtil.getCertificateFromBlob(
                        ArrayUtil.getByteArrayFromInputStream(getResource(arguments[4]))).getPublicKey();
            }

            bankAuthorityUrl = getPropertyString(BANK_HOST);

            ////////////////////////////////////////////////////////////////////////////////////////////
            // SKS key management key
            ////////////////////////////////////////////////////////////////////////////////////////////
            keyManagemenentKey = new KeyStoreEnumerator(getResource(getPropertyString(KEYPROV_KMK)),
                                                                    getPropertyString(KEYSTORE_PASSWORD));

            if (getPropertyString(SERVER_PORT_MAP).length () > 0) {
                serverPortMapping = getPropertyInt(SERVER_PORT_MAP);
            }

            ////////////////////////////////////////////////////////////////////////////////////////////
            // Get KeyGen2 protocol entry
            ////////////////////////////////////////////////////////////////////////////////////////////
            keygen2EnrollmentUrl = getURL(getPropertyString(KEYPROV_HOST)) + "/getkeys";

            ////////////////////////////////////////////////////////////////////////////////////////////
            // Get TLS server certificate (if necessary)
            ////////////////////////////////////////////////////////////////////////////////////////////
            if (keygen2EnrollmentUrl.startsWith("https")) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            HTTPSWrapper wrapper = new HTTPSWrapper();
                            String url = keygen2EnrollmentUrl;
                            wrapper.setRequireSuccess(false);
                            if (serverPortMapping != null) {
                                URL url2 = new URL(url);
                                url = new URL(url2.getProtocol(),
                                              url2.getHost(),
                                              serverPortMapping,
                                              url2.getFile()).toExternalForm();
                            }
                            wrapper.makeGetRequest(url);
                            tlsCertificate = wrapper.getServerCertificate();
                            logger.info("TLS cert: " + tlsCertificate.getSubjectX500Principal().getName());
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "********\n" + e.getMessage() + "\n********", e);
                        }
                    }
                }.start();
            }
            
            logger.info("Web2Native Bridge KeyProvider-server initiated");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "********\n" + e.getMessage() + "\n********", e);
        }
    }

    static boolean isDebug() {
        return true;
    }
}