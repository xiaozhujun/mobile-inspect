package com.csei.inspect;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import com.cesi.analysexml.DbModel;
import com.cesi.analysexml.ParseXml;
import com.csei.adapter.MyAdapter;
import com.csei.util.Tools;
import com.example.psam_demo.PSAM;
import com.example.service.DeviceService;
import com.example.viewpager.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
@SuppressLint("HandlerLeak")
public class PeopleValidateActivity extends Activity implements OnClickListener{
	Button read;// 按钮
	String str = "", result = "";
	ListView rolestablelist;
	int index = 0;// 字符索引
	int count = 0;// 按钮计数器
	MyRunnable myRunnable;
	Handler handler;
	Thread thread;
	int tag = 0;// 标签
	ProgressBar pb;
	TextView textview;
	String fileDir;
	String filename;
	TextView showalert;
	int cur_pos=0;
	MyAdapter myadapter;
	Button searchbtn;
	Button authbtn;
	Button startScan;
	public String part_value = "00";				//块值，默认为0
	public String password_type = "00";				//默认密码A为00 
	//SerialPort API
	private PSAM mpsam;								//用于调用命令
	private MyBroadcast myBroadcast;				//广播接收者
	public static int cmd_flag = 0;				//操作状态  0为不做其他操作，1为寻卡，2为认证，3为读数据，4为写数据
	public static int authentication_flag = 0;		//认证状态  0为认证失败和未认证  1为认证成功
	private String activity = "com.csei.inspect.PeopleValidateActivity";
	//Debug
	public static String TAG= "M1card";
	volatile boolean Thread_flag = false;
	String username=null;
	int uid;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.peoplevalidate);
	  fileDir=Environment.getExternalStorageDirectory().toString();
	 /* read = (Button) findViewById(R.id.read);*/
	  rolestablelist = (ListView) findViewById(R.id.rolestablelist);
	  pb = (ProgressBar) findViewById(R.id.pb);
	  textview = (TextView) findViewById(R.id.textview);
	  showalert=(TextView) findViewById(R.id.showalert);
	  /*getFile();*/
	  // 调用得到文件的方法
	  searchbtn=(Button) this.findViewById(R.id.searchcard);
	  authbtn=(Button) this.findViewById(R.id.auth);
	  startScan=(Button) this.findViewById(R.id.startScan);
	  searchbtn.setOnClickListener(this);
	  authbtn.setOnClickListener(this);
	  startScan.setOnClickListener(this);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent ac = new Intent();
		ac.setAction("com.example.service.DeviceService");
		ac.putExtra("activity", activity);
		sendBroadcast(ac);  
		Log.e(TAG, "send broadcast");	
		byte []cmd = null; //用于存放指令
		Intent sendToservice = new Intent(PeopleValidateActivity.this,DeviceService.class);  //用于发送指令
		String sector_str = "01".toString().trim();   //Sector
		String sector_hex;  //协议所要求的扇区
		String value_str;
		//获取扇区
		int sector_int = Integer.parseInt(sector_str);                //Sector
		int sector_int_temp = sector_int*4;                              
		if( sector_int_temp > 15){     //协议传入的扇区是:扇区号*4的十六进制
			sector_hex  = Integer.toHexString(sector_int_temp);
		}else{
			 sector_hex  ="0" +  Integer.toHexString(sector_int_temp);
		}
		//认证命令所需的数据
		Log.e("auth1",password_type+ sector_hex+ "FFFFFFFFFFFF");
		byte[] auth_byte = Tools.HexString2Bytes(password_type+ sector_hex+ "FFFFFFFFFFFF");
		
		//获取块值
		int value = sector_int_temp + Integer.parseInt("01");
		if(value > 15){
			value_str = Integer.toHexString(value);
		}else{
			value_str = "0" + Integer.toHexString(value);
		}
		byte [] value_array = Tools.HexString2Bytes(value_str);  // 读、写数据块
		switch (v.getId()){
		case R.id.searchcard:
		//先寻卡
	    new AlertDialog.Builder(PeopleValidateActivity.this).setMessage("hah1").show();
		cmd_flag = 1;  	   //寻卡标识为1
		cmd = mpsam.rf_card(); //获取命令
		String cmd_find_card = Tools.Bytes2HexString(cmd, cmd.length);
		Log.e(TAG, cmd_find_card);
		break;
		case R.id.auth:
        //先验证
		new AlertDialog.Builder(PeopleValidateActivity.this).setMessage("hah2").show();
		cmd_flag = 2;
		cmd = mpsam.rf_authentication_cmd(auth_byte);
		if(cmd == null){
			Toast.makeText(getApplicationContext(), "send cmd fail!", Toast.LENGTH_SHORT).show();
			return;
		}
		String sssss = Tools.Bytes2HexString(cmd, cmd.length);
		Log.e(TAG + " auth cmd", sssss);   //Log 认证命令
		break;
		case R.id.startScan:
		//再读取
	     if(authentication_flag != 1){
				Toast.makeText(getApplicationContext(), "Before reading the data for certification", Toast.LENGTH_SHORT).show();
				return;
			}	     
			cmd_flag = 3;
			Log.e("读块",value_array+"");
			cmd = mpsam.rf_read_cmd(value_array);
			if(cmd == null){
				Toast.makeText(getApplicationContext(), "send cmd fail", Toast.LENGTH_SHORT).show();
				return;
			}
			break;
		default:
			break;
		}
		     sendToservice.putExtra("cmd", cmd);  //封装命令
		     PeopleValidateActivity.this.startService(sendToservice); //发送命令到服务
		
		
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		myBroadcast = new MyBroadcast();
		mpsam = new PSAM();//实例化mpsam,用于调用协议封装命令
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.csei.inspect.PeopleValidateActivity");
		registerReceiver(myBroadcast, filter); 		//注册广播接收者
		super.onResume();

	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		cmd_flag = 0;  				  //写状态恢复初始状态
		authentication_flag = 0;
		unregisterReceiver(myBroadcast);  //卸载广播接收者
		super.onPause();
		Log.e("M1CARDPAUSE", "PAUSE");  
		
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Intent stopService = new Intent();
		stopService.setAction("com.example.service.DeviceService");
		stopService.putExtra("stopflag", true);
		sendBroadcast(stopService);  //给服务发送广播,令服务停止
		Log.e(TAG, "send stop");
		super.onDestroy();
	}
	/**
	 *  广播接收者,接收服务发送过来的数据，并更新UI
	 * @author Administrator
	 *
	 */
	private class MyBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String receivedata = intent.getStringExtra("result"); // 服务返回的数据
			if (receivedata != null) {
				switch (cmd_flag) {
				case 1: // 寻卡返回数据
					byte[] M1_cardarr = mpsam.resolveDataFromDevice(Tools
							.HexString2Bytes(receivedata));  //解析数据
					if (M1_cardarr != null) {
						/*new AlertDialog.Builder(MainActivity.this).setMessage(Tools.Bytes2HexString(M1_cardarr,
								M1_cardarr.length)).show();*/
						Toast.makeText(getApplicationContext(), "get UID success",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "get UID fail",
								Toast.LENGTH_SHORT).show();
					}

					break;
				case 2:  //认证返回数据
					int auth_flag = mpsam.rf_check_data(Tools.HexString2Bytes(receivedata));
					if(auth_flag != 0){
						Toast.makeText(getApplicationContext(), "authentication fail", Toast.LENGTH_SHORT).show();
						authentication_flag = 0;
					}else{
						Toast.makeText(getApplicationContext(), "authentication success", Toast.LENGTH_SHORT).show();
						authentication_flag = 1;
					}
					break;
				case 3:
					byte [] temp = Tools.HexString2Bytes(receivedata);
					Log.e("receivedata",receivedata);
					try {
						Log.e("receivedata.getBytes",new String(receivedata.getBytes("UTF-8"))+":");
						
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.e("temp",new String(temp));   
					byte [] readbuffer = mpsam.resolveDataFromDevice(temp);
					if(readbuffer != null){
//						if(read_data.getText().toString().length() > 30) read_data.setText("");  //读取下一个块时清空
						String value_mbuffer = Tools.Bytes2HexString(readbuffer, readbuffer.length);
						Log.e("redabufferjj",value_mbuffer+"");
						try {
							new AlertDialog.Builder(PeopleValidateActivity.this).setMessage(new String(Tools.HexString2Bytes(value_mbuffer),"UTF-8")).show();
							new AlertDialog.Builder(PeopleValidateActivity.this).setMessage(new String(Tools.HexString2Bytes(value_mbuffer),"UTF-8").substring(4,5)).show();
							uid=Integer.parseInt(new String(Tools.HexString2Bytes(value_mbuffer),"UTF-8").substring(5,6));
							username=new String(Tools.HexString2Bytes(value_mbuffer),"UTF-8").substring(6,9);
							getFile(Integer.parseInt(new String(Tools.HexString2Bytes(value_mbuffer),"UTF-8").substring(4,5)));
						    pb.setMax(result.length());// 进度条最大值设为文章的长度
							count++;
							    System.out.println("count=" + count);
							    if (count % 2 == 1) {
							     myRunnable = new MyRunnable();
							     thread = new Thread(myRunnable);
							     thread.start();
							     // 启动线程
							     tag = 0;
							     startScan.setText("暂停扫描");
							    }
							    if (count % 2 == 0) {
							     tag = 1;
							     startScan.setText("开始扫描");
							    }   
						
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Log.e(TAG, value_mbuffer);
					}else{
						Toast.makeText(getApplicationContext(), "read data fail", Toast.LENGTH_SHORT).show();
					}
					break;
				}
				 handler = new Handler() {
					   @Override
					   public void handleMessage(Message msg) {
					    super.handleMessage(msg);
					    switch (msg.what) {
					    case 1:
							drawRolesTableListView();
					       break; 
					    }
					   }
					private void drawRolesTableListView() {
						String[] s=result.split(",");
					      final ArrayList<HashMap<String, Object>> listItem=new ArrayList<HashMap<String,Object>>();
						     for(int i=0;i<s.length;i++){
						    	 HashMap<String, Object> map=new HashMap<String,Object>();
						    	 map.put("ItemImage",R.drawable.item);
						    	 map.put("ItemText", s[i]);
						    	 listItem.add(map);		 
						     }
						     SimpleAdapter listItemAdapter=new SimpleAdapter(PeopleValidateActivity.this,listItem,R.layout.rolestable,new String[]{"ItemImage","ItemText"},new int[]{R.id.ItemImage,R.id.ItemText});		          
						 index++;
					     pb.setProgress(index);
					     textview.setText("当前进度:" + index + "/" + result.length());
					     System.out.println("扫描........");
					     textview.setTextSize(15);
					     // 如果读取结束，则重新读取
					    if (index == result.length()) {
					       index = 0;	     
					       showalert.setText("文件扫描完毕!");      
					       rolestablelist.setAdapter(listItemAdapter);	       
					       rolestablelist.setOnItemClickListener(new OnItemClickListener() {
								@SuppressWarnings({ "unchecked" })
								public void onItemClick(AdapterView<?> parent, View view, int position,
										long id) {
									showalert.setVisibility(View.GONE);
									cur_pos=position;                          
				                    myadapter=new MyAdapter(PeopleValidateActivity.this, listItem, cur_pos);
				                    rolestablelist.setAdapter(myadapter);
				                    rolestablelist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
									ListView listview=(ListView) parent;
									HashMap<String,String>map=(HashMap<String, String>) listview.getItemAtPosition(position);
									String tbname=map.get("ItemText");				
									Intent intent=new Intent(PeopleValidateActivity.this,TagValidateActivity.class);
									Bundle bundle=new Bundle();
									bundle.putString("tbname", tbname);
									bundle.putInt("count", 1);
									bundle.putString("username", username);
									bundle.putInt("uid", uid);
									intent.putExtras(bundle);
									startActivityForResult(intent, 0);
								}
								
								
							});
					       tag=2;	   
					    }
					}
					  };
					  
					}
				
		}
	}
	private class MyRunnable implements Runnable {
		  public void run() {
		   while(tag==0) {
		    handler.sendEmptyMessage(1);
		    try {
		     Thread.sleep(100);
		    } catch (InterruptedException e) {
		     e.printStackTrace();
		    }
		   }
		  }
}
	      
	/**
	 * 写数据验证,用于验证写入数据
	 * @param src
	 * @return boolean
	 */
	public static boolean checkData(String src){
		boolean flag = false;
		String regString = "[a-f0-9A-F]{32}";
		flag = Pattern.matches(regString, src); //匹配数据，是否为32位的十六进制
		return flag;
	}
	// 得到文件内容的方法，返回一个字符串
		@SuppressWarnings("rawtypes")
		public String getFile(int rid) {          //获取人员点检信息
			ParseXml p=new ParseXml();
			filename=fileDir+"/RolesTable.xml";
				List<DbModel> list=p.parseRolesTable(filename,rid);
			    Iterator it=list.iterator();
			    while(it.hasNext()){
			    	DbModel d=(DbModel) it.next();
			    	result+=d.getTableitem()+",";
			    }
			return result;
		}
	
	
	
	
	}

