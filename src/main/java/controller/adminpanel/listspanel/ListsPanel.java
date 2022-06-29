package controller.adminpanel.listspanel;

import controller.adminpanel.AdminPanel;
import database.Loader;
import models.Transaction;
import models.TransactionType;
import models.User;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListsPanel {
    private final AdminPanel adminPanel;
    private ListsPanelEstate estate;

    public ListsPanel(AdminPanel adminPanel) {
        this.adminPanel = adminPanel;
        this.estate = ListsPanelEstate.EXCEL_OUTPUT_PANEL;
    }

    public void handleNewUpdate(Update update){
        if (estate == ListsPanelEstate.EXCEL_OUTPUT_PANEL){
            try {
                handleExcelOutputPanelRequest(update);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
        if (estate == ListsPanelEstate.ADMIN_IS_ASKING_FOR_SOMEBODYS_TRANSACTIONS){
            try {
                sendAllUserTransactionsExcelFile(update);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showListsPanel(){
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), "می‌توانید لیست‌های زیر را به صورت فایل اکسل دریافت کنید:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        row1.add("لیست طلبکاران");
        row1.add("لیست بدهکاران");
        row1.add("لیست کاربران");
        row2.add("تراکنش‌های ورودی");
        row2.add("تراکنش‌های تخصیص بودجه به خدام");
        row2.add("تراکنش‌های انجام شده توسط خدام");
        row3.add("کل تراکنش‌ها");
        row3.add("تراکنش‌های کاربر");
        row4.add("بازگشت به پنل مدیریت");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = ListsPanelEstate.EXCEL_OUTPUT_PANEL;
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void handleExcelOutputPanelRequest(Update update) throws IOException {
        if (update.getMessage().getText().equals("کل تراکنش‌ها")){
            sendAllTransactionsExcelFile();
        } else
        if (update.getMessage().getText().equals("تراکنش‌های ورودی")){
            sendAllIncomeTransactionsExcelFile();
        } else
        if (update.getMessage().getText().equals("تراکنش‌های تخصیص بودجه به خدام")){
            sendAllGiveToUsersTransactionsExcelFile();
        } else
        if (update.getMessage().getText().equals("تراکنش‌های انجام شده توسط خدام")){
            sendAllExpenditureTransactionsExcelFile();
        } else
        if (update.getMessage().getText().equals("تراکنش‌های کاربر")){
            requestWantedUserNumericID();
        } else
        if (update.getMessage().getText().equals("لیست کاربران")){
            sendAllUsersExcelFile();
        } else
        if (update.getMessage().getText().equals("لیست طلبکاران")){
            sendListOfCreditorsExcelFile();
        } else
        if (update.getMessage().getText().equals("لیست بدهکاران")){
            sendListOfDebtorsExcelFile();
        } else
        if (update.getMessage().getText().equals("بازگشت به پنل مدیریت")){
            showAdminPanel();
        } else {
            showListsPanel();
        }
    }

    private void sendAllUsersExcelFile() throws IOException {
        ArrayList<User> users = Loader.loadAllUsers();

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("Users List");

        XSSFRow header = spreadsheet.createRow(0);
        header.createCell(0).setCellValue("آیدی");
        header.createCell(1).setCellValue("نام و نام خانوادگی");
        header.createCell(2).setCellValue("نام‌ کاربری تلگرام");
        header.createCell(3).setCellValue("ایمیل");
        header.createCell(4).setCellValue("شماره تلفن");
        header.createCell(5).setCellValue("تراز مالی");

        for (int i = 1 ; i <= users.size() ; i++){
            User user = users.get(i-1);
            XSSFRow row = spreadsheet.createRow(i);
            row.createCell(0).setCellValue(String.valueOf(user.getId()));
            row.createCell(1).setCellValue(user.getName());
            row.createCell(2).setCellValue(user.getUsername());
            row.createCell(3).setCellValue(user.getEmail());
            row.createCell(4).setCellValue(user.getPhoneNumber());
            row.createCell(5).setCellValue(String.valueOf(user.calculateBalance()));
        }
        spreadsheet.setRightToLeft(true);
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Users.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست کاربران");
        adminPanel.sendMessageToUser(sendDocument);
    }

    private void sendAllTransactionsExcelFile() throws IOException {
        ArrayList<Transaction> transactions = Loader.loadAllTransactions();

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
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست تراکنش‌ها\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
        adminPanel.sendMessageToUser(sendDocument);
    }

    private void sendAllIncomeTransactionsExcelFile() throws IOException {
        ArrayList<Transaction> transactions = Loader.loadAllTransactions(TransactionType.INCOME);

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
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست تراکنش‌های ورودی\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
        adminPanel.sendMessageToUser(sendDocument);
    }

    private void sendAllGiveToUsersTransactionsExcelFile() throws IOException {
        ArrayList<Transaction> transactions = Loader.loadAllTransactions(TransactionType.GIVE_TO_USERS);

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
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست تراکنش‌های تخصیص بودجه به خدام\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
        adminPanel.sendMessageToUser(sendDocument);
    }

    private void sendAllExpenditureTransactionsExcelFile() throws IOException {
        ArrayList<Transaction> transactions = Loader.loadAllTransactions(TransactionType.EXPENDITURE);

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
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست تراکنش‌های انجام شده توسط خدام\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
        adminPanel.sendMessageToUser(sendDocument);
    }

    private void requestWantedUserNumericID(){
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
        estate = ListsPanelEstate.ADMIN_IS_ASKING_FOR_SOMEBODYS_TRANSACTIONS;
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void sendAllUserTransactionsExcelFile(Update update) throws IOException {
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showListsPanel();
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
            estate = ListsPanelEstate.ADMIN_IS_ASKING_FOR_SOMEBODYS_TRANSACTIONS;
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        if (user == null){
            String messageText = "کاربری با این مشخصات پیدا نشد، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = ListsPanelEstate.ADMIN_IS_ASKING_FOR_SOMEBODYS_TRANSACTIONS;
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
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
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست تراکنش‌های انجام شده توسط خادم\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
        adminPanel.sendMessageToUser(sendDocument);
        showListsPanel();
    }

    private void sendListOfCreditorsExcelFile() throws IOException {
        ArrayList<User> loadedUsers = Loader.loadAllUsers();
        ArrayList<User> users = new ArrayList<>();

        for (User loadedUser : loadedUsers){
            if (loadedUser.calculateBalance() >= 0){
                continue;
            }
            users.add(loadedUser);
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("Creditors List");

        XSSFRow header = spreadsheet.createRow(0);
        header.createCell(0).setCellValue("آیدی");
        header.createCell(1).setCellValue("نام و نام خانوادگی");
        header.createCell(2).setCellValue("نام‌ کاربری تلگرام");
        header.createCell(3).setCellValue("ایمیل");
        header.createCell(4).setCellValue("شماره تلفن");
        header.createCell(5).setCellValue("تراز مالی");

        for (int i = 1 ; i <= users.size() ; i++){
            User user = users.get(i-1);
            if (user.calculateBalance() >= 0){
                continue;
            }
            XSSFRow row = spreadsheet.createRow(i);
            row.createCell(0).setCellValue(String.valueOf(user.getId()));
            row.createCell(1).setCellValue(user.getName());
            row.createCell(2).setCellValue(user.getUsername());
            row.createCell(3).setCellValue(user.getEmail());
            row.createCell(4).setCellValue(user.getPhoneNumber());
            row.createCell(5).setCellValue(String.valueOf(user.calculateBalance()));
        }
        spreadsheet.setRightToLeft(true);
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Creditors.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست طلبکاران");
        adminPanel.sendMessageToUser(sendDocument);
    }

    private void sendListOfDebtorsExcelFile() throws IOException {
        ArrayList<User> loadedUsers = Loader.loadAllUsers();
        ArrayList<User> users = new ArrayList<>();

        for (User loadedUser : loadedUsers){
            if (loadedUser.calculateBalance() <= 0){
                continue;
            }
            users.add(loadedUser);
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("Debtors List");

        XSSFRow header = spreadsheet.createRow(0);
        header.createCell(0).setCellValue("آیدی");
        header.createCell(1).setCellValue("نام و نام خانوادگی");
        header.createCell(2).setCellValue("نام‌ کاربری تلگرام");
        header.createCell(3).setCellValue("ایمیل");
        header.createCell(4).setCellValue("شماره تلفن");
        header.createCell(5).setCellValue("تراز مالی");

        for (int i = 1 ; i <= users.size() ; i++){
            User user = users.get(i-1);
            if (user.calculateBalance() <= 0){
                continue;
            }
            XSSFRow row = spreadsheet.createRow(i);
            row.createCell(0).setCellValue(String.valueOf(user.getId()));
            row.createCell(1).setCellValue(user.getName());
            row.createCell(2).setCellValue(user.getUsername());
            row.createCell(3).setCellValue(user.getEmail());
            row.createCell(4).setCellValue(user.getPhoneNumber());
            row.createCell(5).setCellValue(String.valueOf(user.calculateBalance()));
        }
        spreadsheet.setRightToLeft(true);
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Debtors.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست بدهکاران");
        adminPanel.sendMessageToUser(sendDocument);
    }

    public AdminPanel getAdminPanelController() {
        return adminPanel;
    }

    public AdminPanel getAdminPanel() {
        return adminPanel;
    }

    public ListsPanelEstate getEstate() {
        return estate;
    }

    public void setEstate(ListsPanelEstate estate) {
        this.estate = estate;
    }

    private void showAdminPanel(){
        this.estate = ListsPanelEstate.EXCEL_OUTPUT_PANEL;
        adminPanel.showAdminPanel();
    }
}
