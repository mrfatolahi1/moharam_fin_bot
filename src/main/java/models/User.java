package models;
import database.Loader;

import java.util.ArrayList;

public class User {
    private long id;
    private String name;
    private String username;
    private String email;
    private String phoneNumber;
    private String cardNumber;
    private String accountNumber;
    private String shabaNumber;
    private ArrayList<Integer> transactionsIDsList;

    public User() {}

    public User(long id, String name, String username, String email, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.transactionsIDsList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArrayList<Integer> getTransactionsIDsList() {
        return transactionsIDsList;
    }

    public void setTransactionsIDsList(ArrayList<Integer> transactionsIDsList) {
        this.transactionsIDsList = transactionsIDsList;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getShabaNumber() {
        return shabaNumber;
    }

    public void setShabaNumber(String shabaNumber) {
        this.shabaNumber = shabaNumber;
    }

    public long calculateBalance(){
        ArrayList<Transaction> transactionsList = Loader.loadUserTransactions(this, true);
        long balance = 0;

        for (Transaction transaction : transactionsList){
            if (transaction.getType() == TransactionType.GIVE_TO_USERS){
                balance += transaction.getAmount();
            } else if (transaction.getType() == TransactionType.EXPENDITURE){
                balance -= transaction.getAmount();
            }
        }

        return balance;
    }

    @Override
    public String toString() {
        return
                "آیدی: " + id + '\n' +
                "نام: " + name + '\n' +
                "نام‌کاربری تلگرام: " + "@" + username + '\n' +
                "ایمیل: " + email + '\n' +
                "شماره همراه: " + phoneNumber + '\n' +
                "شماره کارت: " + cardNumber + '\n' +
                "شماره حساب: " + accountNumber + '\n' +
                "شماره شبا: " + shabaNumber + '\n' +
                "تراز مالی: " + calculateBalance();
    }
}
