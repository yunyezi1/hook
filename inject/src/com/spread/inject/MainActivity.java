package com.spread.inject;

import com.spread.inject.core.DoInject;
import com.spread.inject.core.DoInject.InjectEvent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				Toast.makeText(MainActivity.this, "注入成功!", Toast.LENGTH_LONG).show();
				break;
			case 2:
				Toast.makeText(MainActivity.this, "注入失败，可能是进程没启动!", Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button btn = (Button) findViewById(R.id.button1);
		final EditText editText = (EditText) findViewById(R.id.editText1);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DoInject.tryStart(MainActivity.this,editText.getText().toString(),
						new InjectEvent() {
							
							@Override
							public void onInjectSucc() {
								// TODO Auto-generated method stub
								mHandler.sendEmptyMessage(1);
							}
							
							@Override
							public void onInjectError() {
								// TODO Auto-generated method stub
								mHandler.sendEmptyMessage(2);
							}
						});
			}
		});
		editText.setText("com.baidu.searchbox");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
