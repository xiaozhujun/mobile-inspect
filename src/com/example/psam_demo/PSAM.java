package com.example.psam_demo;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.util.Log;
/**
 * 
 * ����Ϊjni�㷽���ĵ���,�벻Ҫ�����޸�native����
 * */
public class PSAM {
	static{
		System.load("/data/data/com.example.test/lib/libdevapi.so"); //�������
		System.load("/data/data/com.example.test/lib/libPSAM.so");
	}
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;
	/*��Ҫ��mFd����������ΪmFd�ڹرյ�ʱ��Ҫ�õ�*/
	public PSAM(){}  //�޲ι��췽��
	public PSAM(int port,int baudrate) throws IOException{
		mFd = open(port, baudrate);   //�ļ����������ڴ���InputStream��OutputStream
		if (mFd == null) {
			Log.e("", "native open returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}
	//Getters and Setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}
	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}
	//JNI����
	private native static FileDescriptor open(int port, int baudrate);   //�򿪴���
	public native void close(int port);									 //�رմ���
	public native int PowerOn_HFPsam();									 //�����豸��Դ
	public native int PowerOff_HFPsam();	 							 //�ر��豸��Դ
	public native byte[] getversion();									 //��ȡ�豸�汾����
	public native byte[] resolveDataFromDevice(byte[] resourceData);	 //�������豸����������	
	/*====================M1��==================================*/
	public native byte[] rf_card();										  //Ѱ��ָ��
	public native byte[] rf_authentication_cmd(byte [] sectorAndpassword);//��ָ֤��  
	public native int rf_check_data(byte [] auth);						  //У�鷵�أ�����data��Ϊ�յķ���ֵ��
	public native byte[] rf_read_cmd(byte [] part);					      //����ֵ
//	public native byte[] resolve_read_data(byte[] data);				  //���������ݣ������δ�������ݣ�
	public native byte[] rf_write_cmd(byte [] value);					  //д��ֵ	
	/*====================PSAM��=================================*/
	public native byte[] sam_reset(byte []card);						  //�ϵ縴λ
	public native byte[] sam_send_cmd(byte []senddata);					  //����ָ�APDU��
	public native byte[] sam_shut_dowm(byte []card);					  //�µ�	
	/*=======================�ǽӴ�CPU��==========================*/
	public native byte[] ucpu_open();									  //���Ƭ
	public native byte[] ucpu_close();									  //�رտ�Ƭ
	public native byte[] ucpu_send_cmd(byte []data);					  //����ָ�APDU��
	/*=====================15693=================================*/
	public native byte[] ISO15693_Inventory();							  //15693Ѱ��ָ��
	public native byte[] ISO15693_Select(byte []uid);					  //ѡ��
	public native byte[] ISO15693_Read(byte []area, byte []value);  	  //������
	public native byte[] ISO15693_Write(byte []area, byte []value, byte []data);		  //д����
	/*======================��Ƶ���غ���=============================*/
	public native byte[] open_rf();										 //������Ƶ
	public native byte[] close_rf();									 //�ر���Ƶ
}
