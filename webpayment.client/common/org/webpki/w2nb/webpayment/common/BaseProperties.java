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

public interface BaseProperties {
    String WINDOW_JSON                       = "window";
    String HEIGHT_JSON                       = "height";
    String WIDTH_JSON                        = "width";
    String COMMON_NAME_JSON                  = "commonName";
    String NAME_JSON                         = "name";
    String PAYMENT_REQUEST_JSON              = "paymentRequest";
    String ACQUIRER_AUTHORITY_URL_JSON       = "acquirerAuthorityUrl";  // For CreditCard payments
    String PAYEE_ACCOUNTS_JSON               = "payeeAccounts";         // For Account2Account payments
    String PAYEE_ACCOUNT_JSON                = "payeeAccount";          // Selected payee Account2Account
    String PAYER_ACCOUNT_JSON                = "payerAccount";          // Selected payer Account2Account or Card
    String EXPIRES_JSON                      = "expires";               // Object expiration time
    String AUTHORITY_URL_JSON                = "authorityUrl";
    String TRANSACTION_URL_JSON              = "transactionUrl";
    String PROVIDER_AUTHORIZATION_JSON       = "providerAuthorization";
    String AMOUNT_JSON                       = "amount";
    String DESCRIPTION_JSON                  = "description";
    String CURRENCY_JSON                     = "currency";
    String ERROR_CODE_JSON                   = "errorCode";
    String TIME_STAMP_JSON                   = "timeStamp";
    String TRANSACTION_ID_JSON               = "transactionId";
    String CLIENT_IP_ADDRESS_JSON            = "clientIpAddress";       // Security data for the payment provider
    String CLIENT_GEO_LOCATION_JSON          = "clientGeoLocation";     // Optional security data that client devices may supply
    String REFERENCE_ID_JSON                 = "referenceId";
    String PAYEE_JSON                        = "payee";                 // Common name of payee to be used in UIs
    String AUTHORIZATION_DATA_JSON           = "authorizationData";     // Payer authorization data
    String PROVIDER_AUTHORITY_URL_JSON       = "providerAuthorityUrl";  // URL to payment provider
    String ACCEPTED_ACCOUNT_TYPES_JSON       = "acceptedAccountTypes";  // List of ACCOUNT_TYPE_JSON
    String ACCOUNT_TYPE_JSON                 = "accountType";           // Account/Card type in the form of a URI
    String ACCOUNT_HOLDER_JSON               = "accountHolder";         // Card holder
    String ACCOUNT_SECURITY_CODE_JSON        = "accountSecurityCode";   // CCV
    String CARD_FORMAT_ACCOUNT_ID_JSON       = "cardFormatAccountId";   // Display formatting like cards or not
    String ACCOUNT_REFERENCE_JSON            = "accountReference";      // Account/Card number for payee (like ************5678)
    String PROTECTED_ACCOUNT_DATA_JSON       = "protectedAccountData";  // Account data that (only) an acquirer needs
    String REQUEST_HASH_JSON                 = "requestHash";
    String VALUE_JSON                        = "value";
    String DOMAIN_NAME_JSON                  = "domainName";
    String ENCRYPTED_DATA_JSON               = "encryptedData";
    String ENCRYPTED_KEY_JSON                = "encryptedKey";
    String STATIC_RECEIVER_KEY_JSON          = "staticReceiverKey";
    String EPHEMERAL_SENDER_KEY_JSON         = "ephemeralSenderKey";
    String SIGNATURE_ALGORITHM_JSON          = "signatureAlgorithm";
    String KEY_ENCRYPTION_ALGORITHM_JSON     = "keyEncryptionAlgorithm";     // For acquirer encryption key
    String DATA_ENCRYPTION_ALGORITHM_JSON    = "dataEncryptionAlgorithm";    //    -"-
    String ENCRYPTION_PARAMETERS_JSON        = "encryptionParameters";       //    -"-
    String ALGORITHM_JSON                    = "algorithm";
    String IV_JSON                           = "iv";                    // For symmetric encryption
    String TAG_JSON                          = "tag";                   // For symmetric encryption
    String CIPHER_TEXT_JSON                  = "cipherText";
    String SOFTWARE_JSON                     = "software";
    String ID_JSON                           = "id";
    String TYPE_JSON                         = "type";
    String FIELD1_JSON                       = "field1";                //Used for "additional" account data 
    String FIELD2_JSON                       = "field2";
    String FIELD3_JSON                       = "field3";
    String VERSION_JSON                      = "version";
    
    String W2NB_WEB_PAY_CONTEXT_URI          = "http://xmlns.webpki.org/webpay/v1";

    String JSON_CONTENT_TYPE                 = "application/json";
}
