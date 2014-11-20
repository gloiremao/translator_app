package com.nthu.cloudtranslator;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	private String account = "";
	private String password = "";
	//intent for change activity to LangAvtivity
	private Intent intent = new Intent();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //prepare layout ,call before find objects 
        setContentView(R.layout.activity_login);
        
        //check Network state
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        //Returns connection status information about a particular network type.
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    if (networkInfo != null && networkInfo.isConnected()) {
	    	Toast.makeText(LoginActivity.this, "歡迎使用", Toast.LENGTH_LONG).show();
	    } else {
	    	Toast.makeText(LoginActivity.this, "無可用連線，請檢查網路狀態", Toast.LENGTH_LONG).show();
	    }

        //Start SIP service 
        Intent startSIP = new Intent(this, SipHandler.class); 
        //start SipHandler in Background
        startService(startSIP);  
        
        //declare input area and btn
        final EditText input_account = (EditText)findViewById(R.id.input_account);
        final EditText input_password = (EditText) findViewById(R.id.input_password);
        Button login_btn = (Button)findViewById(R.id.buttonlogin);
        //focus account 
        input_account.requestFocus();
        
        //set hyper link for register
        TextView href = (TextView)findViewById(R.id.herf_reg);
        href.setText(Html.fromHtml("<a href=\"http://140.114.71.168:8080/cloud/member.html\">線上註冊</a> "));
        href.setMovementMethod(LinkMovementMethod.getInstance());
        
        
        //Intnet sent to SipHandler for register 
        final Intent reg = new Intent(SipHandler.ACTION_REGISTER);
        //activity start by intent
        intent.setClass(LoginActivity.this, LangActivity.class);
        
        
        login_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	account = input_account.getText().toString();
            	password = input_password.getText().toString();
            	//debug 
            	//Log.v("Login","account:"+account+" pwd:"+password);
            	if(account.length() == 0 || password.length() == 0){
            		//prevent empty
            		Toast.makeText(LoginActivity.this, "帳號或密碼不可為空", Toast.LENGTH_LONG).show();
            	}else{
            		// set variable in SipHandler
            		SipHandler.setProfile(account, password);
            		//start Register action in background
            		startService(reg);
            		//Register receiver if login successfully .Broadcast by SipHandler
            		IntentFilter receiver_intent_filter = new IntentFilter("action.login.Ready");
            		registerReceiver(receiver, receiver_intent_filter);
            	}
            }
        });  
        
        
        
    }
    
    //Handle login state (sent by SipHandler
    private BroadcastReceiver receiver = new BroadcastReceiver(){
    	
		@Override
		public void onReceive(Context arg0, Intent i) {
			// TODO Auto-generated method stub
			String state = "";
			//extra message from intent
			state = i.getStringExtra("login");
			if(state.equals("ready")){
				unregisterReceiver(receiver);
				//change activity
				startActivity(intent);
			}else if(state.equals("failed")){
				//register failed
				Toast.makeText(LoginActivity.this, "帳號或密碼錯誤", Toast.LENGTH_LONG).show();
			}
		}
    	 
     };
     
     
    
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//Handle login
		if(SipHandler.getState()){
			Log.v("Login","Already Login");
			startActivity(intent);
		}
	}



	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
	}



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.finish();
	}
	
	
    
    
}
