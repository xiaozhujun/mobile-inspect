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
	Button scanTag;              //ɨ���ǩ��ť
	String result;                
	RadioGroup inspectResult;          //�Ҳ�ĵ�����б�
    RadioButton checkRadioButton;
	SelectAdapter selectAdapter;     //��Ӧ������listview�е�ÿһ����ʾ�Ҳ�����ѡ���������
	MyAdapter myadapter;             //��Ӧ������Listview��ÿһ�������������
	int cur_pos=0;               //��Ҫ�����жϵ�ǰ��position����ʹ��ǰ��listview�е�Item����
	String username=null;         //��ȡ�����Ա
	int uid=0;                    //��ȡ�����ԱID
    String filename=null;          //��ȡ������ͬ�ĵ������ֲ�ͬ�ĵ����
	ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
	boolean isInspect=false;            //�Ƿ��ܵ�� ,Ĭ��Ϊfalse,�����ɨ���ǩ֮��,��Ϊtrue
	String fileDir=null;                //ָ�����ļ���ŵ�λ�õĸ�Ŀ¼  /data/data/com.example.viewpager/files/
	TextView inspecttable;              //��ʾ����
	String tag;                         //ɨ���ǩʱ����ѯ����Ӧxml�ļ��е�<location>��ֵ
	ParseXml p=new ParseXml();           //���ý���xml�ļ�����
	boolean Inspect=false;                //
	List<String> ScanedTag=new ArrayList<String>();    //����������ɨ����ı�ǩ
	String tagflag;
	TextView user;
	int isScaned;
	private ExpandableListView inspectItem;
	private ArrayList<String> groupList;
	private ArrayList<List<String>> childList;
	private MyexpandableListAdapter adapter;
	String[] spinnerItem;                        //���������˵���ֵ
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
		//����ϵ�·��
		/*fileDir=Environment.getExternalStorageDirectory().toString()+"/inspectfiles";*/
		//ģ�����ϵ�
		fileDir=Environment.getExternalStorageDirectory().toString();
		int count = bundle.getInt("count");                   //����ScanCardActivity������countֵ
			if (count != 0) {                                  //�����ǵ�һ����ת�����ҳ�棬�������һ�߼���������ʾ�Ƚ��������֤
		    tname = bundle.getString("tbname");        
			filename=getFileNameByTableName(tname);                      //���ݲ�ͬ��tname���õ�filename
			username=bundle.getString("username");
			uid=bundle.getInt("uid");
			setContentView(R.layout.tagvalidate);                              //ʹ��tagvalidate.xml��Դ�ļ�
			//�����ǻ�ȡ��Ӧ����Դ
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
		                if(spinnerItem[index].equals("����")){
						Toast.makeText(getBaseContext(),   
		                   "�ļ���"+spinnerItem[index],   
		                    Toast.LENGTH_SHORT).show();      
						//�ڴ˽���username,uid,inspecttime,devicenumberд��xml�ļ�
						    writeToXmlUserDateDvnum(filename);
						
		                }else if(spinnerItem[index].equals("�˳�")){
		                	Toast.makeText(getBaseContext(),   
		 		                   "����!����"+spinnerItem[index],   
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
			//ɨ���ǩ�¼���
			  scanTag.setOnClickListener(new OnClickListener() {
						//�����ɨ���ǩʱ��
						public void onClick(View v) {
							  new AlertDialog.Builder(TagValidateActivity.this).setMessage("��ǩɨ�����!").show();
							   isInspect=true;   
				    if(isInspect){
				    	     //һˢ��ǩʱ������ɨ��Listview�е�item
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
				itemlist=p.queryItemFromXmlByTag(filename,"��������");
				Iterator it=itemlist.iterator();
				while(it.hasNext()){
				String item=(String) it.next();
				childTemp.add(item);
				}
			} else if (i == 1) {
				childTemp = new ArrayList<String>();
				itemlist=p.queryItemFromXmlByTag(filename,"ת������");
				Iterator it=itemlist.iterator();
				while(it.hasNext()){
				String item=(String) it.next();
				childTemp.add(item);
				}
			} else if(i==2){
				childTemp = new ArrayList<String>();
				itemlist=p.queryItemFromXmlByTag(filename,"˾��������");
				Iterator it=itemlist.iterator();
				while(it.hasNext()){
				String item=(String) it.next();
				childTemp.add(item);
			}
			}else{
					childTemp = new ArrayList<String>();
					itemlist=p.queryItemFromXmlByTag(filename,"�ۼ�����");
					Iterator it=itemlist.iterator();
					while(it.hasNext()){
					String item=(String) it.next();
					childTemp.add(item);	
				}
			}
			childList.add(childTemp);
		}		
	}
	//ģ�����ϵ�
	private String getFileNameByTableName(String tname) {
		if(tname.equals("������Ա����")){                     //�������ѡ��Ĳ�ͬ�ı�����������Ӧ�Ĳ�ͬ��filename
			filename=fileDir+"/jixiu.xml";
		}else if(tname.equals("�Ż��ӻ�е����Ա����")){
			filename=fileDir+"/jixie.xml";
		}else if(tname.equals("�Ż�����Ա�����ճ�����")){
			filename=fileDir+"/dianqi.xml";
		}else if(tname.equals("�Ż����ٻ�ר���쿨")){
			filename=fileDir+"/jiansuji.xml";
		}else if(tname.equals("�Ż�˾���ճ�����")){
			filename=fileDir+"/siji.xml";
		}else if(tname.equals("�Ż���һ����ר���쿨Ƭ")){
			filename=fileDir+"/zhouyidingbao.xml";
		}
		return filename;
	}
	//����ϵ�filename�͵��ڱ���
	/*private String getFileNameByTableName(String tname) {
		filename=tname;
		return filename;
	}
*/	
	public List<String> getLocation() {             //��ȡxml�ļ��е�<location>��<field>��ֵ
		List<String> list = new ArrayList<String>();
	    list = p.parseInspect(filename);		
		return list;
	}
	private void writeToXmlUserDateDvnum(String filename) {        //��username,uid,devnum,insepcttimeд��xml�ļ�
		String devnum=scanDevnum();
		Date d=new Date(System.currentTimeMillis());
		p.writeToXmlUserDateDvnum(filename,tname,username,uid,devnum,d);
		
	}   
	public void writeFormatXml(String pathname){         //��ָ����ʽ���ļ�д��
		p.writeToFormatXml(pathname);
	}
	public String scanTag(){                            //ɨ���ǩ
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
		    new AlertDialog.Builder(TagValidateActivity.this).setMessage("��ɨ���ǩ!").show();
		}
		if(isScaned==2){
			inspectItem.collapseGroup(groupPosition);
			new AlertDialog.Builder(TagValidateActivity.this).setMessage("����ɨ���ǩ����!").show();
			inspectResultPane.setVisibility(View.GONE);
		}
		return false;
	}
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		if(isInspect==false){
			new AlertDialog.Builder(TagValidateActivity.this).setMessage("��ɨ���ǩ!").show();
		}
		if(isScaned==2){
			new AlertDialog.Builder(TagValidateActivity.this).setMessage("����ɨ���ǩ����!").show();
		}
		return false;
}
}
