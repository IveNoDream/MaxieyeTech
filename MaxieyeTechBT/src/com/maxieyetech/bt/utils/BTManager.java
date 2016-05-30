package com.maxieyetech.bt.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BTManager {

	private BluetoothAdapter mBTAdapter = null;
	private static BTManager mBTManager = null;
	private BTStatus mBTStatus = null;
	public BTManager(){
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	public static BTManager getInstance(){
		if (null == mBTManager) {
			mBTManager = new BTManager();
		}
		return mBTManager;
	}
	
	public BluetoothAdapter getAdapter(){
		return this.mBTAdapter;
	}

	public void setBTStatusListner(BTStatus status){
		this.mBTStatus = status;
	}
	
	public void openBT(Activity activity){
		//Device does not support BlueTooth
		if (null == mBTAdapter) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
			dialog.setTitle("No bluetooth devices");
			dialog.setMessage("Your equipment does not support bluetooth, please change device");
			dialog.setNegativeButton("OK", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				}
			});
			dialog.show();
			return;
		}
		
		if (!mBTAdapter.isEnabled()) {
			mBTAdapter.enable();
		}
	}
	
	public void closeBT(){
		if (mBTAdapter.isEnabled()) {
			mBTAdapter.disable();
		}
	}

	public void startDiscoveringDevice(){
		if(!mBTAdapter.isDiscovering())
			mBTAdapter.startDiscovery();
	}

	public void stopDiscoveringDevice(){
		if(mBTAdapter.isDiscovering())
			mBTAdapter.cancelDiscovery();
	}

	public boolean isDiscovering(){
		return mBTAdapter.isDiscovering();
	}

	public void registerBluetoothReceiver(Context mcontext){
		// Register for broadcasts when start bluetooth search
		IntentFilter startSearchFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		mcontext.registerReceiver(mBlueToothReceiver, startSearchFilter);
		// Register for broadcasts when a device is discovered
		IntentFilter discoveryFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		mcontext.registerReceiver(mBlueToothReceiver, discoveryFilter);
		// Register for broadcasts when discovery has finished
		IntentFilter foundFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mcontext.registerReceiver(mBlueToothReceiver, foundFilter);
	}

	public void unregisterBluetooth(Context mcontext){
		stopDiscoveringDevice();
		mcontext.unregisterReceiver(mBlueToothReceiver);
	}

	public List<BTItem> getPairBluetoothItem(){
		List<BTItem> mBTitemList=null;
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
		Iterator<BluetoothDevice> it=pairedDevices.iterator();
		while(it.hasNext()){
			if(mBTitemList==null)
				mBTitemList=new ArrayList<BTItem>();

			BluetoothDevice device=it.next();
			BTItem item=new BTItem();
			item.setBTName(device.getName());
			item.setBTAddress(device.getAddress());
			item.setBTType(BluetoothDevice.BOND_BONDED);
			mBTitemList.add(item);
		}
		return mBTitemList;
	}


	private final BroadcastReceiver mBlueToothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				if(mBTStatus!=null)
					mBTStatus.BTDeviceSearchStatus(BTStatus.SEARCH_START);
			}
			else if (BluetoothDevice.ACTION_FOUND.equals(action)){
				// When discovery finds a device
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					BTItem item=new BTItem();
					item.setBTName(device.getName());
					item.setBTAddress(device.getAddress());
					item.setBTType(device.getBondState());

					if(mBTStatus!=null)
						mBTStatus.BTSearchFindItem(item);
					//                mListDeviceBT.add(item);
				}
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				// When discovery is finished, change the Activity title
				if(mBTStatus!=null)
					mBTStatus.BTDeviceSearchStatus(BTStatus.SEARCH_END);
			}
		}
	};
}
