package com.maxieyetech.bt;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.maxieyetech.bt.utils.BTManager;

public class BTTestMainActivity extends Activity {

	private Button mBTEnable;
	private Button mBTDisable;
	private Button mBTDiscovery;
	private Button mBTCancelDiscovery;
	private BTManager mManager = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bttest_main);

		mBTEnable = (Button)findViewById(R.id.btn_enable);
		mBTDisable = (Button)findViewById(R.id.btn_disable);
		mBTDiscovery = (Button)findViewById(R.id.btn_start_discovery);
		mBTCancelDiscovery = (Button)findViewById(R.id.btn_stop_discovery);

		mBTEnable.setOnClickListener(listener);
		mBTDisable.setOnClickListener(listener);
		mBTDiscovery.setOnClickListener(listener);
		mBTCancelDiscovery.setOnClickListener(listener);

		mManager = BTManager.getInstance();
	}

	public View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()){
				case R.id.btn_enable:
					mManager.openBT(BTTestMainActivity.this);
					break;
				case R.id.btn_disable:
					mManager.closeBT();
					break;
				case R.id.btn_start_discovery:
					mManager.startDiscoveringDevice();
					break;
				case R.id.btn_stop_discovery:
					mManager.stopDiscoveringDevice();
					break;
				default:

					break;
			}
		}
	};
}
