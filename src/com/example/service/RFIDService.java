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
	// 操作员卡片密码
	private static final String OP_PASSWORD = "FFFFFFFFFFFF";
	// 区域卡密码
	@SuppressWarnings("unused")
	private static final String AREA_PASSWORD = "FFFFFFFFFFFF";
	private PSAM mSerialPort; // 用于调用本地方法
	private InputStream mInputStream; // 串口输入流
	private OutputStream mOutputStream; // 串口输出流
	private String data; // 接收数据
	private StringBuffer data_buffer;
	private Timer sendData; // 数据接收计时器
	private Timer searchCard; // 搜寻卡片计时器
	private String activity;
	private boolean run = true; // 线程结束标识
	private boolean skip = false;
	private int readOp = 0;
	private final int SEARCH_CARD = 1; // 寻卡标识
	private final int AUTH_CARD = 2; // 认证标识
	private final int READ_CARD = 3; // 读卡数据标识
	private MyReceiver myReceiver; // 广播接收者
	private ReadThread mReadThread; // 读数据线程
	public String TAG = "RFIDservice"; // Debug
    byte[] value_array;
    String opSector="04";           //人员卡经过处理后的验证所需的区值
    String areaSector="08";           //区域卡经过处理后的验证所需的区值
    String authPeopSector="01";    //读人员卡时的区值
    String authAreaSector="02";   //读区域卡时的区值
    String val="01";              //块值
    String readcardflag;
    String searchflag;                             
    String authflag;
    String readflag;
	private Handler mhandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			skip = true;
			Bundle receiverData = msg.getData();
			String rec = receiverData.getString("receiver");
			String crd=receiverData.getString("card");
			if (rec == null || "".equals(rec))
				return;
			Log.e(TAG + "  RECEIVER", rec);
			switch (readOp) {
			case SEARCH_CARD: // 得到寻卡结果,并进行认证
				// 寻卡结果
				if(crd.equals("01")){
				searchflag=searchCard(rec,opSector);
				}else if(crd.equals("02")){
				searchflag=searchCard(rec,areaSector);	
				}
				break;
			case AUTH_CARD: // 得到认证结果，并进行读数据
				// 认证结果
				if(crd.equals("01")){
				authflag=authCard(rec,authPeopSector,val);
				}else if(crd.equals("02")){
				authflag=authCard(rec,authAreaSector,val);
				}
				break;
			case READ_CARD:
				Log.e(TAG, rec);
				 byte temp[] = Tools.HexString2Bytes(rec);
				 byte[] receive_buffer = mSerialPort.resolveDataFromDevice(temp);
				/*Log.e("read_cmd receive", Tools.Bytes2HexString(receive_buffer, receive_buffer.length));*/
				Log.e("read_cmd arg", Tools.Bytes2HexString(temp, temp.length));
				System.out.println(receive_buffer + "待转化的接受数据");
				if (receive_buffer != null) {
					// 返回读到的数据给请求者
					Intent serviceIntent = new Intent();
					serviceIntent.setAction(activity);
					serviceIntent.putExtra("result", Tools.Bytes2HexString(
							receive_buffer, receive_buffer.length));
					Log.e("searchflag",searchflag+"");
					Log.e("authflag",authflag+"");
					serviceIntent.putExtra("searchflag", searchflag);
					serviceIntent.putExtra("authflag", authflag);
					sendBroadcast(serviceIntent);
					return;
				}
			default:
				break;
			}
			skip = false;
		}
		private String searchCard(String rec,String sector) {      //寻卡
		    String searchresult = "00";
			byte[] handlerCMD;
			byte[] cardID = mSerialPort.resolveDataFromDevice(Tools
					.HexString2Bytes(rec));
			if (cardID != null) {
				searchCard.cancel();
				Log.e(TAG, "send auth");
				// 认证信息为：密码类型 + 扇区号*4 + 密码
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
		private String authCard(String rec,String sector,String block ) {               //验证
			String authresult="00";
			byte[] handlerCMD;
			int auth_flag = mSerialPort.rf_check_data(Tools
					.HexString2Bytes(rec));
			Log.e("auth_result", rec);
			if (auth_flag == 0) {
				// 读数据
				String sector_str =sector.toString().trim();
				int sector_int = Integer.parseInt(sector_str);                //Sector
				int sector_int_temp = sector_int*4;  
				int value = sector_int_temp + Integer.parseInt(block);
				String value_str;
				if(value > 15){
					value_str = Integer.toHexString(value);
				}else{
					value_str = "0" + Integer.toHexString(value);
				}
				value_array = Tools.HexString2Bytes(value_str);  // 读、写数据块
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
				if (!skip) {
					int size;
					try {
						byte[] buffer = new byte[128];
						if (mInputStream == null)
							return;
						size = mInputStream.read(buffer);
						if (size > 0) {
							// 取消寻卡
							data = Tools.Bytes2HexString(buffer, size);
							data_buffer.append(data);
							// 设置数据超时为50ms，发送数据到activity
							sendData.schedule(new TimerTask() {
								@Override
								public void run() {
									boolean valid = true;
									if (data_buffer != null
											&& data_buffer.length() != 0
											&& activity != null) {
										// 数据发送给mhandler，交给handler处理
										data = null;
										int strlen = data_buffer.length();
										if (strlen >= 10) {
											String dataLen = data_buffer
													.substring(2, 6); // 取得数据包的长度
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
											data_buffer.setLength(0);
										}
									}
								}
							}, 50);
						}
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
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
		// 初始化
		init();
	}
	@SuppressWarnings("unused")
	private void init() {
		Log.e("service on create", "service on create");
		try {
			mSerialPort = new PSAM(14, 115200); // 打开串口，设备的端口号设置为3或者14，波特率为115200
			Log.e("mSerialPort", mSerialPort + "");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (mSerialPort == null) { // 没有打开串口
			return;
		}
		int powerflag = mSerialPort.PowerOn_HFPsam(); // 开启电源
		mOutputStream = mSerialPort.getOutputStream();
		mInputStream = mSerialPort.getInputStream();
		data_buffer = new StringBuffer();
		// 注册Broadcast Receiver，用于关闭Service
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
					mOutputStream.write(mSerialPort.rf_card()); // 寻卡

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 1000, 100);
		sendData = new Timer();
		/* Create a receiving thread */
		mReadThread = new ReadThread();
		mReadThread.start(); // 开启读线程
		Log.e(TAG, "start thread");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String cardType = intent.getStringExtra("cardType");
		if (intent.getStringExtra("activity") != null) {
			activity = intent.getStringExtra("activity");
		}
		Log.e("cardType", cardType);
		if (cardType.equals("0x01")) {
			readcardflag="01";
			startSearchCard();
		} else if (cardType.equals("0x02")) {
			readcardflag="02";
			startSearchCard();
		} else {
			Log.e("cardType", cardType + " is not right!0x01|0x02");
			// 返回读到的数据给请求者
			Intent serviceIntent = new Intent();
			serviceIntent.setAction(activity);
			serviceIntent.putExtra("code", "1");
			serviceIntent.putExtra("result", "卡类型错误");
			sendBroadcast(serviceIntent);
		}
		return 0;
	}
	@Override
	public void onDestroy() {
		if (mReadThread != null)
			run = false; // 关闭线程
		mSerialPort.PowerOff_HFPsam(); // 关闭电源
		mSerialPort.close(14); // 关闭串口
		unregisterReceiver(myReceiver); // 卸载注册
		super.onDestroy();
	}
	/**
	 * 广播接受者
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
			activity = ac; // 获取activity
			if (intent.getBooleanExtra("stopflag", false)) {
				searchCard.cancel();
				stopSelf(); // 收到停止服务信号
				Log.e("stop service", intent.getBooleanExtra("stopflag", false)
						+ "");
			}
		}

	}
}
