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
package org.webpki.w2nb.webpayment.common;

import java.io.IOException;
import java.io.InputStream;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import java.util.Enumeration;
import java.util.Vector;

import org.webpki.crypto.KeyStoreReader;

public class KeyStoreEnumerator {

    Vector<X509Certificate> certificatePath = new Vector<X509Certificate>();
    PrivateKey privateKey = null;
    
    public KeyStoreEnumerator(InputStream is, String password) throws IOException {
        try {
            KeyStore ks = KeyStoreReader.loadKeyStore(is, password);
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (ks.isKeyEntry(alias)) {
                    privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
                    for (Certificate cert : ks.getCertificateChain(alias)) {
                        certificatePath.add((X509Certificate) cert);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        if (privateKey == null) {
            throw new IOException("No private key!");
        }
    }

    public PublicKey getPublicKey() {
        return certificatePath.firstElement().getPublicKey();
    }

    public X509Certificate[] getCertificatePath() {
        return certificatePath.toArray(new X509Certificate[0]);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}