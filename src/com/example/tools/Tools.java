package com.example.tools;

public class Tools {

	//byte תʮ������
		public static String Bytes2HexString(byte[] b, int size) {
		    String ret = "";
		    for (int i = 0; i < size; i++) {
		      String hex = Integer.toHexString(b[i] & 0xFF);
		      if (hex.length() == 1) {
		        hex = "0" + hex;
		      }
		      ret += hex.toUpperCase();
		    }
		    return ret;
		  }
		
		public static byte uniteBytes(byte src0, byte src1) {
		    byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
		    _b0 = (byte)(_b0 << 4);
		    byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
		    byte ret = (byte)(_b0 ^ _b1);
		    return ret;
		  }
		
		//ʮ������תbyte
		public static byte[] HexString2Bytes(String src) {
			int len = src.length() / 2;
			byte[] ret = new byte[len];
			byte[] tmp = src.getBytes();

			for (int i = 0; i < len; i++) {
				ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
			}
			return ret;
		}
		//ʮ������ת��
		public static String TransformFromHex(String s){
			 String y = "";
		        char[] baKeyword = new char[s.length() / 4];
		        for (int i = 0; i < baKeyword.length; i++) {
		            try {
		                baKeyword[i] = (char)Integer.parseInt(s.substring(i * 4, i * 4 + 4), 16);
		                y+=baKeyword[i];
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		        }
				return y;
		}
		
		/**
		 * ������յ����ݳ���
		 * @param dataLen �������ݵĳ���
		 * @param data ����
		 * @return
		 */
		public static boolean checkData(String dataLen, String data){
			int length = Integer.parseInt(dataLen, 16);
			if(length == (data.length()/2 - 5)){
				return true;
			}
			return false;
		}
		
		 public static String deUnicode(String content){//��16������ת��Ϊ����
			  String enUnicode=null;
			  String deUnicode=null;
			  for(int i=0;i<content.length();i++){
			      if(enUnicode==null){
			       enUnicode=String.valueOf(content.charAt(i));
			      }else{
			       enUnicode=enUnicode+content.charAt(i);
			      }
			      if(i%4==3){
			       if(enUnicode!=null){
			        if(deUnicode==null){
			         deUnicode=String.valueOf((char)Integer.valueOf(enUnicode, 16).intValue());
			        }else{
			         deUnicode=deUnicode+String.valueOf((char)Integer.valueOf(enUnicode, 16).intValue());
			        }
			       }
			       enUnicode=null;
			      }
			      
			     }
			  return deUnicode;
			 }
			 public static String enUnicode(String content){//������ת��Ϊ16������
			  String enUnicode=null;
			  for(int i=0;i<content.length();i++){
			   if(i==0){
			       enUnicode=getHexString(Integer.toHexString(content.charAt(i)).toUpperCase());
			      }else{   	  
				   enUnicode=enUnicode+getHexString(Integer.toHexString(content.charAt(i)).toUpperCase());
					  }
				 }
			  return enUnicode;
			 }
			 private static String getHexString(String hexString){
			      String hexStr="";
			      for(int i=hexString.length();i<4;i++){
			       if(i==hexString.length())
			        hexStr="0";
			       else
			        hexStr=hexStr+"0";
			      }
			      return hexStr+hexString;
			 }	
		public static void main(String[] args) {
			String aa = "02000a00000000000000";
			String len = aa.substring(2, 6);
			System.out.println(checkData(len, aa));
		}
		
		
}
