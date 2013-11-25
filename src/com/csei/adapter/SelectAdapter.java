package com.csei.adapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import com.cesi.analysexml.ParseXml;
import com.example.viewpager.R;
@SuppressLint("UseSparseArrays")
public class SelectAdapter extends BaseAdapter {
	Context context;
	ArrayList<HashMap<String, Object>> listData;	
	//记录checkbox的状态
	public HashMap<Integer, Boolean> state = new HashMap<Integer, Boolean>();		
	ParseXml p=new ParseXml();
	String filename;
	//构造函数
	public SelectAdapter(Context context,ArrayList<HashMap<String, Object>> listData,String filename) {
		this.context = context;
		this.listData = listData;
		this.filename=filename;
	}

	public int getCount() {
		return listData.size();
	}

	public Object getItem(int position) {
		return listData.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	// 重写View
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater mInflater = LayoutInflater.from(context);
		convertView = mInflater.inflate(R.layout.inspect, null);
		TextView resultitem = (TextView) convertView.findViewById(R.id.ItemResult);
		System.out.println(position+"++++++++++");
		for(int i=0;i<listData.size();i++){
		System.out.println((String) listData.get(i).get("ItemResult")+"-------------");
		resultitem.setText((String) listData.get(i).get("ItemResult"));
		}
	    final CheckBox check = (CheckBox) convertView.findViewById(R.id.item_cb);
		/*final CheckBox check=(CheckBox) listData.get(position).get("ck");*/
	    String value=p.getValueFromXmlByItem(filename, (String)listData.get(position).get("ItemText"));
        String showck=(String) listData.get(position).get("showck");
        if(showck.equals("show")){
	    if(((String) listData.get(position).get("ItemResult")).equals(value)){
			System.out.println(check.isChecked());
	        state.put(position, true);
		}		
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				 //将信息更新
				 String item=(String)listData.get(position).get("ItemText");
				 String value=(String) listData.get(position).get("ItemResult");
				 p.updateInspectXml(filename,item, value);
				 int t=buttonView.getId();
				if (isChecked) {
					state.put(position, isChecked);
					
					/*for(int i=0;i<listData.size();i++){
				    	if(i!=position){
				    		System.out.println(i+"not postion");
				    		state.put(i, false);	
				    	}
				      
					}*/
					System.out.println("已选的"+position);
					//check.setChecked(true);
				} else {
					/*state.remove(position);	*/
					state.put(position, false);
					System.out.println("未选的"+position);
					/*check.setChecked(false);*/
				}
				/*SelectAdapter.this.notifyDataSetChanged();*/
			}			
		});		
		check.setChecked((state.get(position) == null ? false : true));
		/*check.setChecked(state.get(position));*/
        }else if(showck.equals("hide")){
        	check.setVisibility(View.GONE);
        }
		
		return convertView;
	}
}