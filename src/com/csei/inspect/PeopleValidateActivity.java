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
import com.example.service.RFIDService;
import com.example.viewpager.R;
import android.annotation.SuppressLint;
import android.app.Activity;
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
	Button backbutton;
	String str = "", result = "";       //result��һ��ȫ�ֱ��������ڴ�Ŵ�RolesTable.xml�ж�ȡ������ֵ
	ListView rolestablelist;              //��ʾRolesTable.xml�ж�ȡֵ��ListView
	int index = 0;                        // �ַ�����                                     
	int count = 0;                        // ��ť������
	MyRunnable myRunnable;
	Handler handler;
	Thread thread;
	int tag = 0;// ��ǩ
	ProgressBar pb;
	TextView showprocess;                         //��ʾ����
	String fileDir;                               //�ļ��л�·��,����Ĭ��ΪSdCard
	String filename;                              //�ļ���
	TextView showalert;                             //��ʾ 
	int cur_pos=0;                                  //���ڸ�����ʾ
	MyAdapter myadapter;                             //������ʾ��������
	Button startScan;                               //��ʼɨ��
	private MyBroadcast myBroadcast;				//�㲥������
	public static int cmd_flag = 0;				//����״̬  0Ϊ��������������1ΪѰ����2Ϊ��֤��3Ϊ�����ݣ�4Ϊд����
	public static int authentication_flag = 0;		//��֤״̬  0Ϊ��֤ʧ�ܺ�δ��֤  1Ϊ��֤�ɹ�
	private String activity = "com.csei.inspect.PeopleValidateActivity";
	//Debug
	public static String TAG= "M1card";
	volatile boolean Thread_flag = false;
	String username=null;
	int uid;
	String cardType="x1";
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.peoplevalidate);
	  init();
	}
	//��ʼ��
	private void init() {
		  fileDir=Environment.getExternalStorageDirectory().toString();
		  rolestablelist = (ListView) findViewById(R.id.rolestablelist);
		  pb = (ProgressBar) findViewById(R.id.pb);
		  showprocess = (TextView) findViewById(R.id.showprocess);
		  showalert=(TextView) findViewById(R.id.showalert);
		  backbutton=(Button) this.findViewById(R.id.backbutton);
		  startScan=(Button) this.findViewById(R.id.startScan);
		  startScan.setOnClickListener(this);
		  //���ذ�ť
		  backbutton.setOnClickListener(new OnClickListener() {
		  public void onClick(View v) {
					backbutton.setBackgroundResource(R.drawable.btn_back_active);
					System.exit(0);
				}
			});
	}
	//�����������
	public void onClick(View v) {
		Intent sendToservice = new Intent(PeopleValidateActivity.this,RFIDService.class);
		sendToservice.putExtra("cardType", "0x01");
		sendToservice.putExtra("activity", activity);
		startService(sendToservice); 
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		myBroadcast = new MyBroadcast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.csei.inspect.PeopleValidateActivity");
		registerReceiver(myBroadcast, filter); 		//ע��㲥������
		super.onResume();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		cmd_flag = 0;  				  //д״̬�ָ���ʼ״̬
		authentication_flag = 0;
		unregisterReceiver(myBroadcast);  //ж�ع㲥������
		super.onPause();
		Log.e("M1CARDPAUSE", "PAUSE");  	
	}	
	@Override
	protected void onDestroy() {
		Intent stopService = new Intent();
		stopService.setAction("com.example.service.RFIDService");
		stopService.putExtra("stopflag", true);
		sendBroadcast(stopService);  //�������͹㲥,�����ֹͣ
		Log.e(TAG, "send stop");
		super.onDestroy();
	}
	/**
	 *  �㲥������,���շ����͹��������ݣ�������UI
	 * @author Administrator
	 *
	 */
	private class MyBroadcast extends BroadcastReceiver {
		//����
		private void readCard(String receivedata){
			byte [] temp = Tools.HexString2Bytes(receivedata);	
			if(temp != null){
//				if(read_data.getText().toString().length() > 30) read_data.setText("");  //��ȡ��һ����ʱ���
				try {
					int templen=new String(temp,"UTF-8").length();
					if(templen<8){
						//����С��8
					}else{
					String ctype=new String(temp,"UTF-8").substring(0,2);
					if(ctype.equals(cardType)){
				    //������ʾ
					showalert.setVisibility(View.GONE);
					//��ȡUId
					uid=Integer.parseInt(new String(temp,"UTF-8").substring(2,3));
					//��ȡUserName
					username=new String(temp,"UTF-8").substring(4,8);
					//��ȡRolesTable.xml
					getFile(Integer.parseInt(new String(temp,"UTF-8").substring(3,4)));
				    pb.setMax(result.length());// ���������ֵ��Ϊ���µĳ���
					count++;
					    if (count % 2 == 1) {
					     myRunnable = new MyRunnable();
					     thread = new Thread(myRunnable);
					     thread.start();
					     // �����߳�
					     tag = 0;
					     startScan.setText("��ͣɨ��");
					    }
					    if (count % 2 == 0) {
					     tag = 1;
					     startScan.setText("��ʼɨ��");
					    }   
					}else{
						showalert.setVisibility(View.VISIBLE);
						showalert.setText("����������!");
					}
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}else{
				Toast.makeText(getApplicationContext(), "read data fail", Toast.LENGTH_SHORT).show();
			}
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String receivedata = intent.getStringExtra("result"); // ���񷵻ص�����
			Log.e("ooooooo",receivedata+"");
			/*String searchflag=intent.getStringExtra("searchflag");
			String authflag=intent.getStringExtra("authflag");
			if(searchflag.equals("01")){
				showalert.setVisibility(View.VISIBLE);
				showalert.setText("Ѱ��ʧ��!");
				
			}else if(authflag.equals("01")){
				showalert.setVisibility(View.VISIBLE);
				showalert.setText("��֤ʧ��!");
			}	*/
			if (receivedata != null) {
				 readCard(receivedata);
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
					 //��ȾListView,�������ڿ��ж�ȡ��rid��RolesTable.xml�ļ��в��ҳ������ݼ��ص�ListView��
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
					     showprocess.setText("��ǰ����:" + index + "/" + result.length());
					     showprocess.setTextSize(15);
					     // �����ȡ�����������¶�ȡ
					    if (index == result.length()) {
					       index = 0;	     
					       showalert.setText("�ļ�ɨ�����!");      
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
					}else{
						showalert.setVisibility(View.VISIBLE);
						showalert.setText("��ȡ����ʧ��!");
					}
		}
	}
	//��ȡ�ļ��߳�
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
	 * д������֤,������֤д������
	 * @param src
	 * @return boolean
	 */
	public static boolean checkData(String src){
		boolean flag = false;
		String regString = "[a-f0-9A-F]{32}";
		flag = Pattern.matches(regString, src); //ƥ�����ݣ��Ƿ�Ϊ32λ��ʮ������
		return flag;
	}
	// �õ��ļ����ݵķ���������һ���ַ���
		@SuppressWarnings("rawtypes")
		public String getFile(int rid) {          //��ȡ��Ա�����Ϣ
			Log.e("rid",rid+"");
			result="";
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

