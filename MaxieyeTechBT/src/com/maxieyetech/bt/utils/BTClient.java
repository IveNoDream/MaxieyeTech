package com.maxieyetech.bt.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.UUID;

public class BTClient {  
  
    final String Tag=getClass().getSimpleName();  
    private BluetoothSocket btsocket = null;  
    private BluetoothDevice btdevice = null;  
    private BufferedInputStream bis=null;  
    private BufferedOutputStream bos=null;  
    private BluetoothAdapter mBtAdapter =null;  
      
    private Handler detectedHandler=null;  
      
    public BTClient(BluetoothAdapter mBtAdapter, Handler detectedHandler){
        this.mBtAdapter=mBtAdapter;  
        this.detectedHandler=detectedHandler;  
    }
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Log.i("mijie","e: " + e.toString());
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBtAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.i("mijie","connectException: " + connectException.toString());
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.i("mijie","closeException: " + closeException.toString());
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    public void connectBTServer(String address){
        //new ConnectThread(mBtAdapter.getRemoteDevice(address)).start();
        //check address is correct
        if(BluetoothAdapter.checkBluetoothAddress(address)){
            btdevice = mBtAdapter.getRemoteDevice(address);
                ThreadPool.getInstance().excuteTask(new Runnable() {
                    public void run() {  
                        try {
                            //btsocket = btdevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                            btsocket = BTManager
                                    .getInstance()
                                    .getAdapter()
                                    .getRemoteDevice(BTMsg.BlueToothAddress)
                                    .createRfcommSocketToServiceRecord(
                                            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

                            handleMessages(0,"请稍候，正在连接服务器:"+BTMsg.BlueToothAddress);
                            //new Thread(new Runnable() {
                            //    @Override
                            //    public void run() {
                                    if (btsocket != null) {
                                        try {
                                            mBtAdapter.cancelDiscovery();
                                            btsocket.connect();
                                            handleMessages(0,"connect success");
                                        } catch (IOException e) {
                                            handleMessages(0,"Connect Exception: " + e.toString());
                                            Log.i("mijie","socket IOException: " + e.toString());
                                            try {
                                                Log.e("mijie","trying fallback...");

                                                btsocket =(BluetoothSocket) btdevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(btdevice,1);
                                                btsocket.connect();

                                                handleMessages(0,"Connect Success Trying Fallback");
                                                Log.e("mijie","Connected");
                                            }
                                            catch (Exception e2) {
                                                handleMessages(0,"Exception 2: " + e2.toString());
                                                Log.e("mijie", "Couldn't establish Bluetooth connection!");
                                            }
                                        }
                                    }else
                                        Log.i("mijie","socket is null");
                            //    }
                            //}).start();
                            if (btsocket.isConnected()){
                                Log.i("mijie","isConnected,start receive");
                                receiverMessageTask();
                            }else
                                Log.i("mijie","socket is not connect");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("mijie","IOException: " + e.toString());
                            Log.e(Tag, e.getMessage());  
                            handleMessages(0,"connect server exception! please check server,and reconnect");
                        }  
                          
                    }  
                });
            }
    }  
    private void handleMessages(int what, String content){
        Message msg = Message.obtain();
        msg.what = what;
        Bundle data = new Bundle();
        data.putString("msg",content);
        msg.setData(data);
        detectedHandler.sendMessage(msg);
    }
    private void receiverMessageTask(){  
        ThreadPool.getInstance().excuteTask(new Runnable() {  
            public void run() {  
                byte[] buffer = new byte[16];
                int totalRead;  
                /*InputStream input = null; 
                OutputStream output=null;*/  
                try {  
                    bis=new BufferedInputStream(btsocket.getInputStream());  
                    bos=new BufferedOutputStream(btsocket.getOutputStream());  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
                  
                try {  
                //  ByteArrayOutputStream arrayOutput=null;
                    Log.i("mijie","receiverMessageTask");
                    while((totalRead = bis.read(buffer)) > 0 ){  
                //       arrayOutput=new ByteArrayOutputStream();
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < buffer.length; i++){
                            sb.append(Integer.toHexString(buffer[i] & 0xFF));
                            sb.append(" ");
                        }
                        String txt = sb.toString();
                        //String txt = new String(buffer, 0, totalRead, "UTF-8");
                        handleMessages(1,txt);
                    }  
                } catch(EOFException e){
                    handleMessages(1,"server has close!");
                    e.printStackTrace();
                }catch (IOException e) {
                    handleMessages(1,"receiver message error! make sure server is ok,and try again connect!");
                    e.printStackTrace();
                }  
            }  
        });  
    }  
      
    public boolean sendmsg(String msg){  
        boolean result=false;  
        if(null==btsocket||bos==null)  
            return false;  
        try {  
            bos.write(msg.getBytes());  
            bos.flush();  
            result=true;  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return result;  
    }  
      
    public void closeBTClient(){  
        try{
            if(bis!=null)  
                bis.close();  
            if(bos!=null)  
                bos.close();  
            if(btsocket!=null)  
                btsocket.close();  
        }catch(IOException e){  
            e.printStackTrace();  
        }  
    }  
      
}  