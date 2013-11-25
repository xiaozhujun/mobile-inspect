package com.csei.inspect;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import com.cesi.analysexml.ParseXml;
import com.csei.adapter.MyAdapter;
import com.csei.adapter.MyexpandableListAdapter;
import com.csei.adapter.SelectAdapter;
import com.example.viewpager.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
public class TagValidateActivity extends Activity implements ExpandableListView.OnChildClickListener,ExpandableListView.OnGroupClickListener{
	Button scanTag;              //扫描标签按钮
	String result;                
	RadioGroup inspectResult;          //右侧的点检结果列表
    RadioButton checkRadioButton;
	SelectAdapter selectAdapter;     //响应点击左侧listview中的每一项显示右侧点检结果选项的适配器
	MyAdapter myadapter;             //响应点击左侧Listview后每一项高亮的适配器
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
	String[] spinnerItem;                        //保存下拉菜单的值
	String tname;
	View inspectResultPane;
	RadioButton normal;
	RadioButton abnormal;
	RadioButton nothing;
	String itemItem;	
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
			inspecttable=(TextView) this.findViewById(R.id.devnum);
			inspecttable.setText(tname);
			scanTag = (Button) this.findViewById(R.id.scanTag);
			inspectItem =  (ExpandableListView) this.findViewById(R.id.inspectItem);
			inspectResult = (RadioGroup) this.findViewById(R.id.insepctResult);
			checkRadioButton=(RadioButton) this.findViewById(inspectResult.getCheckedRadioButtonId());
			inspectResultPane=this.findViewById(R.id.inspectResultPane);
			normal=(RadioButton) this.findViewById(R.id.normal);
			abnormal=(RadioButton) this.findViewById(R.id.abnormal);
			nothing=(RadioButton) this.findViewById(R.id.nothing);
			user=(TextView) this.findViewById(R.id.username);
			user.setText(username);
			spinnerItem=getResources().getStringArray(R.array.spinner_array);
			 Spinner s1 = (Spinner) findViewById(R.id.savemenu);	
			 ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,spinnerItem);
			 s1.setAdapter(spinnerAdapter);  
		        s1.setOnItemSelectedListener(new OnItemSelectedListener()  
		        {  
		            public void onNothingSelected(AdapterView<?> arg0) {}

					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						int index = arg0.getSelectedItemPosition();  
		                if(spinnerItem[index].equals("保存")){
						Toast.makeText(getBaseContext(),   
		                   "文件已"+spinnerItem[index],   
		                    Toast.LENGTH_SHORT).show();      
						//在此将把username,uid,inspecttime,devicenumber写入xml文件
						    writeToXmlUserDateDvnum(filename);
						
		                }else if(spinnerItem[index].equals("退出")){
		                	Toast.makeText(getBaseContext(),   
		 		                   "您好!正在"+spinnerItem[index],   
		 		                    Toast.LENGTH_SHORT).show(); 
		                	System.exit(0);
		                }
	 				}
		        });          
			InitData();
			adapter = new MyexpandableListAdapter(TagValidateActivity.this,groupList,childList);
			inspectItem.setAdapter(adapter);
			inspectItem.setOnChildClickListener(this);
			inspectItem.setOnGroupClickListener(this);
			//扫描标签事件绑定
			  scanTag.setOnClickListener(new OnClickListener() {
						//当点击扫描标签时，
						public void onClick(View v) {
							  new AlertDialog.Builder(TagValidateActivity.this).setMessage("标签扫描完毕!").show();
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
										String value=p.getValueFromXmlByItem(filename, itemItem);										
										inspectResultPane.setVisibility(View.VISIBLE);
										if(normal.getText().equals(value)){
											normal.setChecked(true);
										}else if(abnormal.getText().equals(value)){
											abnormal.setChecked(true);
										}else if(nothing.getText().equals(value)){
											nothing.setChecked(true);
										}				
										
									     return false;
									}
								});
					    		   inspectResult.setOnCheckedChangeListener(new OnCheckedChangeListener() {
										public void onCheckedChanged(RadioGroup group, int checkedId) {
											// TODO Auto-generated method stub
											checkRadioButton=(RadioButton) inspectResult.findViewById(checkedId);						
											String v=(String) checkRadioButton.getText();
											p.updateInspectXml(filename, itemItem, v);
										}
									});
					    		   }
					    		   }else{
					    			   isScaned=2;
					    		   }
					    	   
					    	   }				        					    	  
				    }
						}
					});			       
		}
   
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
		String devnum=scanDevnum();
		Date d=new Date(System.currentTimeMillis());
		p.writeToXmlUserDateDvnum(filename,tname,username,uid,devnum,d);
		
	}   
	public void writeFormatXml(String pathname){         //将指定格式的文件写入
		p.writeToFormatXml(pathname);
	}
	public String scanTag(){                            //扫描标签
		String tag=null;
		String filename=fileDir+"/scanTag.xml";
		tag=p.scanTag(filename);
		return tag;
	}
	public String scanDevnum(){
		String devnum=null;
		String filename=fileDir+"/scanTag.xml";
		devnum=p.scanDevnum(filename);
		return devnum;
	}
	public List<String> queryLocationFromXml(){
		List<String> list=p.queryLocationFromXml(filename);
		return list;
	}
	public boolean onGroupClick(final ExpandableListView parent, final View v,
			int groupPosition, final long id) {
		if(isInspect==false){
		    new AlertDialog.Builder(TagValidateActivity.this).setMessage("请扫描标签!").show();
		}
		if(isScaned==2){
			inspectItem.collapseGroup(groupPosition);
			new AlertDialog.Builder(TagValidateActivity.this).setMessage("与所扫描标签不符!").show();
			inspectResultPane.setVisibility(View.GONE);
		}
		return false;
	}
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		if(isInspect==false){
			new AlertDialog.Builder(TagValidateActivity.this).setMessage("请扫描标签!").show();
		}
		if(isScaned==2){
			new AlertDialog.Builder(TagValidateActivity.this).setMessage("与所扫描标签不符!").show();
		}
		return false;
}
}
