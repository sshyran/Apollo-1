package com.apollocurrency.aplwallet.apl.exchange.transaction;

import com.apollocurrency.aplwallet.apl.core.account.Account;
import com.apollocurrency.aplwallet.apl.core.account.LedgerEvent;
import com.apollocurrency.aplwallet.apl.core.app.Transaction;
import com.apollocurrency.aplwallet.apl.core.transaction.TransactionType;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.AbstractAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DexControlOfFrozenMoneyAttachment;
import com.apollocurrency.aplwallet.apl.crypto.Convert;
import com.apollocurrency.aplwallet.apl.exchange.model.DexContractDBRequest;
import com.apollocurrency.aplwallet.apl.exchange.model.DexOffer;
import com.apollocurrency.aplwallet.apl.exchange.model.ExchangeContract;
import com.apollocurrency.aplwallet.apl.exchange.model.OfferStatus;
import com.apollocurrency.aplwallet.apl.exchange.service.DexService;
import com.apollocurrency.aplwallet.apl.util.AplException;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Map;
import javax.enterprise.inject.spi.CDI;

@Slf4j
public class DexTransferMoneyTransaction extends DEX {

    private DexService dexService = CDI.current().select(DexService.class).get();


    @Override
    public byte getSubtype() {
        return TransactionType.SUBTYPE_DEX_TRANSFER_MONEY;
    }

    @Override
    public LedgerEvent getLedgerEvent() {
        return LedgerEvent.DEX_TRANSFER_MONEY;
    }

    @Override
    public AbstractAttachment parseAttachment(ByteBuffer buffer) throws AplException.NotValidException {
        return new DexControlOfFrozenMoneyAttachment(buffer);
    }

    @Override
    public AbstractAttachment parseAttachment(JSONObject attachmentData) throws AplException.NotValidException {
        return new DexControlOfFrozenMoneyAttachment(attachmentData);
    }

    @Override
    public void validateAttachment(Transaction transaction) throws AplException.ValidationException {
        // IMPORTANT! Validation should restrict sending this transaction without money freezing and out of the dex scope
        DexControlOfFrozenMoneyAttachment attachment = (DexControlOfFrozenMoneyAttachment) transaction.getAttachment();
        ExchangeContract dexContract = dexService.getDexContract(DexContractDBRequest.builder().id(attachment.getContractId()).build());
        if (dexContract == null) {
            throw new AplException.NotValidException("Contract does not exist: id - " + attachment.getContractId());
        }
        if (dexContract.getRecipient() != transaction.getSenderId() && dexContract.getSender() != transaction.getSenderId()) {
            throw new AplException.NotValidException("Account" + transaction.getSenderId() + " is not a party of the contract. Expected - " + dexContract.getRecipient() + " or  " + dexContract.getSender());
        }
        boolean isSender = dexContract.getSender() == transaction.getSenderId();
        long recipient = isSender ? dexContract.getRecipient() : dexContract.getSender();
        if (recipient != transaction.getRecipientId()) {
            throw new AplException.NotValidException("Tx recipient differs from account, specified in the contract");
        }
        long transactionId = Convert.parseUnsignedLong(isSender ? dexContract.getTransferTxId() : dexContract.getCounterTransferTxId());
        if (transactionId == 0) {
            throw new AplException.NotCurrentlyValidException("Contract transaction was not pre confirmed or missing");
        }
        if (transaction.getId() != transactionId) {
            throw new AplException.NotValidException("Transaction was not registered in the contract. ");
        }
        long orderId =  isSender ? dexContract.getOrderId() : dexContract.getCounterOrderId();
        DexOffer order = dexService.getOfferById(orderId);
        if (order == null) {
            throw new AplException.NotValidException("Contract: " + dexContract.getId() + " refer to non-existent order: " + orderId);
        }
        if (order.getAccountId() != transaction.getSenderId()) {
            throw new AplException.NotValidException("Order" + orderId + " should belong to the account: " + transaction.getSenderId());
        }
        if (order.getStatus() != OfferStatus.WAITING_APPROVAL) {
            throw new AplException.NotValidException("Inconsistent order state for id: " + order + ", expected - " + OfferStatus.WAITING_APPROVAL + ", got " + order.getStatus());
        }
    }

    @Override
    public boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
        return true;
    }


    @Override
    public void applyAttachment(Transaction tx, Account sender, Account recipient) {
        DexControlOfFrozenMoneyAttachment attachment = (DexControlOfFrozenMoneyAttachment) tx.getAttachment();
        sender.addToBalanceATM(getLedgerEvent(), tx.getId(), -attachment.getOfferAmount()); // reduce only balanceATM, assume that unconfirmed balance was reduced earlier and was not recovered yet
        recipient.addToBalanceAndUnconfirmedBalanceATM(getLedgerEvent(), tx.getId(), attachment.getOfferAmount());
        ExchangeContract dexContract = dexService.getDexContract(DexContractDBRequest.builder().id(attachment.getContractId()).build());
        long orderToClose = dexContract.getSender() == sender.getId() ? dexContract.getCounterOrderId() : dexContract.getOrderId(); // close order which was approved
        dexService.closeOrder(tx.getId(), orderToClose);
//        DexControlOfFrozenMoneyAttachment attachment = (DexControlOfFrozenMoneyAttachment) transaction.getAttachment();
//
//        DexOffer offer = dexService.getOfferByTransactionId(attachment.getOrderId());
//
//        if(DexCurrencyValidator.haveFreezeOrRefundApl(offer)) {
//            try {
//                dexService.refundAPLFrozenMoney(offer);
//            } catch (AplException.ExecutiveProcessException e) {
//                log.error(e.getMessage(), e);
//                throw new RuntimeException(e);
//            }
//        }
    }

    @Override
    public void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {}

    @Override
    public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
        DexControlOfFrozenMoneyAttachment attachment = (DexControlOfFrozenMoneyAttachment) transaction.getAttachment();
        return isDuplicate(DEX.DEX_TRANSFER_MONEY_TRANSACTION, Long.toUnsignedString(attachment.getContractId()), duplicates, true);
    }

    @Override
    public boolean canHaveRecipient() {
        return true;
    }

    @Override
    public boolean isPhasingSafe() {
        return true;
    }

    @Override
    public String getName() {
        return "DexTransferMoney";
    }
}
