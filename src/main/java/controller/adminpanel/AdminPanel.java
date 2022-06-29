package controller.adminpanel;

import controller.MainMenu;
import controller.adminpanel.listspanel.ListsPanel;
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
    private final MainMenu mainMenu;
    private AdminPanelEstate estate;
    private final ArrayList<String> cachedInfo;
    private final ListsPanel listsPanel;

    public AdminPanel(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        this.estate = AdminPanelEstate.ADMIN_PANEL;
        this.cachedInfo = new ArrayList<>();
        this.listsPanel = new ListsPanel(this);
    }

    public MainMenu getChat() {
        return mainMenu;
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
            if (estate == AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION){
                addNewTransactionWithAdmin1(update);
            } else
                if (estate == AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION_SENDING_USER_ID){
                    addNewTransactionWithAdmin2(update);
                } else
                    if (estate == AdminPanelEstate.LISTS_PANEL){
                        listsPanel.handleNewUpdate(update);
                    }
    }

    private void handleAdminPanelRequest(Update update){
//        if (update.getMessage().getText().equals("لیست طلبکاران")){//
//            showListOfCreditors();
//        } else
//        if (update.getMessage().getText().equals("لیست بدهکاران")){//
//            showListOfDebtors();
//        } else
//        if (update.getMessage().getText().equals("لیست کاربران")){//
//            showListOfAllUsers();
//        } else
//        if (update.getMessage().getText().equals("تراکنش‌های کاربر")){//
//            requestWantedUserNumericID();
//        } else
        if (update.getMessage().getText().equals("تراکنش جدید (دادن بودجه)")){
            showAddNewTransactionMessageWithAdmin();
        } else
        if (update.getMessage().getText().equals("لیست‌ها")){//
            showListsPanel();
        } else
        if (update.getMessage().getText().equals("بازگشت به منوی اصلی")){
            showMainMenu(update);
        } else {
            showAdminPanel();
        }
    }

    public void showAdminPanel(){
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
        SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), "پنل مدیریت:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        row1.add("لیست‌ها");
//        row1.add("لیست بدهکاران");
//        row1.add("لیست کاربران");
//        row2.add("تراکنش‌های کاربر");
        row2.add("تراکنش جدید (دادن بودجه)");
//        row2.add("دیدن کل تراکنش‌ها");
        row3.add("بازگشت به منوی اصلی");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = AdminPanelEstate.ADMIN_PANEL;
        mainMenu.sendMessageToUser(sendMessage);
    }

    private void showAddNewTransactionMessageWithAdmin(){
        estate = AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION;

        String messageText2 = "لطفا تصویر رسید را ارسال نموده و اطلاعات زیر را هر یک در خطوط جداگانه ارسال کنید (اعداد با ارقام انگلیسی).\n\n[مبلغ کل به ریال]\n[مبلغ کارمزد رابه ریال]\n[توضیحات مبلغ]";
        SendMessage sendMessage2 = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText2);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage2.setReplyMarkup(keyboardMarkup);
        mainMenu.sendMessageToUser(sendMessage2);
    }

    private void addNewTransactionWithAdmin1(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminPanel();
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
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            mainMenu.sendMessageToUser(sendMessage);
            return;
        }
        requestWantedTransactionUserNumericID();
    }

    private void addNewTransactionWithAdmin2(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminPanel();
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
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            estate = AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION_SENDING_USER_ID;
            mainMenu.sendMessageToUser(sendMessage);
            return;
        }
        if (user == null){
            String messageText = "کاربری با این مشخصات پیدا نشد، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            estate = AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION_SENDING_USER_ID;
            mainMenu.sendMessageToUser(sendMessage);
            return;
        }

        Transaction transaction = new Transaction(user, Long.parseLong(cachedInfo.get(1)), Integer.parseInt(cachedInfo.get(2)), cachedInfo.get(3), TransactionType.GIVE_TO_USERS, cachedInfo.get(0));
        user.getTransactionsIDsList().add(transaction.getId());
        Saver.saveUser(user);
        Saver.saveTransaction(transaction);
        cachedInfo.clear();

        String messageText = "تراکنش با موفقیت ثبت شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
        mainMenu.sendMessageToUser(sendMessage);

        showAdminPanel();
    }

    private void requestWantedTransactionUserNumericID(){
        estate = AdminPanelEstate.ADMIN_IS_ADDING_NEW_TRANSACTION_SENDING_USER_ID;
        String messageText1 = Loader.getUsersIDsList();
        SendMessage sendMessage1 = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText1);
        mainMenu.sendMessageToUser(sendMessage1);
        String messageText = "آیدی عددی کاربر مورد نظر را وارد کنید (با استفاده از لیست بالا):";
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

    public void showMainMenu(Update update){
        estate = AdminPanelEstate.ADMIN_PANEL;
        mainMenu.showMainMenu(update);
    }

    private void showListsPanel(){
        estate = AdminPanelEstate.LISTS_PANEL;
        listsPanel.showListsPanel();
    }

    public void sendMessageToUser(Object object){
        mainMenu.sendMessageToUser(object);
    }
}
