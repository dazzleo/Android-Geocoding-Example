package bsadd.rafi.distancecalculator;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
import android.widget.Toast;

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
			URL url = new URL(getString(R.string.url) + 
					"?address=" + address + "&key=" + getString(R.string.api_key));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			InputStream in = connection.getInputStream();
			String response = IOUtils.toString(in);
			return response;
		} catch (Exception e) {
			Toast.makeText(this, "Error fetching data", Toast.LENGTH_LONG).show();
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
	};
}
