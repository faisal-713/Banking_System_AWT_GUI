package com.banking;

import com.banking.ui.LoginFrame;

import java.awt.*;


public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
