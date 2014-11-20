package com.nthu.cloudtranslator;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class SipHandler extends Service {
	
	

    public SipManager manager = null;
    public SipProfile profile = null;
    public SipAudioCall call = null;
    public BroadcastReceiver callReceiver;
    
    public static String user_account = "";
    public static String user_password = "";
    //the phone number 
    public static String sipAddress = null;
    
    static boolean login_state = false;
    
    public static String ACTION_REGISTER = "action.REGISTER";
    public static String ACTION_CALL = "action.CALL";
    public static String ACTION_ENDCALL = "action.ENDCALL";
    public static String ACTION_NEXTCALL = "action.NEXTCALL";
    public static String ACTION_SPEALER = "action.SPEAKER";
    
    private NotificationManager mNotificationManager;
    //get call list(temp)
    //Not formal solution
    public static List<String> CallNumberArray = new ArrayList<String>();
    
    int mId = 1;
    
	
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		IntentFilter filter = new IntentFilter();
        filter.addAction("action.INCOMING_CALL");
        this.registerReceiver(IncomingCallReceiver, filter);
        
        //Create a Notification 
       
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker("服務執行中") 
                .setContentTitle("雲端翻譯服務")
                .setContentText("服務執行中")
                .setContentInfo("");
        //Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, LoginActivity.class);
        
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,resultIntent,0);
        
	    mBuilder.setContentIntent(resultPendingIntent);
	    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	     
		// mId allows you to update the notification later on.
	    mNotificationManager.notify(mId,mBuilder.build());
        
		initializeManager();
	}
	
	

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		// TODO Auto-generated method stub
		//prevent from deleting whole task by Task Manager 
		//If task stop, the whole service will stop
		super.onTaskRemoved(rootIntent);
		closeLocalProfile();
		SipManager manager = null;
	    SipProfile profile = null;
	    SipAudioCall call = null;
	    mNotificationManager.cancel(mId);
		stopSelf();
	}



	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeLocalProfile();
		SipManager manager = null;
	    SipProfile profile = null;
	    SipAudioCall call = null;
	    mNotificationManager.cancel(mId);
		stopSelf();
	    
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if(action != null){
			if(action.equals("action.REGISTER")){
				Log.v("SipHandle","initializeManager");
				initializeManager();
			}else if (action.equals("action.CALL")){
				Log.v("SipHandle","initiateCall");
				PhoneCallHandler();
			}else if (action.equals("action.ENDCALL")){
				Log.v("SipHandle","endCall");
				endCall();
			}else if (action.equals("action.NEXTCALL")){
				Log.v("SipHandler", "next call");
				if(call != null){
					try {
						call.endCall();
					} catch (SipException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					PhoneCallHandler();
				}
				
			}
		}else{
			Log.v("SipHandle","action null");
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	//handle call
	private void PhoneCallHandler() {
		// TODO Auto-generated method stub
		int len = CallNumberArray.size();
		if(len > 0){
			double random = Math.random()*10;
			int index = (int) (random%len);
			//Log.v("SipHandler", "Test random at len:"+len+"random:"+index);
			if(CallNumberArray.get(index) != null){
				sipAddress = CallNumberArray.get(index);
				Log.v("SipHandler", "Call: "+sipAddress+" at "+index);
				CallNumberArray.remove(index);
				initiateCall();
			}
		}else {
			Log.v("SipHandle","EndCall from "+sipAddress);
	    	Intent no_phone_available_i = new Intent("action.call.END");
	    	no_phone_available_i.putExtra("call", "no_phone");
	    	sendBroadcast(no_phone_available_i);
		}
		
		//for test 
		//sipAddress = "0953701571";
		//initiateCall();
		
	}



	//Get call list by http get 
	
	public static void setProfile(String account,String password){
		user_account = account;
		user_password = password;
	}
	
	public static void setSipAddress(String address){
		sipAddress = address;
	}
	
	public static boolean getState(){
		
		return login_state;
	}
	
	
	public void initializeManager() {
        if(manager == null) {
          manager = SipManager.newInstance(this);
        }
        initializeLocalProfile();
    } 
	 	
	private void initializeLocalProfile() {
		// TODO Auto-generated method stub
		if (manager == null) {
            return;
        }

        if (profile != null) {
            closeLocalProfile();
        }
        Log.v("SipHandle","Try to build Profile");
        if(user_account.length() == 0 || user_password.length() == 0){
        	Log.v("SipHandle","empty");
        	return;
        }
        
       try {
    	   	
    	   	String domain = "140.114.71.168";
        	
    	   	SipProfile.Builder builder = new SipProfile.Builder(user_account, domain);
    	   	builder.setPassword(user_password);
    	   	builder.setPort(5060);
    	   	profile = builder.build();
    	   	
    	   	if(profile.getAutoRegistration()){
				Log.v("SipHandle","getAutoRegistration  TRUE");
			}else{
				Log.v("SipHandle","getAutoRegistration  FALSE");
			}
	       
        }catch(ParseException pe){
        	Log.v("SipHandle","ParseException Connection Error.");
        } 
       
       SendRegistrationRequest();
	}
	
	private void SendRegistrationRequest(){
        try {
        	login_state = false;
        	Log.v("SipHandle","Try to register");
        	
        	Intent i = new Intent("action.INCOMING_CALL");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, Intent.FILL_IN_DATA);
			manager.open(profile,pi,null);
			
			//debug
			
			if(manager.isOpened(profile.getUriString())){
				Log.v("SipHandle","Profile OPEN");
			}else{
				Log.v("SipHandle","Profile CLOSE");
			}
			
			manager.setRegistrationListener(profile.getUriString(), new SipRegistrationListener() {
                public void onRegistering(String localProfileUri) {
                	Log.v("SipHandle","Registering with SIP Server...");
                	login_state = false;
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                	Log.v("SipHandle","Ready");
                	
                	//send a broadcast  
                	Intent i = new Intent("action.login.Ready");
                	i.putExtra("login", "ready");
                	sendBroadcast(i);
                	login_state = true;
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                        String errorMessage) {
                	Log.v("SipHandle","Registration failed.  Please check settings.");
                	Intent i = new Intent("action.login.Ready");
                	i.putExtra("login", "failed");
                	sendBroadcast(i);
                	login_state = false;
                }
            });
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Log.v("SipHandle","Done");
	}
	
	
	private void closeLocalProfile() {
		// TODO Auto-generated method stub
		Log.v("SipHandler","Close Profile ");
		if (manager == null) {
            return;
        }
        try {
            if (profile != null) {
            	Log.v("SipHandler","Profile " + profile.getUserName()+" close.");
                manager.close(profile.getUriString());
            }
        } catch (Exception ee) {
            Log.v("SipHandler","Failed to close local profile.");
        }
	}
	
	public void initiateCall() {

        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                // Much of the client's interaction with the SIP Stack will
                // happen via listeners.  Even making an outgoing call, don't
                // forget to set up a listener to set things up once the call is established.
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    call.startAudio();
                    call.setSpeakerMode(false);
                    if(call.isMuted())call.toggleMute();
                    Log.v("SipHandle","MakePhoneCall to "+sipAddress+"@140.114.71.168");
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                	//endCall();
                	Log.v("SipHandle","Hang Up Phone by Translator");
                }
            };

            call = manager.makeAudioCall(profile.getUriString(), sipAddress+"@140.114.71.168", listener, 30);

        }
        catch (Exception e) {
            Log.v("SipHandler", "Error when trying to close manager.");
            if (profile != null) {
                try {
                    manager.close(profile.getUriString());
                } catch (Exception ee) {
                    Log.v("SipHandler",
                            "Error when trying to close manager.");
                    ee.printStackTrace();
                }
            }
            if (call != null) {
                call.close();
            }
        }
    }
	
	public void endCall(){
		if(call != null) {
            try {
              call.endCall();
            } catch (SipException se) {
                Log.v("SipHandler",
                        "Error ending call.", se);
            }
            call.close();
        }
		Log.v("SipHandle","EndCall from "+sipAddress);
    	Intent endCall_i = new Intent("action.call.END");
    	endCall_i.putExtra("call", "end");
    	sendBroadcast(endCall_i);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private BroadcastReceiver IncomingCallReceiver = new BroadcastReceiver(){
    	
		@Override
		public void onReceive(Context context, Intent intent) {
	        SipAudioCall incomingCall = null;
	        try {

	            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
	                @Override
	                public void onRinging(SipAudioCall call, SipProfile caller) {
	                    try {
	                        call.answerCall(30);
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }
	            };

	           

	            incomingCall = manager.takeAudioCall(intent, listener);
	            incomingCall.answerCall(30);
	            incomingCall.startAudio();
	            incomingCall.setSpeakerMode(true);
	            if(incomingCall.isMuted()) {
	                incomingCall.toggleMute();
	            }

	            
	        } catch (Exception e) {
	            if (incomingCall != null) {
	                incomingCall.close();
	            }
	        }
	    }
    	 
     };
	
}
