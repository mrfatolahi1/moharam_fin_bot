package controller.userEditTransactionPanel;

import controller.MainMenu;
import database.Loader;
import database.Saver;
import models.Transaction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class UserEditTransactionPanel {
    private final MainMenu mainMenu;
    private UserEditTransactionPanelEstate estate;
    private int cachedTransactionId;

    public UserEditTransactionPanel(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        this.estate = UserEditTransactionPanelEstate.USER_EDIT_TRANSACTION_PANEL;
    }

    public void handleNewUpdate(Update update){
        if (estate == UserEditTransactionPanelEstate.USER_EDIT_TRANSACTION_PANEL){
            hanleUserEditTransactionPanelRequest(update);
        } else
            if (estate == UserEditTransactionPanelEstate.SENDING_NEW_INVOICE_PHOTO){
                updateTransactionInvoicePhoto(update);
            } else
                if (estate == UserEditTransactionPanelEstate.SENDING_NEW_AMOUNT){
                    updateTransactionAmount(update);
                } else
                    if (estate == UserEditTransactionPanelEstate.SENDING_NEW_DESCRIPTION){
                        updateTransactionDescription(update);
                    } else
                        if (estate == UserEditTransactionPanelEstate.DELETING_TRANSACTION){
                            deleteTransaction(update);
                        }
    }

    private void hanleUserEditTransactionPanelRequest(Update update){
        if (update.getMessage().getText().equals("تصویر فاکتور")){
            requestNewInvoicePhoto();
        } else
        if (update.getMessage().getText().equals("مبلغ")){
            requestAmount();
        } else
        if (update.getMessage().getText().equals("توضیحات")){
            requestNewDescription();
        } else
        if (update.getMessage().getText().equals("بازگشت به منوی اصلی")){
            showMainMenu(update);
        } else
        if (update.getMessage().getText().equals("حذف تراکنش")){
            requestDeleteConfirmation();
        } else {
            estate = UserEditTransactionPanelEstate.USER_EDIT_TRANSACTION_PANEL;
        }
    }

    public void showUserEditTransactionPanel(int transactionId){
        Transaction transaction = Loader.loadTransaction(transactionId, false);
        SendPhoto sendPhoto = new SendPhoto(String.valueOf(mainMenu.getChatID()), new InputFile(transaction.getFactorImageFileId()));
        sendPhoto.setCaption(transaction.toString());
        mainMenu.sendMessageToUser(sendPhoto);

        SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), "قسمت مورد نظر جهت ویرایش را انتخاب کنید:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        row1.add("تصویر فاکتور");
        row1.add("مبلغ");
        row1.add("توضیحات");
        row2.add("حذف تراکنش");
        row3.add("بازگشت به منوی اصلی");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = UserEditTransactionPanelEstate.USER_EDIT_TRANSACTION_PANEL;
        this.cachedTransactionId = transactionId;
        mainMenu.sendMessageToUser(sendMessage);
    }

    private void requestNewInvoicePhoto(){
        estate = UserEditTransactionPanelEstate.SENDING_NEW_INVOICE_PHOTO;
        String messageText = "تصویر جدید را ارسال کنید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        mainMenu.sendMessageToUser(sendMessage);
    }

    private void updateTransactionInvoicePhoto(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showMainMenu(update);
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
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            mainMenu.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تغییرات با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
        mainMenu.sendMessageToUser(sendMessage);
        showMainMenu(update);
    }

    private void requestAmount(){
        estate = UserEditTransactionPanelEstate.SENDING_NEW_AMOUNT;
        String messageText = "مبلغ جدید را ارسال کنید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        mainMenu.sendMessageToUser(sendMessage);
    }

    private void updateTransactionAmount(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showMainMenu(update);
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
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            mainMenu.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تغییرات با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
        mainMenu.sendMessageToUser(sendMessage);
        showMainMenu(update);
    }

    private void requestNewDescription(){
        estate = UserEditTransactionPanelEstate.SENDING_NEW_DESCRIPTION;
        String messageText = "توضیحات جدید را ارسال کنید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        mainMenu.sendMessageToUser(sendMessage);
    }

    private void updateTransactionDescription(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showMainMenu(update);
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
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            mainMenu.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تغییرات با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
        mainMenu.sendMessageToUser(sendMessage);
        showMainMenu(update);
    }

    private void requestDeleteConfirmation(){
        estate = UserEditTransactionPanelEstate.DELETING_TRANSACTION;
        String messageText = "آیا از حذف تراکنش اطمینان دارید؟";
        SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
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
        mainMenu.sendMessageToUser(sendMessage);
    }

    private void deleteTransaction(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showMainMenu(update);
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
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            mainMenu.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تراکنش با موفقیت حذف شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
        mainMenu.sendMessageToUser(sendMessage);
        showMainMenu(update);
    }

    public MainMenu getMainMenu() {
        return mainMenu;
    }

    private void showMainMenu(Update update){
        estate = UserEditTransactionPanelEstate.USER_EDIT_TRANSACTION_PANEL;
        mainMenu.showMainMenu(update);
    }
}
