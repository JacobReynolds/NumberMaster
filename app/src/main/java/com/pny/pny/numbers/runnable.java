package com.pny.pny.numbers;

import android.widget.Button;

/**
 * Created by pny on 8/11/15.
 */
public class runnable implements Runnable {
    private Button currentButton;
    private String i;
    private Boolean enable = false;

    public void setParams(Button button, String i, boolean enable) {
        this.currentButton = button;
        this.i = i;
        this.enable = enable;
    }

    @Override
    public void run() {
        if (enable) {
            currentButton.setClickable(true);
            currentButton.setText(i);
        }
        else
            currentButton.setText(i);
    }
}
