package com.jieehd.villain.toolkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class FeedbackTab extends ListActivity {
	
	public static String URL = "http://dl.dropbox.com/u/44265003/bugs.json";
	public static JSONObject json;
	public static Preference avail_tweaks;
	public static String name;
	public static ListView showTweaks;
	public static List<String> listItems = new ArrayList<String>();
	public static List<String> urlItems = new ArrayList<String>();
	public static ArrayAdapter<String> adapter;
	public static Dialog dialog;
	public static Context cx;
	
    /** Called when the activity is first created. */
    @Override
    @SuppressWarnings("unused")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ArrayAdapter<String>(this,R.layout.row_bugs, R.id.filename,listItems);
        setListAdapter(adapter);
        
		dialog = new Dialog(this);
		dialog.setTitle(getString(R.string.loading_dialog_title));
		dialog.setContentView(R.layout.spinner_dialog);
		Spinner spin = (Spinner) findViewById(R.id.spinner);
		dialog.show();
        
		listItems.clear();
		
        new Read().execute();
        
    }
    
    
    public class Display {
    	public HashMap<String, String> mContent;
    	public String name;
    	public String url;

    	public Display (HashMap<String,String> tweaks_list) {
    		mContent = tweaks_list;
    		for (Map.Entry<String, String> entry : mContent.entrySet()) {
    		    name = entry.getKey();
    		    url = entry.getValue();
    		    runOnUiThread(new Runnable() {
    		        public void run() { 		        	
    		        	listItems.add(name);
    		        	urlItems.add(url);
    		        }
    		    });
    		    
    		}
    		
    	}
    }
    
	    // A class that will run Toast messages in the main GUI context
	    private class ToastMessageTask extends AsyncTask<String, String, String> {
	     String toastMessage;
	
		     @Override
		     protected String doInBackground(String... params) {
		         toastMessage = params[0];
		         return toastMessage;
		     }
		     
		     @SuppressWarnings("unused")
		     protected void OnProgressUpdate(String... values) { 
		         super.onProgressUpdate(values);
		     }
		    // This is executed in the context of the main GUI thread
		     protected void onPostExecute(String result){
		            Toast toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT);
		            toast.show();
		     }
	    }
    
    public JSONObject getQuote() throws ClientProtocolException, IOException, JSONException{
		HttpClient client = new DefaultHttpClient();
    	StringBuilder url = new StringBuilder(URL);
    	HttpGet get = new HttpGet(url.toString());
    	HttpResponse r = client.execute(get);
    	int status = r.getStatusLine().getStatusCode();
    	if (status == 200) {
    		HttpEntity e = r.getEntity();
    		String data = EntityUtils.toString(e);
    		JSONObject stream = new JSONObject(data);
    		JSONObject tweaks = stream.getJSONObject("current-bugs");
    		return tweaks;
    	} else {
    		return null;
    	}
    }
    
    
    public class Read extends AsyncTask<String, Integer, Display> {
		@Override
		protected Display doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {

				json = getQuote();
				JSONArray avail_tweaks = null;
				HashMap<String, String> tweaks_list = new HashMap<String, String>();
				avail_tweaks = json.getJSONArray("buglist");
					for (int i = 0; i < avail_tweaks.length(); i++) {
					    JSONObject row = avail_tweaks.getJSONObject(i);
					    name = row.getString("bug");
					    String desc = row.getString("desc");
					    tweaks_list.put(name, desc);
					}
					
				return new Display(tweaks_list);

			} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			new ToastMessageTask().execute("A server issue occured, please try again.");
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			new ToastMessageTask().execute("Error whilst reading content.");
			} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			new ToastMessageTask().execute("No bugs HOORAY!");
			}

			return null;
		}
		
		@Override
		public void onPostExecute(final Display result) {
			 dialog.dismiss();
			 adapter.notifyDataSetChanged();
		}
    }
}