package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import database.Loader;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    private int id;
    private User user;
    private long amount;
    private int fee;
    private String description;
    private String adminDescription;
    private String adminInternalDescription;
    private TransactionType type;
    private String factorImageFileId;
    private LocalDateTime time;
    private PersianDate persianDate;
    private String committee;
    private boolean verificated;
    private boolean hasPaperInvoice;
    private boolean deleted;
    private boolean completed;

    public Transaction(){}

    public Transaction(User user, long amount, int fee, String description, TransactionType type, String factorImageFileId) {
        File directory=new File(Loader.rootPath + "Transactions/");
        int fileCount= Objects.requireNonNull(directory.list()).length;
        this.id = fileCount + 1;
        this.user = user;
        this.amount = amount;
        this.fee = fee;
        this.description = description;
        this.type = type;
        this.factorImageFileId = factorImageFileId;
        this.time = LocalDateTime.now();
        this.verificated = true;
        this.persianDate = PersianDate.now();
        this.adminDescription = "";
        this.adminInternalDescription = "";
        this.hasPaperInvoice = false;
        this.deleted = false;
        this.committee = null;
        this.completed = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAmount() {
        return amount;
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

    public boolean isVerificated() {
        return verificated;
    }

    public void setVerificated(boolean verificated) {
        this.verificated = verificated;
    }

    public PersianDate getPersianDate() {
        return persianDate;
    }

    public void setPersianDate(PersianDate persianDate) {
        this.persianDate = persianDate;
    }

    public String getAdminDescription() {
        return adminDescription;
    }

    public void setAdminDescription(String adminDescription) {
        this.adminDescription = adminDescription;
    }

    public boolean isHasPaperInvoice() {
        return hasPaperInvoice;
    }

    public void setHasPaperInvoice(boolean hasPaperInvoice) {
        this.hasPaperInvoice = hasPaperInvoice;
    }

    public static String getPersianType(TransactionType type){
        if (type == TransactionType.EXPENDITURE){
            return "خرج شده توسط خدام";
        } else
            if (type == TransactionType.INCOME){
                return "ورودی";
            }
        return "تخصیص بودجه به خدام";
    }

    public String getAdminInternalDescription() {
        return adminInternalDescription;
    }

    public void setAdminInternalDescription(String adminInternalDescription) {
        this.adminInternalDescription = adminInternalDescription;
    }

    public String getCommittee() {
        return committee;
    }

    public void setCommittee(String committee) {
        this.committee = committee;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return
                "آیدی: " + id + '\n' +
                        "کاربر: " + "@" +user.getUsername() + '\n' +
                        "مبلغ: " + amount + '\n' +
                        "کارمزد: " + fee + '\n' +
                        "نوع: " + Transaction.getPersianType(type)
                        + "\n" + "تاریخ: "
                        + getPersianDate().getDay()
                        + " " + PersianDate.getMonthNameByItNumber(getPersianDate().getMonth())
                        + " " + getPersianDate().getYear()
                        + "\n" + "زمان: "
                        + getTime().getHour() + ":"
                        + getTime().getMinute() + ":"
                        + getTime().getSecond() + "\n"
                        + "وضعیت تایید: " + isVerificated() + "\n"
                        + "دارای فاکتور کاغذی: " + isHasPaperInvoice() + "\n"
                        + "بخش: " + committee + "\n"
                        + "توضیحات: " + description + "\n"
                        + "توضیحات مدیر: " +adminDescription;
    }

    public String adminToString() {
        return
                "آیدی: " + id + '\n' +
                        "کاربر: " + "@" +user.getUsername() + '\n' +
                        "مبلغ: " + amount + '\n' +
                        "کارمزد: " + fee + '\n' +
                        "نوع: " + Transaction.getPersianType(type)
                        + "\n" + "تاریخ: "
                        + getPersianDate().getDay()
                        + " " + PersianDate.getMonthNameByItNumber(getPersianDate().getMonth())
                        + " " + getPersianDate().getYear()
                        + "\n" + "زمان: "
                        + getTime().getHour() + ":"
                        + getTime().getMinute() + ":"
                        + getTime().getSecond() + "\n"
                        + "وضعیت تایید: " + isVerificated() + "\n"
                        + "دارای فاکتور کاغذی: " + isHasPaperInvoice() + "\n"
                        + "بخش: " + committee + "\n"
                        + "توضیحات: " + description + "\n"
                        + "توضیحات مدیر: " +adminDescription + "\n"
                        + "توضیحات داخلی مدیر: " +adminInternalDescription;
    }

    @JsonIgnore
    public String getReadableTime(){
        return getTime().getHour() + ":"
                + getTime().getMinute() + ":"
                + getTime().getSecond();
    }

    @JsonIgnore
    public String getReadableDate(){
        return getPersianDate().getYear() + "/" + getPersianDate().getMonth() + "/" + getPersianDate().getDay();
//        return getPersianDate().getDay()
//                + " " + PersianDate.getMonthNameByItNumber(getPersianDate().getMonth())
//                + " " + getPersianDate().getYear();
    }

}
