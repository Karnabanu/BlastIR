package ashalagari.karnabanu.blastir;

import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by kalpana on 7/29/2016.
 */
public class GridListener implements View.OnClickListener {
    MainActivity mainActivity;
    Button remoteButtons[];
    char codes[]={'k','l','B','x','C','o','a','b','c','n','d','e','f','u','g','h','i','t','q','j','y','v','r','r','r','m','z','p','w','A'};//=new int[30];


    GridListener(MainActivity mA){
        mainActivity=mA;
        remoteButtons=new Button[30];
        fillTheButtonArray();
    }
    public void fillTheButtonArray(){
        ViewGroup grid;
        ViewGroup row;
        int k=0;
        grid=(ViewGroup)mainActivity.findViewById(R.id.buttonGrid);

        for(int i=0;i<grid.getChildCount();i++){
            row=(ViewGroup)grid.getChildAt(i);
            for(int j=0;j<row.getChildCount();j++){
                if((i==0)&&((j==1)||(j==2))){
                    continue;
                }
                remoteButtons[k]=(Button)row.getChildAt(j);
                k++;
            }
        }
    }
    @Override
    public void onClick(View v) {
        if(!mainActivity.connected){
            System.out.println("Please Connect your Divice");
            return;
        }
        for(int i=0;i<30;i++){
            if(remoteButtons[i]==(Button)v){
                mainActivity.sendCode((byte)codes[i]);
            }
        }
    }
}
