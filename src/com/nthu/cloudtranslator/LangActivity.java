package com.nthu.cloudtranslator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class LangActivity extends Activity {
	private List<lang> langList = new ArrayList<lang>();
	JSONArray jArray = null ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lang);
		
		setLangList();
		showLangListview();
		showDetailView();
		//temp method 
		//new HttpRequestTask().execute();
	}
	
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		new HttpRequestTask().execute();
	}



	private class HttpRequestTask extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub

			return getCallList();
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			try {
				jArray = new JSONArray(result);
				
				if(jArray != null && jArray.length() > 0){
					for(int i = 0;i < jArray.length();i++){
						SipHandler.CallNumberArray.add(jArray.getString(i));
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		
		
	}
	
	private String getCallList(){
		String result = null;
		HttpClient client = null;
		try {
			client= new DefaultHttpClient();
                        
            HttpGet get = new HttpGet("http://140.114.71.168:8080/cloud/phone.get");
            
            HttpResponse response = client.execute(get); 

            HttpEntity resEntity = response.getEntity();
            
            if (response != null) {    
                result = EntityUtils.toString(resEntity);
            	//result = response.toString();
                Log.v("HTTP","Resp:"+result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
			client.getConnectionManager().shutdown();
        }
		
		return result;
	}

	
	private void setLangList() {
		// TODO Auto-generated method stub
		langList.add(new lang( "泰文中文互翻","尋找泰文翻譯員", "123",R.drawable.tai));
		langList.add(new lang( "英文中文互翻","尋找英文翻譯員", "123",R.drawable.eng));
		langList.add(new lang( "越南語中文互翻","尋找越南語翻譯員", "123",R.drawable.v));
		langList.add(new lang( "日文中文互翻","尋找日文翻譯員", "123",R.drawable.jp));
		
	}

	private void showLangListview() {
		// TODO Auto-generated method stub
		
		//build adapter
		ArrayAdapter<lang> adapter = new langListAdapter();
		//configure the listview
		ListView list = (ListView)findViewById(R.id.lang_listView);
		list.setAdapter(adapter);
	}
	
	private class langListAdapter extends ArrayAdapter<lang>{
		public langListAdapter(){
			super(LangActivity.this, R.layout.lang_item, langList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View itemView = convertView;
			if(itemView == null){
				itemView = getLayoutInflater().inflate(R.layout.lang_item,parent,false);
			}
			//find item
			lang cur_lang = langList.get(position);
			//set item description
			ImageView icon = (ImageView)itemView.findViewById(R.id.lang_icon);
			//Log.v("debug", "id:"+cur_lang.getIconId());
			icon.setImageResource(cur_lang.getIconId());
			
			TextView name = (TextView)itemView.findViewById(R.id.lang_name);
			name.setText(cur_lang.getLanguage());
			
			TextView subtitle = (TextView)itemView.findViewById(R.id.lang_subtitle);
			subtitle.setText(cur_lang.getDescription());
			
			return itemView;
		}
		
	}
	
	private void showDetailView() {
		//Declare an intent with action 
		final Intent intent = new Intent(".call_lang");
    	intent.setClass(this, CallActivity.class);
    	
		// TODO Auto-generated method stub
		ListView list = (ListView) findViewById(R.id.lang_listView);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View viewClicked,int position, long id) {
				
				//Get selected language
				lang mylang = langList.get(position);
				
				//make a toast notify
				String message = "Call :" + mylang.getDescription();
				Toast.makeText(LangActivity.this, message, Toast.LENGTH_LONG).show();
				
				//put data into intent
				intent.putExtra("lang", mylang.getIconId());
				
				//set call number
				String ip_address = "@140.114.71.168";
				//SipHandler.setSipAddress(test);
				//SipHandler.setSipAddress(mylang.getCallNumber()+""+ip_address );
				
				//start Activity with action
				if(jArray == null || jArray.length() == 0){
					Toast.makeText(LangActivity.this, "翻譯人員忙線中，請稍後再撥...", Toast.LENGTH_LONG).show();
				}else{
					startActivityForResult(intent,1);
				}
				
				//for test
				//startActivityForResult(intent,1);
				
			}
		});
	}

}
