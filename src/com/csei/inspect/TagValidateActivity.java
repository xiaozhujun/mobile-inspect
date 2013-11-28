package com.csei.inspect;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.cesi.analysexml.ParseXml;
import com.csei.adapter.MyexpandableListAdapter;
import com.example.viewpager.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
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
public class TagValidateActivity extends Activity implements ExpandableListView.OnChildClickListener,ExpandableListView.OnGroupClickListener{
	Button scanTag;              //ɨ���ǩ��ť               
	RadioGroup inspectResult;          //�Ҳ�ĵ�����б�
    RadioButton checkRadioButton;
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
			inspecttable=(TextView) this.findViewById(R.id.tbname);
			inspecttable.setText(tname);
			scanTag = (Button) this.findViewById(R.id.scanTag);
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
			beizhu.setOnClickListener(new ClickEvent());
			String dnumber=scanDevnum();
			devnum.setText(dnumber);
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
			//ɨ���ǩ�¼���
			  scanTag.setOnClickListener(new OnClickListener() {
						//�����ɨ���ǩʱ��
						public void onClick(View v) {
							showalert.setText("��ǩɨ�����!");
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
											 showalert.setText("����������ɨ���ǩ!");
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
					});			       
		}
   
	}
	// �������¼�
		class ClickEvent implements OnClickListener {
			public void onClick(View v) {
				if (v == beizhu) {
					showRoundCornerDialog(TagValidateActivity.this, TagValidateActivity.this
							.findViewById(R.id.beizhu));
					
				}
			}
		}
		// ��ʾԲ�ǶԻ���
		public void showRoundCornerDialog(Context context, View parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// ��ȡԲ�ǶԻ��򲼾�View��������ΪԲ��
			final View dialogView = inflater.inflate(R.layout.popupwindow, null,
					false);
			dialogView.setBackgroundResource(R.drawable.rounded_corners_view);
			// ���������Ի������õ����Ի���ı���ΪԲ��
			final PopupWindow pw = new PopupWindow(dialogView,LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT, true);
			pw.setOutsideTouchable(true);
			pw.setAnimationStyle(R.style.PopupAnimation);
			/*pw.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.rounded_corners_pop));	*/
			// ע��������豳������Ϊ�ص㲿�֣���������ע�͵�����һ���������豳���������鿴�Ի���Ч��
			final EditText edtUsername = (EditText) dialogView
					.findViewById(R.id.username_edit);
			edtUsername.setHint("�����뱸ע..."); // ������ʾ��
			// OK��ť���䴦���¼�
			Button btnOK = (Button) dialogView.findViewById(R.id.BtnOK);
			btnOK.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// �����ı�������
					String comment=edtUsername.getText().toString();
					p.writeCommentToXml(filename, itemItem, comment,tag);
					pw.dismiss();
				}
			});
			// Cancel��ť���䴦���¼�
			Button btnCancel = (Button) dialogView.findViewById(R.id.BtnCancel);
			btnCancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					pw.dismiss();// �ر�
				}
			});
			// ��ʾRoundCorner�Ի���
			pw.showAtLocation(parent, Gravity.CENTER | Gravity.CENTER, 0, 0);
		}
	
	/**
	 * ����Pop״̬
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
			if(map.get(KEY).equals("����")){
				Toast.makeText(mContext,"�ļ���"+map.get(KEY)+"", Toast.LENGTH_SHORT).show();
				writeToXmlUserDateDvnum(filename);
			}else if(map.get(KEY).equals("�˳�")){
				Toast.makeText(mContext,"����!����"+map.get(KEY)+".......", Toast.LENGTH_SHORT).show();
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
		map.put(KEY, "����");
		items.add(map);
		map = new HashMap<String, Object>();
		map.put(KEY, "�˳�");
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
			showalert.setVisibility(View.VISIBLE);
			showalert.setText("��ɨ���ǩ!");
		}
		if(isScaned==2){
			inspectItem.collapseGroup(groupPosition);
			showalert.setVisibility(View.VISIBLE);
			showalert.setText("����ɨ���ǩ����!");
			inspectResultPane.setVisibility(View.GONE);
		}
		return false;
	}
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		  
		if(isInspect==false){
			showalert.setVisibility(View.VISIBLE);
			showalert.setText("��ɨ���ǩ!");
		}
		if(isScaned==2){
			showalert.setVisibility(View.VISIBLE);
			showalert.setText("����ɨ���ǩ����!");
		}
		return true;
}
}
