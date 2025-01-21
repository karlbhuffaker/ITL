package com.optum.itl;

import java.util.Date;

public class ITLHourlyJob implements Runnable {

    @Override
    public void run() {
        System.out.println("A Hourly Job trigged by scheduler @" + new Date(System.currentTimeMillis()));
    }
}
