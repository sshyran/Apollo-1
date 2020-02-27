/*
 * Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.core.rest.converter;

import com.apollocurrency.aplwallet.api.dto.UnconfirmedTransactionDTO;
import com.apollocurrency.aplwallet.apl.core.app.Convert2;
import com.apollocurrency.aplwallet.apl.core.app.Transaction;
import com.apollocurrency.aplwallet.apl.core.transaction.Payment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.Appendix;
import com.apollocurrency.aplwallet.apl.crypto.Convert;
import com.apollocurrency.aplwallet.apl.crypto.Crypto;
import org.json.simple.JSONObject;

import java.util.Map;

public class UnconfirmedTransactionConverter implements Converter<Transaction, UnconfirmedTransactionDTO>{
    @Override
    public UnconfirmedTransactionDTO apply(Transaction model) {
        UnconfirmedTransactionDTO dto = new UnconfirmedTransactionDTO();
        dto.setType(model.getType().getType());
        dto.setSubtype(model.getType().getSubtype());
        dto.setPhased(model.getPhasing() != null);
        dto.setTimestamp(model.getTimestamp());
        dto.setDeadline(model.getDeadline());
        dto.setSenderPublicKey(Convert.toHexString(model.getSenderPublicKey()));
        if(model.getRecipientId() != 0 ){
            long recipientId;
            if(model.getType() == Payment.PRIVATE){
                recipientId = AccountConverter.anonymizeAccount();
            }else {
                recipientId = model.getRecipientId();
            }
            dto.setRecipient(Long.toUnsignedString(recipientId));
            dto.setRecipientRS(Convert2.rsAccount(recipientId));
        }
        long amountATM;
        if(model.getType() == Payment.PRIVATE){
            amountATM = AccountConverter.anonymizeBalance();
        }else{
            amountATM = model.getAmountATM();
        }
        dto.setAmountATM(String.valueOf(amountATM));
        dto.setFeeATM(String.valueOf(model.getFeeATM()));
        dto.setReferencedTransactionFullHash(model.getReferencedTransactionFullHash());
        byte[] signature = Convert.emptyToNull(model.getSignature());
        if (signature != null) {
            dto.setSignature(Convert.toHexString(signature));
            dto.setSignatureHash(Convert.toHexString(Crypto.sha256().digest(signature)));
            dto.setFullHash(model.getFullHashString());
            dto.setTransaction(model.getStringId());
        }
        JSONObject attachmentJSON = new JSONObject();
        for (Appendix appendage : model.getAppendages(true)) {
            attachmentJSON.putAll(appendage.getJSONObject());
        }
        if (!attachmentJSON.isEmpty()) {
            for (Map.Entry entry : (Iterable<Map.Entry>) attachmentJSON.entrySet()) {
                if (entry.getValue() instanceof Long) {
                    entry.setValue(String.valueOf(entry.getValue()));
                }
            }
            dto.setAttachment(attachmentJSON);
        }
        long senderId;
        if(model.getType() == Payment.PRIVATE){
            senderId = AccountConverter.anonymizeAccount();
        }else {
            senderId = model.getSenderId();
        }
        dto.setSender(Long.toUnsignedString(senderId));
        dto.setSenderRS(Convert2.rsAccount(senderId));

        dto.setHeight(model.getHeight());
        dto.setVersion(model.getVersion());
        dto.setEcBlockId(Long.toUnsignedString(model.getECBlockId()));
        dto.setEcBlockHeight(model.getECBlockHeight());

        return dto;
    }

}