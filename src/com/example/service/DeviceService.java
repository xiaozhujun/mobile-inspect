package com.example.service;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import com.example.psam_demo.PSAM;
import com.example.tools.Tools;
/**
 * �豸���������ں�̨���У���������ͽ�������
 * �ڷ����ʼ��ʱ��ִ�д��ڴ򿪣��豸��Դ������
 * ��ȡ�������������
 * �����������߳�
 * @author Administrator
 *
 */
public class DeviceService extends Service {
	protected PSAM mSerialPort;		//���ڵ��ñ��ط���
	protected OutputStream mOutputStream;  //���������
	private InputStream mInputStream;      //����������
	private ReadThread mReadThread;		   //���߳�
	private Boolean run = true; // �߳��ж��ź�
	private String data = null; // ���ص�����
	private StringBuffer data_buffer = new StringBuffer();
//	private Timer sendData;
	private MyReceiver myReceive;  	//�㲥������
	//��Ƶ���ؼ�ʱ��
	@SuppressWarnings("unused")
	private Timer stopRF;
	public String activity = null; // �ش����ݵ�activity
	/**
	 *  ���߳� ,��ȡ�豸���ص���Ϣ������ش������������activity
	 * @author Jimmy Pang
	 *
	 */
	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (run) {
				int size;
				try {
					byte[] buffer = new byte[128];
					if (mInputStream == null)
						return;
					size = mInputStream.read(buffer);
					Log.e("size", size+"---------------");
					if (size > 0) {                  
						data = Tools.Bytes2HexString(buffer, size);
						Log.e("data",data);
						data_buffer.append(data);
						data = null;
						int strlen=data_buffer.length();
						if(strlen<10){
						if(data_buffer!=null){
						Log.e("DeviceService data", data_buffer.toString());
						Intent serviceIntent = new Intent();
						serviceIntent.setAction(activity);
						serviceIntent.putExtra("result", data_buffer.toString());
						data_buffer.setLength(0);
						sendBroadcast(serviceIntent);
						} 
						}else{
						String dataLen = data_buffer.substring(2, 6); // ȡ�����ݰ��ĳ���
						Log.e("datalength", dataLen+"**" +data_buffer.toString());
						if(Tools.checkData(dataLen, data_buffer.toString())){
						if(data_buffer!=null){
							Log.e("DeviceService data", data_buffer.toString());
							Intent serviceIntent = new Intent();
							serviceIntent.setAction(activity);
							serviceIntent.putExtra("result", data_buffer.toString());
							data_buffer.setLength(0);
							sendBroadcast(serviceIntent);
						} 
					}
					}
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}	
	}	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		init();
	}
	private void init(){
		Log.e("service on create", "service on create");
		try {
			mSerialPort = new PSAM(14, 115200); // �򿪴��ڣ��豸�Ķ˿ں�����Ϊ14��������Ϊ115200
			Log.e("mSerialPort", mSerialPort + "");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(mSerialPort == null){  //û�ҵ��豸
//			Toast.makeText(getApplicationContext(), "�޷��ҵ��豸", Toast.LENGTH_SHORT).show();			
			return;
		}
		mSerialPort.PowerOn_HFPsam(); // ������Դ
		mOutputStream = mSerialPort.getOutputStream();
		mInputStream = mSerialPort.getInputStream();
		myReceive = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.service.DeviceService");
		registerReceiver(myReceive, filter);
		// ע��Broadcast Receiver�����ڹر�Service
//		sendData = new Timer();
		/* Create a receiving thread */
		mReadThread = new ReadThread();
		mReadThread.start(); // �������߳�
		Log.e("DeviceService", "start thread");			
	}	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		byte[] cmd_arr = intent.getByteArrayExtra("cmd");
		Log.e("cmd_arr",cmd_arr+"");
		if (cmd_arr == null)
			return 0; // û�յ�����ֱ�ӷ���
		Log.e("CMD", Tools.Bytes2HexString(cmd_arr, cmd_arr.length));		
		try {
			mOutputStream.write(cmd_arr); // ��������
			 Log.e("WRITE", "SUCCESS");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	@Override
	public void onDestroy() {
		if (mReadThread != null)
			run = false; 					// �ر��߳�
		mSerialPort.PowerOff_HFPsam(); 		// �رյ�Դ
		mSerialPort.close(14); 				// �رմ���
		unregisterReceiver(myReceive); 		// ж��ע��
		super.onDestroy();
	}
	/**
	 *  �㲥������
	 * @author Jimmy Pang
	 *
	 */
	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String ac = intent.getStringExtra("activity");
			if(ac!=null) 
				Log.e("receive activity", ac);
			activity = ac; // ��ȡactivity
			if (intent.getBooleanExtra("stopflag", false))
				stopSelf(); // �յ�ֹͣ�����ź�
			Log.e("stop service", intent.getBooleanExtra("stopflag", false)
					+ "");
		}
	}
}
