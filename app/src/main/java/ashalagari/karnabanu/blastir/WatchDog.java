package ashalagari.karnabanu.blastir;

/**
 * Created by kalpana on 7/28/2016.
 */
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Button;

/**
 * Created by kalpana on 6/7/2016.
 */
public class WatchDog extends BroadcastReceiver {
    private MainActivity mainActivity;
    private android.support.v7.view.menu.ActionMenuItemView menuItem;
    WatchDog(MainActivity mA){
        mainActivity=mA;
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        final MainActivity mainActivity=(MainActivity)context;
        String action=intent.getAction();
        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
            menuItem=((android.support.v7.view.menu.ActionMenuItemView)mainActivity.findViewById(R.id.bluetoothSwitch));
            if(mainActivity.BA.getState()==BluetoothAdapter.STATE_TURNING_OFF){
                System.out.println("Watchdog off");
                menuItem.setIcon(mainActivity.icBluetoothDisable);
                /*mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });*/
                return;

            }
            if(mainActivity.BA.getState()==BluetoothAdapter.STATE_TURNING_ON){
                System.out.println("Watchdog on");
                menuItem.setIcon(mainActivity.icBluetoothEnable);
                /*mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                    }
                });*/
                return;
            }
        }
    }
}
