package controller.adminpanel;

import controller.Chat;
import database.Loader;
import database.Saver;
import models.Transaction;
import models.TransactionType;
import models.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminPanel {
    private final Chat chat;
    private AdminPanelEstate estate;
    private final ArrayList<String> cachedInfo;

    public AdminPanel(Chat chat) {
        this.chat = chat;
        this.estate = AdminPanelEstate.ADMIN_PANEL;
        this.cachedInfo = new ArrayList<>();
    }

    public Chat getChat() {
        return chat;
    }

    public AdminPanelEstate getEstate() {
        return estate;
    }

    public void setEstate(AdminPanelEstate estate) {
        this.estate = estate;
    }

    public void handleNewUpdate(Update update){
        if (estate == AdminPanelEstate.ADMIN_PANEL){
            handleAdminPanelRequest(update);
        } else
            if (estate == AdminPanelEstate.ADMIN_IS_ASKING_FOR_SOMEBODYS_TRANSACTIONS){
                showUserTransactionsInAdminPanel(update);
            } else
                if (estate == AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION){
                    addNewTransactionWithAdmin1(update);
                } else
                    if (estate == AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION_SENDING_USER_ID){
                        addNewTransactionWithAdmin2(update);
                    }
    }

    private void handleAdminPanelRequest(Update update){
        if (update.getMessage().getText().equals("لیست طلبکاران")){
            showListOfCreditors();
        } else
        if (update.getMessage().getText().equals("لیست بدهکاران")){
            showListOfDebtors();
        } else
        if (update.getMessage().getText().equals("لیست کاربران")){
            showListOfAllUsers();
        } else
        if (update.getMessage().getText().equals("تراکنش‌های کاربر")){
            requestWantedUserNumericID();
        } else
        if (update.getMessage().getText().equals("تراکنش جدید (دادن بودجه)")){
            showAddNewTransactionMessageWithAdmin();
        } else
        if (update.getMessage().getText().equals("دیدن کل تراکنش‌ها")){
            showAllTransactions();
        } else
        if (update.getMessage().getText().equals("بازگشت به منوی اصلی")){
            showMainMenu(update);
        } else {
            showAdminPanel(update);
        }
    }

    public void showAdminPanel(Update update){
//        if (!Loader.getAdminsIDs().contains(update.getMessage().getFrom().getId())){
//            String messageText = "شما به این قسمت دسترسی ندارید.";
//            SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
//            try {
//                mainController.sendMessageToUser(sendMessage);
//            } catch (TelegramApiException e) {
//                e.printStackTrace();
//            }
//            return;
//        }
        SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), "پنل مدیریت:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        row1.add("لیست طلبکاران");
        row1.add("لیست بدهکاران");
        row1.add("لیست کاربران");
        row2.add("تراکنش‌های کاربر");
        row2.add("تراکنش جدید (دادن بودجه)");
        row2.add("دیدن کل تراکنش‌ها");
        row3.add("بازگشت به منوی اصلی");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = AdminPanelEstate.ADMIN_PANEL;
        chat.sendMessageToUser(sendMessage);
    }

    private void showListOfCreditors(){
        String messageText = "";
        ArrayList<User> users = Loader.loadAllUsers();

        for (User user : users){
            if (user.calculateBalance() < 0){
                messageText = messageText + user.toString() + "\n--------------------------\n";
            }
        }

        SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText+"\n\nEND");
        chat.sendMessageToUser(sendMessage);
    }

    private void showListOfDebtors(){
        String messageText = "";
        ArrayList<User> users = Loader.loadAllUsers();

        for (User user : users){
            if (user.calculateBalance() > 0){
                messageText = messageText + user.toString() + "\n--------------------------\n";
            }
        }

        SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText+"\n\nEND");
        chat.sendMessageToUser(sendMessage);
    }

    private void showListOfAllUsers(){
        String messageText = "";
        ArrayList<User> users = Loader.loadAllUsers();

        for (User user : users){
            messageText = messageText + user.toString() + "\n--------------------------\n";
        }

        SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText+"\n\nEND");
        chat.sendMessageToUser(sendMessage);
    }

    private void requestWantedUserNumericID(){
        estate = AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION;
        String messageText1 = Loader.getUsersIDsList();
        SendMessage sendMessage1 = new SendMessage(String.valueOf(chat.getChatID()), messageText1);
        chat.sendMessageToUser(sendMessage1);
        String messageText = "آیدی عددی کاربر مورد نظر را وارد کنید (با استفاده از لیست بالا):";
        SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = AdminPanelEstate.ADMIN_IS_ASKING_FOR_SOMEBODYS_TRANSACTIONS;
        chat.sendMessageToUser(sendMessage);
    }

    private void showUserTransactionsInAdminPanel(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminPanel(update);
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
            SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText);
            estate = AdminPanelEstate.ADMIN_IS_ASKING_FOR_SOMEBODYS_TRANSACTIONS;
            chat.sendMessageToUser(sendMessage);
            return;
        }
        if (user == null){
            String messageText = "کاربری با این مشخصات پیدا نشد، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText);
            estate = AdminPanelEstate.ADMIN_IS_ASKING_FOR_SOMEBODYS_TRANSACTIONS;
            chat.sendMessageToUser(sendMessage);
            return;
        }
        ArrayList<Transaction> transactionsList = Loader.getUserTransactions(Loader.loadUser(longId));

        String messageText = "";

        for (Transaction transaction : transactionsList){
            String transactionInfo = "";
            transactionInfo = transactionInfo
                    + transaction.toString()
                    + "\n---------------------------";

            messageText = messageText + transactionInfo + "\n\n";
        }
        SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText+"\n\nEND");
        chat.sendMessageToUser(sendMessage);
        showAdminPanel(update);
    }

    private void showAddNewTransactionMessageWithAdmin(){
        estate = AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION;

        String messageText2 = "لطفا تصویر رسید را ارسال نموده و اطلاعات زیر را هر یک در خطوط جداگانه ارسال کنید (اعداد با ارقام انگلیسی).\n\n[مبلغ کل به ریال]\n[مبلغ کارمزد رابه ریال]\n[توضیحات مبلغ]";
        SendMessage sendMessage2 = new SendMessage(String.valueOf(chat.getChatID()), messageText2);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage2.setReplyMarkup(keyboardMarkup);
        chat.sendMessageToUser(sendMessage2);
    }

    private void addNewTransactionWithAdmin1(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminPanel(update);
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
            SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText);
            chat.sendMessageToUser(sendMessage);
            return;
        }
        requestWantedTransactionUserNumericID();
    }

    private void addNewTransactionWithAdmin2(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminPanel(update);
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
            SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText);
            estate = AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION_SENDING_USER_ID;
            chat.sendMessageToUser(sendMessage);
            return;
        }
        if (user == null){
            String messageText = "کاربری با این مشخصات پیدا نشد، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText);
            estate = AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION_SENDING_USER_ID;
            chat.sendMessageToUser(sendMessage);
            return;
        }

        Transaction transaction = new Transaction(user, Long.parseLong(cachedInfo.get(1)), Integer.parseInt(cachedInfo.get(2)), cachedInfo.get(3), TransactionType.GIVE_TO_USERS, cachedInfo.get(0));
        user.getTransactionsIDsList().add(transaction.getId());
        Saver.saveUser(user);
        Saver.saveTransaction(transaction);
        cachedInfo.clear();

        String messageText = "تراکنش با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText);
        chat.sendMessageToUser(sendMessage);

        showAdminPanel(update);
    }

    private void requestWantedTransactionUserNumericID(){
        estate = AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION_SENDING_USER_ID;
        String messageText1 = Loader.getUsersIDsList();
        SendMessage sendMessage1 = new SendMessage(String.valueOf(chat.getChatID()), messageText1);
        chat.sendMessageToUser(sendMessage1);
        String messageText = "آیدی عددی کاربر مورد نظر را وارد کنید (با استفاده از لیست بالا):";
        SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        chat.sendMessageToUser(sendMessage);
    }

    private void showAllTransactions(){
        ArrayList<Transaction> transactions = Loader.loadAllTransactions();
        String messageText = "";

        for (Transaction transaction : transactions){
            messageText = messageText + transaction.toString() + "\n--------------------------\n";
        }

        SendMessage sendMessage = new SendMessage(String.valueOf(chat.getChatID()), messageText+"\n\nEND");
        chat.sendMessageToUser(sendMessage);

    }

    public void showMainMenu(Update update){
        estate = AdminPanelEstate.ADMIN_PANEL;
        chat.showMainMenu(update);
    }
}
