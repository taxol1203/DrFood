package com.example.drfood;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Setting_Activity extends Activity {
    ToggleButton Start_Barcode;
    Boolean Ready;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_page);
        String True = getIntent().getStringExtra("True");
        if(True.equals("1")){
            Ready = true;
        }else{
            Ready = false;
        }

        Start_Barcode = (ToggleButton)findViewById(R.id.Switch);


        Start_Barcode.setChecked(Ready);
        Log.d("Ready1", ""+Ready);



        Start_Barcode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Ready = true;
                   //Log.d("Ready", ""+Ready);
                }else{
                    Ready = false;
                    //Log.d("Ready", ""+Ready);
                }
            }
        });




    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        Intent intent = new Intent();
        intent.putExtra("Ready",Ready);
        Log.d("Ready", ""+Ready);
        setResult(3200, intent);


        //액티비티(팝업) 닫기
        finish();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        return;
    }
}


