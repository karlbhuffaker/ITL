package com.optum.itl;

import java.util.Date;

public class ITLMinuteJob implements Runnable {

    @Override
    public void run() {
        System.out.println("A minute Job trigged by scheduler @" + new Date(System.currentTimeMillis()));
    }
}
