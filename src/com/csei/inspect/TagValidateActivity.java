package com.csei.inspect;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import com.cesi.analysexml.ParseXml;
import com.csei.adapter.MyexpandableListAdapter;
import com.example.service.RFIDService;
import com.example.tools.Tools;
import com.example.viewpager.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
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
import android.widget.SimpleAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
@SuppressLint("HandlerLeak")
public class TagValidateActivity extends Activity implements ExpandableListView.OnChildClickListener,ExpandableListView.OnGroupClickListener, OnClickListener{            
	RadioGroup inspectResult;          //右侧的点检结果列表
    RadioButton checkRadioButton;
	int cur_pos=0;               //主要用于判断当前的position，以使当前的listview中的Item高亮
	int cur_pos1=0;
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
	String groupItem;
	Button backbutton;
	TextView devnum;
	private ImageView arrow;
	private boolean isOpenPop = false;
	private PopupWindow window;
	private ListView list;
	public static final String KEY = "key";
	ArrayList<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
	Context mContext;
	Button beizhu;
	Button startScan;
	private MyBroadcast myBroadcast;				//广播接收者
	public static int cmd_flag = 0;				//操作状态  0为不做其他操作，1为寻卡，2为认证，3为读数据，4为写数据
	public static int authentication_flag = 0;		//认证状态  0为认证失败和未认证  1为认证成功
	private String activity = "com.csei.inspect.TagValidateActivity";
		//Debug
	public static String TAG= "M1card";
	volatile boolean Thread_flag = false;
	String dnum;
	int areaid;
	private TextView title;
	String savefile="保存";
	String exit="退出";
	String cardType="0x02";
	private ProgressDialog shibieDialog; //识别搜索框
	View view_Group;
	private Timer timerDialog;  //搜索框计时器
	private Timer timeThread;
	private int MSG_FLAG = 1;
	//Dialog结束标识
	private int MSG_OVER = 2;
	String beizhustr;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == MSG_FLAG){
				
			}else if(msg.what == MSG_OVER){
				Toast.makeText(getApplicationContext(), "未识别到标签卡，请重试", Toast.LENGTH_SHORT).show();
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	//初始化
	private void init() {
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
		p.writeToFormatXml(filename);
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
		backbutton=(Button) this.findViewById(R.id.backbutton);
		devnum=(TextView) this.findViewById(R.id.devnum);
		beizhu = (Button) this.findViewById(R.id.beizhu);
		startScan=(Button) this.findViewById(R.id.startScan);
		title=(TextView) this.findViewById(R.id.title);
		startScan.setOnClickListener(this);
		beizhu.setOnClickListener(new ClickEvent());
		user=(TextView) this.findViewById(R.id.username);
		user.setText(username);
		mContext=this;
		arrow = (ImageView) findViewById(R.id.arrow); 
		title.setOnClickListener(new OnClickListener() {
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
			// 注：上面的设背景操作为重点部分，可以自行注释掉其中一个或两个设背景操作，查看对话框效果
			final EditText edtUsername = (EditText) dialogView
					.findViewById(R.id.username_edit);
			//在这里通过点击当前项，根据区域和点检项来查找相应的备注
			edtUsername.setHint(beizhustr); // 设置提示语
			// OK按钮及其处理事件
			TextView beizhutitle=(TextView) dialogView.findViewById(R.id.username_view);
			beizhutitle.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					//隐藏软键盘
					 InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
			         imm.hideSoftInputFromWindow(edtUsername.getWindowToken(),0);
				}
			});
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
			if(map.get(KEY).equals(savefile)){
				Toast.makeText(mContext,"文件已"+map.get(KEY)+"", Toast.LENGTH_SHORT).show();
				writeToXmlUserDateDvnum(filename);
			}else if(map.get(KEY).equals(exit)){
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
		map.put(KEY, savefile);
		items.add(map);
		map = new HashMap<String, Object>();
		map.put(KEY, exit);
		items.add(map);		
		return items;
	}
    @SuppressWarnings("rawtypes")
	private void InitData() {
		// TODO Auto-generated method stub
    	//在这之中
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
				childTemp = new ArrayList<String>();
				itemlist=p.queryItemFromXmlByTag(filename,groupList.get(i));
				Iterator it=itemlist.iterator();
				while(it.hasNext()){
				String item=(String) it.next();
				childTemp.add(item);
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
			Toast.makeText(TagValidateActivity.this, "请扫描标签!", Toast.LENGTH_SHORT).show();
		}
		if(isScaned==2){
			inspectItem.collapseGroup(groupPosition);
			Toast.makeText(TagValidateActivity.this, "与所扫描标签不符!", Toast.LENGTH_SHORT).show();
			inspectResultPane.setVisibility(View.GONE);
		}
		return false;
	}
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		if(isInspect==false){
			Toast.makeText(TagValidateActivity.this, "请扫描标签!", Toast.LENGTH_SHORT).show();
		}
		if(isScaned==2){
			Toast.makeText(TagValidateActivity.this, "与所扫描标签不符!", Toast.LENGTH_SHORT).show();
		}
		return true;
}
	public void onClick(View v) {
		shibieDialog = new ProgressDialog(TagValidateActivity.this, R.style.mProgressDialog);
		shibieDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		shibieDialog.setMessage("识别标签中...");
		shibieDialog.setCancelable(false);
		shibieDialog.show();
		timerDialog = new Timer();
		//7秒后取消搜索
		timerDialog.schedule(new TimerTask() {
			@Override
			public void run() {
				shibieDialog.cancel();
				Message msg = new Message();
				msg.what = MSG_OVER;
				mHandler.sendMessage(msg);
			}
		}, 7000);
		Intent sendToservice = new Intent(TagValidateActivity.this,RFIDService.class);
		sendToservice.putExtra("cardType", cardType);
		sendToservice.putExtra("activity", activity);
		startService(sendToservice); 
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		timeThread = new Timer();
		timeThread.schedule(new TimerTask() {
			@Override
			public void run() {
//				String timeStr = Tools.getTime();
				Message msg = new Message();
				msg.what = MSG_FLAG;
				mHandler.sendMessage(msg);
			}
		}, 0 , 1000);
		myBroadcast = new MyBroadcast();
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
		timeThread.cancel();
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
					byte [] temp = Tools.HexString2Bytes(receivedata);
					try {
						Log.e("temp",new String(temp,"UTF-8"));
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if(temp != null){
//						if(read_data.getText().toString().length() > 30) read_data.setText("");  //读取下一个块时清空
						shibieDialog.cancel();
						timerDialog.cancel();
						try {
						    //
							int templen=new String(temp,"UTF-8").length();
							if(templen<12){
							    //长度小于12
							}else{
							String ctype=new String(temp,"UTF-8").substring(0,2);
							if(ctype.equals("x2")){
							dnum=new String(temp,"UTF-8").substring(2,11);
							areaid=Integer.parseInt(new String(temp,"UTF-8").substring(11,12));
							devnum.setText(dnum);
						    //根据这个DNUM和AreAid在tags.xml中查出点检区域
							String t=new String(temp,"UTF-8").substring(17,21);
							if(t.equals("司机室区")){
								t="司机室区域";
							}
							Toast.makeText(TagValidateActivity.this, "标签扫描完毕!", Toast.LENGTH_SHORT).show();
							   isInspect=true;   
				          if(isInspect){
				    	     //一刷标签时，即会扫描ListView中的item
				    		tag=t;				  
				    		
				    		for(int i=0;i<groupList.size();i++){
					    	   if(groupList.get(i).equals(tag)){				    
					    		   isScaned=1;
					    		   inspectItem.expandGroup(i);
					    		   for(int j=0;j<groupList.size();j++){
					    			   if(j!=i){
					    				   inspectItem.collapseGroup(j);
					    			   }
					    		   }
					    		   if(isScaned==1){
					    		   inspectItem.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
					    		   inspectItem.setOnChildClickListener(new OnChildClickListener() {
									public boolean onChildClick(ExpandableListView parent, View v,
											int groupPosition, int childPosition, long id) {
										// TODO Auto-generated method stub	
									     itemItem=childList.get(groupPosition).get(childPosition);
									     groupItem=groupList.get(groupPosition);
									     Log.e("groupItem",groupItem);
									     v.setSelected(true); 
									    boolean f=judgeIsBelongToScanTag(filename,itemItem,tag,groupItem);
									    if(f){
										String value=p.getValueFromXmlByItem(filename, itemItem,tag);										
										inspectResultPane.setVisibility(View.VISIBLE);
										if(normal.getText().equals(value)){
											normal.setChecked(true);
										}else if(abnormal.getText().equals(value)){
											abnormal.setChecked(true);
										}else if(nothing.getText().equals(value)){
											nothing.setChecked(true);
										}
										beizhustr=p.getBeiZhuFromInspectXml(filename,itemItem,tag,groupItem);
									     }else{
												Toast.makeText(TagValidateActivity.this, "点检项不属于所扫描标签!", Toast.LENGTH_SHORT).show();
											 inspectResultPane.setVisibility(View.GONE);
									     }
									     return false;
									}
                                    //判断一个点检项是否属于某个区域
									private boolean judgeIsBelongToScanTag(
											String filename, String itemItem,
											String tag,String groupItem) {
										     boolean flag=false;
										    	flag=p.judgeItemIsBelong(filename, tag, itemItem,groupItem);	 
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
							}else{
								Toast.makeText(TagValidateActivity.this, "卡类型有误!", Toast.LENGTH_SHORT).show();
							}
							}
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else{
					Toast.makeText(TagValidateActivity.this, "读取数据失败!", Toast.LENGTH_SHORT).show();
					shibieDialog.cancel();
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
