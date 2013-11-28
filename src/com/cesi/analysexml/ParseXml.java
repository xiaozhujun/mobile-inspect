package com.cesi.analysexml;
import android.annotation.SuppressLint;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
public class ParseXml {
	@SuppressWarnings("unchecked")
	public List<DbModel> parseRolesTable(String filename){    
		//�˷�����Ҫ���ڽ�ѡ�е�RolesTablexml�ļ����н���
		SAXReader saxReader = new SAXReader();
		List<DbModel> list=new ArrayList<DbModel>();
        try {  
           Document document = saxReader.read(new File(filename));  
           Element root = document.getRootElement();  
           List<Element> elements = root.elements();  
           Iterator<Element> it = elements.iterator();
              while(it.hasNext()) {  
               Element e = it.next();  
               System.out.println(e.getName() + " : " + e.attribute("name").getValue() + " -- " + e.attribute("roleNum").getValue());  
               List<Element> group = e.elements();  
               Iterator<Element> git = group.iterator();  
               //���еĹ�����ʵ���ǲ������Ĺ���  
               while(git.hasNext()) {  
                   Element ge = git.next();  
                   //ͨ��ʹ��e.attribute(" ").getValue()������Ե�ֵ  
                   System.out.println(ge.getName() + " : " + ge.attribute("name").getValue());
                   String tableitem=ge.attribute("name").getValue();
                   DbModel d=new DbModel();
                   d.setTableitem(tableitem);
                   list.add(d);
               }  
           } 
         } catch (DocumentException e) {  
           e.printStackTrace();  
       }  
        return list;
   } 
	@SuppressWarnings("unchecked")
	public List<String> parseInspect(String filename){    
		//�˷�����Ҫ���ڽ�ѡ�е�RolesTablexml�ļ����н���
	        List<String> list=new ArrayList<String>();			
			String tag = null;
			String item = null;
			SAXReader saxReader = new SAXReader();
			try {	
				System.out.println(filename+"�ļ�·��.....");
				Document document = saxReader.read(new File(filename));
				Element root = document.getRootElement();	           
				Element e1 = root.element("devicetype");	    
				List<Element> e2 = e1.elements();
				Iterator<Element> it2 = e2.iterator();
				while (it2.hasNext()) {					
					Element e5 = it2.next();
					System.out.println(e5.getName() + ":"
							+ e5.attribute("name").getValue());
					tag = e5.attribute("name").getValue();
					list.add(tag);
					List<Element> elements = e5.elements();
					Iterator<Element> it = elements.iterator();
					while (it.hasNext()) {
						Element e = it.next();
						item = e.attribute("name").getValue();
						list.add(item);
					}	
				}      
			} catch (DocumentException e) {
				e.printStackTrace();
			}     
              return list;
   } 	
	@SuppressWarnings("unchecked")
	public List<DbModel> parseEmployers(String filename){          
		//������Ա����
		SAXReader saxReader = new SAXReader();
		List<DbModel> list=new ArrayList<DbModel>();
        try {  
           Document document = saxReader.read(new File(filename));  
           Element root = document.getRootElement();  
           List<Element> elements = root.elements();  
           Iterator<Element> it = elements.iterator();  
           while(it.hasNext()) {  
               Element e = it.next();  
               System.out.println(e.getName());  
               List<Element> employers=e.elements();
               Iterator<Element> em=employers.iterator();
               DbModel d=new DbModel();
               while(em.hasNext()){
            	   Element Subem=em.next();
            	   System.out.println(Subem.getName()+":"+Subem.getText());
            	   if(Subem.getName()=="cardType"){
            		   String cardtype=Subem.getText();
            		   d.setCardType(cardtype);
            	   }else if(Subem.getName()=="role"){
            		   String role=Subem.getText();
            		   d.setRolename(role);	   
            	   }else if(Subem.getName()=="roleNum"){
            		   String roleid=Subem.getText();
            		   d.setRoleid(Integer.parseInt(roleid));
            	   }else if(Subem.getName()=="name"){
            		   String uname=Subem.getText();
            		   d.setUsername(uname);
            	   }else if(Subem.getName()=="number"){
            		   String uid=Subem.getText();
            		   d.setUid(Integer.parseInt(uid));
            	   } 
               }
               list.add(d);
           }  
       } catch (DocumentException e) {  
           e.printStackTrace();  
       }
		return list;  
	}
	
    @SuppressWarnings("unchecked")
	public void updateInspectXml(String filename,String itrel,String val){    
    	//ÿ�ε�ѡ��ֵʱ��������Լ�ѡ���ֵд��inspect.xml��   
    	System.out.println(filename+"::"+itrel+"::"+val);
			String item = null;
			SAXReader saxReader = new SAXReader();
			try {
				Document document = saxReader.read(new File(filename));
				Element root = document.getRootElement();	           				        			  
				Element e1 = root.element("devicetype"); 
	            List<Element> e2 = e1.elements();
				Iterator<Element> it2 = e2.iterator();
				while (it2.hasNext()) {
					Element e5 = it2.next();
					List<Element> elements = e5.elements();
					Iterator<Element> it = elements.iterator();
					while (it.hasNext()) {
						Element e = it.next();
						item = e.attribute("name").getValue();
						List<Element> group = e.elements();
						Iterator<Element> git = group.iterator();
					    if(item.equals(itrel)){
						while (git.hasNext()) {
							Element ge = git.next();						
						    ge.attribute("name").setValue(val);							
						}	
					}
					}
				}
				try{
	    			OutputFormat format=OutputFormat.createPrettyPrint();
	    			String ENCODING="UTF-8";
	    			format.setEncoding(ENCODING);
	    			format.setNewlines(true);
	    			XMLWriter writer=new XMLWriter(new java.io.FileOutputStream(filename),format);
	    			writer.write(document);
	    			writer.close();	
	    	}catch(Exception e)
	    	{
	    		e.printStackTrace();
	    	}
				
			  }catch (DocumentException e) {
				e.printStackTrace();
			}  
			
    }
    public void writeToInspectXml(String pathname){      
    	//�����д��xml�ļ�
    		/*SAXReader reader=new SAXReader();*/
    		Document document=DocumentHelper.createDocument();
    		Element root=document.addElement("check").addAttribute("inspecttype", "");
    		Element devicetype=root.addElement("devicetype").addAttribute("name", "�Ż�");
    		Element location=devicetype.addElement("location").addAttribute("name", "");
    		Element field=location.addElement("field");
    		field.addAttribute("name", "");
    		field.addAttribute("isInput", "");
    		field.addAttribute("description", "");
    		field.addAttribute("unit", "");
    		field.addElement("value").addAttribute("name", "");
    		try{
    			OutputFormat format=OutputFormat.createPrettyPrint();
    			String ENCODING="UTF-8";
    			format.setEncoding(ENCODING);
    			format.setNewlines(true);
    			XMLWriter writer=new XMLWriter(new java.io.FileOutputStream(pathname),format);
    			writer.write(document);
    			writer.close();	
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    }
    @SuppressWarnings("unchecked")
	public void writeToFormatXml(String filename) {       
    	//���̶���ʽ��xml�ļ�д��
    	SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(new File(filename));
			Element root = document.getRootElement();
			Element e1 = root.element("devicetype");
			List<Element> e2 = e1.elements();
			Iterator<Element> it2 = e2.iterator();
			while (it2.hasNext()) {				
				Element e5 = it2.next();						
				List<Element> elements = e5.elements();
				Iterator<Element> it = elements.iterator();
				while (it.hasNext()) {
					Element e = it.next();					
			        	List<Element> group = e.elements();
						Iterator<Element> git = group.iterator();
						while (git.hasNext()) {
							Element ge = git.next();
							e.remove(ge);														
			        }
						e.addElement("value").addAttribute("name", "����");	
				}
			}
			try{
    			OutputFormat format=OutputFormat.createPrettyPrint();
    			String ENCODING="UTF-8";
    			format.setEncoding(ENCODING);
    			format.setNewlines(true);
    			XMLWriter writer=new XMLWriter(new java.io.FileOutputStream(filename),format);
    			writer.write(document);
    			writer.close();	
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
            
		  }catch (DocumentException e) {
			e.printStackTrace();
		}
	}
    
    @SuppressWarnings("unchecked")
	public  String getValueFromXmlByItem(String filename,String itrvalue){
    	//ͨ�����������ȡֵ
		String item = null;
		String value = null;
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(new File(filename));
			Element root = document.getRootElement();	           
			Element e1 = root.element("devicetype"); 
            List<Element> e2 = e1.elements();
			Iterator<Element> it2 = e2.iterator();
			while (it2.hasNext()) {
				Element e5 = it2.next();
				List<Element> elements = e5.elements();
				Iterator<Element> it = elements.iterator();
				while (it.hasNext()) {
					Element e = it.next();
					item = e.attribute("name").getValue();
					List<Element> group = e.elements();
					Iterator<Element> git = group.iterator();
				    if(item.equals(itrvalue)){
						Element ge = git.next();
						value=ge.attribute("name").getValue();							 							
				}
				}
			}
		  }catch (DocumentException e) {
			e.printStackTrace();
		}  
    	return value;	
    }
   
	/*public String scanTag(String filename) {
		//ɨ���ǩ
		String tag = null;
		SAXReader saxReader = new SAXReader();
		try {	
			Document document = saxReader.read(new File(filename));
			Element root = document.getRootElement();	           
			Element e1 = root.element("tag");	    
			List<Element> e2 = e1.elements();
			Iterator<Element> it2 = e2.iterator();
			while (it2.hasNext()) {
				Element e5 = it2.next();
				
				tag = e5.attribute("name").getValue();		    
			}
		}catch(Exception e){
			e.printStackTrace();
		}	
          return tag;
	}*/
    @SuppressWarnings("unchecked")
	public String scanTag(String filename) {
		//ɨ���ǩ,��tag.xmlȡ��
		String devnum = null;
		SAXReader saxReader = new SAXReader();
		try {	
			Document document = saxReader.read(new File(filename));
			Element root = document.getRootElement();	           
			Element e1 = root.element("tag");	    
			List<Element> e2 = e1.elements();
			Iterator<Element> it2 = e2.iterator();
			while (it2.hasNext()) {
				Element e5 = it2.next();
				System.out.println(e5.getName());
				if(e5.getName().equals("tagArea")){
					devnum=e5.getText();
				}
				//tag = e5.attribute("name").getValue();		    
			}
		}catch(Exception e){
			e.printStackTrace();
		}	
          return devnum;
	}
	@SuppressWarnings("unchecked")
	public String scanDevnum(String filename) {
		//ɨ���ǩ����tag.xml��ȡ���豸���
		String devnum = null;
		SAXReader saxReader = new SAXReader();
		try {	
			Document document = saxReader.read(new File(filename));
			Element root = document.getRootElement();	           
			Element e1 = root.element("tag");	    
			List<Element> e2 = e1.elements();
			Iterator<Element> it2 = e2.iterator();
			while (it2.hasNext()) {
				Element e5 = it2.next();
				System.out.println(e5.getName());
				if(e5.getName().equals("deviceNum")){
					devnum=e5.getText();
				}
				//tag = e5.attribute("name").getValue();		    
			}
		}catch(Exception e){
			e.printStackTrace();
		}	
          return devnum;
	}
    @SuppressWarnings("unchecked")
	public List<String> queryItemFromXmlByTag(String filename,String loc){  
    	//���������ѯ�����
        List<String> list=new ArrayList<String>();
		String tag = null;
		String item = null;
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(new File(filename));
			Element root = document.getRootElement();	                     
			Element e1 = root.element("devicetype"); 
            List<Element> e2 = e1.elements();
			Iterator<Element> it2 = e2.iterator();
			while (it2.hasNext()) {
				Element e5 = it2.next();
				tag = e5.attribute("name").getValue();
				if(tag.equals(loc)){
				List<Element> elements = e5.elements();
				Iterator<Element> it = elements.iterator();
				while (it.hasNext()) {
					Element e = it.next();
					item = e.attribute("name").getValue();
                    list.add(item);
				}
				}
			}
             }catch (DocumentException e) {
			e.printStackTrace();
		}  
    	     return list;	
    }
	@SuppressWarnings("rawtypes")
	public boolean judgeItemIsBelong(String filename,String tag, String item) {
		// �жϵ�����Ƿ�����ĳ�����򣬱�ǩ
		     boolean flag=false;
		     List<String> itemlist=queryItemFromXmlByTag(filename, tag);
		     Iterator it=itemlist.iterator();
		     while(it.hasNext()){
		    	 String insitem=(String) it.next();
		    	 if(item.equals(insitem)){		    	
		    		 flag=true;
		    	 }
		     }
		       return flag;
	}
	@SuppressWarnings("unchecked")
	public List<String> queryLocationFromXml(String filename) {
		// TODO Auto-generated method stub
		List<String> list=new ArrayList<String>();
		SAXReader saxReader = new SAXReader();
		try {	
			Document document = saxReader.read(new File(filename));
			Element root = document.getRootElement();	           
			Element e1 = root.element("devicetype");	    
			List<Element> e2 = e1.elements();
			Iterator<Element> it2 = e2.iterator();
			while (it2.hasNext()) {
				Element e5 = it2.next();
				String tag = e5.attribute("name").getValue();
				list.add(tag);
			}
		}catch(Exception e){
			e.printStackTrace();
		}	 
		return list;
	}
	public void writeToXmlUserDateDvnum(String filename,String dev,String username,int uid,String devnumber,Date inspecttime) {
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(new File(filename));
			Element root = document.getRootElement();
			root.attribute("inspecttype").setValue(dev);
			root.attribute("inspecttime").setValue(transformDateToString(inspecttime));
			root.attribute("worker").setValue(username);
			root.attribute("workernumber").setValue(String.valueOf(uid));
			root.attribute("devicenumber").setValue(devnumber);
			try{
    			OutputFormat format=OutputFormat.createPrettyPrint();
    			String ENCODING="UTF-8";
    			format.setEncoding(ENCODING);
    			format.setNewlines(true);
    			XMLWriter writer=new XMLWriter(new java.io.FileOutputStream(filename),format);
    			writer.write(document);
    			writer.close();	
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
			
		  }catch (DocumentException e) {
			e.printStackTrace();
		}  
		
	}
	@SuppressLint("SimpleDateFormat")
	public String transformDateToString(Date d){
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String s=format.format(d);
		return s;
	}
}
	
