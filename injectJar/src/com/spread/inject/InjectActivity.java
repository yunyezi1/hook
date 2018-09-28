package com.spread.inject;

import com.spread.injectjar.R;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class InjectActivity extends Activity {

	private static final String TAG ="Inject";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				GlobalPatch.main();
			}
		});
		Log.d(TAG, "Button ID Name:" + btn.getId() + ";" + R.id.button1 + ";"
				+this.getWindow().getDecorView().getId()+";"
				+R.layout.activity_main+";");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
