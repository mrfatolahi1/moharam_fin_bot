package controller;

import controller.adminpanel.AdminPanel;
import controller.userEditTransactionPanel.UserEditTransactionPanel;
import database.Loader;
import database.Saver;
import models.Transaction;
import models.TransactionType;
import models.User;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class MainMenu {
    private final MainController mainController;
    private final long chatID;
    private final User user;
    private MainMenuEstate estate;
    private final AdminPanel adminPanel;
    private final UserEditTransactionPanel userEditTransactionPanel;

    public MainMenu(MainController mainController, long chatID, User user) {
        this.mainController = mainController;
        this.chatID = chatID;
        this.user = user;
        this.estate = MainMenuEstate.NOT_SIGNED_UP;
        this.adminPanel = new AdminPanel(this);
        this.userEditTransactionPanel = new UserEditTransactionPanel(this);
    }

    public MainMenu(MainController mainController, long chatID, User user, MainMenuEstate estate) {
        this.mainController = mainController;
        this.chatID = chatID;
        this.user = user;
        this.estate = estate;
        this.adminPanel = new AdminPanel(this);
        this.userEditTransactionPanel = new UserEditTransactionPanel(this);
    }

    public void handleNewUpdate(Update update){
        System.out.println("name: " + user.getName());
        System.out.println("this.estate = " + this.estate);
        try {
            System.out.println("update = " + update.getMessage().getText());
        }catch (Exception ignored){
            try {
                System.out.println("update = " + update.getMessage().getCaption());
            }catch (Exception w){
                System.out.println("NO TEXT");
            }
        }
        System.out.println();

        if (estate == MainMenuEstate.NOT_SIGNED_UP){
            sendSignUpError(update);
        } else
        if (estate == MainMenuEstate.SIGNING_UP){
            signUp(update);
        } else
        if (estate == MainMenuEstate.MAIN_MENU){
            handleMainMenuRequest(update);
        } else
        if (estate == MainMenuEstate.ADDING_NEW_TRANSACTION){
            addNewTransaction(update);
        } else
        if (estate == MainMenuEstate.ASKING_FOR_TRANSACTION_INFO){
            showTransactionInfo(update);
        } else
        if (estate == MainMenuEstate.ASKING_FOR_TRANSACTION_INFO_FOR_EDIT){
            showUserEditTransactionPanel(update);
        } else
        if (estate == MainMenuEstate.ADMIN_PANEL){
            adminPanel.handleNewUpdate(update);
        } else
        if (estate == MainMenuEstate.OPENING_USER_EDIT_TRANSACTION_PANEL){
            showUserEditTransactionPanel(update);
        } else
        if (estate == MainMenuEstate.USER_EDIT_TRANSACTION_PANEL){
            userEditTransactionPanel.handleNewUpdate(update);
        }
    }

    private void handleMainMenuRequest(Update update){
        if (update.getMessage().getText().equals("تراکنش جدید")){
            showAddNewTransactionMessageForUser();
        } else
        if (update.getMessage().getText().equals("لیست تراکنش‌ها")){
            sendAllUserTransactionsExcelFile(update);
        } else
        if (update.getMessage().getText().equals("مشاهده تراکنش")){
            requestWantedTransactionID(1);
        } else
        if (update.getMessage().getText().equals("ویرایش تراکنش")){
            requestWantedTransactionID(2);
        } else
        if (update.getMessage().getText().equals("تراز مالی")){
            showUserBalance(update);
        } else
        if (update.getMessage().getText().equals("پنل مدیریت")){
            showAdminPanel(update);
        } else {
            showMainMenu(update);
        }
    }

    private void sendSignUpError(Update update){
        this.estate = MainMenuEstate.SIGNING_UP;
        String messageText = "خادم محترم، مشخصات شما در سامانه ثبت نشده است، لطفا مشخصات زیر را به ترتیب (در خطوط جداگانه) در یک پیام ارسال نمایید.\n\n نام و نام خانوادگی \n شماره همراه\n ایمیل";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        try {

            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void signUp(Update update){
        try{
            Scanner scanner = new Scanner(update.getMessage().getText());
            String name = scanner.nextLine();
            String phoneNumber = scanner.nextLine();
            String email = scanner.nextLine();
            String username = update.getMessage().getFrom().getUserName();
            long userID = update.getMessage().getFrom().getId();

            User user = new User(userID, name, username, email, phoneNumber);
            Saver.saveUser(user);
        }catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید۱.";
            SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
            try {
                mainController.sendMessageToUser(sendMessage);
            } catch (TelegramApiException q) {
                e.printStackTrace();
            }
            return;
        }

        String messageText = "مشخصات شما با موفقیت در سامانه ثبت شد، حالا می‌توانید تراکنش‌های خود را ثبت کنید.";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        showMainMenu(update);
    }

    public void showMainMenu(Update update){
        String messageText = "منوی اصلی:";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add("تراکنش جدید");
        row1.add("لیست تراکنش‌ها");
        row1.add("مشاهده تراکنش");
        row2.add("ویرایش تراکنش");
        row2.add("تراز مالی");
        row2.add("پنل مدیریت");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = MainMenuEstate.MAIN_MENU;
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showAddNewTransactionMessageForUser(){
        estate = MainMenuEstate.ADDING_NEW_TRANSACTION;
        String messageText = "لطفا تصویر فاکتور را ارسال نموده و اطلاعات خواسته شده را در خطوط جداگانه در کپشن آن بنویسید.\n\n[مبلغ به ریال با ارقام انگلیسی]\n[توضیحات]";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendAllUserTransactionsExcelFile(Update update) {
        ArrayList<Transaction> transactions = Loader.loadUserTransactions(user);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("تراکنش‌ها");

        XSSFRow header = spreadsheet.createRow(0);
        header.createCell(0).setCellValue("آیدی");
        header.createCell(1).setCellValue("نام و نام خانوادگی کاربر");
        header.createCell(2).setCellValue("نام‌ کاربری تلگرام کاربر");
        header.createCell(3).setCellValue("مبلغ");
        header.createCell(4).setCellValue("کارمزد");
        header.createCell(5).setCellValue("تاریخ");
        header.createCell(6).setCellValue("زمان");
        header.createCell(7).setCellValue("وضعیت تایید");
        header.createCell(8).setCellValue("دارای فاکتور کاغذی");
        header.createCell(9).setCellValue("توضیحات کاربر");
        header.createCell(10).setCellValue("توضیحات مدیر");

        for (int i = 1 ; i <= transactions.size() ; i++){
            Transaction transaction = transactions.get(i-1);
            XSSFRow row = spreadsheet.createRow(i);
            row.createCell(0).setCellValue(String.valueOf(transaction.getId()));
            row.createCell(1).setCellValue(transaction.getUser().getName());
            row.createCell(2).setCellValue(transaction.getUser().getUsername());
            row.createCell(3).setCellValue(String.valueOf(transaction.getAmount()));
            row.createCell(4).setCellValue(String.valueOf(transaction.getFee()));
            row.createCell(5).setCellValue(String.valueOf(transaction.getReadableDate()));
            row.createCell(6).setCellValue(String.valueOf(transaction.getReadableTime()));
            row.createCell(7).setCellValue(String.valueOf(transaction.isVerificated()));
            row.createCell(8).setCellValue(String.valueOf(transaction.isHasPaperInvoice()));
            row.createCell(9).setCellValue(String.valueOf(transaction.getDescription()));
            row.createCell(10).setCellValue(String.valueOf(transaction.getAdminDescription()));
        }
        try {
            File file = new File("Excels/" + adminPanel.getChat().getUser().getId() + ".xlsx");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            workbook.write(out);
            out.close();

            SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
            sendDocument.setCaption("خروجی اکسل لیست تراکنش‌های انجام شده توسط خادم\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
            adminPanel.sendMessageToUser(sendDocument);
        } catch (IOException ignored){}
    }

    private void showUserBalance(Update update){
        long balance = Loader.loadUser(update.getMessage().getFrom().getId()).calculateBalance();
        String messageText;

        if (balance < 0){
            messageText = "شما " + Math.abs(balance) + "ریال طلبکار هستید.";
        } else
            if (balance > 0){
                messageText = "شما" + balance + "ریال بدهکار هستید.";
            } else {
                messageText = "تراز مالی شما صفر است و شما بدهکار یا طلبکار نیستید.";
            }


        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showAdminPanel(Update update){
        estate = MainMenuEstate.ADMIN_PANEL;
        adminPanel.showAdminPanel();
    }

    private void addNewTransaction(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showMainMenu(update);
                return;
            }
        }
        try {
            Scanner scanner = new Scanner(update.getMessage().getCaption());
            String photoFileID = update.getMessage().getPhoto().get(0).getFileId();
            long amount = Long.parseLong(scanner.nextLine());
            String description = scanner.nextLine();

            Transaction transaction = new Transaction(this.user, amount, 0, description, TransactionType.EXPENDITURE, photoFileID);
            user.getTransactionsIDsList().add(transaction.getId());
            Saver.saveUser(user);
            Saver.saveTransaction(transaction);
        } catch (Exception e){
            String messageText = "فرمت مشخصات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
            try {
                mainController.sendMessageToUser(sendMessage);
            } catch (TelegramApiException q) {
                e.printStackTrace();
            }
            return;
        }
        String messageText = "تراکنش با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        showMainMenu(update);
    }

    private void requestWantedTransactionID(int type){
        if (type == 1){
            estate = MainMenuEstate.ASKING_FOR_TRANSACTION_INFO;
        } else {
            estate = MainMenuEstate.ASKING_FOR_TRANSACTION_INFO_FOR_EDIT;
        }
        String messageText1 = "آیدی تراکنش‌های شما، اطلاعات کامل‌تر را می‌توانید از طریق گزینه «لیست تراکنش‌ها» در منوی اصلی ببینید.\n";
        for (int transactionID : user.getTransactionsIDsList()){
            Transaction transaction = Loader.loadTransaction(transactionID);
            if (transaction != null){
                messageText1 = messageText1 + transactionID + ": " + transaction.getDescription() + "\n";
            }
        }
        SendMessage sendMessage1 = new SendMessage(String.valueOf(chatID), messageText1);
        try {
            mainController.sendMessageToUser(sendMessage1);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        String messageText;
        if (type == 1){
            messageText = "آیدی تراکنش مورد نظر را وارد کنید (با استفاده از لیست بالا):\n\n(چنانچه مدیر هستید در اینجا توانایی دیدن تراکنش‌های در دسترس مدیران را نیز دارید.)";
        } else {
            messageText = "آیدی تراکنش مورد نظر را وارد کنید (با استفاده از لیست بالا):\n\n(چنانچه مدیر هستید در اینجا توانایی ویرایش تراکنش‌های در دسترس مدیران را ندارید.)";
        }
        SendMessage sendMessage = new SendMessage(String.valueOf(chatID), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        try {
            mainController.sendMessageToUser(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showTransactionInfo(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showMainMenu(update);
                return;
            }
        }

        int longId = 0;
        Transaction transaction = null;
        try {
            String transactionId = update.getMessage().getText();
            longId = Integer.parseInt(transactionId);
            transaction = Loader.loadTransaction(longId);
        } catch (Exception e){
            String messageText = "فرمت اطلاعات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = MainMenuEstate.ASKING_FOR_TRANSACTION_INFO;
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        if (transaction == null || transaction.getUser().getId() != user.getId()){
            String messageText = "شما تراکنشی با این آیدی ندارید، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = MainMenuEstate.ASKING_FOR_TRANSACTION_INFO;
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }

        SendPhoto sendPhoto = new SendPhoto(String.valueOf(chatID), new InputFile(transaction.getFactorImageFileId()));
        sendPhoto.setCaption(transaction.toString());
        sendMessageToUser(sendPhoto);
        showMainMenu(update);
    }

    private void showUserEditTransactionPanel(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showMainMenu(update);
                return;
            }
        }

        int longId = 0;
        Transaction transaction = null;
        try {
            String transactionId = update.getMessage().getText();
            longId = Integer.parseInt(transactionId);
            transaction = Loader.loadTransaction(longId);
        } catch (Exception e){
            String messageText = "فرمت اطلاعات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = MainMenuEstate.ASKING_FOR_TRANSACTION_INFO;
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        if (transaction == null || transaction.getUser().getId() != user.getId()){
            String messageText = "شما تراکنشی با این آیدی ندارید، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = MainMenuEstate.ASKING_FOR_TRANSACTION_INFO;
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        this.estate = MainMenuEstate.USER_EDIT_TRANSACTION_PANEL;
        userEditTransactionPanel.showUserEditTransactionPanel(transaction.getId());
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

    public MainMenuEstate getEstate() {
        return estate;
    }

    public void setEstate(MainMenuEstate mainMenuEstate) {
        this.estate = mainMenuEstate;
    }

    public void sendMessageToUser(Object object){
        try {
            mainController.sendMessageToUser(object);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
