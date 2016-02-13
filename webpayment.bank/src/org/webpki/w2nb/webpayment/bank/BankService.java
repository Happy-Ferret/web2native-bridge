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
package org.webpki.w2nb.webpayment.bank;

import java.io.IOException;
import java.io.InputStream;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PublicKey;

import java.security.cert.X509Certificate;

import java.security.interfaces.RSAPublicKey;

import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.webpki.crypto.CertificateUtil;
import org.webpki.crypto.CustomCryptoProvider;
import org.webpki.crypto.KeyStoreVerifier;

import org.webpki.json.JSONOutputFormats;
import org.webpki.json.JSONX509Verifier;

import org.webpki.util.ArrayUtil;

import org.webpki.w2nb.webpayment.common.Authority;
import org.webpki.w2nb.webpayment.common.BaseProperties;
import org.webpki.w2nb.webpayment.common.DecryptionKeyHolder;
import org.webpki.w2nb.webpayment.common.Encryption;
import org.webpki.w2nb.webpayment.common.Expires;
import org.webpki.w2nb.webpayment.common.KeyStoreEnumerator;
import org.webpki.w2nb.webpayment.common.ServerX509Signer;

import org.webpki.webutil.InitPropertyReader;

public class BankService extends InitPropertyReader implements ServletContextListener {

    static Logger logger = Logger.getLogger(BankService.class.getCanonicalName());
    
    static final String KEYSTORE_PASSWORD     = "key_password";

    static final String BANK_EECERT           = "bank_eecert";
    static final String BANK_HOST             = "bank_host";
    static final String DECRYPTION_KEY1       = "bank_decryptionkey1";
    static final String DECRYPTION_KEY2       = "bank_decryptionkey2";
    
    static final String MERCHANT_KEY          = "merchant_key";

    static final String CLIENT_ROOT           = "bank_client_root";
    
    static final String ERR_MEDIA             = "err_media_type";

    static final String SERVER_PORT_MAP       = "server_port_map";
    
    static Vector<DecryptionKeyHolder> decryptionKeys = new Vector<DecryptionKeyHolder>();
    
    static PublicKey merchantKey;
    
    static JSONX509Verifier clientRoot;
    
    static ServerX509Signer bankKey;
    
    static X509Certificate[] bankCertificatePath;
    
    static String jsonMediaType = BaseProperties.JSON_CONTENT_TYPE;

    static byte[] publishedAuthorityData;

    static Integer serverPortMapping;

    InputStream getResource(String name) throws IOException {
        return this.getClass().getResourceAsStream(getPropertyString(name));
    }
    
    void addDecryptionKey(String name) throws IOException {
        KeyStoreEnumerator keyStoreEnumerator = new KeyStoreEnumerator(getResource(name),
                                                                       getPropertyString(KEYSTORE_PASSWORD));
        decryptionKeys.add(new DecryptionKeyHolder(keyStoreEnumerator.getPublicKey(),
                                                   keyStoreEnumerator.getPrivateKey(),
                                                   keyStoreEnumerator.getPublicKey() instanceof RSAPublicKey ?
                                          Encryption.JOSE_RSA_OAEP_256_ALG_ID : Encryption.JOSE_ECDH_ES_ALG_ID));
    }

    JSONX509Verifier getRoot(String name) throws IOException, GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load (null, null);
        keyStore.setCertificateEntry ("mykey",
                                      CertificateUtil.getCertificateFromBlob (
                                           ArrayUtil.getByteArrayFromInputStream (getResource(name))));        
        return new JSONX509Verifier(new KeyStoreVerifier(keyStore));
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        initProperties (event);
         try {
            CustomCryptoProvider.forcedLoad (false);

            if (getPropertyString(SERVER_PORT_MAP).length () > 0) {
                serverPortMapping = getPropertyInt(SERVER_PORT_MAP);
            }
            
            KeyStoreEnumerator bankcreds = new KeyStoreEnumerator(getResource(BANK_EECERT),
                                                                  getPropertyString(KEYSTORE_PASSWORD));
            bankCertificatePath = bankcreds.getCertificatePath();
            bankKey = new ServerX509Signer(bankcreds);

            merchantKey = CertificateUtil.getCertificateFromBlob (
                ArrayUtil.getByteArrayFromInputStream (getResource(MERCHANT_KEY))).getPublicKey();
            clientRoot = getRoot(CLIENT_ROOT);

            addDecryptionKey(DECRYPTION_KEY1);
            addDecryptionKey(DECRYPTION_KEY2);
            
            String bankHost = getPropertyString(BANK_HOST);
            publishedAuthorityData =
                Authority.encode(bankHost + "/authority",
                                 bankHost + "/transact",
                                 decryptionKeys.get(0).getPublicKey(),
                                 Expires.inDays(365),
                                 bankKey).serializeJSONObject(JSONOutputFormats.PRETTY_PRINT);

            if (getPropertyBoolean(ERR_MEDIA)) {
                jsonMediaType = "text/html";
            }

            logger.info("Web2Native Bridge Bank-server initiated");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "********\n" + e.getMessage() + "\n********", e);
        }
    }
}
