package ashalagari.karnabanu.blastir;


import android.content.Context;
import android.view.MenuItem;

import java.io.IOException;

public class ConnectionCheck extends Thread implements Runnable{
    MainActivity mainActivity;
    ConnectionCheck(Context context){
        mainActivity=(MainActivity)context;
    }

    @Override
    public void run() {
        System.out.println("Check Started");
        try{
            int data=mainActivity.inputStream.read();
            if(data==-1){
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MenuItem) (mainActivity.menu.findItem(R.id.action_connect))).setTitle("Connect");
                        mainActivity.setBluetoothStatus(0, mainActivity.menu.findItem(R.id.bluetoothSwitch));
                        mainActivity.findViewById(R.id.buttonGrid).startAnimation(mainActivity.blur);
                    }
                });
                mainActivity.connected=false;
                return;
            }
        } catch (IOException e) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MenuItem) (mainActivity.menu.findItem(R.id.action_connect))).setTitle("Connect");
                    mainActivity.setBluetoothStatus(0, mainActivity.menu.findItem(R.id.bluetoothSwitch));
                    mainActivity.findViewById(R.id.buttonGrid).startAnimation(mainActivity.blur);
                }
            });
            mainActivity.connected=false;
            return;
        }
    }

}
