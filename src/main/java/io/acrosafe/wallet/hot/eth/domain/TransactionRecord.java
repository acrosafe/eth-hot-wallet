package io.acrosafe.wallet.hot.eth.domain;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import io.acrosafe.wallet.core.eth.TransactionStatus;
import io.acrosafe.wallet.core.eth.TransactionType;

@Entity
@Table(name = "transaction_record")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TransactionRecord implements Serializable
{
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "internal_transaction_id", nullable = true)
    private String internalTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, columnDefinition = "DECIMAL(36,0)")
    private BigInteger amount;

    @Column(name = "fee", nullable = false, columnDefinition = "DECIMAL(36,0)")
    private BigInteger fee;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "destination", nullable = false)
    private String destination;

    @CreatedDate
    @Column(name = "created_date", nullable = false)
    private Instant createdDate = Instant.now();

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Instant lastModifiedDate = Instant.now();

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }

    public String getInternalTransactionId()
    {
        return internalTransactionId;
    }

    public void setInternalTransactionId(String internalTransactionId)
    {
        this.internalTransactionId = internalTransactionId;
    }

    public TransactionType getTransactionType()
    {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType)
    {
        this.transactionType = transactionType;
    }

    public BigInteger getAmount()
    {
        return amount;
    }

    public void setAmount(BigInteger amount)
    {
        this.amount = amount;
    }

    public BigInteger getFee()
    {
        return fee;
    }

    public void setFee(BigInteger fee)
    {
        this.fee = fee;
    }

    public String getAccountId()
    {
        return accountId;
    }

    public void setAccountId(String accountId)
    {
        this.accountId = accountId;
    }

    public TransactionStatus getStatus()
    {
        return status;
    }

    public void setStatus(TransactionStatus status)
    {
        this.status = status;
    }

    public String getDestination()
    {
        return destination;
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    public Instant getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate)
    {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate)
    {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }
}
