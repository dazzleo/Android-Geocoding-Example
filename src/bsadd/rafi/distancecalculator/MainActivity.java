package bsadd.rafi.distancecalculator;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private EditText addressField;
	private Button calculateButton;
	private TextView resultView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		// Get references of the view eleemnts from the xml layout 
		addressField = (EditText) findViewById(R.id.et_address);
		calculateButton = (Button) findViewById(R.id.bt_calculate);
		resultView = (TextView) findViewById(R.id.tv_result);
		
		// Set what will happen if the Calculate button is clicked
		calculateButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				calculateResult();
			}
		});
	}
	
	private void calculateResult() {
		String address = addressField.getText().toString();
		
		// In Android, we don't perform long running tasks (e.g. fetching something from the Internet)
		// in the main thread. Otherwise it will freeze the user interface and you will get an 
		// "Application Not Responding" error. AsyncTask class provides an easy way to perform 
		// long running tasks in the background thread. The DataLoader class used here is a subclass
		// of the AsyncTask. It fetches data from the web API and shows it in the textview. 
		//
		// See the definition of the DataLoader class below.
		DataLoader dataLoader = new DataLoader();
		
		// When you call the execute method, the doInBackground method of the DataLoader will be called
		// with corresponding parameter.
		dataLoader.execute(address);
	}
	
	/**
	 * See also: {@link AsyncTask}
	 * 
	 * DataLoader extends AsyncTask. When you execute an AsynTask, first the
	 * {@link #onPreExecute()} method is called. Then
	 * {@link #doInBackground(String...)} will be called with the parameters of
	 * the dataLoader.execute() method. The doInBackground returns a jsonString.
	 * Then the {@link #onPostExecute(String)} method will be called with this
	 * string as the parameter.
	 * 
	 * You should place all of your long running tasks in the doInBackground
	 * method. But you cannot place here any code that modifies user interface
	 * elements (e.g. textview or textfield). All such codes must be placed in
	 * either onPreExecute or onPostExecute method. In our implementation, we
	 * set the resultView text in both onPreExecute and onPostExecute method. If
	 * we did this in the doInBackground method, the application would crash.
	 * 
	 * Among the three type parameters of AsyncTask (String, Void, String), the
	 * first one denotes that what will be the type of the parameters in
	 * doInBackground method (In this case, we pass the address we want to
	 * geocode, so the type is String). The second type parameter is unused
	 * here, so we set it to Void. The last parameter is the return type of the
	 * doInBackground method (which is also the parameter type of the
	 * onPostExecute method).
	 * 
	 */
	private class DataLoader extends AsyncTask<String, Void, String> {

		/**
		 * Before fetching the JSON string from the API, show a loading 
		 * message to the user.
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			resultView.setText("Loading data..");
		}
		
		/**
		 * Fetch the JSON response from the API
		 */
		@Override
		protected String doInBackground(String... params) {
			String address = params[0];
			String jsonString = readJSONString(address);
			return jsonString;
		}
		
		/**
		 * The jsonString returned from the doInBackground will be passed
		 * as the parameter of this method. Parse the jsonString and show
		 * the result to the user.
		 */
		@Override
		protected void onPostExecute(String jsonString) {
			super.onPostExecute(jsonString);
			
			if (jsonString == null) {
				resultView.setText("Error loading data");
			}
			else {
				try {
					JSONObject json = new JSONObject(jsonString);
					JSONObject locationObject = json.getJSONArray("results")
						.getJSONObject(0)
						.getJSONObject("geometry")
						.getJSONObject("location");
					
					String latitude = locationObject.getString("lat");
					String longitude = locationObject.getString("lng");
					
					resultView.setText("Latitude: " + latitude + ", Longitude: " + longitude);
				}
				catch (JSONException exception) {
					resultView.setText("Error loading data");
					exception.printStackTrace();
				}
			}
		}
	}
	
	private String readJSONString(String address) {
		try {
			// The URL of the API and our API key is stored in the values is stored 
			// in the values/string.xml file.
			//
			String urlString = getString(R.string.url) + "?address=" + 
					URLEncoder.encode(address, "utf-8") + 	// The address might contain spaces, so we need to encode it
					"&key=" + getString(R.string.api_key);
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			InputStream in = connection.getInputStream();
			
			// The IOUtils.toString method fetches all data from an input stream
			// and converts it into a string. The IOUtils class is a part of the
			// the Apache Commons IO library (you will find the corresponding jar file
			// of the library in libs folder).
			String response = IOUtils.toString(in);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
