package com.maxieyetech.bt;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.maxieyetech.bt.utils.BTDeviceAdapter;
import com.maxieyetech.bt.utils.BTItem;
import com.maxieyetech.bt.utils.BTManager;
import com.maxieyetech.bt.utils.BTStatus;

public class BTTestMainActivity extends Activity implements BTStatus,AdapterView.OnItemClickListener{

	private Button mBTEnable;
	private Button mBTDisable;
	//private Button mBTDiscovery;
	//private Button mBTCancelDiscovery;
	private Button mBTSearchDevices;
	private BTDeviceAdapter mAdapter;
	private ListView mDeviceList;
	private BTManager mManager = null;
	private boolean mIsRegister = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bttest_main);

		mBTEnable = (Button)findViewById(R.id.btn_enable);
		mBTDisable = (Button)findViewById(R.id.btn_disable);
		//mBTDiscovery = (Button)findViewById(R.id.btn_start_discovery);
		//mBTCancelDiscovery = (Button)findViewById(R.id.btn_stop_discovery);
		mBTSearchDevices = (Button) findViewById(R.id.btn_search_devices);

		mBTEnable.setOnClickListener(listener);
		mBTDisable.setOnClickListener(listener);
		//mBTDiscovery.setOnClickListener(listener);
		//mBTCancelDiscovery.setOnClickListener(listener);
		mBTSearchDevices.setOnClickListener(listener);

		mAdapter = new BTDeviceAdapter(this);
		mDeviceList = (ListView) findViewById(R.id.lv_device_list);
		mDeviceList.setOnItemClickListener(this);
		mDeviceList.setAdapter(mAdapter);

		mManager = BTManager.getInstance();
		mManager.setBTStatusListner(this);
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
				/*case R.id.btn_start_discovery:
					mManager.startDiscoveringDevice();
					break;
				case R.id.btn_stop_discovery:
					mManager.stopDiscoveringDevice();
					break;*/
				case R.id.btn_search_devices:
					mManager.startDiscoveringDevice();
					break;
				default:

					break;
			}
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		if (!mIsRegister){
			mIsRegister = true;
			BTManager.getInstance().registerBluetoothReceiver(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mIsRegister) {
			BTManager.getInstance().unregisterBluetooth(this);
		}
	}

	@Override
	public void BTDeviceSearchStatus(int resultCode) {
		switch (resultCode){
			case BTStatus.SEARCH_START:
				mAdapter.clearData();
				mAdapter.addDataModel(BTManager.getInstance().getPairBluetoothItem());
				break;
			case BTStatus.SEARCH_END:
				break;
		}
	}

	@Override
	public void BTSearchFindItem(BTItem item) {
		mAdapter.addDataModel(item);
	}

	@Override
	public void BTConnectStatus(int result) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}
}
