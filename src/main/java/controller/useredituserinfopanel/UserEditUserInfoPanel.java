package controller.useredituserinfopanel;

import controller.MainMenu;
import controller.userEditTransactionPanel.UserEditTransactionPanelEstate;

public class UserEditUserInfoPanel {
    private final MainMenu mainMenu;
    private UserEditTransactionPanelEstate estate;

    public UserEditUserInfoPanel(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        this.estate = UserEditTransactionPanelEstate.USER_EDIT_TRANSACTION_PANEL;
    }
}
