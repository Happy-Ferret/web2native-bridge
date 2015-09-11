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
import java.math.BigDecimal;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webpki.json.JSONAlgorithmPreferences;
import org.webpki.json.JSONDecoderCache;
import org.webpki.json.JSONObjectReader;
import org.webpki.json.JSONObjectWriter;
import org.webpki.json.JSONOutputFormats;
import org.webpki.json.JSONParser;
import org.webpki.net.HTTPSWrapper;
import org.webpki.w2nb.webpayment.common.AccountTypes;
import org.webpki.w2nb.webpayment.common.Authority;
import org.webpki.w2nb.webpayment.common.BaseProperties;
import org.webpki.w2nb.webpayment.common.EncryptedData;
import org.webpki.w2nb.webpayment.common.ReserveOrDebitRequest;
import org.webpki.w2nb.webpayment.common.AuthorizationData;
import org.webpki.w2nb.webpayment.common.ReserveOrDebitResponse;
import org.webpki.w2nb.webpayment.common.Messages;
import org.webpki.w2nb.webpayment.common.PaymentRequest;
import org.webpki.w2nb.webpayment.common.PaymentTypeDescriptor;
import org.webpki.w2nb.webpayment.common.ProtectedAccountData;
import org.webpki.webutil.ServletUtil;

public class PaymentServlet extends HttpServlet implements BaseProperties {

    private static final long serialVersionUID = 1L;
    
    static Logger logger = Logger.getLogger(PaymentServlet.class.getCanonicalName());
    
    static final int TIMEOUT_FOR_REQUEST = 5000;

    static int referenceId = 164006;
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        JSONObjectWriter authorizationResponse = null;
        String clientIpAddress = null;
        try {
            String contentType = request.getContentType();
            if (!contentType.equals(JSON_CONTENT_TYPE)) {
                throw new IOException("Content-Type must be \"" + JSON_CONTENT_TYPE + "\" , found: " + contentType);
            }
            JSONObjectReader payeeRequest = JSONParser.parse(ServletUtil.getData(request));
            logger.info("Received:\n" + payeeRequest);

            ////////////////////////////////////////////////////////////////////////////////////////
            // We rationalize here by using a single end-point for both pull and push modes       //
            ////////////////////////////////////////////////////////////////////////////////////////

            // Read the attested and encrypted request
            ReserveOrDebitRequest attestedEncryptedRequest = new ReserveOrDebitRequest(payeeRequest);

            // Decrypt the encrypted request
            AuthorizationData authorizationData =
                    attestedEncryptedRequest.getDecryptedAuthorizationRequest(BankService.decryptionKeys);

            // In the indirect mode the merchant is the only one who can provide the client's IP address
            clientIpAddress = attestedEncryptedRequest.getClientIpAddress();

            // Client IP could be used for risk-based authentication, here it is only logged
            logger.info("Client address: " + clientIpAddress);

            // Verify that the outer signature (the user's) matches the bank's client root
            authorizationData.getSignatureDecoder().verify(BankService.clientRoot);

            // Get the embedded (counter-signed) payment request
            PaymentRequest paymentRequest = authorizationData.getPaymentRequest();

            // Verify that the merchant's signature belongs to a valid merchant trust network
            paymentRequest.getSignatureDecoder().verify(BankService.merchantRoot);
            
            if (paymentRequest.getAmount().compareTo(new BigDecimal("1000000.00")) > 0) {
                throw new IOException("Toomuch");
            }

            // Separate credit-card and account2account payments
            JSONObjectWriter encryptedCardData = null;
            if (AccountTypes.fromType(authorizationData.getAccountType()).isAcquirerBased()) {
                logger.info("card");
                String authorityUrl = attestedEncryptedRequest.getAcquirerAuthorityUrl();
                HTTPSWrapper wrap = new HTTPSWrapper();
                wrap.setTimeout(TIMEOUT_FOR_REQUEST);
                wrap.setHeader("Content-Type", JSON_CONTENT_TYPE);
                wrap.setRequireSuccess(true);
                wrap.makeGetRequest(authorityUrl);
                if (!wrap.getContentType().equals(JSON_CONTENT_TYPE)) {
                    throw new IOException("Content-Type must be \"" + JSON_CONTENT_TYPE + "\" , found: " + wrap.getContentType());
                }
                Authority authority = new Authority(JSONParser.parse(wrap.getData()),authorityUrl);
                JSONObjectWriter protectedAccountData =
                     ProtectedAccountData.encode(authorizationData.getAccountId());
                encryptedCardData = EncryptedData.encode(protectedAccountData,
                                                         authority.getDataEncryptionAlgorithm(),
                                                         authority.getPublicKey(),
                                                         authority.getKeyEncryptionAlgorithm());
             } else {
                 logger.info("account");
             }

            ////////////////////////////////////////////////////////////////////////////
            // We got an authentic request.  Now we need to check available funds etc.//
            // However, since we haven't a real bank we simply accept :-)             //
            ////////////////////////////////////////////////////////////////////////////

            // Return the authorized request
            authorizationResponse = ReserveOrDebitResponse.encode(false,
                                                                  paymentRequest,
                                                                        authorizationData.getAccountType(),
                                                                        authorizationData.getAccountId(),
                                                                        encryptedCardData,
                                                                        "#" + (referenceId ++),
                                                                        BankService.bankKey);

            logger.info("Returned to caller:\n" + authorizationResponse);
            
        } catch (Exception e) {
            authorizationResponse = Messages.createBaseMessage(Messages.ERROR_RESPONSE);
            authorizationResponse.setString(DESCRIPTION_JSON, e.getMessage());
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        response.setContentType(BankService.jsonMediaType);
        response.setHeader("Pragma", "No-Cache");
        response.setDateHeader("EXPIRES", 0);
        response.getOutputStream().write(authorizationResponse.serializeJSONObject(JSONOutputFormats.NORMALIZED));
      }
  }
