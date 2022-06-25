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
        System.out.println("this.estate = " + this.estate);

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
            addNewTransAction(update);
        }

    }

    private void handleMainMenuRequest(Update update){
        if (update.getMessage().getText().equals("تراکنش جدید")){
            showAddNewTransactionMessage(update);
        } else
        if (update.getMessage().getText().equals("لیست تراکنش‌ها")){
            showUserTransactions(update);
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
        estate = Estate.MAIN_MENU;
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
        try {

            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showAddNewTransactionMessage(Update update){
        estate = Estate.ADDING_NEW_TRANSACTION;
        String messageText = "لطفا تصویر فاکتور را ارسال نموده و مبلغ کل را با ارقام انگلیسی به ریال در خط اول کپشن آن بنویسید. توضیحات مبلغ هزینه شده را در خط بعدی فاکتور بنویسید (این قسمت می‌تواند هر چقدر که لازم است زیاد باشد.)";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        try {
            mainController.sendMessageToUser(sendMessage);
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

    private void addNewTransAction(Update update){
        Scanner scanner = new Scanner(update.getMessage().getCaption());
        String photoFileID = update.getMessage().getPhoto().get(0).getFileId();
        long amount = Long.parseLong(scanner.nextLine());
        String description = scanner.nextLine();

        Transaction transaction = new Transaction(this.user, amount, 0, description, TransactionType.EXPENDITURE, photoFileID);
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
