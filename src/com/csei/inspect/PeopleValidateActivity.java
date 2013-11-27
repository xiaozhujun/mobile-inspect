package com.csei.inspect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import com.cesi.analysexml.DbModel;
import com.cesi.analysexml.ParseXml;
import com.csei.adapter.MyAdapter;
import com.example.viewpager.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
@SuppressLint("HandlerLeak")
public class PeopleValidateActivity extends Activity{
	Button read;// ��ť
	String str = "", result = "";
	ListView rolestablelist;
	int index = 0;// �ַ�����
	int count = 0;// ��ť������
	MyRunnable myRunnable;
	Handler handler;
	Thread thread;
	int tag = 0;// ��ǩ
	ProgressBar pb;
	TextView textview;
	String fileDir;
	String filename;
	TextView showalert;
	int cur_pos=0;
	MyAdapter myadapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.peoplevalidate);
	  fileDir=Environment.getExternalStorageDirectory().toString();
	  read = (Button) findViewById(R.id.read);
	  rolestablelist = (ListView) findViewById(R.id.rolestablelist);
	  pb = (ProgressBar) findViewById(R.id.pb);
	  textview = (TextView) findViewById(R.id.textview);
	  showalert=(TextView) findViewById(R.id.showalert);
	  getFile();
	  // ���õõ��ļ��ķ���
	  pb.setMax(result.length());// ���������ֵ��Ϊ���µĳ���
	  read.setOnClickListener(new Button.OnClickListener() {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			 count++;
			    System.out.println("count=" + count);
			    if (count % 2 == 1) {
			     myRunnable = new MyRunnable();
			     thread = new Thread(myRunnable);
			     thread.start();
			     // �����߳�
			     tag = 0;
			     read.setText("��ͣɨ��");
			    }
			    if (count % 2 == 0) {
			     tag = 1;
			     read.setText("��ʼɨ��");
			    }
		}
	  });
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
	     textview.setText("��ǰ����:" + index + "/" + result.length());
	     textview.setTextSize(15);
	     // �����ȡ�����������¶�ȡ
	    if (index == result.length()) {
	       index = 0;	     
	       showalert.setText("�ļ�ɨ�����!");      
	       rolestablelist.setAdapter(listItemAdapter);	       
	       rolestablelist.setOnItemClickListener(new OnItemClickListener() {
				@SuppressWarnings({ "rawtypes", "unchecked" })
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
					List<DbModel> employerlist=getEmployers();
					Iterator it=employerlist.iterator();
					String username=null;
					int uid=0;
					while(it.hasNext()){
						DbModel d=(DbModel) it.next();
						username=d.getUsername();
						uid=d.getUid();	
					}
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
	// �õ��ļ����ݵķ���������һ���ַ���
	@SuppressWarnings("rawtypes")
	public String getFile() {          //��ȡ��Ա�����Ϣ
		ParseXml p=new ParseXml();
		filename=fileDir+"/RolesTable.xml";
			List<DbModel> list=p.parseRolesTable(filename);
		    Iterator it=list.iterator();
		    while(it.hasNext()){
		    	DbModel d=(DbModel) it.next();
		    	result+=d.getTableitem()+",";
		    }
		return result;
	}
	public List<DbModel> getEmployers(){     //��ȡ��Ա������Ϣ
		ParseXml p=new ParseXml();
		List<DbModel> list=new ArrayList<DbModel>();
		filename=fileDir+"/Employer.xml";
	    list=p.parseEmployers(filename);
		return list;
	}
	}

