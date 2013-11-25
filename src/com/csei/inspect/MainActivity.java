package com.csei.inspect;
import com.example.viewpager.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Window;
@SuppressLint("WorldReadableFiles")
public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//ȥ��������ȫ����ʾ
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         SharedPreferences preferences=getSharedPreferences("count", MODE_WORLD_READABLE); 
         int count=preferences.getInt("count", 0);
         if(count!=0){
        	 Intent intent=new Intent(MainActivity.this,StartInspectActivity.class);
        	 MainActivity.this.startActivity(intent);
        	 MainActivity.this.finish();
         }else{
        	 Intent intent=new Intent(MainActivity.this,WelcomeActivity.class);
        	 MainActivity.this.startActivity(intent);
        	 MainActivity.this.finish();
        	 
         }
         Editor editor=preferences.edit();
         editor.putInt("count", ++count);
         editor.commit();
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
    }   
    
}
