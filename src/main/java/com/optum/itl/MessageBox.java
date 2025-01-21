package com.optum.itl;

import javax.swing.*;

public class MessageBox {

    public MessageBox() {
        MessageBox mbox = new MessageBox();
    }

    public static void infoBox(String infoMessage, String titleBar)
    {
        final JDialog dialog = new JDialog();
        dialog.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(dialog, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
}
