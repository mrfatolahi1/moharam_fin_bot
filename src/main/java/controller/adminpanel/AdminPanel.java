package controller.adminpanel;

import controller.MainMenu;
import controller.adminpanel.adddeleteadminspanel.AddDeleteAdminsPanel;
import controller.adminpanel.addtransactionpanel.AdminAddTransactionPanel;
import controller.adminpanel.adminedittransactionpanel.AdminEditTransactionPanel;
import controller.adminpanel.listspanel.ListsPanel;
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

public class AdminPanel {
    private final MainMenu mainMenu;
    private AdminPanelEstate estate;
    private final ArrayList<String> cachedInfo;
    private final ListsPanel listsPanel;
    private final AdminEditTransactionPanel adminEditTransactionPanel;
    private final AddDeleteAdminsPanel addDeleteAdminsPanel;
    private final AdminAddTransactionPanel adminAddTransactionPanel;

    public AdminPanel(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        this.estate = AdminPanelEstate.ADMIN_PANEL;
        this.cachedInfo = new ArrayList<>();
        this.listsPanel = new ListsPanel(this);
        this.adminEditTransactionPanel = new AdminEditTransactionPanel(this);
        this.addDeleteAdminsPanel = new AddDeleteAdminsPanel(this);
        this.adminAddTransactionPanel = new AdminAddTransactionPanel(this);
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
            if (estate == AdminPanelEstate.LISTS_PANEL){
                listsPanel.handleNewUpdate(update);
            } else
                if (estate == AdminPanelEstate.ASKING_FOR_TRANSACTION_INFO){
                    showTransactionInfo(update);
                } else
                    if (estate == AdminPanelEstate.ASKING_FOR_TRANSACTION_INFO_FOR_EDIT){
                        showAdminEditTransactionPanel(update);
                    } else
                        if (estate == AdminPanelEstate.ADMIN_EDIT_TRANSACTION_PANEL){
                            adminEditTransactionPanel.handleNewUpdate(update);
                        } else
                            if (estate == AdminPanelEstate.ADD_DELETE_ADMINS_PANEL){
                                addDeleteAdminsPanel.handleNewUpdate(update);
                            } else
                                if (estate == AdminPanelEstate.ADMIN_ADD_TRANSACTION_PANEL){
                                    adminAddTransactionPanel.handleNewUpdate(update);
                                } else
                                    if (estate == AdminPanelEstate.ADMIN_IS_ADDING_NEW_USER_SENDING_NAME){
                                        saveNewUserName(update);
                                    } else
                                        if (estate == AdminPanelEstate.ADMIN_IS_ADDING_NEW_USER_FORWARDING_MESSAGE){
                                            addNewUser(update);
                                        }
    }

    private void handleAdminPanelRequest(Update update){
        if (update.getMessage().getText().equals("مشاهده تراکنش")){
            requestWantedTransactionID(1);
        } else
        if (update.getMessage().getText().equals("ویرایش تراکنش")){
            requestWantedTransactionID(2);
        } else
        if (update.getMessage().getText().equals("لیست‌ها")){
            showListsPanel();
        } else
        if (update.getMessage().getText().equals("افزودن تراکنش")){
            showAdminAddTransactionPanel();
        } else
        if (update.getMessage().getText().equals("مدیریت مدیران!")){
            showAddDeleteAdminsPanel(update);
        } else
        if (update.getMessage().getText().equals("اطلاعات مالی")){
            showFinInfo();
        } else
        if (update.getMessage().getText().equals("ثبت کاربر جدید")){
            requestNewUserName();
        } else
        if (update.getMessage().getText().equals("بازگشت به منوی اصلی")){
            showMainMenu(update);
        } else {
            showAdminPanel(update);
        }
    }

    public void showAdminPanel(Update update){
        if (!Loader.loadAdminsIDsList().contains(update.getMessage().getFrom().getId())){
            System.out.println("Loader.loadAdminsIDsList() = " + Loader.loadAdminsIDsList());
            System.out.println("update.getMessage().getFrom().getId() = " + update.getMessage().getFrom().getId());
            String messageText = "شما به این قسمت دسترسی ندارید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            mainMenu.sendMessageToUser(sendMessage);
            return;
        }
        SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), "پنل مدیریت:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        row1.add("لیست‌ها");
        row1.add("مشاهده تراکنش");
        row1.add("ویرایش تراکنش");
        row2.add("اطلاعات مالی");
        row2.add("افزودن تراکنش");
        row2.add("مدیریت مدیران!");
        row3.add("ثبت کاربر جدید");
        row4.add("بازگشت به منوی اصلی");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = AdminPanelEstate.ADMIN_PANEL;
        mainMenu.sendMessageToUser(sendMessage);
    }

    private void requestWantedTransactionID(int type){
        if (type == 1){
            estate = AdminPanelEstate.ASKING_FOR_TRANSACTION_INFO;
        } else {
            estate = AdminPanelEstate.ASKING_FOR_TRANSACTION_INFO_FOR_EDIT;
        }

        String messageText = "آیدی تراکنش مورد نظر را وارد کنید:";
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

    private void showTransactionInfo(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminPanel(update);
                return;
            }
        }

        int longId = 0;
        Transaction transaction = null;
        try {
            String transactionId = update.getMessage().getText();
            longId = Integer.parseInt(transactionId);
            transaction = Loader.loadTransaction(longId, false);
        } catch (Exception e){
            String messageText = "فرمت اطلاعات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            estate = AdminPanelEstate.ASKING_FOR_TRANSACTION_INFO;
            sendMessageToUser(sendMessage);
            return;
        }
        if (transaction == null){
            String messageText = "تراکنشی با این آیدی یافت نشد، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            estate = AdminPanelEstate.ASKING_FOR_TRANSACTION_INFO;
            sendMessageToUser(sendMessage);
            return;
        }

        SendPhoto sendPhoto = new SendPhoto(String.valueOf(mainMenu.getChatID()), new InputFile(transaction.getFactorImageFileId()));
        sendPhoto.setCaption(transaction.adminToString());
        sendMessageToUser(sendPhoto);
        showAdminPanel(update);
    }

    private void showAdminEditTransactionPanel(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminPanel(update);
                return;
            }
        }

        int longId = 0;
        Transaction transaction = null;
        try {
            String transactionId = update.getMessage().getText();
            longId = Integer.parseInt(transactionId);
            transaction = Loader.loadTransaction(longId, false);
        } catch (Exception e){
            String messageText = "فرمت اطلاعات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            sendMessageToUser(sendMessage);
            return;
        }
        if (transaction == null){
            String messageText = "تراکنشی با این آیدی یافت نشد، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            sendMessageToUser(sendMessage);
            return;
        }
        this.estate = AdminPanelEstate.ADMIN_EDIT_TRANSACTION_PANEL;
        adminEditTransactionPanel.showAdminEditTransactionPanel(transaction.getId());
    }

    public void showMainMenu(Update update){
        estate = AdminPanelEstate.ADMIN_PANEL;
        mainMenu.showMainMenu(update);
    }

    private void showListsPanel(){
        estate = AdminPanelEstate.LISTS_PANEL;
        listsPanel.showListsPanel();
    }

    private void showAdminAddTransactionPanel(){
        estate = AdminPanelEstate.ADMIN_ADD_TRANSACTION_PANEL;
        adminAddTransactionPanel.showAdminAddTransactionPanel();
    }

    private void showAddDeleteAdminsPanel(Update update){
        estate = AdminPanelEstate.ADD_DELETE_ADMINS_PANEL;
        addDeleteAdminsPanel.showAddDeleteAdminsPanel(update);
    }

    public void sendMessageToUser(Object object){
        mainMenu.sendMessageToUser(object);
    }

    private void showFinInfo(){
        String info = calculateFinInfo();
        String messageText = "اطلاعات مالی به شرح زیر است (مبالغ به ریال):" + "\n\n" + info;

        SendMessage sendMessage = new SendMessage(String.valueOf(getChat().getChatID()), messageText);
        sendMessageToUser(sendMessage);
    }

    private String calculateFinInfo(){
        long balance = 0;
        long incomeSum = 0;
        long giveToUsersSum = 0;
        long expenditureSum = 0;
        ArrayList<Transaction> transactions = Loader.loadAllTransactions(true);

        for (Transaction transaction : transactions){
            if (transaction.getType() == TransactionType.INCOME){
                incomeSum += transaction.getAmount();
                balance += transaction.getAmount();
            } else
            if (transaction.getType() == TransactionType.GIVE_TO_USERS){
                giveToUsersSum += (transaction.getAmount() + ((long) transaction.getFee()));
                balance -= (transaction.getAmount() + ((long) transaction.getFee()));
            } else
            if (transaction.getType() == TransactionType.EXPENDITURE){
                expenditureSum += transaction.getAmount();
            }
        }
        String balanceInfo = "تراز مالی (تراکنش‌های ورودی منهای تراکنش‌های تخصیص داده شده به خدام): " + balance;
        String incomeInfo = "مجموع تراکنش‌های ورودی: " + incomeSum;
        String giveToUsersInfo = "مجموع تراکنش‌های تخصیص داده‌شده به خدام: " + giveToUsersSum;
        String expenditureInfo = "مجموع تراکنش‌های خرج شده توسط خدام: " + expenditureSum;

        return balanceInfo + "\n" + incomeInfo + "\n" + giveToUsersInfo + "\n" + expenditureInfo;
    }

    private void requestNewUserName(){
        estate = AdminPanelEstate.ADMIN_IS_ADDING_NEW_USER_SENDING_NAME;

        String messageText = "نام و نام خانوادگی کاربر جدید را وارد کنید:";
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

    private void saveNewUserName(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAdminPanel(update);
                return;
            }
        }

        try {
            String name = update.getMessage().getText();
            cachedInfo.add(name);
        } catch (Exception e){
            String messageText = "فرمت اطلاعات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            estate = AdminPanelEstate.ASKING_FOR_TRANSACTION_INFO;
            sendMessageToUser(sendMessage);
            return;
        }

        requestNewUserForwardedMessage();
    }

    private void requestNewUserForwardedMessage(){
        estate = AdminPanelEstate.ADMIN_IS_ADDING_NEW_USER_FORWARDING_MESSAGE;

        String messageText = "یک پیام از کاربر جدید برای بات فوروارد کنید:";
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

    private void addNewUser(Update update){
        try {
            org.telegram.telegrambots.meta.api.objects.User user = update.getMessage().getForwardFrom();
            String username = user.getUserName();
            long ID = user.getId();
            String name = cachedInfo.get(0);
            cachedInfo.clear();
            User newUser = new User(ID, name, username, null, null);
            Saver.saveUser(newUser);
        } catch (Exception e){
            e.printStackTrace();
            String messageText = "فرمت اطلاعات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(mainMenu.getChatID()), messageText);
            estate = AdminPanelEstate.ASKING_FOR_TRANSACTION_INFO;
            sendMessageToUser(sendMessage);
            return;
        }

        String messageText = "کاربر جدید با موفقیت افزوده شد.";
        SendMessage sendMessage = new SendMessage(String.valueOf(getChat().getChatID()), messageText);
        sendMessageToUser(sendMessage);
        showAdminPanel(update);
    }
}
