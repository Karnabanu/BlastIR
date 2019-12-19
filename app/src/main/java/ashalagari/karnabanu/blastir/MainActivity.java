package ashalagari.karnabanu.blastir;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
    final WatchDog watchDog=new WatchDog(this);
    private Set<BluetoothDevice> pairedDevices;
    BluetoothSocket socket = null;
    InputStream inputStream;
    OutputStream sendingStream;
    ConnectionCheck connectionCheck;
    GridListener gridListener;
    Drawable icBluetoothEnable;
    Drawable icBluetoothDisable;
    Menu menu;
    AlphaAnimation blur = new AlphaAnimation(0.5F, 0.5F);
    AlphaAnimation unblur=new AlphaAnimation(1F,1F);
    boolean connected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        icBluetoothEnable=ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bluetooth_enable,null);
        icBluetoothDisable=ResourcesCompat.getDrawable(getResources(),R.drawable.ic_bluetooth_disable,null);
        //findViewById(R.id.buttonGrid).setAlpha((float) 0.5);
        blur.setDuration(0); // Make animation instant
        blur.setFillAfter(true); // Tell it to persist after the animation ends
        unblur.setDuration(0);
        unblur.setFillAfter(true);
        findViewById(R.id.buttonGrid).startAnimation(blur);
        this.registerReceiver(watchDog, new IntentFilter(BA.ACTION_STATE_CHANGED));
        gridListener=new GridListener(this);
        rigisterGridListner();
        System.out.println("Child count is" + ((ViewGroup) ((ViewGroup) findViewById(R.id.buttonGrid)).getChildAt(0)).getChildCount());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu pmenu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu=pmenu;
        getMenuInflater().inflate(R.menu.menu_main, pmenu);
        setBluetoothStatus(0, pmenu.findItem(R.id.bluetoothSwitch));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id==R.id.bluetoothSwitch){
            setBluetoothStatus(1,null);
            return true;
        }
        if (id == R.id.action_exit) {
            finish();
            return true;
        }
        if(id==R.id.action_connect){
            if(!connected) {
                if (!BA.isEnabled()) {
                    Toast.makeText(this, "Please Trun On Bluetooth", Toast.LENGTH_LONG).show();
                    return true;
                }
                setPopupWindow();
                //connect();
                return true;
            }
            else{
                disconnect();
            }
        }

        return super.onOptionsItemSelected(item);
    }
    public void setBluetoothStatus(int callFrom,MenuItem menuItem){
        int ONCREATE=0,ONSELECT=1;
        if(callFrom==ONCREATE){
            if(BA.isEnabled()){
                menuItem.setIcon(icBluetoothEnable);
            }
            else {
                menuItem.setIcon(icBluetoothDisable);
            }
        }
        if(callFrom==ONSELECT){
            if(BA.isEnabled()){
                BA.disable();
            }
            else{
                BA.enable();
            }
        }
    }

    public void setPopupWindow(){
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.devicelist, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,true);

        Button cancelButton = (Button)popupView.findViewById(R.id.cancelButton);
        Button scanButton=(Button)popupView.findViewById(R.id.scanButton);
        final ListView devicesList=(ListView)popupView.findViewById(R.id.devicesList);

        pairedDevices = BA.getBondedDevices();
        ArrayList list = new ArrayList();

        for(BluetoothDevice bt : pairedDevices) {
            list.add(bt.getName()+"\n"+bt.getAddress());
        }
        if(list.isEmpty())
        {
            list.add(" \n ");
            Toast.makeText(getBaseContext(),"No Paired Devices Found",Toast.LENGTH_LONG);
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,R.layout.devicelist_item, list);
        devicesList.setAdapter(adapter);

        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                popupWindow.dismiss();
                ArrayList temp=new ArrayList();
                temp.addAll(pairedDevices);
                if(connect((BluetoothDevice) (temp.get(position)))) {
                    findViewById(R.id.buttonGrid).startAnimation(unblur);
                    (menu.findItem(R.id.action_connect)).setTitle("Disconnect");
                    //((android.support.v7.view.menu.ActionMenuItemView)findViewById(R.id.action_connect)).setTitle("Disconnect");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT);
                        }
                    });
                    setBluetoothStatus(0, menu.findItem(R.id.bluetoothSwitch));
                    System.out.println("Connected");
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Client Not Reachable", Toast.LENGTH_SHORT);
                        }
                    });

                }
            }
        });

        cancelButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                popupWindow.dismiss();
            }
        });

        scanButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                System.out.println("list number is " + devicesList.getFocusedChild() + " " + devicesList.isClickable()+" "+devicesList.isSelected());

                Toast.makeText(getApplicationContext(), "Under Contruction", Toast.LENGTH_SHORT);
            }
        });
        //popupWindow.showAsDropDown(item, 50, -30);
        popupWindow.showAtLocation(findViewById(R.id.buttonGrid), Gravity.CENTER,0,0);
    }

    public boolean connect(BluetoothDevice device){
        try{
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket.connect();

            inputStream = socket.getInputStream();
            sendingStream = socket.getOutputStream();
            try {
                connectionCheck = new ConnectionCheck(this);
                connectionCheck.start();
                connected=true;
                return true;
            } catch (Exception e) {
                System.out.println("Bug " + e);
            }
        }
        catch (IOException e){
            System.out.println(e);
            return false;
        }
        return false;
    }
    public void disconnect(){
        if(socket!=null) {
            try {
                socket.close();
                sendingStream.close();
                inputStream.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public void rigisterGridListner(){
        ViewGroup grid;
        ViewGroup row;
        View button;
        grid=(ViewGroup)findViewById(R.id.buttonGrid);

        for(int i=0;i<grid.getChildCount();i++){
            row=(ViewGroup)grid.getChildAt(i);
            for(int j=0;j<row.getChildCount();j++){
                if((i==0)&&((j==1)||(j==2))){
                    continue;
                }
                button=(View)row.getChildAt(j);
                button.setOnClickListener(gridListener);
            }
        }
    }

    public void sendCode(byte buffer)
    {
        try {
            sendingStream.write(buffer);
            sendingStream.flush();
            System.out.println("Sent "+(char)buffer+" "+buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
