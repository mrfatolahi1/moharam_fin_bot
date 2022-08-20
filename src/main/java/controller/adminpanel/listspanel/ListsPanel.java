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
import java.util.HashMap;
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
        } else
        if (estate == ListsPanelEstate.ADMIN_IS_ASKING_FOR_ONE_COMMITEE_TRANSACTIONS){
            try {
                sendAllCommiteeTransactionsExcelFile(update);
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
        KeyboardRow row5 = new KeyboardRow();
        row1.add("لیست طلبکاران");
        row1.add("لیست بدهکاران");
        row1.add("لیست کاربران");
        row2.add("تراکنش‌های ورودی");
        row2.add("تراکنش‌های تخصیص بودجه به خدام");
        row2.add("تراکنش‌های انجام شده توسط خدام");
        row3.add("کل تراکنش‌ها");
        row3.add("تراکنش‌های ناقص");
        row3.add("تراکنش‌های کاربر");
        row5.add("تراکنش‌های یک بخش خاص");
        row5.add("تطابق تَخامین");
        row4.add("بازگشت به پنل مدیریت");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboard.add(row5);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = ListsPanelEstate.EXCEL_OUTPUT_PANEL;
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void handleExcelOutputPanelRequest(Update update) throws IOException {
        if (update.getMessage().getText().equals("کل تراکنش‌ها")){
            sendAllTransactionsExcelFile();
        } else
        if (update.getMessage().getText().equals("تراکنش‌های ناقص")){
            sendInCompleteTransactionsExcelFile();
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
        if (update.getMessage().getText().equals("تراکنش‌های یک بخش خاص")){
            requestWantedCommiteeName();
        } else
        if (update.getMessage().getText().equals("بازگشت به پنل مدیریت")){
            showAdminPanel(update);
        } else
        if (update.getMessage().getText().equals("تطابق تَخامین")){
            sendEstimationsExcelFile();
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
            row.createCell(5).setCellValue(user.calculateBalance());
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
        ArrayList<Transaction> transactions = Loader.loadAllTransactions(true);

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
        header.createCell(11).setCellValue("توضیحات داخلی مدیر");
        header.createCell(12).setCellValue("بخش");
        header.createCell(13).setCellValue("نوع");

        for (int i = 1 ; i <= transactions.size() ; i++){
            Transaction transaction = transactions.get(i-1);
            XSSFRow row = spreadsheet.createRow(i);
            row.createCell(0).setCellValue(String.valueOf(transaction.getId()));
            if (transaction.getUser() != null){
                row.createCell(1).setCellValue(transaction.getUser().getName());
                row.createCell(2).setCellValue(transaction.getUser().getUsername());
            } else {
                row.createCell(1).setCellValue("ندارد");
                row.createCell(2).setCellValue("ندارد");
            }
            row.createCell(3).setCellValue(transaction.getAmount());
            row.createCell(4).setCellValue(transaction.getFee());
            row.createCell(5).setCellValue(String.valueOf(transaction.getReadableDate()));
            row.createCell(6).setCellValue(String.valueOf(transaction.getReadableTime()));
            row.createCell(7).setCellValue(String.valueOf(transaction.isVerificated()));
            row.createCell(8).setCellValue(String.valueOf(transaction.isHasPaperInvoice()));
            row.createCell(9).setCellValue(String.valueOf(transaction.getDescription()));
            row.createCell(10).setCellValue(String.valueOf(transaction.getAdminDescription()));
            row.createCell(11).setCellValue(String.valueOf(transaction.getAdminInternalDescription()));
            row.createCell(12).setCellValue(String.valueOf(transaction.getCommittee()));
            row.createCell(13).setCellValue(Transaction.getPersianType(transaction.getType()));
        }
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست تراکنش‌ها (فقط تراکنش‌های تایید شده)\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
        adminPanel.sendMessageToUser(sendDocument);
    }

    private void sendAllIncomeTransactionsExcelFile() throws IOException {
        ArrayList<Transaction> transactions = Loader.loadAllTransactions(TransactionType.INCOME, true);

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
        header.createCell(11).setCellValue("توضیحات داخلی مدیر");
        header.createCell(12).setCellValue("بخش");
        header.createCell(13).setCellValue("نوع");

        for (int i = 1 ; i <= transactions.size() ; i++){
            Transaction transaction = transactions.get(i-1);
            XSSFRow row = spreadsheet.createRow(i);
            row.createCell(0).setCellValue(String.valueOf(transaction.getId()));
            if (transaction.getUser() != null){
                row.createCell(1).setCellValue(transaction.getUser().getName());
                row.createCell(2).setCellValue(transaction.getUser().getUsername());
            } else {
                row.createCell(1).setCellValue("ندارد");
                row.createCell(2).setCellValue("ندارد");
            }
            row.createCell(3).setCellValue(transaction.getAmount());
            row.createCell(4).setCellValue(transaction.getFee());
            row.createCell(5).setCellValue(String.valueOf(transaction.getReadableDate()));
            row.createCell(6).setCellValue(String.valueOf(transaction.getReadableTime()));
            row.createCell(7).setCellValue(String.valueOf(transaction.isVerificated()));
            row.createCell(8).setCellValue(String.valueOf(transaction.isHasPaperInvoice()));
            row.createCell(9).setCellValue(String.valueOf(transaction.getDescription()));
            row.createCell(10).setCellValue(String.valueOf(transaction.getAdminDescription()));
            row.createCell(11).setCellValue(String.valueOf(transaction.getAdminInternalDescription()));
            row.createCell(12).setCellValue(String.valueOf(transaction.getCommittee()));
            row.createCell(13).setCellValue(Transaction.getPersianType(transaction.getType()));
        }
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست تراکنش‌های ورودی (فقط تراکنش‌های تایید شده)\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
        adminPanel.sendMessageToUser(sendDocument);
    }

    private void sendAllGiveToUsersTransactionsExcelFile() throws IOException {
        ArrayList<Transaction> transactions = Loader.loadAllTransactions(TransactionType.GIVE_TO_USERS, true);

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
        header.createCell(11).setCellValue("توضیحات داخلی مدیر");
        header.createCell(12).setCellValue("بخش");
        header.createCell(13).setCellValue("نوع");

        for (int i = 1 ; i <= transactions.size() ; i++){
            Transaction transaction = transactions.get(i-1);
            XSSFRow row = spreadsheet.createRow(i);
            row.createCell(0).setCellValue(String.valueOf(transaction.getId()));
            if (transaction.getUser() != null){
                row.createCell(1).setCellValue(transaction.getUser().getName());
                row.createCell(2).setCellValue(transaction.getUser().getUsername());
            } else {
                row.createCell(1).setCellValue("ندارد");
                row.createCell(2).setCellValue("ندارد");
            }
            row.createCell(3).setCellValue(transaction.getAmount());
            row.createCell(4).setCellValue(transaction.getFee());
            row.createCell(5).setCellValue(String.valueOf(transaction.getReadableDate()));
            row.createCell(6).setCellValue(String.valueOf(transaction.getReadableTime()));
            row.createCell(7).setCellValue(String.valueOf(transaction.isVerificated()));
            row.createCell(8).setCellValue(String.valueOf(transaction.isHasPaperInvoice()));
            row.createCell(9).setCellValue(String.valueOf(transaction.getDescription()));
            row.createCell(10).setCellValue(String.valueOf(transaction.getAdminDescription()));
            row.createCell(11).setCellValue(String.valueOf(transaction.getAdminInternalDescription()));
            row.createCell(12).setCellValue(String.valueOf(transaction.getCommittee()));
            row.createCell(13).setCellValue(Transaction.getPersianType(transaction.getType()));
        }
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست تراکنش‌های تخصیص بودجه به خدام (فقط تراکنش‌های تایید شده)\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
        adminPanel.sendMessageToUser(sendDocument);
    }

    private void sendAllExpenditureTransactionsExcelFile() throws IOException {
        ArrayList<Transaction> transactions = Loader.loadAllTransactions(TransactionType.EXPENDITURE, true);

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
        header.createCell(11).setCellValue("توضیحات داخلی مدیر");
        header.createCell(12).setCellValue("بخش");
        header.createCell(13).setCellValue("نوع");
        header.createCell(14).setCellValue("وضعیت ثبت در تنخواه");

        for (int i = 1 ; i <= transactions.size() ; i++){
            Transaction transaction = transactions.get(i-1);
            XSSFRow row = spreadsheet.createRow(i);
            row.createCell(0).setCellValue(String.valueOf(transaction.getId()));
            if (transaction.getUser() != null){
                row.createCell(1).setCellValue(transaction.getUser().getName());
                row.createCell(2).setCellValue(transaction.getUser().getUsername());
            } else {
                row.createCell(1).setCellValue("ندارد");
                row.createCell(2).setCellValue("ندارد");
            }
            row.createCell(3).setCellValue(transaction.getAmount());
            row.createCell(4).setCellValue(transaction.getFee());
            row.createCell(5).setCellValue(String.valueOf(transaction.getReadableDate()));
            row.createCell(6).setCellValue(String.valueOf(transaction.getReadableTime()));
            row.createCell(7).setCellValue(String.valueOf(transaction.isVerificated()));
            row.createCell(8).setCellValue(String.valueOf(transaction.isHasPaperInvoice()));
            row.createCell(9).setCellValue(String.valueOf(transaction.getDescription()));
            row.createCell(10).setCellValue(String.valueOf(transaction.getAdminDescription()));
            row.createCell(11).setCellValue(String.valueOf(transaction.getAdminInternalDescription()));
            row.createCell(12).setCellValue(String.valueOf(transaction.getCommittee()));
            row.createCell(13).setCellValue(Transaction.getPersianType(transaction.getType()));
            row.createCell(14).setCellValue(transaction.isCompleted());
        }
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست تراکنش‌های انجام شده توسط خدام (فقط تراکنش‌های تایید شده)\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
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
        ArrayList<Transaction> transactions = Loader.loadUserTransactions(user, false);
//        ArrayList<Transaction> transactions = new ArrayList<>();
//
//        for (Transaction transaction : transactions1){
//            if (transaction.getType() == TransactionType.GIVE_TO_USERS){
//                transactions.add(transaction);
//            }
//        }

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
        header.createCell(11).setCellValue("توضیحات داخلی مدیر");
        header.createCell(12).setCellValue("بخش");
        header.createCell(13).setCellValue("نوع");

        for (int i = 1 ; i <= transactions.size() ; i++){
            Transaction transaction = transactions.get(i-1);
            XSSFRow row = spreadsheet.createRow(i);
            row.createCell(0).setCellValue(String.valueOf(transaction.getId()));
            if (transaction.getUser() != null){
                row.createCell(1).setCellValue(transaction.getUser().getName());
                row.createCell(2).setCellValue(transaction.getUser().getUsername());
            } else {
                row.createCell(1).setCellValue("ندارد");
                row.createCell(2).setCellValue("ندارد");
            }
            row.createCell(3).setCellValue(transaction.getAmount());
            row.createCell(4).setCellValue(transaction.getFee());
            row.createCell(5).setCellValue(String.valueOf(transaction.getReadableDate()));
            row.createCell(6).setCellValue(String.valueOf(transaction.getReadableTime()));
            row.createCell(7).setCellValue(String.valueOf(transaction.isVerificated()));
            row.createCell(8).setCellValue(String.valueOf(transaction.isHasPaperInvoice()));
            row.createCell(9).setCellValue(String.valueOf(transaction.getDescription()));
            row.createCell(10).setCellValue(String.valueOf(transaction.getAdminDescription()));
            row.createCell(11).setCellValue(String.valueOf(transaction.getAdminInternalDescription()));
            row.createCell(12).setCellValue(String.valueOf(transaction.getCommittee()));
            row.createCell(13).setCellValue(Transaction.getPersianType(transaction.getType()));
        }
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست تراکنش‌های انجام شده توسط خادم (تایید شده و تایید نشده)\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
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
            row.createCell(5).setCellValue(user.calculateBalance());
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
            row.createCell(5).setCellValue(user.calculateBalance());
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

    private void sendInCompleteTransactionsExcelFile() throws IOException {
        ArrayList<Transaction> transactions = Loader.loadAllTransactions(TransactionType.EXPENDITURE, false);
        ArrayList<Transaction> inCompleteTransactions = new ArrayList<>();

        for (Transaction transaction : transactions){
            if (!transaction.isVerificated() || !transaction.isHasPaperInvoice()){
                inCompleteTransactions.add(transaction);
            }
        }

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
        header.createCell(11).setCellValue("توضیحات داخلی مدیر");
        header.createCell(12).setCellValue("بخش");
        header.createCell(13).setCellValue("نوع");

        for (int i = 1 ; i <= inCompleteTransactions.size() ; i++){
            Transaction transaction = inCompleteTransactions.get(i-1);
            XSSFRow row = spreadsheet.createRow(i);
            row.createCell(0).setCellValue(String.valueOf(transaction.getId()));
            if (transaction.getUser() != null){
                row.createCell(1).setCellValue(transaction.getUser().getName());
                row.createCell(2).setCellValue(transaction.getUser().getUsername());
            } else {
                row.createCell(1).setCellValue("ندارد");
                row.createCell(2).setCellValue("ندارد");
            }
            row.createCell(3).setCellValue(transaction.getAmount());
            row.createCell(4).setCellValue(transaction.getFee());
            row.createCell(5).setCellValue(String.valueOf(transaction.getReadableDate()));
            row.createCell(6).setCellValue(String.valueOf(transaction.getReadableTime()));
            row.createCell(7).setCellValue(String.valueOf(transaction.isVerificated()));
            row.createCell(8).setCellValue(String.valueOf(transaction.isHasPaperInvoice()));
            row.createCell(9).setCellValue(String.valueOf(transaction.getDescription()));
            row.createCell(10).setCellValue(String.valueOf(transaction.getAdminDescription()));
            row.createCell(11).setCellValue(String.valueOf(transaction.getAdminInternalDescription()));
            row.createCell(12).setCellValue(String.valueOf(transaction.getCommittee()));
            row.createCell(13).setCellValue(Transaction.getPersianType(transaction.getType()));
        }
        spreadsheet.setRightToLeft(true);
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست تراکنش‌های ناقص (تراکنش‌های تایید نشده یا بدون فاکتور کاغذی)\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
        adminPanel.sendMessageToUser(sendDocument);
    }

    private void requestWantedCommiteeName(){
        ArrayList<String> commiteesList = Loader.loadCommiteesList();
        String messageText1 = "";
        for (String commitee : commiteesList){
            messageText1 += commitee + "\n";
        }
        SendMessage sendMessage1 = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText1);
        adminPanel.sendMessageToUser(sendMessage1);
        String messageText = "نام بخش مورد نظر را وارد کنید (با استفاده از لیست بالا):";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = ListsPanelEstate.ADMIN_IS_ASKING_FOR_ONE_COMMITEE_TRANSACTIONS;
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void sendAllCommiteeTransactionsExcelFile(Update update) throws IOException {
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showListsPanel();
                return;
            }
        }
        String commiteeName = update.getMessage().getText();
        try {
            if (!Loader.loadCommiteesList().contains(commiteeName)){
                throw new NullPointerException();
            }
        } catch (Exception e){
            String messageText = "کمیته‌ای با این نام پیدا نشد، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = ListsPanelEstate.ADMIN_IS_ASKING_FOR_ONE_COMMITEE_TRANSACTIONS;
            adminPanel.sendMessageToUser(sendMessage);
            return;
        }
        ArrayList<Transaction> transactions1 = Loader.loadCommiteeTransactions(commiteeName, false);
        ArrayList<Transaction> transactions = new ArrayList<>();

        for (Transaction transaction : transactions1){
            if (transaction.getType() == TransactionType.GIVE_TO_USERS){
                transactions.add(transaction);
            }
        }

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
        header.createCell(11).setCellValue("توضیحات داخلی مدیر");
        header.createCell(12).setCellValue("بخش");
        header.createCell(13).setCellValue("نوع");

        for (int i = 1 ; i <= transactions.size() ; i++){
            Transaction transaction = transactions.get(i-1);
            XSSFRow row = spreadsheet.createRow(i);
            row.createCell(0).setCellValue(String.valueOf(transaction.getId()));
            if (transaction.getUser() != null){
                row.createCell(1).setCellValue(transaction.getUser().getName());
                row.createCell(2).setCellValue(transaction.getUser().getUsername());
            } else {
                row.createCell(1).setCellValue("ندارد");
                row.createCell(2).setCellValue("ندارد");
            }
            row.createCell(3).setCellValue(transaction.getAmount());
            row.createCell(4).setCellValue(transaction.getFee());
            row.createCell(5).setCellValue(String.valueOf(transaction.getReadableDate()));
            row.createCell(6).setCellValue(String.valueOf(transaction.getReadableTime()));
            row.createCell(7).setCellValue(String.valueOf(transaction.isVerificated()));
            row.createCell(8).setCellValue(String.valueOf(transaction.isHasPaperInvoice()));
            row.createCell(9).setCellValue(String.valueOf(transaction.getDescription()));
            row.createCell(10).setCellValue(String.valueOf(transaction.getAdminDescription()));
            row.createCell(11).setCellValue(String.valueOf(transaction.getAdminInternalDescription()));
            row.createCell(12).setCellValue(String.valueOf(transaction.getCommittee()));
            row.createCell(13).setCellValue(Transaction.getPersianType(transaction.getType()));
        }
        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "Transactions.xlsx"));
        sendDocument.setCaption("خروجی اکسل لیست تراکنش‌های انجام شده توسط خادم (تایید شده و تایید نشده)\nتصویر فاکتور هر تراکنش‌ را با استفاده از آیدی آن می‌توانید از ربات دریافت کنید.");
        adminPanel.sendMessageToUser(sendDocument);
        showListsPanel();
    }

    private void sendEstimationsExcelFile() throws IOException {
        ArrayList<Transaction> transactions = Loader.loadAllTransactions(TransactionType.GIVE_TO_USERS, true);

        long tadarokatSum = 0;
        long egraiiSum = 0;
        long resaneSum = 0;
        long tablighatSum = 0;
        long entezamatSum = 0;
        long marasemSum = 0;
        long khaharanSum = 0;
        long farhangiMaktobatSum = 0;
        long nojavananSum = 0;
        long khodamSum = 0;
        long motafaregheSum = 0;
        long fazasaziDecorSum = 0;

        for (Transaction transaction : transactions){
            if (transaction.getCommittee() == null){
                continue;
            }
            switch (transaction.getCommittee()){
                case "TADAROKAT": tadarokatSum += transaction.getAmount(); break;
                case "EGRAII": egraiiSum += transaction.getAmount(); break;
                case "RESANE": resaneSum += transaction.getAmount(); break;
                case "TABLIGHAT": tablighatSum += transaction.getAmount(); break;
                case "ENTEZAMAT": entezamatSum += transaction.getAmount(); break;
                case "MARASEM": marasemSum += transaction.getAmount(); break;
                case "KHAHARAN": khaharanSum += transaction.getAmount(); break;
                case "FARHANGI_MAKTOBAT": farhangiMaktobatSum += transaction.getAmount(); break;
                case "NOJAVANAN": nojavananSum += transaction.getAmount(); break;
                case "KHODAM": khodamSum += transaction.getAmount(); break;
                case "MOTAFAREGHE": motafaregheSum += transaction.getAmount(); break;
                case "FAZASAZI_DECOR": fazasaziDecorSum += transaction.getAmount(); break;
                default: System.out.println(transaction.getId() + " has no commitee");
            }
        }

        HashMap<String, String> estimations = Loader.loadEstimationsMap();

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("تطابق تَخامین");

        XSSFRow header = spreadsheet.createRow(0);
        header.createCell(0).setCellValue("بخش");
        header.createCell(1).setCellValue("تخمین");
        header.createCell(2).setCellValue("بودجه دریافتی");
        header.createCell(3).setCellValue("درصد");
////
        XSSFRow row1 = spreadsheet.createRow(1);
        row1.createCell(0).setCellValue("تدارکات");
        row1.createCell(1).setCellValue(Long.valueOf(estimations.get("TADAROKAT")));
        row1.createCell(2).setCellValue(tadarokatSum);
        row1.createCell(3).setCellValue(100 * tadarokatSum / Long.valueOf(estimations.get("TADAROKAT")));

        XSSFRow row2 = spreadsheet.createRow(2);
        row2.createCell(0).setCellValue("اجرایی");
        row2.createCell(1).setCellValue(Long.valueOf(estimations.get("EGRAII")));
        row2.createCell(2).setCellValue(egraiiSum);
        row2.createCell(3).setCellValue(100 * egraiiSum / Long.valueOf(estimations.get("EGRAII")));

        XSSFRow row3 = spreadsheet.createRow(3);
        row3.createCell(0).setCellValue("رسانه");
        row3.createCell(1).setCellValue(Long.valueOf(estimations.get("RESANE")));
        row3.createCell(2).setCellValue(resaneSum);
        row3.createCell(3).setCellValue(100 * resaneSum / Long.valueOf(estimations.get("RESANE")));

        XSSFRow row4 = spreadsheet.createRow(4);
        row4.createCell(0).setCellValue("تبلیغات");
        row4.createCell(1).setCellValue(Long.valueOf(estimations.get("TABLIGHAT")));
        row4.createCell(2).setCellValue(tablighatSum);
        row4.createCell(3).setCellValue(100 * tablighatSum / Long.valueOf(estimations.get("TABLIGHAT")));

        XSSFRow row5 = spreadsheet.createRow(5);
        row5.createCell(0).setCellValue("انتظامات");
        row5.createCell(1).setCellValue(Long.valueOf(estimations.get("ENTEZAMAT")));
        row5.createCell(2).setCellValue(entezamatSum);
        row5.createCell(3).setCellValue(100 * entezamatSum / Long.valueOf(estimations.get("ENTEZAMAT")));

        XSSFRow row6 = spreadsheet.createRow(6);
        row6.createCell(0).setCellValue("مراسم");
        row6.createCell(1).setCellValue(Long.valueOf(estimations.get("MARASEM")));
        row6.createCell(2).setCellValue(marasemSum);
        row6.createCell(3).setCellValue(100 * marasemSum / Long.valueOf(estimations.get("MARASEM")));

        XSSFRow row7 = spreadsheet.createRow(7);
        row7.createCell(0).setCellValue("خواهران");
        row7.createCell(1).setCellValue(Long.valueOf(estimations.get("KHAHARAN")));
        row7.createCell(2).setCellValue(khaharanSum);
        row7.createCell(3).setCellValue(100 * khaharanSum / Long.valueOf(estimations.get("KHAHARAN")));

        XSSFRow row8 = spreadsheet.createRow(8);
        row8.createCell(0).setCellValue("فرهنگی-مکتوبات");
        row8.createCell(1).setCellValue(Long.valueOf(estimations.get("FARHANGI_MAKTOBAT")));
        row8.createCell(2).setCellValue(farhangiMaktobatSum);
        row8.createCell(3).setCellValue(100 * farhangiMaktobatSum / Long.valueOf(estimations.get("FARHANGI_MAKTOBAT")));

        XSSFRow row9 = spreadsheet.createRow(9);
        row9.createCell(0).setCellValue("نوجوانان");
        row9.createCell(1).setCellValue(Long.valueOf(estimations.get("NOJAVANAN")));
        row9.createCell(2).setCellValue(nojavananSum);
        row9.createCell(3).setCellValue(100 * nojavananSum / Long.valueOf(estimations.get("NOJAVANAN")));

        XSSFRow row10 = spreadsheet.createRow(10);
        row10.createCell(0).setCellValue("خدام");
        row10.createCell(1).setCellValue(Long.valueOf(estimations.get("KHODAM")));
        row10.createCell(2).setCellValue(khodamSum);
        row10.createCell(3).setCellValue(100 * khodamSum / Long.valueOf(estimations.get("KHODAM")));

        XSSFRow row11 = spreadsheet.createRow(11);
        row11.createCell(0).setCellValue("فضاسازی-دکور");
        row11.createCell(1).setCellValue(Long.valueOf(estimations.get("FAZASAZI_DECOR")));
        row11.createCell(2).setCellValue(fazasaziDecorSum);
        row11.createCell(3).setCellValue(100 * fazasaziDecorSum / Long.valueOf(estimations.get("FAZASAZI_DECOR")));

        XSSFRow row12 = spreadsheet.createRow(12);
        row12.createCell(0).setCellValue("متفرقه");
        row12.createCell(1).setCellValue(Long.valueOf(estimations.get("MOTAFAREGHE")));
        row12.createCell(2).setCellValue(motafaregheSum);
        row12.createCell(3).setCellValue(100 * motafaregheSum / Long.valueOf(estimations.get("MOTAFAREGHE")));

        File file = new File("Excels/" + adminPanel.getChat().getUser().getId()+".xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        SendDocument sendDocument = new SendDocument(String.valueOf(adminPanel.getChat().getChatID()), new InputFile(file, "estimations.xlsx"));
        sendDocument.setCaption("خروجی اکسل تطابق تَخامین");
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

    private void showAdminPanel(Update update){
        this.estate = ListsPanelEstate.EXCEL_OUTPUT_PANEL;
        adminPanel.showAdminPanel(update);
    }
}
