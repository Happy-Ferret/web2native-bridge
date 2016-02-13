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

import java.math.BigDecimal;

import java.util.Date;
import java.util.GregorianCalendar;

import org.webpki.crypto.AlgorithmPreferences;

import org.webpki.json.JSONObjectReader;
import org.webpki.json.JSONObjectWriter;
import org.webpki.json.JSONSignatureDecoder;
import org.webpki.json.JSONSignatureTypes;
import org.webpki.json.JSONAsymKeySigner;

public class PaymentRequest implements BaseProperties {
    
    public static final String SOFTWARE_NAME    = "WebPKI.org - Merchant";
    public static final String SOFTWARE_VERSION = "1.00";

    public static JSONObjectWriter encode(JSONObjectWriter payee,
                                          BigDecimal amount,
                                          Currencies currency,
                                          String referenceId,
                                          Date expires,
                                          JSONAsymKeySigner signer) throws IOException {
        return new JSONObjectWriter()
            .setObject(PAYEE_JSON, payee)
            .setBigDecimal(AMOUNT_JSON, amount)
            .setString(CURRENCY_JSON, currency.toString())
            .setString(REFERENCE_ID_JSON, referenceId)
            .setDateTime(TIME_STAMP_JSON, new Date(), true)
            .setDateTime(EXPIRES_JSON, expires, true)
            .setObject(SOFTWARE_JSON, Software.encode (SOFTWARE_NAME, SOFTWARE_VERSION))
            .setSignature(signer);
    }

    public PaymentRequest(JSONObjectReader rd) throws IOException {
        root = rd;
        payee = new Payee(rd.getObject(PAYEE_JSON));
        amount = rd.getBigDecimal(AMOUNT_JSON);
        try {
            currency = Currencies.valueOf(rd.getString(CURRENCY_JSON));
        } catch (Exception e) {
            throw new IOException(e);
        }
        referenceId = rd.getString(REFERENCE_ID_JSON);
        dateTime = rd.getDateTime(TIME_STAMP_JSON);
        expires = rd.getDateTime(EXPIRES_JSON);
        software = new Software(rd);
        signatureDecoder = rd.getSignature(AlgorithmPreferences.JOSE);
        signatureDecoder.verify(JSONSignatureTypes.ASYMMETRIC_KEY);
        rd.checkForUnread();
    }

    GregorianCalendar expires;
    public GregorianCalendar getExpires() {
        return expires;
    }

    
    Payee payee;
    public Payee getPayee() {
        return payee;
    }


    BigDecimal amount;
    public BigDecimal getAmount() {
        return amount;
    }


    Currencies currency;
    public Currencies getCurrency() {
        return currency;
    }


    String referenceId;
    public String getReferenceId() {
        return referenceId;
    }

    
    GregorianCalendar dateTime;
    public GregorianCalendar getDateTime() {
        return dateTime;
    }


    Software software;
    public Software getSoftware() {
        return software;
    }

    
    JSONSignatureDecoder signatureDecoder;
    public JSONSignatureDecoder getSignatureDecoder() {
        return signatureDecoder;
    }

    JSONObjectReader root;

    public byte[] getRequestHash() throws IOException {
        return RequestHash.getRequestHash(new JSONObjectWriter(root));
    }
}
