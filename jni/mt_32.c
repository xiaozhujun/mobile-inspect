#include "string.h"
#include "mt_32.h"

#include <stdio.h>
#include <time.h>
#include<signal.h>
#include "devapi.h"
#include "jni.h"
#include "android/log.h"
static const char *TAG="serial_port";
#define LOG_TAG "System.out.c"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
//#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
//#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
//#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

#define   TEST_ERROR					0X01



//*************************Command **************************
#define   CMD_DELAY                     0x55
//Device
#define   CMDH_DEVICE					0x00		//�豸������λ
#define   CMDL_VERSION					0x01		//��ȡӲ���汾��
#define   CMDL_HALT						0x45		//rf_halt  

#define		CMDL_CLOSERF				0x00		//�ر���Ƶ
#define		CMDL_OPENRF					0x01		//������Ƶ

//CPU Card
#define   CMDH_RFCPU					0xC1		//�ǽ�CPU��
#define   CMDH_CPU						0xC0		//�Ӵ�ʽCPU��
#define   CMDH_SAM1						0xC2		//SAM1��
#define   CMDH_SAM2						0xC3		//SAM2��
#define   CMDL_OPENCARD					0x30		//�ǽ�CPU�� �򿪿�Ƭ
#define   CMDL_RESET					0x30		//��λ
#define   CMDL_APDU						0x31		//APDU
#define   CMDL_DOWN						0x32		//�µ�
#define   CMDL_DESELE					0x33		//rf_desele

//M1 Card
#define   CMDH_M1						0xC1		//M1��
#define   MT_OKH						0x00		//�������óɹ���λ
#define   MT_OKL						0x00		//�������óɹ���λ

#define   CMDL_RFRESET					0x4E		//��Ƶ��λ
#define   CMDL_CARD						0x40		//Ѱ��
#define   CMDL_AUTH						0x5F		//��֤
#define   CMDL_READ						0x46		//������
#define   CMDL_WRITE					0x47		//д����
#define   CMDL_INC						0x48		//��ֵ
#define   CMDL_DEC						0x49		//��ֵ
#define   CMDL_RESTORE					0x4A		//�ش�
#define   CMDL_TRANSFER					0x4B		//����
#define   CMDL_RFHALT  					0x45		//���ÿ�Ƭ״̬Ϊhalt

//15693
#define   CMDL_15693INV					0x60		//���Ѱ��
#define   CMDL_15693SEL					0x61		//ѡ��
#define   CMDL_15693Read				0x62		//����
#define   CMDL_15693Write				0x63		//д��
#define   CMDL_15693RTR					0x64		//��λ��׼��״̬
#define   CMDL_15693STQ					0x65		//���þ�Ĭ
#define   CMDL_15693GSI					0x66		//ȡ��Ƭϵͳ��Ϣ

//*************************Command**************************
//************************* Error code ***********************//
#define ERR_DATAFORMAT					-0x13		//����ֵ��Χ����
#define ERR_UNDEFINE_CARD               -0x14       //δʶ������
#define ERR_NO_CARD						-0x12
#define ERR_RFREAD						-0x31		//
#define ERR_OVER						-0x32		//
#define ERR_LESS						-0x33		//

#define ERR_CLRDATA						-0x34		//��������ʧ��
#define ERR_OPENCARD					-0X35		//�ǽӴ�CPU����δѰ��
#define ERR_WRITECOM					-0X36       //д���ڴ���
#define FALSE							-1

int bOpenCard=0;						//�ǽ�CPU����0--δѰ����1--��Ѱ��
unsigned char nCardResetLen=0;			//�ǽ�CPU����λ��Ϣ����
unsigned char sCardResetData[100]={0};	//�ǽ�CPU����λ��Ϣ

//************************************************************//

//connect to the device
typedef int HANDLE;
typedef unsigned int DWORD;
//************************ ͨ��Э��İ�ͷ��β****************//
#define OP_OK      					0
#define STX							0x02
#define ETX							0x03

//************************ ͨ��Э���Error code *************//
#define ERR_UNDEFINED_HANDLE		-0x21
#define ERR_STX						-0x22
#define ERR_ETX						-0x23
#define ERR_BCC						-0x24
#define ERR_GENERAL					-0x25


//*********************java����*******************************//
/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    open
 * Signature: (II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_com_zhouzhi_es_jni_CardJNI_open
  (JNIEnv *env, jclass thiz, jint port, jint baudrate)
{
	int fd;
	jobject mFileDescriptor;

	fd = h900_uart_open(port,baudrate);

	if(fd == -1){
//		LOGE("Cannot open port");
		return NULL;
	}

	{
			jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
			jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
			jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
			mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
			(*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint)fd);
	}

	return mFileDescriptor;
}

/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    close
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_zhouzhi_es_jni_CardJNI_close
  (JNIEnv *env, jobject thiz, jint port)
{
		jclass CardJNIClass = (*env)->GetObjectClass(env, thiz);
		jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

		jfieldID mFdID = (*env)->GetFieldID(env, CardJNIClass, "mFd", "Ljava/io/FileDescriptor;");
		jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");

		jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
		jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

	//	LOGD("close(fd = %d)", descriptor);
		h900_uart_close(port, descriptor);
}


/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    getversion
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_zhouzhi_es_jni_CardJNI_getversion
  (JNIEnv *env, jobject obj){

	unsigned char buffer[512];
	unsigned char length;

	get_version(&length,buffer);
	jbyteArray array = (*env)->NewByteArray(env,length);
	(*env)->SetByteArrayRegion(env,array,0,length,buffer);

	return array;
}

/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    PowerOn_HFPsam
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_zhouzhi_es_jni_CardJNI_PowerOn_1HFPsam
  (JNIEnv *env, jobject thiz)
{
	return h900_psam_power_on();
}

/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    PowerOff_HFPsam
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_zhouzhi_es_jni_CardJNI_PowerOff_1HFPsam
  (JNIEnv *env, jobject thiz)
{
	return h900_psam_power_off();
}

/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    resolveDataFromDevice
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_zhouzhi_es_jni_CardJNI_resolveDataFromDevice
  (JNIEnv *env, jobject thiz, jbyteArray arr)
{
	unsigned char buffer[512];
	unsigned char rlen ;
	int st;
	jbyte* bytebuffer = (*env)->GetByteArrayElements(env,arr,0);
	jsize len = (*env)->GetArrayLength(env,arr);

	st = resolve_data_mt3(bytebuffer,&rlen,buffer);
	if(st != 1) return NULL;
	jbyteArray array = (*env)->NewByteArray(env,rlen);
	(*env)->SetByteArrayRegion(env,array,0,rlen,buffer);
	return array;
}


/**************************M1��************************************************/
/******************************************************************************/
/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    rf_card
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_zhouzhi_es_jni_CardJNI_rf_1card
  (JNIEnv *env, jobject thiz)
{
	unsigned char buffer[512];
	unsigned char length;
	unsigned char mode = 0x01; //ѡ��ģʽ1��ȫ����Ƭ���ɶ�ȡ
	rf_card(mode,buffer);
	jbyteArray array = (*env)->NewByteArray(env,9);  //����ĳ���Ϊ9
	(*env)->SetByteArrayRegion(env,array,0,9,buffer);

	return array;
}

/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    rf_authentication_cmd
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_zhouzhi_es_jni_CardJNI_rf_1authentication_1cmd
  (JNIEnv *env, jobject thiz, jbyteArray res)
{
	unsigned char buffer[512];
	unsigned char length;
	jbyteArray res_buffer = (*env)->GetByteArrayElements(env,res,0);
	jsize len = (*env)->GetArrayLength(env,res);
	rf_authentication_key(res_buffer,&length,buffer);
	jbyteArray receive_array = (*env)->NewByteArray(env,16);
	(*env)->SetByteArrayRegion(env,receive_array,0,16,buffer);

	return receive_array;
}

/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    rf_check_data
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_com_zhouzhi_es_jni_CardJNI_rf_1check_1data
  (JNIEnv *env, jobject thiz, jbyteArray authdata)
{
	jbyteArray buffer = (*env)->GetByteArrayElements(env,authdata,0);
	jsize len = (*env)->GetArrayLength(env,authdata);
	if(len < 6) return -1;
	return check_data(buffer);
}

/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    rf_read_cmd
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_zhouzhi_es_jni_CardJNI_rf_1read_1cmd
  (JNIEnv *env, jobject thiz, jbyteArray part)
{
	unsigned char buffer[512];
	int st = 0;
	jbyteArray part_buffer = (*env)->GetByteArrayElements(env,part,0);
	st = rf_read(part_buffer,buffer);
	if(st != 0) return NULL;
	jbyteArray receive = (*env)->NewByteArray(env,9);
	(*env)->SetByteArrayRegion(env,receive,0, 9, buffer);
	return receive;
}

/*��������������
 * Class:     com_example_psam_demo_PSAM
 * Method:    resolve_read_data
 * Signature: ([B)[B
 */
//JNIEXPORT jbyteArray JNICALL Java_com_example_psam_1demo_PSAM_resolve_1read_1data
//  (JNIEnv *env, jobject thiz, jbyteArray data)
//{
//	unsigned char buffer[512];
//	int st = 0;
//	int mLen = 0;
//	jbyteArray data_buffer = (*env)->GetByteArrayElements(env,data,0);
//	jsize len = (*env)->GetArrayLength(env,data);
//	st = resolve_read_data(len, data_buffer,&mLen,buffer);
//	if(st != 0) return NULL;
//
//	jbyteArray receive = (*env)->NewByteArray(env,mLen);
//	(*env)->SetByteArrayRegion(env,receive,0,mLen,buffer);
//
//	return receive;
//}

/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    rf_write_cmd
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_zhouzhi_es_jni_CardJNI_rf_1write_1cmd
  (JNIEnv *env, jobject thiz, jbyteArray value)
{
	unsigned char buffer[512];
	int rlen;
	int st = 0;
	jbyteArray value_buffer = (*env)->GetByteArrayElements(env,value,0);
	st = rf_write(value_buffer, &rlen, buffer);
	if(st != 0) return NULL;
	jbyteArray receive = (*env)->NewByteArray(env,rlen);
	(*env)->SetByteArrayRegion(env,receive,0,rlen,buffer);
	return receive;
}


//****************************��Ƶ����*****************************//
/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    open_rf
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_zhouzhi_es_jni_CardJNI_open_1rf
  (JNIEnv *env, jobject thiz)
{
	unsigned char buffer[512];
	unsigned char len;
	int st = 0;
	st =OPEN_RF(&len, buffer);
	if(st != 0) return NULL;
	jbyteArray receive = (*env)->NewByteArray(env,len);
	(*env)->SetByteArrayRegion(env, receive, 0, len, buffer);
	return receive;
}

/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    close_rf
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_zhouzhi_es_jni_CardJNI_close_1rf
  (JNIEnv *env, jobject thiz)
{
	unsigned char buffer[512];
	unsigned char len;
	int st = 0;
	st = CLOSE_RF(&len, buffer);
	if (st != 0)
		return NULL;
	jbyteArray receive = (*env)->NewByteArray(env, len);
	(*env)->SetByteArrayRegion(env, receive, 0, len, buffer);
	return receive;
}

/*===================�¶�ģ��=======================*/
/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    i2cinit
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_zhouzhi_es_jni_CardJNI_i2cinit
(JNIEnv *env, jobject thiz, jint udelay)
{
	int fd;
	h900_ex3v3_power_on();
	fd = mc90_smbus_gpio_init(158, 159, udelay);
	return fd;
}


/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    i2cclose
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_zhouzhi_es_jni_CardJNI_i2cclose
	(JNIEnv *env, jobject thiz, jint fd)
{
	mc90_smbus_gpio_close(fd);
	h900_ex3v3_power_off();
}

int read_ir(int fd, unsigned char* irdat)
{
	unsigned short IRtemp;
	IRtemp=mc90_smbus_gpio_read_reg(fd, 0x00, 0x07);//IRtemp=MemRead(0x00,0x07);
//	LOGE("i2creadreg111,fd=%d,IRtemp=%d", fd, IRtemp);
	IRtemp=IRtemp*2;
	if(IRtemp>=27315)
	{
		IRtemp=IRtemp-27315;
	}
	else
	{
		IRtemp=0;
	}

	irdat[0]=IRtemp/10000+0x30;
	IRtemp=IRtemp%10000;

	irdat[1]=IRtemp/1000+0x30;
	IRtemp=IRtemp%1000;

	irdat[2]=IRtemp/100+0x30;
	IRtemp=IRtemp%100;
//	LOGE("i2creadreg222,IRtemp=%d", IRtemp);
	return IRtemp;
}
unsigned short g_IRtemp=0;

/*
 * Class:     com_zhouzhi_es_jni_CardJNI
 * Method:    i2creadreg
 * Signature: (I)S
 */
JNIEXPORT jshort JNICALL Java_com_zhouzhi_es_jni_CardJNI_i2creadreg
(JNIEnv *env, jobject thiz, jint fd)
{
	unsigned char ret[2];
	unsigned short res;
	unsigned long int DATA=0;
	unsigned int *mahm;
  unsigned char irdat[3];

	unsigned short IRtemp;
	IRtemp=mc90_smbus_gpio_read_reg(fd, 0x00, 0x07);//IRtemp=MemRead(0x00,0x07);
//	LOGE("i2creadreg111,fd=%d,IRtemp=%d", fd, IRtemp);
	if(IRtemp == 65535)
	{
		return(g_IRtemp);
	}
	IRtemp=IRtemp*2;
	if(IRtemp>=27315)
	{
		IRtemp=IRtemp-27315;
	}
	else
	{
		IRtemp=0;
	}
//	LOGE("i2creadreg222,IRtemp=%d", IRtemp);
	g_IRtemp = IRtemp;
	return(IRtemp);

}

//************************************************************//
//************************************************************//


//����mc90_uart_close()
int CloseHandle(HANDLE a)
{

	return mc90_uart_close(a,0);
}


//����mc90_uart_open()
int open_device_com(unsigned int nPort,unsigned int uBaud)
{

	return mc90_uart_open(nPort,uBaud);
}


//**********************δʵ�ֵķ���***************************//
int receive_data(HANDLE handle ,unsigned char* arg0){

	return 0;
}

int SetTimeOuts(int x, int y){  //�������Ҫ��android��ʵ��
	return 0;
}



int WaitCommEvent(HANDLE x,DWORD* env,int y){
	return 0;
}

int WriteFile(HANDLE x,unsigned char* arg0,int y,DWORD* env,int z){  //д�ļ�����Ҳ����android��ʵ��
	return 0;
}

DWORD GetTickCount(){  //
	return 0;
}

//****************************************************************//

//δʵ�ֵķ���,���ó�ʱ
void set_timeouts(unsigned long msTotal,unsigned long msMulti)
{
#if 0
	struct timerval tick;
	 int ret = 0 ;
	 //sigroutine;
	 signal(SIGALRM, sigroutine);
//	 systime_idx = idx;


//	 tick.it_value.tv_sec = 10;  //ʮ���Ӻ�������ʱ��
	 tick.it_value.tv_usec =100*1000;//100����
	 tick.it_interval.tv_sec  =1; //��ʱ��������ÿ��1�뽫ִ����Ӧ�ĺ���
	 tick.it_interval.tv_usec = 0;

	 ret = setitimer(ITIMER_REAL , &tick, NULL);//ITIMER_REAL
	 if ( ret != 0)
	 {
	  //DEBUG("TIMER ERROR");
	 }
#endif //0

}


/*=====================================================
mt3Э���·�һ������
����slen: �·���ԭʼ���ݳ��ȣ�cmd1,cmd2,delay,data[n]
����sendcmd���·����ݻ���ָ�룬�·������ݰ���2�ֽ����1�ֽ�delay��n�ֽ�����
����rlen���������ݳ���
����receivedata���������ݻ���ָ��
==============================================*/
int send_cmd_mt3(int slen,unsigned char* sendcmd,int* rlen,unsigned char* receivedata)
{
	unsigned char send_buffer[512]={0};
	unsigned char receive_buffer[512]={0};
	unsigned char nLRC=0;	//LRC
	int nRecLen=0;	 		//�������ݳ���
	int st=0;
	unsigned int  cRecLen=sizeof(receive_buffer)-1;
	memset(send_buffer,0,512);
	memset(receive_buffer,0,512);

	*rlen=slen+5;

	send_buffer[0]=STX;
	send_buffer[1]=slen/256;
	send_buffer[2]=slen%256;
	

	//cmddata
	memcpy(&send_buffer[3],sendcmd,slen);
	//BCC
	nLRC=cr_bcc(slen, sendcmd);	
	send_buffer[slen+3]=nLRC;

	//ETX
	send_buffer[slen+4]=ETX;	//���˴��һ��
	memcpy(receivedata, send_buffer, slen+5);  //�ѷ�װ�õ����ݰ�����receivedata
	*receivedata=STX;
	return(OP_OK);
}


/*���豸���ص����ݰ��н���������
 * ����len�� ���ݰ�����
 * ����resourceData�����ݰ�
 * ����rlen: �������������ݳ���
 * ����receive_buffer����������������
 * */
int resolve_data_mt3(unsigned char *resourceData, int *recvlen, unsigned char *receive_buffer)
{
	int st = 0;
	int len;
	if(*resourceData != STX) return -1;
	len = (*(resourceData+1))*256 + *(resourceData + 2);
	*recvlen = len - 3;
	if(*(resourceData+3) == 0x00 && *(resourceData+4) == 0x00)
	{
		memcpy(receive_buffer,(resourceData + 6),len - 3);
	}else{
		return -1;
	}

	return 1;
}


//�Ӵ��ڽ�������,��ʼ�����ݴ����ݰ��н����
int receive_com_mt3(HANDLE icdev,int* len,unsigned char *receive_buffer)
{
	int st=0;
	int i=0,j=0;
	unsigned char data_buffer[65530];
	unsigned char bcc=0;
	int rlen=0,datalen=0;

	//�ڴ��ʼ��
	memset(data_buffer, 0, 65530);

	st=receive_data(icdev, &data_buffer[0]);
	if(st!=OP_OK)	return(st);    //����STX����ʱʱ��ΪulTotalTimeOuts ms
	if(data_buffer[0]!=STX)	return(ERR_STX);

	//�������ݳ���
	st=receive_data(icdev, &data_buffer[1]);
	if(st!=OP_OK)	return(st);
	st=receive_data(icdev, &data_buffer[2]);
	if(st!=OP_OK)	return(st);

	rlen=data_buffer[1]*256+data_buffer[2]+5;
	datalen= rlen;
	* len=datalen;

	//��������  DATA+CRC+ETX
	for(i=3;i<rlen;i++){
		st=receive_data(icdev, &data_buffer[i]);
		if(st!=OP_OK)	return(st);
	}
	
	//��ֵ
	memcpy(receive_buffer,data_buffer,rlen);

	//�ж����ݺ�����
	if(receive_buffer[datalen-1]!=ETX)
		return ERR_ETX;
	bcc=ck_bcc(datalen-4, &receive_buffer[3]);      
	if(bcc!=0)  
		return (ERR_BCC);
	
	return(OP_OK);
	
}

//�������ͣ����ͳ�ȥ
int cr_bcc(int len, unsigned char *bcc_buffer)
{	int temp=0, i;
	for(i=0;i<len;i++)
		temp=temp^bcc_buffer[i];
	return(temp);
}


//����ʱ���������ͣ�У�������Ƿ���ȷ
int ck_bcc(int len, unsigned char *bcc_buffer)
{
	int temp=0, i;
	for(i=0;i<len;i++)
		temp=temp^bcc_buffer[i];
	if(temp==0)
		return(OP_OK);
	else
		return(ERR_BCC);
}

//************************************************************//

//************************************************************//
HANDLE open_device(unsigned int nPort,unsigned int ulBaud)
{
	int st=0;
	HANDLE hDev=0;
	unsigned char nVerLen=0;	 //the length of the version infomation
	unsigned char sVerData[80];  //version infomation
	memset(sVerData,0,80);
	
	hDev=open_device_com(nPort,ulBaud);

	return hDev;
}
	

//disconnect from the device
HANDLE close_device(HANDLE icdev)
{
	int st=0;
	if(icdev != 0)
	{
		st=CloseHandle(icdev); 
		if(st>0)
			return 0;
		else
			return -1;
	}
	return -1;
}

//get version infomation
int get_version(unsigned char *nVerLen,unsigned char *sVerData)
{
	int st=0;
	int nRLen=0;
	unsigned char send_buffer[5];
	memset(send_buffer,0,5);
	unsigned char receive_buffer[100];
	memset(receive_buffer,0,100);

	*nVerLen=0;
	send_buffer[0]=CMDH_DEVICE;
	send_buffer[1]=CMDL_VERSION;
	send_buffer[2]=0;		//delay
    
//	set_timeouts(100, 0);
	st=send_cmd_mt3(3,send_buffer,&nRLen,receive_buffer);
	if(st==0)
	{
		*nVerLen=nRLen;
		memcpy(sVerData,receive_buffer,nRLen);
	}
//	set_timeouts(5000, 0);
	return st;
}
//hexתasc
int  hex_asc(unsigned char *sHex,unsigned char *sAsc,unsigned long ulLength)
{
	unsigned char hLowbit,hHighbit;
	unsigned long i;
	
	for(i=0;i<ulLength*2;i+=2)
	{
		hLowbit=sHex[i/2]&0x0f;
		hHighbit=sHex[i/2]/16;
		if (hHighbit>=10) 
			sAsc[i]=hHighbit+'7';
		else 
			sAsc[i]=hHighbit+'0';
		if (hLowbit>=10) 
			sAsc[i+1]=hLowbit+'7';
		else 
			sAsc[i+1]=hLowbit+'0';
	}
	sAsc[ulLength*2]='\0';
	
	return(OP_OK);
}

//ascתhex
int asc_hex(unsigned char *sAsc,unsigned char *sHex,unsigned long ulLength)
{
	unsigned long i;
	unsigned char aLowbit,aHighbit;
	unsigned char hconval,lconval;
	
	for(i=0;i<ulLength*2;i+=2)
	{
		aHighbit=toupper(sAsc[i]);	//��ת���ɴ�д��
		if ((aHighbit>='A')&&(aHighbit<='F')) 
			hconval='7';
		else
			if ((aHighbit>='0')&&(aHighbit<='9')) 
				hconval='0';
			else  
				return -1;

		aLowbit=toupper(sAsc[i+1]);
		if ((aLowbit>='A')&&(aLowbit<='F')) 
			lconval='7';
		else
			if ((aLowbit>='0')&&(aLowbit<='9')) 
				lconval='0';
			else 
				return -1;

		sHex[(i/2)]=((aHighbit-hconval)*16+(aLowbit-lconval));
	}
	sHex[ulLength]=0x00;
	return 0;
}

//******************************* Contactless CPU card *****************************************//
int OpenCard(unsigned char *rLen, unsigned char *receive)
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
//	unsigned char receive_buffer[100];
//	memset(receive_buffer,0,100);
	int nRLen=0;
//	unsigned char nCardType=0,nSnrLen=0;
	int st=0;

//	*nCardInfoLen=0;			//AtrLen

	send_buffer[0]=CMDH_RFCPU;
	send_buffer[1]=CMDL_OPENCARD;
	send_buffer[2]=0x00;
	send_buffer[3]=0x01; //Ѱ��ģʽΪ����״̬�¶�����Ѱ

    st=send_cmd_mt3(4,send_buffer,&nRLen,receive);
    *rLen = nRLen;
//	if(st==(OP_OK))
//	{
//		nCardType=receive_buffer[0];		//Type
//		nSnrLen=receive_buffer[1];			//SnrLen
//		for(i=0;i<nSnrLen;i++)
//			sSnr[i]=receive_buffer[i+2];
//
//		*nCardInfoLen=receive_buffer[nSnrLen+2];	//AtrLen
//		for (i=0;i<*nCardInfoLen;i++)
//			sCardInfo[i]=receive_buffer[nSnrLen+3+i];		//receive_buffer[i+6]

//	}
	return st;
}


int Open_Card(unsigned char *sSnr)
{
	int st=0;
	unsigned char nRLen=0;
	unsigned char receive_buffer[100];
	memset(receive_buffer,0,100);

	nCardResetLen=0;
	memset(sCardResetData,0,100);
	st=OpenCard(&nRLen, receive_buffer);
	if(st==0)
	{
		nCardResetLen=nRLen;
		memcpy(sCardResetData,receive_buffer,nRLen);
		bOpenCard=1;
	}
	return st;
}

int Reset_Card(unsigned char *sCardInfo, unsigned char *nCardInfoLen)
{
	int st=0;

	if(bOpenCard!=1)	//δѰ��
	{
		st=ERR_OPENCARD;
	}
	else
	{
		*nCardInfoLen=nCardResetLen;
		memcpy(sCardInfo,sCardResetData,nCardResetLen);
	}
	return st;
}

int ExchangePro(unsigned char *sCmd, unsigned char nCmdLen, unsigned char *sResp, unsigned char *nRespLen)
{
	unsigned char send_buffer[300];
	memset(send_buffer,0,300);
	unsigned char receive_buffer[300];
	memset(receive_buffer,0,300);
	int nRLen=0;
	int st=0;

	if(nCmdLen <1)
		return ERR_DATAFORMAT;
	*nRespLen=0;		

	send_buffer[0]=CMDH_RFCPU;
	send_buffer[1]=CMDL_APDU;
	send_buffer[2]=0x00;
	
	memcpy(&send_buffer[3],sCmd,nCmdLen);

    st=send_cmd_mt3( 3+nCmdLen,send_buffer,&nRLen,receive_buffer);
	if(st==(OP_OK))
	{
		memcpy(sResp,receive_buffer,nRLen);
		*nRespLen=nRLen;
	}

	return st;
}


int CloseCard(unsigned char *rlen, unsigned char *receive_buffer)
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	int nRLen=0;
	int st=0;

	send_buffer[0]=CMDH_RFCPU;
	send_buffer[1]=CMDL_DOWN;
	send_buffer[2]=0x00;

    st=send_cmd_mt3( 3, send_buffer,&nRLen, receive_buffer);
    *rlen = nRLen;

	return st;
}

//******************************* Contact CPU card and PSAM card ********************************//
int ICC_Reset( unsigned char *nCardSet, unsigned char *sAtr, unsigned char *nAtrLen)
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[100];
	memset(receive_buffer,0,100);
	int nRLen=0;
	int st=0;

	*nAtrLen=0;		

	if((*nCardSet<0)||(*nCardSet>4))
		return ERR_DATAFORMAT;
	switch(*nCardSet)
	{
		case 1:
				send_buffer[0]=CMDH_SAM1;
				break;
		case 2:
				send_buffer[0]=CMDH_SAM2;
				break;
	}
	send_buffer[1]=CMDL_RESET;
	send_buffer[2]=0x01;
	
    st=send_cmd_mt3(3, send_buffer,&nRLen, receive_buffer);
	if(st==(OP_OK))
	{
		memcpy(sAtr,receive_buffer,nRLen);
		*nAtrLen=nRLen;
	}

	return st;
}

int ICC_PowerOn(HANDLE icdev, unsigned char nCardSet, unsigned char *sAtr, unsigned char *nAtrLen)
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[100];
	memset(receive_buffer,0,100);
	int nRLen=0;
	int st=0;

	*nAtrLen=0;		

	if((nCardSet<0)||(nCardSet>4))
		return ERR_DATAFORMAT;
	switch(nCardSet)
	{
		case 0:
				send_buffer[0]=CMDH_CPU;
				break;
		case 1:
				send_buffer[0]=CMDH_SAM1;
				break;
		case 2:
				send_buffer[0]=CMDH_SAM2;
				break;
	}
	send_buffer[1]=CMDL_RESET;
	send_buffer[2]=0x00;
	
    st=send_cmd_mt3(3, send_buffer,&nRLen, receive_buffer);
	if(st==(OP_OK))
	{
		memcpy(sAtr,receive_buffer,nRLen);
		*nAtrLen=nRLen;
	}

	return st;
}


int ICC_CommandExchange(unsigned char *sCmd, unsigned char nCmdLen, unsigned char *sResp, unsigned char *nRespLen)
{
	unsigned char send_buffer[256];
	memset(send_buffer,0,256);
	unsigned char receive_buffer[512];
	memset(receive_buffer,0,512);
	int nRLen=0;
	int st=0;

	*nRespLen=0;		

	if(nCmdLen <1)
		return ERR_DATAFORMAT;
	switch(*sCmd)
	{

		case 1:
				send_buffer[0]=CMDH_SAM1;
				break;
		case 2:
				send_buffer[0]=CMDH_SAM2;
				break;
	}
	send_buffer[1]=CMDL_APDU;
	send_buffer[2]=0X00;
	
	memcpy(&send_buffer[3],sCmd + 1,nCmdLen - 1);

    st=send_cmd_mt3( 2+nCmdLen,send_buffer,&nRLen,receive_buffer);
	if(st==(OP_OK))
	{
		memcpy(sResp,receive_buffer,nRLen);
		*nRespLen=nRLen;
	}

	return st;
}

int ICC_PowerOff(unsigned char *nCardSet, unsigned char *resCmd, unsigned char *resLen)
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[60];
	memset(receive_buffer,0,60);
	int nRLen=0;
	int st=0;		

	switch(*nCardSet)
	{
		case 1:
				send_buffer[0]=CMDH_SAM1;
				break;
		case 2:
				send_buffer[0]=CMDH_SAM2;
				break;
	}
	send_buffer[1]=CMDL_DOWN;
	send_buffer[2]=0x00;

    st=send_cmd_mt3(3, send_buffer,&nRLen,receive_buffer);
    if(st == OP_OK)
    {
    	memcpy(resCmd,receive_buffer,nRLen);
    	*resLen = nRLen;
    }

	return st;
}

//******************************* M1 card ********************************************************//

//���鷵������
int check_data(unsigned char* data)
{
	if(*data != STX) return -1;
	if(*(data+1) != 0x00) return -1;
	if(*(data+2) != 0x03) return -1;
	if(*(data+3) != MT_OKH) return -1;
	if(*(data+3) != MT_OKL) return -1;
	return OP_OK;
}

//reset
int rf_reset(HANDLE icdev)
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[100];
	memset(receive_buffer,0,100);
	int nRLen=0;
	int st=0;

	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_RFRESET;
	send_buffer[2]=0x00;
	send_buffer[3]=0x01;		//T1,
	send_buffer[4]=0x01;		//T2,��Ƶ�ȴ�ʱ��:t1*256+t2,

    st=send_cmd_mt3( 3,send_buffer,&nRLen,receive_buffer);
	
	return st;
}

//Ѱ��
int rf_card(unsigned char nMode,unsigned char *sSnr)
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[100];
	memset(receive_buffer,0,100);
	int nRLen=0;
	int st=0;

	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_CARD;
	send_buffer[2]=0x00;
	send_buffer[3]=nMode;

    st=send_cmd_mt3( 4,send_buffer,&nRLen,receive_buffer);
	if(st==(OP_OK))
	{
		memcpy(sSnr,receive_buffer,nRLen);
	}

   return st;
}

//��֤
int rf_authentication_key (unsigned char *sNkey, unsigned char* len, unsigned char* receiveCMD)
{
	unsigned char send_buffer[17]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[60];
	memset(receive_buffer,0,60);
	int nRLen=0;
	int st=0;

	send_buffer[0] = CMDH_M1;
	send_buffer[1] = CMDL_AUTH;
	send_buffer[2] = 0x00;
	send_buffer[3] = *sNkey;
	send_buffer[4] = *(sNkey+1);
	
	memcpy(&send_buffer[5],sNkey+2,6);

    st=send_cmd_mt3(11, send_buffer,&nRLen,receive_buffer);
    *len = nRLen;
    memcpy(receiveCMD,receive_buffer,nRLen);

    return st;
}
 //������
int rf_read (unsigned char *nAdr, unsigned char *sReadData)
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[100];
	memset(receive_buffer,0,100);
	int nRLen=0;
	int st=0;

	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_READ;
	send_buffer[2]=0x00;
	send_buffer[3]=*nAdr;

    st=send_cmd_mt3(4, send_buffer, &nRLen, receive_buffer);
	if(st==(OP_OK))
	{
		memcpy(sReadData,receive_buffer,nRLen);
	}
	
	return st;
}

//����������������
int resolve_read_data( int len, unsigned char *srcdata,int *rlen, unsigned char *recdata )
{

	if(*srcdata == STX && *(srcdata + len - 1) != ETX ){
		if(*(srcdata+1) != 0x00) return -1;
		if(*(srcdata+3) != 0x00) return -1;
		if(*(srcdata+4) != 0x00) return -1;
		memcpy(recdata,(srcdata+6),6);
		*rlen = 6;
		return 0;
	}else if(*(srcdata + len - 1) == ETX){
		memcpy(recdata,srcdata,len - 1);
		*rlen = len - 1;
		return 0;
	}
	return -1;
}

//д����
int rf_write (unsigned char *sWriteData,int *rlen, unsigned char *receive)
{
	unsigned char send_buffer[60];
	memset(send_buffer,0,60);
	unsigned char receive_buffer[100];
	memset(receive_buffer,0,100);
	int nRLen=0;
	int st=0;

	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_WRITE;
	send_buffer[2]=0x00;
	send_buffer[3]=*sWriteData;
	
	memcpy(&send_buffer[4],(sWriteData+1),16);

    st=send_cmd_mt3(20, send_buffer,&nRLen, receive_buffer);
    *rlen = nRLen;
    memcpy(receive,receive_buffer,nRLen);
	
    return st;
}

int rf_initval(HANDLE icdev,unsigned char nAdr,unsigned long ulValue)
{
	unsigned char send_buffer[100];
	memset(send_buffer,0,100);
	int st=0;

	memcpy(send_buffer,&ulValue,4);
// 	for(i = 0; i < 4; i ++)
	// 		data[i] = (_Value >> (8*i));
	send_buffer[4]  = ~send_buffer[0];
	send_buffer[5]  = ~send_buffer[1];
	send_buffer[6]  = ~send_buffer[2];
	send_buffer[7]  = ~send_buffer[3];
	send_buffer[8]  = send_buffer[0];
	send_buffer[9]  = send_buffer[1];
	send_buffer[10] = send_buffer[2];
	send_buffer[11] = send_buffer[3];
	send_buffer[12] = nAdr;
	send_buffer[13] = ~send_buffer[12];
	send_buffer[14] = send_buffer[12];
    send_buffer[15] = send_buffer[13];

//	st=rf_write(nAdr,send_buffer);

	return st;
}

int rf_readval(HANDLE icdev,unsigned char nAdr,unsigned long *ulValue)
{
	unsigned char receive_buffer[40];
	memset(receive_buffer,0,40);
	unsigned char i;
	int st=0;

	st=rf_read(&nAdr,receive_buffer);
	if (st) 
		return ERR_RFREAD;

    for(i=0;i<4;i++)
	{
		if (receive_buffer[i]==receive_buffer[i+4])
			return ERR_DATAFORMAT;
	}

	for(i=0;i<4;i++)
	{
		if (receive_buffer[i]!=receive_buffer[i+8]) 
			return ERR_DATAFORMAT;
	}
	if (receive_buffer[12]!=receive_buffer[14]) 
		return ERR_DATAFORMAT;

	if (receive_buffer[12]==receive_buffer[13]) 
		return ERR_DATAFORMAT;

	if (receive_buffer[13]!=receive_buffer[15]) 
		return ERR_DATAFORMAT;
	memcpy(ulValue,receive_buffer,4);

	return st;

}


int rf_increment(HANDLE icdev,unsigned char nAdr,unsigned long ulValue)
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[60];
	memset(receive_buffer,0,60);
	int nRLen=0;
	unsigned long nCurValue=0;
	int st=0;

	if (rf_readval(icdev,nAdr,&nCurValue)!=0) 
		return ERR_RFREAD;
	long lngWriteVal = nCurValue;
	lngWriteVal += ulValue;
    if (lngWriteVal > 2147483647) 
		return ERR_OVER;	

	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_INC;
	send_buffer[2]=0x00;
	send_buffer[3]=nAdr;
	
	memcpy(&send_buffer[4],&ulValue,4);

    st=send_cmd_mt3( 8,send_buffer,&nRLen, receive_buffer);
	if(st==(OP_OK))
	{
		st=rf_transfer(icdev,nAdr);
	}

	return st;

}

int rf_decrement(HANDLE icdev,unsigned char nAdr,unsigned long ulValue)
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[60];
	memset(receive_buffer,0,60);
	int nRLen=0;
	unsigned long nCurValue=0;
	int st=0;

	if (rf_readval(icdev,nAdr,&nCurValue)!=0) 
		return ERR_RFREAD;
    if (nCurValue<ulValue) 
		return ERR_LESS;

	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_DEC;
	send_buffer[2]=0x00;
	send_buffer[3]=nAdr;
	
	memcpy(&send_buffer[4],&ulValue,4);

    st=send_cmd_mt3(8, send_buffer,&nRLen, receive_buffer);
	if(st==(OP_OK))
	{
		st=rf_transfer(icdev,nAdr);
	}

	return st;
}

int rf_transfer(HANDLE icdev,unsigned char nAdr)
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[60];
	memset(receive_buffer,0,60);
	int nRLen=0;
	int st=0;

	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_TRANSFER;
	send_buffer[2]=0x00;
	send_buffer[3]=nAdr;

    st=send_cmd_mt3(4, send_buffer,&nRLen, receive_buffer);

    return st;
}

int rf_terminal(HANDLE icdev)
{
	unsigned char send_buffer[22]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[60];
	memset(receive_buffer,0,60);
	int nRLen=0;
	int st=0;
	
	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_RFHALT;
	send_buffer[2]=0x00;

    st=send_cmd_mt3(3,send_buffer,&nRLen, receive_buffer);

    return st;
}

int rf_restore(HANDLE icdev,unsigned char nAdr)
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[60];
	memset(receive_buffer,0,60);
	int nRLen=0;
	int st=0;

	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_RESTORE;
	send_buffer[2]=0x00;
	send_buffer[3]=nAdr;

    st=send_cmd_mt3( 4,send_buffer,&nRLen, receive_buffer);

    return st;
}

int  rf_desele(HANDLE icdev)

{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[60];
	memset(receive_buffer,0,60);
	int nRLen=0;
	int st=0;

	send_buffer[0]=CMDH_RFCPU;
	send_buffer[1]=CMDL_DESELE;
	send_buffer[2]=0x00;

    st=send_cmd_mt3( 3,send_buffer,&nRLen, receive_buffer);

	return st;
  
}
int  rf_halt(HANDLE icdev)

{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[60];
	memset(receive_buffer,0,60);
	int nRLen=0;
	int st=0;

	send_buffer[0]=CMDH_RFCPU;
	send_buffer[1]=CMDL_HALT;
	send_buffer[2]=0x00;

    st=send_cmd_mt3( 3,send_buffer,&nRLen, receive_buffer);

    return st;
}





////////////////////////////////////////////////////////////////////////////////////LCD��ʾ


int rf_read_hex( HANDLE icdev,unsigned char nAdr,unsigned char *sReadData )
{
	unsigned char send_buffer[10]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[100];
	memset(receive_buffer,0,100);
	int nRLen=0;
	int st=0;
	
	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_READ;
	send_buffer[2]=0x00;
	send_buffer[3]=nAdr;
	
    st=send_cmd_mt3(4, send_buffer, &nRLen, receive_buffer);
	if(st==(OP_OK))
	{
		hex_asc(receive_buffer,sReadData,nRLen);
	}
	
	return st;
}

int rf_write_hex( HANDLE icdev,unsigned char nAdr,unsigned char *sWriteData )
{
	unsigned char send_buffer[60];
	memset(send_buffer,0,60);
	unsigned char receive_buffer[100];
	memset(receive_buffer,0,100);
	unsigned char write_buffer[100];
	memset(write_buffer,0,100);
	int nRLen=0;
	int st=0;
	
	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_WRITE;
	send_buffer[2]=0x00;
	send_buffer[3]=nAdr;
	
	asc_hex(sWriteData,write_buffer,16);
	memcpy(&send_buffer[4],write_buffer,16);
	
    st=send_cmd_mt3(20, send_buffer,&nRLen, receive_buffer);
	
    return st;
}


/**********************************15693*************************************/

/****************************************************************************
��������:Ѱ15693��Ƭ
���:	
		flags		��־
		afi			ѡ���Ӧ��
		masklengh	ʱ϶
		uid			Ҫѡ�Ŀ�Ƭ
����:
		resplen		�������ݳ���
		resp		�������ݵ�ַ
BY:		
		LSQ			20130813
****************************************************************************/
int ISO15693_rf_Inventory(unsigned char *resplen,unsigned char *resp)
{
	unsigned char send_buffer[20]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[100];
	unsigned char uid[8] = {0,0,0,0,0,0,0,0,};
	memset(receive_buffer,0,100);
	int nRLen=0;
	int st=0;
	
	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_15693INV;
	send_buffer[2]=0x00;
//	send_buffer[3]=flags;//0x26
//	send_buffer[4]=afi;//0x00
//	send_buffer[5]=masklengh;//0x00
	send_buffer[3]=0x26;
	send_buffer[4]=0x00;
	send_buffer[5]=0x00;
	memcpy(&send_buffer[6], uid, 8);//0x00*8
	
    st=send_cmd_mt3(14,send_buffer,&nRLen,receive_buffer);
	if(st==(OP_OK))
	{
		*resplen=nRLen;
		memcpy(resp,receive_buffer,*resplen);
	}	
	return st;
}
/****************************************************************************
��������:ѡ��15693��Ƭ
���:	
		flags	��־
		uid		Ҫѡ�Ŀ�Ƭ
����:
		resplen	�������ݳ���
		resp    �������ݵ�ַ
BY:		
		LSQ			20130813
****************************************************************************/
int ISO15693_Select(unsigned char *uid, unsigned char *resplen, unsigned char *resp)
{
	unsigned char send_buffer[20]={0,0,0,0,0,0,0,0,0,0};
	unsigned char receive_buffer[100];
	memset(receive_buffer,0,100);
	int nRLen=0;
	int st=0;
	
	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_15693SEL;
	send_buffer[2]=0x00;
//	send_buffer[3]=flags;//0x22
	send_buffer[3]=0x22;
	memcpy(&send_buffer[4], uid, 8);//ȡInventory�õ���UID

    st=send_cmd_mt3(12,send_buffer,&nRLen,receive_buffer);
	if(st==(OP_OK))
	{
		*resplen=nRLen;
		memcpy(resp,receive_buffer,*resplen);
	}
	return st;
}
/****************************************************************************
��������:��ȡ15693��Ƭ
���:
		flags	��־
		uid		Ҫѡ�Ŀ�Ƭ(ѡ��ģʽ���˴�ȫ��0)
		blnr	��ʼ���ַ
		nbl		���
����:
		resplen	�������ݳ���
		resp    �������ݵ�ַ
BY:
		LSQ			20130824
****************************************************************************/
int ISO15693_Read_sm (unsigned char *blnr, unsigned char *nbl, unsigned char *resplen, unsigned char *resp)
{
	unsigned char send_buffer[20];
	unsigned char receive_buffer[100];
	unsigned char uid[8] = {0,0,0,0,0,0,0,0,};
	int nRLen=0;
	int st=0;

	memset(send_buffer,0,20);
	memset(receive_buffer,0,100);
	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_15693Read;
	send_buffer[2]=0x00;
//	send_buffer[3]=flags;//0x12
	send_buffer[3]=0x12;
	send_buffer[4]=*blnr;//
	send_buffer[5]=*nbl;
	memcpy(&send_buffer[6], uid, 8);//0*8
	
    st=send_cmd_mt3(14,send_buffer,&nRLen,receive_buffer);
	if(st==(OP_OK))
	{
		*resplen=nRLen;
		memcpy(resp,receive_buffer,*resplen);
	}	
	return st;
}	
/****************************************************************************
��������:д15693��Ƭ
���:
		flags	��־
		uid		Ҫѡ�Ŀ�Ƭ(ѡ��ģʽ���˴�ȫ��0)
		blnr	��ʼ���ַ
		nbl		���
		*data	Ҫд��������ʼ��ַ
����:
		resplen	�������ݳ���
		resp    �������ݵ�ַ
BY:
		LSQ			20130824
****************************************************************************/
int ISO15693_Write_sm(unsigned char *blnr,unsigned char *nbl,unsigned char *data, unsigned char *resplen, unsigned char *resp)
{
	unsigned char send_buffer[300];
	unsigned char receive_buffer[20];
	unsigned char uid[8] = {0,0,0,0,0,0,0,0,};
	int nRLen=0;
	int st=0;
	if(*nbl == 0x00)
	{
		*nbl = 0x01;
	}
	memset(send_buffer,0,300);
	memset(receive_buffer,0,20);
	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_15693Write;
	send_buffer[2]=0x00;
//	send_buffer[3]=flags;//0x12
	send_buffer[3]=0x12;
	send_buffer[4]=*blnr;
	send_buffer[5]=*nbl;
	memcpy(&send_buffer[6], uid, 8);//8*0
	memcpy(&send_buffer[14], data, (*nbl)*4);

    st=send_cmd_mt3(14+(*nbl)*4,send_buffer,&nRLen,receive_buffer);
	if(st==(OP_OK))
	{
		*resplen=nRLen;
		memcpy(resp,receive_buffer,*resplen);
	}
	return st;
}



/****************************************************************************
��������:�ر���Ƶ
���:
		��
����:
		��
BY:
		LSQ			20130813
****************************************************************************/
int CLOSE_RF(unsigned char *rlen, unsigned char *receive)
{
	unsigned char send_buffer[20];
	int nRLen=0;
	int st=0;

	memset(send_buffer,0,20);
	send_buffer[0]=CMDH_M1;
	send_buffer[1]=CMDL_CLOSERF;
	send_buffer[2]=0x00;

    st=send_cmd_mt3( 3,send_buffer,&nRLen,receive);
    *rlen = nRLen;
	return st;
}

int OPEN_RF(unsigned char *rlen, unsigned char *receive)
{
	unsigned char send_buffer[20];
	int nRLen = 0;
	int st = 0;

	memset(send_buffer, 0, 20);
	send_buffer[0] = CMDH_M1;
	send_buffer[1] = CMDL_OPENRF;
	send_buffer[2] = 0x00;

	st = send_cmd_mt3( 3, send_buffer, &nRLen, receive);
	*rlen = nRLen;
	return st;
}
