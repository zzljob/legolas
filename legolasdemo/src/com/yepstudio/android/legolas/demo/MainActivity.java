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
import com.yepstudio.android.legolas.http.Response.OnErrorListener;

public class MainActivity extends Activity implements Response.OnResponseListener<JSONArray> {
	private HttpApi api, api2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		OnErrorListener errorListener = new OnErrorListener() {

			@Override
			public void onError(LegolasError paramVolleyError) {
				Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_LONG)
						.show();

			}

		};
		Response.OnResponseListener<JSONArray> listener = new Response.OnResponseListener<JSONArray>() {

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

		api = legolas.newInstance(this, HttpApi.class);
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
	public void onResponse(JSONArray arg0) {
		// TODO Auto-generated method stub
		
	}

}
