package controller;

import database.Loader;
import database.Saver;
import models.PersianDate;
import models.Transaction;
import models.TransactionType;
import models.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class Chat {
    private final MainController mainController;
    private final long chatID;
    private final User user;
    private Estate estate;

    public Chat(MainController mainController, long chatID, User user) {
        this.mainController = mainController;
        this.chatID = chatID;
        this.user = user;
        this.estate = Estate.NOT_SIGNED_UP;
    }

    public Chat(MainController mainController, long chatID, User user, Estate estate) {
        this.mainController = mainController;
        this.chatID = chatID;
        this.user = user;
        this.estate = estate;
    }

    public void handleNewUpdate(Update update){
        System.out.println("\nthis.estate = " + this.estate);
        try {
            System.out.println("update = " + update.getMessage().getText());
        }catch (Exception ignored){
            System.out.println("NO OOO");
        }

        if (estate == Estate.NOT_SIGNED_UP){
            sendSignUpError(update);
        } else
        if (estate == Estate.SIGNING_UP){
            signUp(update);
        } else
        if (estate == Estate.MAIN_MENU){
            handleMainMenuRequest(update);
        } else
        if (estate == Estate.ADDING_NEW_TRANSACTION){
            addNewTransaction(update);
        } else
        if (estate == Estate.ADMIN_PANEL){
            handleAdminPanelRequest(update);
        } else
        if (estate == Estate.ADMIN_IS_ASKING_FOR_SOMEBODYS_TRANSACTIONS){
            showUserTransactions(Long.parseLong(update.getMessage().getText()));
        } else
        if (estate == Estate.ADMIN_IS_ADDING_NEW_TRANSACTION){
            addNewTransactionWithAdmin(update);
        }

    }

    private void handleMainMenuRequest(Update update){
        if (update.getMessage().getText().equals("تراکنش جدید")){
            showAddNewTransactionMessageForUser();
        } else
        if (update.getMessage().getText().equals("لیست تراکنش‌ها")){
            showUserTransactions(update);
        } else
        if (update.getMessage().getText().equals("پنل مدیریت")){
            showAdminPanel(update);
        }else {
            showMainMenu(update);
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

    private void sendSignUpError(Update update){
        this.estate = Estate.SIGNING_UP;
        String messageText = "خادم محترم، مشخصات شما در سامانه ثبت نشده است، لطفا مشخصات زیر را به ترتیب (در خطوط جداگانه) در یک پیام ارسال نمایید.\n\n نام و نام خانوادگی \n شماره همراه\n ایمیل";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        try {

            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void signUp(Update update){
        Scanner scanner = new Scanner(update.getMessage().getText());
        String name = scanner.nextLine();
        String phoneNumber = scanner.nextLine();
        String email = scanner.nextLine();
        String username = update.getMessage().getFrom().getUserName();
        long userID = update.getMessage().getFrom().getId();

        User user = new User(userID, name, username, email, phoneNumber);
        Saver.saveUser(user);

        String messageText = "مشخصات شما با موفقیت در سامانه ثبت شد، حالا می‌توانید تراکنش‌های خود را ثبت کنید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        showMainMenu(update);
    }

    private void showMainMenu(Update update){
        String messageText = "منوی اصلی:";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("تراکنش جدید");
        row.add("لیست تراکنش‌ها");
        row.add("پنل مدیریت");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = Estate.MAIN_MENU;
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showAddNewTransactionMessageForUser(){
        estate = Estate.ADDING_NEW_TRANSACTION;
        String messageText = "لطفا تصویر فاکتور را ارسال نموده و مبلغ کل را با ارقام انگلیسی به ریال در خط اول کپشن آن بنویسید. توضیحات مبلغ هزینه شده را در خط بعدی فاکتور بنویسید (این قسمت می‌تواند هر چقدر که لازم است زیاد باشد.)";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showAddNewTransactionMessageWithAdmin(){
        estate = Estate.ADMIN_IS_ADDING_NEW_TRANSACTION;
        String messageText1 = Loader.getUsersIDsList();
        SendMessage sendMessage1 = new SendMessage(String.valueOf(chatID), messageText1);
        try {
            mainController.sendMessageToUser(sendMessage1);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        String messageText2 = "لطفا تصویر رسید را ارسال نموده و اطلاعات زیر را هر یک در خطوط جداگانه ارسال کنید (اعداد با ارقام انگلیسی).\n\n[مبلغ کل به ریال]\n[مبلغ کارمزد رابه ریال]\n[آیدی عددی خادم مورد نظر با توجه به لیست بالا]\n[توضیحات مبلغ]";
        SendMessage sendMessage2 = new SendMessage(String.valueOf(chatID), messageText2);
        try {
            mainController.sendMessageToUser(sendMessage2);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showUserTransactions(Update update){
        ArrayList<Transaction> transactionsList = Loader.getUserTransactions(Loader.loadUser(update.getMessage().getFrom().getId()));

        String messageText = "";

        for (Transaction transaction : transactionsList){
            String transactionInfo = "";
            transactionInfo = transactionInfo
                    + "مبلغ: " + transaction.getAmount() + " " + "ریال"
                    + "\n" + "تاریخ: "
                    + transaction.getPersianDate().getDay()
                    + " " + PersianDate.getMonthNameByItNumber(transaction.getPersianDate().getMonth())
                    + " " + transaction.getPersianDate().getYear()
                    + "\n" + "زمان: "
                    + transaction.getTime().getHour() + ":"
                    + transaction.getTime().getMinute() + ":"
                    + transaction.getTime().getSecond() + "\n"
                    + "وضعیت تایید: " + transaction.isVerificated()
                    + "\n---------------------------";

            messageText = messageText + transactionInfo + "\n\n";
        }
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText+"\n\nEND");
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showUserTransactions(long userId){
        ArrayList<Transaction> transactionsList = Loader.getUserTransactions(Loader.loadUser(userId));

        String messageText = "";

        for (Transaction transaction : transactionsList){
            String transactionInfo = "";
            transactionInfo = transactionInfo
                    + "مبلغ: " + transaction.getAmount() + " " + "ریال"
                    + "\n" + "تاریخ: "
                    + transaction.getPersianDate().getDay()
                    + " " + PersianDate.getMonthNameByItNumber(transaction.getPersianDate().getMonth())
                    + " " + transaction.getPersianDate().getYear()
                    + "\n" + "زمان: "
                    + transaction.getTime().getHour() + ":"
                    + transaction.getTime().getMinute() + ":"
                    + transaction.getTime().getSecond() + "\n"
                    + "وضعیت تایید: " + transaction.isVerificated()
                    + "\n---------------------------";

            messageText = messageText + transactionInfo + "\n\n";
        }
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText+"\n\nEND");
        estate = Estate.ADMIN_PANEL;
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showAdminPanel(Update update){
        if (!Loader.getAdminsIDs().contains(update.getMessage().getFrom().getId())){
            String messageText = "شما به این قسمت دسترسی ندارید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
            try {
                mainController.sendMessageToUser(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), "پنل مدیریت:");
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
        estate = Estate.ADMIN_PANEL;
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void addNewTransaction(Update update){
        Scanner scanner = new Scanner(update.getMessage().getCaption());
        String photoFileID = update.getMessage().getPhoto().get(0).getFileId();
        long amount = Long.parseLong(scanner.nextLine());
        String description = scanner.nextLine();

        Transaction transaction = new Transaction(this.user, amount, 0, description, TransactionType.EXPENDITURE, photoFileID);
        user.getTransactionsIDsList().add(transaction.getId());
        Saver.saveUser(user);
        Saver.saveTransaction(transaction);
        String messageText = "تراکنش با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        estate = Estate.MAIN_MENU;
    }

    private void addNewTransactionWithAdmin(Update update){
        Scanner scanner = new Scanner(update.getMessage().getCaption());
        String photoFileID = update.getMessage().getPhoto().get(0).getFileId();
        long amount = Long.parseLong(scanner.nextLine());
        int fee = Integer.parseInt(scanner.nextLine());
        User user = Loader.loadUser(Long.parseLong(scanner.nextLine()));
        String description = scanner.nextLine();

        Transaction transaction = new Transaction(this.user, amount, fee, description, TransactionType.INCOME, photoFileID);
        user.getTransactionsIDsList().add(transaction.getId());
        Saver.saveUser(user);
        Saver.saveTransaction(transaction);
        String messageText = "تراکنش با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        estate = Estate.ADMIN_PANEL;
        showAdminPanel(update);
    }

    private void showListOfCreditors(){
        String messageText = "";
        ArrayList<User> users = Loader.loadAllUsers();

        for (User user : users){
            if (user.calculateBalance() < 0){
                messageText = messageText + user.toString() + "\n--------------------------\n";
            }
        }

        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText+"\n\nEND");
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showListOfDebtors(){
        String messageText = "";
        ArrayList<User> users = Loader.loadAllUsers();

        for (User user : users){
            if (user.calculateBalance() > 0){
                messageText = messageText + user.toString() + "\n--------------------------\n";
            }
        }

        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText+"\n\nEND");
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showListOfAllUsers(){
        String messageText = "";
        ArrayList<User> users = Loader.loadAllUsers();

        for (User user : users){
            messageText = messageText + user.toString() + "\n--------------------------\n";
        }

        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText+"\n\nEND");
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void requestWantedUserNumericID(){
        String messageText = "آیدی عددی کاربر مورد نظر را وارد کنید:";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        estate = Estate.ADMIN_IS_ASKING_FOR_SOMEBODYS_TRANSACTIONS;
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showUserInfo(Update update){
        User loadedUser = Loader.loadUser(Long.parseLong(update.getMessage().getText()));
        if (loadedUser != null){
            String messageText = loadedUser.toString();
            SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText+"\nEND");
            estate = Estate.ADMIN_IS_ASKING_FOR_SOMEBODYS_TRANSACTIONS;
            try {
                mainController.sendMessageToUser(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            String messageText = "کاربری با این آیدی یافت نشد، به پنل ادمین هدایت می‌شوید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
            try {
                mainController.sendMessageToUser(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        estate = Estate.ADMIN_PANEL;
    }

    private void showAllTransactions(){
        ArrayList<Transaction> transactions = Loader.loadAllTransactions();
        String messageText = "";

        for (Transaction transaction : transactions){
            messageText = messageText + transaction.toString() + "\n--------------------------\n";
        }

        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText+"\n\nEND");
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public long getChatID() {
        return chatID;
    }

    public User getPerson() {
        return user;
    }

    public MainController getMainController() {
        return mainController;
    }

    public User getUser() {
        return user;
    }

    public Estate getEstate() {
        return estate;
    }

    public void setEstate(Estate estate) {
        this.estate = estate;
    }
}
