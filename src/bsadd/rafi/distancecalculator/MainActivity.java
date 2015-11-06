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
		
		addressField = (EditText) findViewById(R.id.et_address);
		calculateButton = (Button) findViewById(R.id.bt_calculate);
		resultView = (TextView) findViewById(R.id.tv_result);
		
		calculateButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				calculateResult();
			}
		});
	}
	
	private void calculateResult() {
		String address = addressField.getText().toString();
		DataLoader dataLoader = new DataLoader();
		dataLoader.execute(address);
	}
	
	private String readJSONString(String address) {
		try {
			String urlString = getString(R.string.url) + 
					"?address=" + URLEncoder.encode(address, "utf-8") + "&key=" + getString(R.string.api_key);
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			InputStream in = connection.getInputStream();
			String response = IOUtils.toString(in);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private class DataLoader extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			resultView.setText("Loading data..");
		}
		
		@Override
		protected String doInBackground(String... params) {
			String address = params[0];
			String jsonString = readJSONString(address);
			return jsonString;
		}
		
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
					exception.printStackTrace();
				}
			}
		}
	};
}
