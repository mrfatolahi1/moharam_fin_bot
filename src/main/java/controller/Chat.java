package controller;

import database.Loader;
import database.Saver;
import models.Transaction;
import models.TransactionType;
import models.User;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileNotFoundException;
import java.io.IOException;
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

    public void handleNewUpdate(Update update){
        System.out.println("this.estate = " + this.estate);

        if (estate == Estate.NOT_SIGNED_UP){
            User loadedUser = null;
            try {
                loadedUser = Loader.loadUser(update.getMessage().getFrom().getId());
            }catch (FileNotFoundException fileNotFoundException){
                sendSignUpError(update);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            estate = Estate.MAIN_MENU;
        } else

        if (estate == Estate.SIGNING_UP){
            signUp(update);
        } else

        if (estate == Estate.MAIN_MENU){
            if (update.getMessage().getText().equals("تراکنش جدید")){
                showAddNewTransactionMessage(update);
            }
        } else

        if (estate == Estate.ADDING_NEW_TRANSACTION){
            addNewTransAction(update);
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
}
