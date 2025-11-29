package com.example.shopgaubong.util;

import com.example.shopgaubong.entity.Account;

public class SessionManager {

    private static SessionManager instance;
    private Account currentAccount;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(Account account) {
        this.currentAccount = account;
    }

    public void logout() {
        this.currentAccount = null;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public boolean isLoggedIn() {
        return currentAccount != null;
    }

    public String getCurrentUsername() {
        return currentAccount != null ? currentAccount.getUsername() : null;
    }
}

