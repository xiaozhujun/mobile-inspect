package com.example.service;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import com.example.psam_demo.PSAM;
import com.example.tools.Tools;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
@SuppressLint("HandlerLeak")
public class RFIDService extends Service {
	// ����Ա��Ƭ����
	private static final String OP_PASSWORD = "FFFFFFFFFFFF";
	// ��������
	@SuppressWarnings("unused")
	private static final String AREA_PASSWORD = "FFFFFFFFFFFF";
	private PSAM mSerialPort; // ���ڵ��ñ��ط���
	private InputStream mInputStream; // ����������
	private OutputStream mOutputStream; // ���������
	private String data; // ��������
	private StringBuffer data_buffer;
	private Timer sendData; // ���ݽ��ռ�ʱ��
	private Timer searchCard; // ��Ѱ��Ƭ��ʱ��
	private String activity;
	private boolean run = true; // �߳̽�����ʶ
	@SuppressWarnings("unused")
	private boolean skip = false;
	private int readOp = 0;
	private final int SEARCH_CARD = 1; // Ѱ����ʶ
	private final int AUTH_CARD = 2; // ��֤��ʶ
	private final int READ_CARD = 3; // �������ݱ�ʶ
	private MyReceiver myReceiver; // �㲥������
	private ReadThread mReadThread; // �������߳�
	public String TAG = "RFIDservice"; // Debug
    byte[] value_array;
    String opSector="04";           //��Ա��������������֤�������ֵ
    String areaSector="08";           //���򿨾�����������֤�������ֵ
    String authPeopSector="01";    //����Ա��ʱ����ֵ
    String authAreaSector="02";   //������ʱ����ֵ
    String val="01";              //��ֵ
    String val1="02";
    String readcardflag;
    String searchflag;                             
    String authflag;
    String readflag;
    String cardType;
    String result=null;
    String result1=null;
    int readcount=2;
    String[] resultArray=new String[2];
    String re="";
	private Handler mhandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			skip = true;
			Bundle receiverData = msg.getData();
			String rec = receiverData.getString("receiver");
			Log.e("rec",rec);
			String crd=receiverData.getString("card");
			Log.e("crd",crd);
			if (rec == null || "".equals(rec))
				return;
			Log.e(TAG + "  RECEIVER", rec);
			handleCard(rec, crd,opSector,areaSector,authPeopSector,authAreaSector,val);
			//startSearch(cardType);
			skip = false;
		}
		private void handleCard(String rec, String crd,String opSector,String areaSector,String authPeopSector,String authAreaSector,String value) {
			
			switch (readOp) {
			case SEARCH_CARD: // �õ�Ѱ�����,��������֤
				// Ѱ�����
				if(crd.equals("01")){
				searchCard(rec,opSector);
				}else if(crd.equals("02")){
				searchCard(rec,areaSector);	
				}
				break;
			case AUTH_CARD: // �õ���֤����������ж�����
				// ��֤���
				if(crd.equals("01")){
				authCard(rec,authPeopSector,value);
				}else if(crd.equals("02")){
				authCard(rec,authAreaSector,value);
				}
				break;
			case READ_CARD:
				Log.e(TAG, rec);
				 byte temp[] = Tools.HexString2Bytes(rec);
				 byte[] receive_buffer = mSerialPort.resolveDataFromDevice(temp);
				/*Log.e("read_cmd receive", Tools.Bytes2HexString(receive_buffer, receive_buffer.length));*/
				Log.e("read_cmd arg", Tools.Bytes2HexString(temp, temp.length));
				System.out.println(receive_buffer + "��ת���Ľ�������");
				if (receive_buffer != null) {
					// ���ض��������ݸ�������
					re+=Tools.Bytes2HexString(receive_buffer, receive_buffer.length);
					readcount--;
					if(readcount!=0&&readcount>0){
				    Log.e("readcount",readcount+"");
					Log.e("hahaha","����");
					startSearch(cardType,2);	
					}	
				}else{
					re=null;
					readcount=0;
				}
				if(readcount==0){
				Intent serviceIntent = new Intent();
				serviceIntent.setAction(activity);
				serviceIntent.putExtra("result",re);
				sendBroadcast(serviceIntent);
				}
			default:
				break;
			}
			
		}
		private String searchCard(String rec,String sector) {      //Ѱ��
		    String searchresult = "00";
			byte[] handlerCMD;
			byte[] cardID = mSerialPort.resolveDataFromDevice(Tools
					.HexString2Bytes(rec));
			if (cardID != null) {
				searchCard.cancel();
				Log.e(TAG, "send auth");
				// ��֤��ϢΪ���������� + ������*4 + ����
				byte[] auth_byte = Tools.HexString2Bytes("00" + sector
						+ OP_PASSWORD);
				handlerCMD = mSerialPort.rf_authentication_cmd(auth_byte);
				Log.e(TAG+"sss", Tools.Bytes2HexString(handlerCMD,
						handlerCMD.length));
				readOp = AUTH_CARD;
				try {
					mOutputStream.write(handlerCMD);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				searchresult="01";
			}
			return searchresult;
		}
		private String authCard(String rec,String sector,String block ) {               //��֤
			String authresult="00";
			byte[] handlerCMD;
			int auth_flag = mSerialPort.rf_check_data(Tools
					.HexString2Bytes(rec));
			Log.e("auth_result", rec);
			if (auth_flag == 0) {
				// ������
				String sector_str =sector.toString().trim();
				int sector_int = Integer.parseInt(sector_str);                //Sector
				int sector_int_temp = sector_int*4; 
				Log.e("block",block);
				int value = sector_int_temp + Integer.parseInt(block);
				Log.e("value",value+"");
				String value_str;
				if(value > 15){
					value_str = Integer.toHexString(value);
				}else{
					value_str = "0" + Integer.toHexString(value);
				}
				Log.e("value_str",value_str);
				value_array = Tools.HexString2Bytes(value_str);  // ����д���ݿ�
			    Log.e("value_array",Tools.Bytes2HexString(value_array, value_array.length));
				handlerCMD = mSerialPort.rf_read_cmd(value_array);
				Log.e("read_cmd", Tools.Bytes2HexString(handlerCMD, handlerCMD.length));
				readOp = READ_CARD;
				try {
					mOutputStream.write(handlerCMD);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				authresult="01";
			}
			return authresult;
		}
	};
	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (run) {
				/*if (!skip) {*/
					int size;
					try {
						byte[] buffer = new byte[128];
						if (mInputStream == null)
							return;
						size = mInputStream.read(buffer);
						if (size > 0) {
							// ȡ��Ѱ��
							data = Tools.Bytes2HexString(buffer, size);
							data_buffer.append(data);
							// �������ݳ�ʱΪ50ms���������ݵ�activity
							sendData.schedule(new TimerTask() {
								@Override
								public void run() {
									boolean valid = true;
									if (data_buffer != null
											&& data_buffer.length() != 0
											&& activity != null) {
										// ���ݷ��͸�mhandler������handler����
										data = null;
										int strlen = data_buffer.length();
										if (strlen >= 10) {
											String dataLen = data_buffer
													.substring(2, 6); // ȡ�����ݰ��ĳ���
											valid = Tools.checkData(dataLen,
													data_buffer.toString());
											Log.e("datalength", dataLen + "**"
													+ data_buffer.toString()+" valid:"+valid);
										}				
										Message msg = new Message();
										Bundle bundle = new Bundle();
										bundle.putString("receiver",
												data_buffer.toString());
										if(readcardflag.equals("01")){
										bundle.putString("card", "01");
									    }else if(readcardflag.equals("02")){
									    bundle.putString("card", "02");										
									    }
										msg.setData(bundle);
										data_buffer.setLength(0);
										msg.setData(bundle);
										mhandler.sendMessage(msg);										
										if(valid){	
											
										}else{
											//validΪfalseʱֹͣ�߳�
											data_buffer.setLength(0);
										}
									}
								}
							}, 5);
						}
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
				/*}*/
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
		// ��ʼ��
		init();
	}
	@SuppressWarnings("unused")
	private void init() {
		Log.e("service on create", "service on create");
		try {
			mSerialPort = new PSAM(14, 115200); // �򿪴��ڣ��豸�Ķ˿ں�����Ϊ3����14��������Ϊ115200
			Log.e("mSerialPort", mSerialPort + "");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (mSerialPort == null) { // û�д򿪴���
			return;
		}
		int powerflag = mSerialPort.PowerOn_HFPsam(); // ������Դ
		mOutputStream = mSerialPort.getOutputStream();
		mInputStream = mSerialPort.getInputStream();
		data_buffer = new StringBuffer();
		// ע��Broadcast Receiver�����ڹر�Service
		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.service.RFIDService");
		registerReceiver(myReceiver, filter);
	}
	private void startSearchCard() {
		readOp = SEARCH_CARD;
		searchCard = new Timer();
		searchCard.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					mOutputStream.write(mSerialPort.rf_card()); // Ѱ��

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 1000, 100);
		sendData = new Timer();
		/* Create a receiving thread */
		mReadThread = new ReadThread();
		mReadThread.start(); // �������߳�
		Log.e(TAG, "start thread");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    cardType = intent.getStringExtra("cardType");
		if (intent.getStringExtra("activity") != null) {
			activity = intent.getStringExtra("activity");
		}
		Log.e("cardType", cardType);
		re="";
		readcount=2;
		startSearch(cardType,1);
		return 0;
	}
	private void startSearch(String cardType,int count) {
		if(count==1){
			val="01";
		}else if(count==2){
			val="02";
		}
		if (cardType.equals("0x01")) {
			startSearchCard();
			readcardflag="01";
		} else if (cardType.equals("0x02")) {
			startSearchCard();
			readcardflag="02";
		} else {
			Log.e("cardType", cardType + " is not right!0x01|0x02");
			// ���ض��������ݸ�������
			Intent serviceIntent = new Intent();
			serviceIntent.setAction(activity);
			serviceIntent.putExtra("code", "1");
			serviceIntent.putExtra("result", "�����ʹ���");
			sendBroadcast(serviceIntent);
		}
	}
	@Override
	public void onDestroy() {
		if (mReadThread != null)
			run = false; // �ر��߳�
		mSerialPort.PowerOff_HFPsam(); // �رյ�Դ
		mSerialPort.close(14); // �رմ���
		unregisterReceiver(myReceiver); // ж��ע��
		super.onDestroy();
	}
	/**
	 * �㲥������
	 * 
	 * @author Jimmy Pang
	 * 
	 */
	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String ac = intent.getStringExtra("activity");
			if (ac != null)
				Log.e("receive activity", ac);
			activity = ac; // ��ȡactivity
			if (intent.getBooleanExtra("stopflag", false)) {
				searchCard.cancel();
				stopSelf(); // �յ�ֹͣ�����ź�
				Log.e("stop service", intent.getBooleanExtra("stopflag", false)
						+ "");
			}
		}

	}
}
