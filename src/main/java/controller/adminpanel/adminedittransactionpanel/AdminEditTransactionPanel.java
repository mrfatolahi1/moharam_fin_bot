package controller.adminpanel.adminedittransactionpanel;

import controller.adminpanel.AdminPanel;
import database.Loader;
import database.Saver;
import models.Committee;
import models.Transaction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminEditTransactionPanel {
    private final AdminPanel adminPanel;
    private AdminEditTransactionPanelEstate estate;
    private int cachedTransactionId;

    public AdminEditTransactionPanel(AdminPanel adminPanel) {
        this.adminPanel = adminPanel;
        this.estate = AdminEditTransactionPanelEstate.ADMIN_EDIT_TRANSACTION_PANEL;
    }

    public void showAdminEditTransactionPanel(int transactionId){
        Transaction transaction = Loader.loadTransaction(transactionId, false);
        SendPhoto sendPhoto = new SendPhoto(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(transaction.getFactorImageFileId()));
        sendPhoto.setCaption(transaction.adminToString());
        adminPanel.sendMessageToUser(sendPhoto);

        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), "قسمت مورد نظر جهت ویرایش را انتخاب کنید:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        row1.add("تصویر فاکتور");
        row1.add("مبلغ");
        row1.add("توضیحات");
        row1.add("کارمزد");
        row2.add("توضیحات مدیر");
        row2.add("توضیحات داخلی مدیر");
        row2.add("وضعیت تایید");
        row2.add("وضعیت فاکتور کاغذی");
        row3.add("حذف تراکنش");
        row3.add("کمیته");
        row4.add("بازگشت به پنل مدیریت");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = AdminEditTransactionPanelEstate.ADMIN_EDIT_TRANSACTION_PANEL;
        this.cachedTransactionId = transactionId;
        adminPanel.sendMessageToUser(sendMessage);
    }

    public void handleNewUpdate(Update update){
        if (estate == AdminEditTransactionPanelEstate.ADMIN_EDIT_TRANSACTION_PANEL){
            handleUserEditTransactionPanelRequest(update);
        } else
        if (estate == AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_INVOICE_PHOTO){
            updateTransactionInvoicePhoto(update);
        } else
        if (estate == AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_AMOUNT){
            updateTransactionAmount(update);
        } else
        if (estate == AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_DESCRIPTION){
            updateTransactionDescription(update);
        } else
        if (estate == AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_FEE){
            updateTransactionFee(update);
        } else
        if (estate == AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_ADMIN_DESCRIPTION){
            updateTransactionAdminDescription(update);
        } else
        if (estate == AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_ADMIN_INTERNAL_DESCRIPTION){
            updateTransactionAdminInternalDescription(update);
        } else
        if (estate == AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_VERIFICATION_STATUS){
            updateTransactionVerificationStatus(update);
        } else
        if (estate == AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_PAPER_INVOICE_STATUS){
            updateTransactionVPaperInvoiceStatus(update);
        } else
        if (estate == AdminEditTransactionPanelEstate.ADMIN_IS_DELETING_TRANSACTION){
            deleteTransaction(update);
        } else
        if (estate == AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_COMMITTEE){
            updateTransactionCommitee(update);
        }
    }

    private void handleUserEditTransactionPanelRequest(Update update){
        if (update.getMessage().getText().equals("تصویر فاکتور")){
            requestNewInvoicePhoto();
        } else
        if (update.getMessage().getText().equals("مبلغ")){
            requestAmount();
        } else
        if (update.getMessage().getText().equals("توضیحات")){
            requestNewDescription();
        } else
        if (update.getMessage().getText().equals("کارمزد")){
            requestFee();
        } else
        if (update.getMessage().getText().equals("توضیحات مدیر")){
            requestNewAdminDescription();
        } else
        if (update.getMessage().getText().equals("توضیحات داخلی مدیر")){
            requestNewAdminInternalDescription();
        } else
        if (update.getMessage().getText().equals("وضعیت تایید")){
            requestNewVerificationStatus();
        } else
        if (update.getMessage().getText().equals("کمیته")){
            requestNewCommittee();
        } else
        if (update.getMessage().getText().equals("وضعیت فاکتور کاغذی")){
            requestNewPaperInvoiceStatus();
        } else
        if (update.getMessage().getText().equals("حذف تراکنش")){
            requestDeleteConfirmation();
        } else
        if (update.getMessage().getText().equals("بازگشت به پنل مدیریت")){
            showAdminPanel(update);
        } else {
            estate = AdminEditTransactionPanelEstate.ADMIN_EDIT_TRANSACTION_PANEL;
        }
    }

    private void requestNewInvoicePhoto(){
        estate = AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_INVOICE_PHOTO;
        String messageText = "تصویر جدید را ارسال کنید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void updateTransactionInvoicePhoto(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminEditTransactionPanel(cachedTransactionId);
                return;
            }
        }
        try {
            String photoFileID = update.getMessage().getPhoto().get(0).getFileId();
            Transaction transaction = Loader.loadTransaction(cachedTransactionId, false);
            transaction.setFactorImageFileId(photoFileID);
            Saver.saveTransaction(transaction);
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تغییرات با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        adminPanel.sendMessageToUser(sendMessage);
        showAdminEditTransactionPanel(cachedTransactionId);
    }

    private void requestAmount(){
        estate = AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_AMOUNT;
        String messageText = "مبلغ جدید را ارسال کنید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void updateTransactionAmount(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminEditTransactionPanel(cachedTransactionId);
                return;
            }
        }
        try {
            long amount = Long.parseLong(update.getMessage().getText());
            Transaction transaction = Loader.loadTransaction(cachedTransactionId, false);
            transaction.setAmount(amount);
            Saver.saveTransaction(transaction);
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تغییرات با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        adminPanel.sendMessageToUser(sendMessage);
        showAdminEditTransactionPanel(cachedTransactionId);
    }

    private void requestNewDescription(){
        estate = AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_DESCRIPTION;
        String messageText = "توضیحات جدید را ارسال کنید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void updateTransactionDescription(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminEditTransactionPanel(cachedTransactionId);
                return;
            }
        }
        try {
            String description = update.getMessage().getText();
            Transaction transaction = Loader.loadTransaction(cachedTransactionId, false);
            transaction.setDescription(description);
            Saver.saveTransaction(transaction);
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تغییرات با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        adminPanel.sendMessageToUser(sendMessage);
        showAdminEditTransactionPanel(cachedTransactionId);
    }

    private void requestFee(){
        estate = AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_FEE;
        String messageText = "کارمزد جدید را ارسال کنید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void updateTransactionFee(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminEditTransactionPanel(cachedTransactionId);
                return;
            }
        }
        try {
            int fee = Integer.parseInt(update.getMessage().getText());
            Transaction transaction = Loader.loadTransaction(cachedTransactionId, false);
            transaction.setFee(fee);
            Saver.saveTransaction(transaction);
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تغییرات با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        adminPanel.sendMessageToUser(sendMessage);
        showAdminEditTransactionPanel(cachedTransactionId);
    }

    private void requestNewAdminDescription(){
        estate = AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_ADMIN_DESCRIPTION;
        String messageText = "توضیحات جدید را ارسال کنید، اگر می‌خواهید توضیحات را حذف کنید به طوری که ثبت‌کننده تراکنش نوتیف نگیرد، به جای توضیحات یک نقطه («.») بفرستید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void updateTransactionAdminDescription(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminEditTransactionPanel(cachedTransactionId);
                return;
            }
        }
        try {
            String description = update.getMessage().getText();
            Transaction transaction = Loader.loadTransaction(cachedTransactionId, false);
            transaction.setAdminDescription(description);
            Saver.saveTransaction(transaction);
            if (!description.equals(".")){
                SendPhoto sendPhoto = new SendPhoto(String.valueOf(transaction.getUser().getId()), new InputFile(transaction.getFactorImageFileId()));
                sendPhoto.setCaption("مدیر توضیحات جدیدی برای این تراکنش شما ثبت کرده است:\n\n" + transaction.toString());
                adminPanel.sendMessageToUser(sendPhoto);
            }
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تغییرات با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        adminPanel.sendMessageToUser(sendMessage);
        showAdminEditTransactionPanel(cachedTransactionId);
    }

    private void requestNewAdminInternalDescription(){
        estate = AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_ADMIN_INTERNAL_DESCRIPTION;
        String messageText = "توضیحات جدید را ارسال کنید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void updateTransactionAdminInternalDescription(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminEditTransactionPanel(cachedTransactionId);
                return;
            }
        }
        try {
            String description = update.getMessage().getText();
            Transaction transaction = Loader.loadTransaction(cachedTransactionId, false);
            transaction.setAdminInternalDescription(description);
            Saver.saveTransaction(transaction);
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تغییرات با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        adminPanel.sendMessageToUser(sendMessage);
        showAdminEditTransactionPanel(cachedTransactionId);
    }

    private void requestNewVerificationStatus(){
        estate = AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_VERIFICATION_STATUS;
        String messageText = "وضعیت جدید را انتخاب کنید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add("تایید شده");
        row1.add("تایید نشده");
        row2.add("انصراف");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void updateTransactionVerificationStatus(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminEditTransactionPanel(cachedTransactionId);
                return;
            }
        }
        try {
            String newStatus = update.getMessage().getText();
            Transaction transaction = Loader.loadTransaction(cachedTransactionId, false);
            if (Objects.equals(newStatus, "تایید شده")){
                transaction.setVerificated(true);
            } else
                if (Objects.equals(newStatus, "تایید نشده")){
                    transaction.setVerificated(false);
                } else {
                    throw new Exception();
                }
            Saver.saveTransaction(transaction);
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تغییرات با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        adminPanel.sendMessageToUser(sendMessage);
        showAdminEditTransactionPanel(cachedTransactionId);
    }

    private void requestNewPaperInvoiceStatus(){
        estate = AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_PAPER_INVOICE_STATUS;
        String messageText = "وضعیت جدید را انتخاب کنید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add("فاکتور کاغذی دارد");
        row1.add("فاکتور کاغذی ندارد");
        row2.add("انصراف");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void updateTransactionVPaperInvoiceStatus(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminEditTransactionPanel(cachedTransactionId);
                return;
            }
        }
        try {
            String newStatus = update.getMessage().getText();
            Transaction transaction = Loader.loadTransaction(cachedTransactionId, false);
            if (Objects.equals(newStatus, "فاکتور کاغذی دارد")){
                transaction.setHasPaperInvoice(true);
            } else
            if (Objects.equals(newStatus, "فاکتور کاغذی ندارد")){
                transaction.setHasPaperInvoice(false);
            } else {
                throw new Exception();
            }
            Saver.saveTransaction(transaction);
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تغییرات با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        adminPanel.sendMessageToUser(sendMessage);
        showAdminEditTransactionPanel(cachedTransactionId);
    }

    private void requestDeleteConfirmation(){
        estate = AdminEditTransactionPanelEstate.ADMIN_IS_DELETING_TRANSACTION;
        String messageText = "آیا از حذف تراکنش اطمینان دارید؟";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add("تایید");
        row2.add("انصراف");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void deleteTransaction(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminEditTransactionPanel(cachedTransactionId);
                return;
            }
        }
        try {
            if (update.getMessage().getText().equals("تایید")){
                Transaction transaction = Loader.loadTransaction(cachedTransactionId, false);
                transaction.setDeleted(true);
                Saver.saveTransaction(transaction);
            }
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تراکنش با موفقیت حذف شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        adminPanel.sendMessageToUser(sendMessage);
        showAdminEditTransactionPanel(cachedTransactionId);
    }

    private void requestNewCommittee(){
        ArrayList<String> commitees = Loader.loadCommiteesList();
        String messageText1 = "";
        for (String commitee : commitees){
            messageText1 += commitee + "\n";
        }
        SendMessage sendMessage1 = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText1);
        adminPanel.sendMessageToUser(sendMessage1);
        estate = AdminEditTransactionPanelEstate.ADMIN_SENDING_NEW_COMMITTEE;
        String messageText = "کمیته جدید را ارسال کنید (با استفاده از لیست بالا).";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void updateTransactionCommitee(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminEditTransactionPanel(cachedTransactionId);
                return;
            }
        }
        try {
            String newCommitee = update.getMessage().getText();
            ArrayList<String> commitees = Loader.loadCommiteesList();
            Transaction transaction = Loader.loadTransaction(cachedTransactionId, false);
            if (commitees.contains(newCommitee)){
                transaction.setCommittee(newCommitee);
            } else {
                throw new Exception();
            }
            Saver.saveTransaction(transaction);
        } catch (Exception e){
            String messageText = "کمیته‌ای با این نام وجود ندارد، مجددا تلاش کنید";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تغییرات با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        adminPanel.sendMessageToUser(sendMessage);
        showAdminEditTransactionPanel(cachedTransactionId);
    }

    public AdminPanel getAdminPanel() {
        return adminPanel;
    }

    public AdminEditTransactionPanelEstate getEstate() {
        return estate;
    }

    public void setEstate(AdminEditTransactionPanelEstate estate) {
        this.estate = estate;
    }

    public int getCachedTransactionId() {
        return cachedTransactionId;
    }

    public void setCachedTransactionId(int cachedTransactionId) {
        this.cachedTransactionId = cachedTransactionId;
    }

    private void showAdminPanel(Update update){
        estate = AdminEditTransactionPanelEstate.ADMIN_EDIT_TRANSACTION_PANEL;
        adminPanel.showAdminPanel(update);
    }
}
