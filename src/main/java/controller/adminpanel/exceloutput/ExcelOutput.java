//package controller.adminpanel.exceloutput;
//
//import controller.adminpanel.AdminPanel;
//import database.Loader;
//import models.User;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.telegram.telegrambots.meta.api.objects.Update;
//
//import java.util.ArrayList;
//
//public class ExcelOutput {
//    private final AdminPanel adminPanel;
//
//    public ExcelOutput(AdminPanel adminPanel) {
//        this.adminPanel = adminPanel;
//    }
//
//    public void handleNewUpdate(Update update){
//        if (update.getMessage().getText().equals("کل تراکنش‌ها")){
//            showListOfCreditors();
//        } else
//        if (update.getMessage().getText().equals("کل تراکنش‌های ورودی")){
//            showListOfDebtors();
//        } else
//        if (update.getMessage().getText().equals("کل تراکنش‌های خروجی")){
//            showListOfAllUsers();
//        } else
//        if (update.getMessage().getText().equals("کل کاربران")){
//            requestWantedUserNumericID();
//        }
//    }
//
//    private void sendAllUsersExcelFile(Update update){
//        ArrayList<User> users = Loader.loadAllUsers();
//
//        XSSFWorkbook workbook = new XSSFWorkbook();
//        XSSFSheet spreadsheet = workbook.createSheet(" Student Data ");
//    }
//
//    public AdminPanel getAdminPanelController() {
//        return adminPanel;
//    }
//}
