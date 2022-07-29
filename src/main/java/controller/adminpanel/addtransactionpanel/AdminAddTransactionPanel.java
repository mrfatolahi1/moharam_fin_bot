package controller.adminpanel.addtransactionpanel;

import controller.adminpanel.AdminPanel;
import database.Loader;
import database.Saver;
import models.Transaction;
import models.TransactionType;
import models.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminAddTransactionPanel {
    private final AdminPanel adminPanel;
    private AdminAddTransactionPanelEstate estate;
    private final ArrayList<String> cachedInfo;

    public AdminAddTransactionPanel(AdminPanel adminPanel) {
        this.adminPanel = adminPanel;
        this.cachedInfo = new ArrayList<>();
    }

    public void showAdminAddTransactionPanel(){
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), "قسمت مورد نظر را انتخاب کنید:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        row1.add("تراکنش ورودی");
        row1.add("تخصیص بودجه به خدام");
        row2.add("ثبت تراکنش به جای خدام");
        row3.add("بازگشت به پنل مدیریت");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
//        keyboard.add(row4);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = AdminAddTransactionPanelEstate.ADMIN_ADD_TRANSACTION_PANEL;
        adminPanel.sendMessageToUser(sendMessage);
    }

    public void handleNewUpdate(Update update){
        System.out.println("estate = " + estate);
        if (estate == AdminAddTransactionPanelEstate.ADMIN_ADD_TRANSACTION_PANEL){
            handleAdminAddTransactionPanelRequest(update);
        } else
        if (estate == AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_INCOME_TRANSACTION){
            addNewIncomeTransaction(update);
        } else
        if (estate == AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_GIVE_TO_USERS_TRANSACTION_SENDING_INFO){
            saveNewGiveToUsersTransactionInfo(update);
        } else
        if (estate == AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_GIVE_TO_USERS_TRANSACTION_SENDING_USER_ID){
            addNewGiveToUsersTransaction(update);
        } else
        if (estate == AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_EXPENDITURE_TRANSACTION_SENDING_INFO){
            saveNewExpenditureTransactionInfo(update);
        } else
        if (estate == AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_EXPENDITURE_TRANSACTION_SENDING_USER_ID){
            addNewExpenditureTransaction(update);
        }
    }

    private void handleAdminAddTransactionPanelRequest(Update update){
        if (update.getMessage().getText().equals("تراکنش ورودی")){
            showAddIncomeTransactionMessage();
        } else
        if (update.getMessage().getText().equals("تخصیص بودجه به خدام")){
            showAddGiveToUsersTransactionMessage();
        } else
        if (update.getMessage().getText().equals("ثبت تراکنش به جای خدام")){
            showAddExpenditureTransactionMessage();
        } else
        if (update.getMessage().getText().equals("بازگشت به پنل مدیریت")){
            showAdminPanel(update);
        } else {
            estate = AdminAddTransactionPanelEstate.ADMIN_ADD_TRANSACTION_PANEL;
        }
    }

    private void showAddIncomeTransactionMessage(){
        estate = AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_INCOME_TRANSACTION;

        String messageText2 = "لطفا تصویر رسید را ارسال نموده و اطلاعات زیر را هر یک در خطوط جداگانه در کپشن آن ارسال کنید (اعداد با ارقام انگلیسی).\n\n[مبلغ کل به ریال]\n[توضیحات]";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText2);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void addNewIncomeTransaction(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminAddTransactionPanel();
                return;
            }
        }
        int transactionId = 0;
        try {
            Scanner scanner = new Scanner(update.getMessage().getCaption());
            String photoFileID = update.getMessage().getPhoto().get(0).getFileId();
            long amount = Long.parseLong(scanner.nextLine());
            String description = scanner.nextLine();

            Transaction transaction = new Transaction(null, amount, 0, description, TransactionType.INCOME, photoFileID);
            transactionId = transaction.getId();
            Saver.saveTransaction(transaction);
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        String messageText = "تراکنش با موفقیت ثبت شد.\n" + "آیدی تراکنش: " + transactionId;
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        adminPanel.sendMessageToUser(sendMessage);
        showAdminAddTransactionPanel();
    }
//
    private void showAddGiveToUsersTransactionMessage(){
        estate = AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_GIVE_TO_USERS_TRANSACTION_SENDING_INFO;

        String messageText2 = "لطفا تصویر رسید را ارسال نموده و اطلاعات زیر را هر یک در خطوط جداگانه در کپشن آن ارسال کنید (اعداد با ارقام انگلیسی).\n\n[مبلغ کل به ریال]\n[کارمزد]\n[توضیحات]";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText2);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void saveNewGiveToUsersTransactionInfo(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminAddTransactionPanel();
                return;
            }
        }
        try {
            Scanner scanner = new Scanner(update.getMessage().getCaption());
            String photoFileID = update.getMessage().getPhoto().get(0).getFileId();
            long amount = Long.parseLong(scanner.nextLine());
            int fee = Integer.parseInt(scanner.nextLine());
            String description = scanner.nextLine();

            cachedInfo.add(photoFileID);
            cachedInfo.add(String.valueOf(amount));
            cachedInfo.add(String.valueOf(fee));
            cachedInfo.add(description);
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        requestNewGiveToUsersTransactionUserNumericID();
    }

    private void requestNewGiveToUsersTransactionUserNumericID(){
        estate = AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_GIVE_TO_USERS_TRANSACTION_SENDING_USER_ID;
        String messageText1 = Loader.getUsersIDsList();
        SendMessage sendMessage1 = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText1);
        adminPanel.sendMessageToUser(sendMessage1);
        String messageText = "آیدی عددی کاربر مورد نظر را وارد کنید (با استفاده از لیست بالا):";
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

    private void addNewGiveToUsersTransaction(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminAddTransactionPanel();
                return;
            }
        }
        String userId = update.getMessage().getText();
        long longId = 0;
        User user = null;
        try {
            longId = Long.parseLong(userId);
            user = Loader.loadUser(longId);
        } catch (Exception e){
            String messageText = "فرمت اطلاعات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_GIVE_TO_USERS_TRANSACTION_SENDING_USER_ID;
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        if (user == null){
            String messageText = "کاربری با این مشخصات پیدا نشد، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_GIVE_TO_USERS_TRANSACTION_SENDING_USER_ID;
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }

        Transaction transaction = new Transaction(user, Long.parseLong(cachedInfo.get(1)), Integer.parseInt(cachedInfo.get(2)), cachedInfo.get(3), TransactionType.GIVE_TO_USERS, cachedInfo.get(0));
        user.getTransactionsIDsList().add(transaction.getId());
        Saver.saveUser(user);
        Saver.saveTransaction(transaction);
        cachedInfo.clear();

        String messageText = "تراکنش با موفقیت ثبت شد.\n" + "آیدی تراکنش: " + transaction.getId();
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        adminPanel.sendMessageToUser(sendMessage);
        sendTransactionConfirmation(transaction);

        showAdminAddTransactionPanel();
    }

    private void showAddExpenditureTransactionMessage(){
        estate = AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_EXPENDITURE_TRANSACTION_SENDING_INFO;

        String messageText2 = "لطفا تصویر رسید را ارسال نموده و اطلاعات زیر را هر یک در خطوط جداگانه در کپشن آن ارسال کنید (اعداد با ارقام انگلیسی).\n\n[مبلغ کل به ریال]\n[کارمزد]\n[توضیحات]";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText2);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void saveNewExpenditureTransactionInfo(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminAddTransactionPanel();
                return;
            }
        }
        try {
            Scanner scanner = new Scanner(update.getMessage().getCaption());
            String photoFileID = update.getMessage().getPhoto().get(0).getFileId();
            long amount = Long.parseLong(scanner.nextLine());
            int fee = Integer.parseInt(scanner.nextLine());
            String description = scanner.nextLine();

            cachedInfo.add(photoFileID);
            cachedInfo.add(String.valueOf(amount));
            cachedInfo.add(String.valueOf(fee));
            cachedInfo.add(description);
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        requestExpenditureTransactionUserNumericID();
    }

    private void requestExpenditureTransactionUserNumericID(){
        estate = AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_EXPENDITURE_TRANSACTION_SENDING_USER_ID;
        String messageText1 = Loader.getUsersIDsList();
        SendMessage sendMessage1 = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText1);
        adminPanel.sendMessageToUser(sendMessage1);
        String messageText = "آیدی عددی کاربر مورد نظر را وارد کنید (با استفاده از لیست بالا):";
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

    private void addNewExpenditureTransaction(Update update){
        System.out.println("AAAA");
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminAddTransactionPanel();
                return;
            }
        }
        String userId = update.getMessage().getText();
        long longId = 0;
        User user = null;
        try {
            longId = Long.parseLong(userId);
            user = Loader.loadUser(longId);
        } catch (Exception e){
            e.printStackTrace();
            String messageText = "فرمت اطلاعات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_EXPENDITURE_TRANSACTION_SENDING_USER_ID;
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        if (user == null){
            String messageText = "کاربری با این مشخصات پیدا نشد، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = AdminAddTransactionPanelEstate.ADMIN_IS_ADDING_NEW_EXPENDITURE_TRANSACTION_SENDING_USER_ID;
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }

        Transaction transaction = new Transaction(user, Long.parseLong(cachedInfo.get(1)), Integer.parseInt(cachedInfo.get(2)), cachedInfo.get(3), TransactionType.EXPENDITURE, cachedInfo.get(0));
        user.getTransactionsIDsList().add(transaction.getId());
        Saver.saveUser(user);
        Saver.saveTransaction(transaction);
        cachedInfo.clear();

        String messageText = "تراکنش با موفقیت ثبت شد.\n" + "آیدی تراکنش: " + transaction.getId();
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);

        adminPanel.sendMessageToUser(sendMessage);
        sendTransactionConfirmation(transaction);
        showAdminAddTransactionPanel();
    }

    private void sendTransactionConfirmation(Transaction transaction){
        SendPhoto sendPhoto = new SendPhoto(String.valueOf(transaction.getUser().getId()), new InputFile(transaction.getFactorImageFileId()));
        sendPhoto.setCaption("تراکنش‌ زیر توسط مدیر برای شما ثبت شده است، در صورت هر گونه مغایرت مراتب را سریعا به پشتیبانی بات اطلاع دهید. \n\n" + transaction.toString());
        adminPanel.sendMessageToUser(sendPhoto);
    }

    private void showAdminPanel(Update update){
        this.estate = AdminAddTransactionPanelEstate.ADMIN_ADD_TRANSACTION_PANEL;
        adminPanel.showAdminPanel(update);
    }
}
