package models;

import java.time.LocalDateTime;

public class Transaction {
    private User user;
    private long amount;
    private int fee;
    private String description;
    private TransactionType type;
    private String factorImageFileId;
    private LocalDateTime time;

    public Transaction(User user, long amount, int fee, String description, TransactionType type, String factorImageFileId) {
        this.user = user;
        this.amount = amount;
        this.fee = fee;
        this.description = description;
        this.type = type;
        this.factorImageFileId = factorImageFileId;
        this.time = LocalDateTime.now();
    }

    public User getPerson() {
        return user;
    }

    public void setPerson(User user) {
        this.user = user;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getFee() {
        return fee;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getFactorImageFileId() {
        return factorImageFileId;
    }

    public void setFactorImageFileId(String factorImageFileId) {
        this.factorImageFileId = factorImageFileId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
