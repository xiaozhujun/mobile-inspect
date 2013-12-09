package com.csei.inspect;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.cesi.analysexml.ParseXml;
import com.csei.adapter.MyexpandableListAdapter;
import com.example.psam_demo.PSAM;
import com.example.service.DeviceService;
import com.example.tools.Tools;
import com.example.viewpager.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
public class TagValidateActivity extends Activity implements ExpandableListView.OnChildClickListener,ExpandableListView.OnGroupClickListener, OnClickListener{
	/*Button scanTag;              //扫描标签按钮               
*/	RadioGroup inspectResult;          //右侧的点检结果列表
    RadioButton checkRadioButton;
	int cur_pos=0;               //主要用于判断当前的position，以使当前的listview中的Item高亮
	String username=null;         //获取点检人员
	int uid=0;                    //获取点检人员ID
    String filename=null;          //获取点检表，不同的点检表会出现不同的点检项
	ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
	boolean isInspect=false;            //是否能点检 ,默认为false,当点击扫描标签之后,变为true
	String fileDir=null;                //指向点检文件存放的位置的根目录  /data/data/com.example.viewpager/files/
	TextView inspecttable;              //显示点检表
	String tag;                         //扫描标签时，查询出相应xml文件中的<location>的值
	ParseXml p=new ParseXml();           //调用解析xml文件的类
	boolean Inspect=false;                //
	List<String> ScanedTag=new ArrayList<String>();    //用来保存已扫描过的标签
	String tagflag;
	TextView user;
	int isScaned;
	private ExpandableListView inspectItem;
	private ArrayList<String> groupList;
	private ArrayList<List<String>> childList;
	private MyexpandableListAdapter adapter;
	String tname;
	View inspectResultPane;
	RadioButton normal;
	RadioButton abnormal;
	RadioButton nothing;
	String itemItem;
	TextView showalert;
	Button backbutton;
	TextView devnum;
	private ImageView arrow;
	private boolean isOpenPop = false;
	private PopupWindow window;
	private ListView list;
	private RelativeLayout title_layout;
	public static final String KEY = "key";
	ArrayList<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
	Context mContext;
	Button beizhu;
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
	private String activity = "com.csei.inspect.TagValidateActivity";
		//Debug
	public static String TAG= "M1card";
	volatile boolean Thread_flag = false;
	String dnum;
	int areaid;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView textview = new TextView(this);
		setContentView(textview);
		Bundle bundle = getIntent().getExtras();
		/*fileDir=this.getApplication().getFilesDir().getAbsolutePath();*/
		//真机上的路径
		/*fileDir=Environment.getExternalStorageDirectory().toString()+"/inspectfiles";*/
		//模拟器上的
		fileDir=Environment.getExternalStorageDirectory().toString();
		int count = bundle.getInt("count");                   //接收ScanCardActivity传来的count值
			if (count != 0) {                                  //若不是第一次跳转到这个页面，则进行下一逻辑，否则提示先进行身份验证
		    tname = bundle.getString("tbname");        
			filename=getFileNameByTableName(tname);                      //根据不同的tname来得到filename
			username=bundle.getString("username");
			uid=bundle.getInt("uid");
			setContentView(R.layout.tagvalidate);                              //使用tagvalidate.xml资源文件
			//以下是获取相应的资源
			inspecttable=(TextView) this.findViewById(R.id.tbname);
			inspecttable.setText(tname);
			inspectItem =  (ExpandableListView) this.findViewById(R.id.inspectItem);
			inspectResult = (RadioGroup) this.findViewById(R.id.insepctResult);
			checkRadioButton=(RadioButton) this.findViewById(inspectResult.getCheckedRadioButtonId());
			inspectResultPane=this.findViewById(R.id.inspectResultPane);
			normal=(RadioButton) this.findViewById(R.id.normal);
			abnormal=(RadioButton) this.findViewById(R.id.abnormal);
			nothing=(RadioButton) this.findViewById(R.id.nothing);
			showalert=(TextView) this.findViewById(R.id.showalert);
			backbutton=(Button) this.findViewById(R.id.backbutton);
			devnum=(TextView) this.findViewById(R.id.devnum);
			beizhu = (Button) this.findViewById(R.id.beizhu);
			searchbtn=(Button) this.findViewById(R.id.searchcard);
			authbtn=(Button) this.findViewById(R.id.auth);
			startScan=(Button) this.findViewById(R.id.startScan);
			searchbtn.setOnClickListener(this);
			authbtn.setOnClickListener(this);
			startScan.setOnClickListener(this);
			beizhu.setOnClickListener(new ClickEvent());
			user=(TextView) this.findViewById(R.id.username);
			user.setText(username);
			mContext=this;
			arrow = (ImageView) findViewById(R.id.arrow);
			title_layout =  (RelativeLayout) findViewById(R.id.title_layout);
			title_layout.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
					// TODO Auto-generated method stub
                      changPopState(v);

				}
			});
		        backbutton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						backbutton.setBackgroundResource(R.drawable.btn_back_active);
						finish();
					}
				});
			InitData();
			adapter = new MyexpandableListAdapter(TagValidateActivity.this,groupList,childList);
			inspectItem.setAdapter(adapter);
			inspectItem.setOnChildClickListener(this);
			inspectItem.setOnGroupClickListener(this);
			//扫描标签事件绑定
			 /* scanTag.setOnClickListener(new OnClickListener() {
						//当点击扫描标签时，
						public void onClick(View v) {
							showalert.setText("标签扫描完毕!");
							   isInspect=true;   
				    if(isInspect){
				    	     //一刷标签时，机会扫描Listview中的item
				    		tag=scanTag();
				    		p.writeToFormatXml(filename);
				    		for(int i=0;i<groupList.size();i++){
					    	   if((groupList.get(i)).equals(tag)){					    		
					    		   isScaned=1;
					    		   inspectItem.expandGroup(i);
					    		   for(int j=0;j<groupList.size();j++){
					    			   if(j!=i){
					    				   inspectItem.collapseGroup(j);
					    			   }
					    		   }
					    		   if(isScaned==1){
					    		   inspectItem.setOnChildClickListener(new OnChildClickListener() {
									public boolean onChildClick(ExpandableListView parent, View v,
											int groupPosition, int childPosition, long id) {
										// TODO Auto-generated method stub		        
									     itemItem=childList.get(groupPosition).get(childPosition);
									    boolean f=judgeIsBelongToScanTag(filename,itemItem,tag);
									    if(f){
									    showalert.setVisibility(View.GONE);
										String value=p.getValueFromXmlByItem(filename, itemItem);										
										inspectResultPane.setVisibility(View.VISIBLE);
										if(normal.getText().equals(value)){
											normal.setChecked(true);
										}else if(abnormal.getText().equals(value)){
											abnormal.setChecked(true);
										}else if(nothing.getText().equals(value)){
											nothing.setChecked(true);
										}
									     }else{
									    	 showalert.setVisibility(View.VISIBLE);
											 showalert.setText("点检项不属于所扫描标签!");
											 inspectResultPane.setVisibility(View.GONE);
									     }
										
									     return false;
									}

									private boolean judgeIsBelongToScanTag(
											String filename, String itemItem,
											String tag) {
										     boolean flag=false;
										    	flag=p.judgeItemIsBelong(filename, tag, itemItem);	 
		                                         return flag;	
									}
								});
					    		   inspectResult.setOnCheckedChangeListener(new OnCheckedChangeListener() {
										public void onCheckedChanged(RadioGroup group, int checkedId) {
											// TODO Auto-generated method stub
											checkRadioButton=(RadioButton) inspectResult.findViewById(checkedId);						
											String v=(String) checkRadioButton.getText();
											p.updateInspectXml(filename, itemItem, v,tag);
										}
									});
					    		   }
					    		   }else{
					    			   isScaned=2;
					    		   }
					    	   
					    	   }				        					    	  
				    }
						}
					});			       */
		}
   
	}
	// 处理按键事件
		class ClickEvent implements OnClickListener {
			public void onClick(View v) {
				if (v == beizhu) {
					showRoundCornerDialog(TagValidateActivity.this, TagValidateActivity.this
							.findViewById(R.id.beizhu));
					
				}
			}
		}
		// 显示圆角对话框
		@SuppressWarnings("deprecation")
		public void showRoundCornerDialog(Context context, View parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// 获取圆角对话框布局View，背景设为圆角
			final View dialogView = inflater.inflate(R.layout.popupwindow, null,
					false);
			dialogView.setBackgroundResource(R.drawable.rounded_corners_view);
			// 创建弹出对话框，设置弹出对话框的背景为圆角
			final PopupWindow pw = new PopupWindow(dialogView,LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT, true);
			pw.setOutsideTouchable(true);
			pw.setAnimationStyle(R.style.PopupAnimation);
			/*pw.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.rounded_corners_pop));	*/
			// 注：上面的设背景操作为重点部分，可以自行注释掉其中一个或两个设背景操作，查看对话框效果
			final EditText edtUsername = (EditText) dialogView
					.findViewById(R.id.username_edit);
			edtUsername.setHint("请输入备注..."); // 设置提示语
			// OK按钮及其处理事件
			Button btnOK = (Button) dialogView.findViewById(R.id.BtnOK);
			btnOK.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// 设置文本框内容
					String comment=edtUsername.getText().toString();
					p.writeCommentToXml(filename, itemItem, comment,tag);
					pw.dismiss();
				}
			});
			// Cancel按钮及其处理事件
			Button btnCancel = (Button) dialogView.findViewById(R.id.BtnCancel);
			btnCancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					pw.dismiss();// 关闭
				}
			});
			// 显示RoundCorner对话框
			pw.showAtLocation(parent, Gravity.CENTER | Gravity.CENTER, 0, 0);
		}
	
	/**
	 * 更改Pop状态
	 * */

	public void changPopState(View v) {

		isOpenPop = !isOpenPop;
		if (isOpenPop) {
			arrow.setBackgroundResource(R.drawable.icon_arrow_up);
			popAwindow(v);

		} else {
			arrow.setBackgroundResource(R.drawable.icon_arrow_down);
			if (window != null) {
				window.dismiss();

			}
		}
	}
	private void popAwindow(View parent) {
		if (window == null) {
			LayoutInflater lay = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = lay.inflate(R.layout.pop, null);
			list = (ListView) v.findViewById(R.id.pop_list);

			SimpleAdapter adapter = new SimpleAdapter(this, CreateData(),
					R.layout.pop_list_item, new String[] { KEY },
					new int[] { R.id.title });
			list.setAdapter(adapter);
			list.setItemsCanFocus(false);
			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			list.setOnItemClickListener(listClickListener);
			// window = new PopupWindow(v, 260, 300);
			int x = (int) getResources().getDimension(R.dimen.pop_x);
			int y = (int) getResources().getDimension(R.dimen.pop_y);
			window = new PopupWindow(v, x, y);
		}
		window.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.pop_bg));
		window.setFocusable(true);
		window.setOutsideTouchable(false);
		window.setOnDismissListener(new OnDismissListener() {

			public void onDismiss() {
				// TODO Auto-generated method stub
				isOpenPop = false;
				arrow.setBackgroundResource(R.drawable.icon_arrow_down);
			}
		});
		window.update();
		window.showAtLocation(parent, Gravity.RIGHT | Gravity.TOP,
				0, (int) getResources().getDimension(R.dimen.pop_layout_y));
	}
	OnItemClickListener listClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map=(Map<String, Object>) parent.getItemAtPosition(position);
			if(map.get(KEY).equals("保存")){
				Toast.makeText(mContext,"文件已"+map.get(KEY)+"", Toast.LENGTH_SHORT).show();
				writeToXmlUserDateDvnum(filename);
			}else if(map.get(KEY).equals("退出")){
				Toast.makeText(mContext,"您好!正在"+map.get(KEY)+".......", Toast.LENGTH_SHORT).show();
				System.exit(0);
			}
			if (window != null) {
				window.dismiss();
			}
		}
	};
	public ArrayList<Map<String, Object>> CreateData() {		
		Map<String, Object> map;
		map = new HashMap<String, Object>();
		map.put(KEY, "保存");
		items.add(map);
		map = new HashMap<String, Object>();
		map.put(KEY, "退出");
		items.add(map);		
		return items;
	}
    @SuppressWarnings("rawtypes")
	private void InitData() {
		// TODO Auto-generated method stub
    	List<String> taglist=p.queryLocationFromXml(filename);
    	Iterator t=taglist.iterator();
    	groupList = new ArrayList<String>();
    	String tag = null;
    	List<String> itemlist=new ArrayList<String>();
    	while(t.hasNext()){
    	tag=(String) t.next();
		groupList.add(tag);
    	}
		childList = new ArrayList<List<String>>();
		for (int i = 0; i < groupList.size(); i++) {
			ArrayList<String> childTemp;
			if (i == 0) {
				childTemp = new ArrayList<String>();
				itemlist=p.queryItemFromXmlByTag(filename,"行走区域");
				Iterator it=itemlist.iterator();
				while(it.hasNext()){
				String item=(String) it.next();
				childTemp.add(item);
				}
			} else if (i == 1) {
				childTemp = new ArrayList<String>();
				itemlist=p.queryItemFromXmlByTag(filename,"转盘区域");
				Iterator it=itemlist.iterator();
				while(it.hasNext()){
				String item=(String) it.next();
				childTemp.add(item);
				}
			} else if(i==2){
				childTemp = new ArrayList<String>();
				itemlist=p.queryItemFromXmlByTag(filename,"司机室区域");
				Iterator it=itemlist.iterator();
				while(it.hasNext()){
				String item=(String) it.next();
				childTemp.add(item);
			}
			}else{
					childTemp = new ArrayList<String>();
					itemlist=p.queryItemFromXmlByTag(filename,"臂架区域");
					Iterator it=itemlist.iterator();
					while(it.hasNext()){
					String item=(String) it.next();
					childTemp.add(item);	
				}
			}
			childList.add(childTemp);
		}		
	}
	//模拟器上的
	private String getFileNameByTableName(String tname) {
		if(tname.equals("机修人员点检表")){                     //这里根据选择的不同的表名来赋给相应的不同的filename
			filename=fileDir+"/jixiu.xml";
		}else if(tname.equals("门机队机械技术员点检表")){
			filename=fileDir+"/jixie.xml";
		}else if(tname.equals("门机技术员电气日常点检表")){
			filename=fileDir+"/dianqi.xml";
		}else if(tname.equals("门机减速机专项点检卡")){
			filename=fileDir+"/jiansuji.xml";
		}else if(tname.equals("门机司机日常点检表")){
			filename=fileDir+"/siji.xml";
		}else if(tname.equals("门机周一定保专项点检卡片")){
			filename=fileDir+"/zhouyidingbao.xml";
		}
		return filename;
	}
	//真机上的filename就等于表名
	/*private String getFileNameByTableName(String tname) {
		filename=tname;
		return filename;
	}
*/	
	public List<String> getLocation() {             //获取xml文件中的<location>和<field>的值
		List<String> list = new ArrayList<String>();
	    list = p.parseInspect(filename);		
		return list;
	}
	private void writeToXmlUserDateDvnum(String filename) {        //将username,uid,devnum,insepcttime写入xml文件
		Date d=new Date(System.currentTimeMillis());
		p.writeToXmlUserDateDvnum(filename,tname,username,uid,dnum,d);	
	}   
	public void writeFormatXml(String pathname){         //将指定格式的文件写入
		p.writeToFormatXml(pathname);
	}
	
	public List<String> queryLocationFromXml(){
		List<String> list=p.queryLocationFromXml(filename);
		return list;
	}
	public boolean onGroupClick(final ExpandableListView parent, final View v,
			int groupPosition, final long id) {
		if(isInspect==false){
			showalert.setVisibility(View.VISIBLE);
			showalert.setText("请扫描标签!");
		}
		if(isScaned==2){
			inspectItem.collapseGroup(groupPosition);
			showalert.setVisibility(View.VISIBLE);
			showalert.setText("与所扫描标签不符!");
			inspectResultPane.setVisibility(View.GONE);
		}
		return false;
	}
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		  
		if(isInspect==false){
			showalert.setVisibility(View.VISIBLE);
			showalert.setText("请扫描标签!");
		}
		if(isScaned==2){
			showalert.setVisibility(View.VISIBLE);
			showalert.setText("与所扫描标签不符!");
		}
		return true;
}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent ac = new Intent();
		ac.setAction("com.example.service.DeviceService");
		ac.putExtra("activity", activity);
		sendBroadcast(ac);  
		Log.e(TAG, "send broadcast");	
		byte []cmd = null; //用于存放指令
		Intent sendToservice = new Intent(TagValidateActivity.this,DeviceService.class);  //用于发送指令
		String sector_str = "02".toString().trim();   //Sector
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
	    new AlertDialog.Builder(TagValidateActivity.this).setMessage("hah1").show();
		cmd_flag = 1;  	   //寻卡标识为1
		cmd = mpsam.rf_card(); //获取命令
		String cmd_find_card = Tools.Bytes2HexString(cmd, cmd.length);
		Log.e(TAG, cmd_find_card);
		break;
		case R.id.auth:
        //先验证
		new AlertDialog.Builder(TagValidateActivity.this).setMessage("hah2").show();
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
		     TagValidateActivity.this.startService(sendToservice); //发送命令到服务
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		myBroadcast = new MyBroadcast();
		mpsam = new PSAM();//实例化mpsam,用于调用协议封装命令
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.csei.inspect.TagValidateActivity");
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
							new AlertDialog.Builder(TagValidateActivity.this).setMessage(new String(Tools.HexString2Bytes(value_mbuffer),"UTF-8")).show();
						    //
							dnum=new String(Tools.HexString2Bytes(value_mbuffer),"UTF-8").substring(0,9);
							areaid=Integer.parseInt(new String(Tools.HexString2Bytes(value_mbuffer),"UTF-8").substring(9,10));
							devnum.setText(dnum);
						    //根据这个dnum和areaid在tags.xml中查出点检区域
							String t=new String(Tools.HexString2Bytes(value_mbuffer),"UTF-8").substring(10,12);
							showalert.setText("标签扫描完毕!");
							   isInspect=true;   
				    if(isInspect){
				    	     //一刷标签时，机会扫描Listview中的item
				    		tag=t+"区域";
				    		p.writeToFormatXml(filename);
				    		for(int i=0;i<groupList.size();i++){
					    	   if((groupList.get(i)).equals(tag)){					    		
					    		   isScaned=1;
					    		   inspectItem.expandGroup(i);
					    		   for(int j=0;j<groupList.size();j++){
					    			   if(j!=i){
					    				   inspectItem.collapseGroup(j);
					    			   }
					    		   }
					    		   if(isScaned==1){
					    		   inspectItem.setOnChildClickListener(new OnChildClickListener() {
									public boolean onChildClick(ExpandableListView parent, View v,
											int groupPosition, int childPosition, long id) {
										// TODO Auto-generated method stub		        
									     itemItem=childList.get(groupPosition).get(childPosition);
									    boolean f=judgeIsBelongToScanTag(filename,itemItem,tag);
									    if(f){
									    showalert.setVisibility(View.GONE);
										String value=p.getValueFromXmlByItem(filename, itemItem);										
										inspectResultPane.setVisibility(View.VISIBLE);
										if(normal.getText().equals(value)){
											normal.setChecked(true);
										}else if(abnormal.getText().equals(value)){
											abnormal.setChecked(true);
										}else if(nothing.getText().equals(value)){
											nothing.setChecked(true);
										}
									     }else{
									    	 showalert.setVisibility(View.VISIBLE);
											 showalert.setText("点检项不属于所扫描标签!");
											 inspectResultPane.setVisibility(View.GONE);
									     }
										
									     return false;
									}

									private boolean judgeIsBelongToScanTag(
											String filename, String itemItem,
											String tag) {
										     boolean flag=false;
										    	flag=p.judgeItemIsBelong(filename, tag, itemItem);	 
		                                         return flag;	
									}
								});
					    		   inspectResult.setOnCheckedChangeListener(new OnCheckedChangeListener() {
										public void onCheckedChanged(RadioGroup group, int checkedId) {
											// TODO Auto-generated method stub
											checkRadioButton=(RadioButton) inspectResult.findViewById(checkedId);						
											String v=(String) checkRadioButton.getText();
											p.updateInspectXml(filename, itemItem, v,tag);
										}
									});
					    		   }
					    		   }else{
					    			   isScaned=2;
					    		   }
					    	   
					    	   }				        					    	  
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
}
