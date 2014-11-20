package com.nthu.cloudtranslator;

import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class CallActivity extends Activity {
	
	//Back Language Intent 
	private Intent back_lang = new Intent();
	
	//declare variable for time
	private int sec = 0;
	private int min = 0;
	private String time_format = "";
	private TextView timerTextView;
	
	//declare Audio Manager to control device ,initial in create();
	public AudioManager _audioManager;
	
	//Receive Broadcast from SipHandler when End_Call ,register is in create();
	private BroadcastReceiver receiver = new BroadcastReceiver(){
    	
		@Override
		public void onReceive(Context arg0, Intent i) {
			// TODO Auto-generated method stub
			String state = "";
			state = i.getStringExtra("call");
			if(state.equals("end")){
				//if state is end call 
				unregisterReceiver(receiver);
				//Activity will back to Lang 
				startActivity(back_lang);
			}else if(state.equals("no_phone")){
				//if state is no available call to make
				Toast.makeText(CallActivity.this, "翻譯員忙線中，請稍後撥打", Toast.LENGTH_LONG).show();
				unregisterReceiver(receiver);
				//Activity will back to Lang and send request to get call again
				startActivity(back_lang);
			}
		}
    	
     };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//set view
		setContentView(R.layout.activity_call);
		
		//get audio control permission
		_audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
		
		//set the default is speaker off default
		if(_audioManager.isSpeakerphoneOn()){
    		Log.v("CallActivity", "Speaker OFF");
    		_audioManager.setSpeakerphoneOn(false);
    	}
		
		//declare buttons which declare in res/layout/activity_call.xml
		Button endCallBtn = (Button)findViewById(R.id.end_call);
		Button nextCallBtn = (Button)findViewById(R.id.next_call_btn);
		ToggleButton speakerToggle = (ToggleButton)findViewById(R.id.speaker_btn);
		
		//declare a timer to count the time 
		timerTextView = (TextView)findViewById(R.id.showTime);
		
		//declare Intent to control SipHandler Service 
		final Intent call = new Intent(SipHandler.ACTION_CALL);
		final Intent endCall = new Intent(SipHandler.ACTION_ENDCALL);
		final Intent nextCall = new Intent(SipHandler.ACTION_NEXTCALL);
		//set get back to LangActivity.class Intent 
		back_lang.setClass(CallActivity.this, LangActivity.class);
		
		/*Handle selected language and set image*/
		// get Intent object from LangActivity class
        Intent intent = getIntent();
        String action = intent.getAction();
		if (action.equals(".call_lang")) {
            //set image 
            int img_id = intent.getIntExtra("lang",0);
            ImageView icon = (ImageView)findViewById(R.id.call_lang_icon);
			//Log.v("debug", "id:"+img_id);
			icon.setImageResource(img_id);
        }
		
		
		//register receiver for SipHandler when phone end  
		IntentFilter endCall_filter = new IntentFilter("action.call.END");
		registerReceiver(receiver, endCall_filter);
		
		//added end_call_btn listener
		endCallBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	startService(endCall);
            }
        });
		//added next_call_btn listener
		nextCallBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	startService(nextCall);
            	Toast.makeText(CallActivity.this, "嘗試撥打下一通電話", Toast.LENGTH_LONG).show();
            }
        });
		//add speakerToggle listener
		speakerToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		            // The toggle is enabled
		        	//set Speaker ON
		        	if(!_audioManager.isSpeakerphoneOn()){
		        		Log.v("CallActivity", "Speaker ON");
		        		_audioManager.setSpeakerphoneOn(true);
		        	}
		        } else {
		            // The toggle is disabled
		        	//set Speaker OFF
		        	if(_audioManager.isSpeakerphoneOn()){
		        		Log.v("CallActivity", "Speaker OFF");
		        		_audioManager.setSpeakerphoneOn(false);
		        	}
		        	
		        }
		    }
		});
		
		sec = 0;
		min = 0;
		//TIMER
		Timer time = new Timer();
		time.schedule(task, 0, 1000);
		
		/*Here to make phone Call */
		//send call to SipHandler to generate call
		startService(call);
	}
	
	@SuppressLint("HandlerLeak") 
	//handle counter
	private Handler timeHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(sec >= 60){
				sec = 0;
				min++;
			}
			if(min < 10){
				time_format = "0"+min+":";
			}else {
				time_format = ""+min+":";
			}
			
			if(sec < 10){
				time_format += "0"+sec;
			}else {
				time_format += ""+sec;
			}
			
			timerTextView.setText(time_format);
		}
		
		
		
	};
	//Task to run timer
	private TimerTask task = new TimerTask(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			sec++;
			Message msg = new Message();
			msg.what = 1;
			
			timeHandler.sendMessage(msg);
			
		}
		
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//return to Lang and flush the call  
		this.finish();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//return to Lang and flush the callActivity ,in case back btn ERROR!!!!
		this.finish();
	}

	
	
	
	
	
	
}
