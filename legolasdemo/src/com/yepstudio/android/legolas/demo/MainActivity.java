package com.yepstudio.android.legolas.demo;

import org.json.JSONArray;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.yepstudio.android.legolas.Legolas;
import com.yepstudio.android.legolas.error.LegolasError;
import com.yepstudio.android.legolas.http.Response;

public class MainActivity extends Activity implements Response.Listener<JSONArray> {
	private HttpApi api, api2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Response.ErrorListener errorListener = new Response.ErrorListener() {

			@Override
			public void onErrorResponse(LegolasError paramVolleyError) {
				Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_LONG)
						.show();

			}

		};
		Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {

			@Override
			public void onResponse(JSONArray paramT) {
//				Log.e("aaaaa", paramT);
				Toast.makeText(MainActivity.this, "网络请求完成", Toast.LENGTH_LONG)
						.show();
			}

		};
		String[] srtArr = new String[] { "1" };
		MainActivity[] legolasArr = new MainActivity[9];
		Log.e("aaaaaa", srtArr.getClass().getName());

		Legolas legolas = new Legolas.Build().create();

		api = legolas.getInstance(this, HttpApi.class);
		api.getUserInfo("list", 1, 2, new AdddDTO(), listener, errorListener);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResponse(JSONArray response) {
		// TODO Auto-generated method stub
		
	}

}
