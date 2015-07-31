package com.example.udpvoice;

import java.util.LinkedList;

import com.ryong21.encode.Speex;

import test.UDPClient;
import android.R.integer;
import android.R.string;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * ¼��
 * 
 * @author Administrator
 */
public class MyAudioRecord extends Thread {
	private LengthReceiver receiver;
	protected AudioRecord m_in_rec;
	protected int m_in_buf_size;
	protected byte[] m_in_bytes;
	protected boolean m_keep_running;
	protected LinkedList<byte[]> m_in_q;
	private UDPClient client;
	private Speex speex;
	private int frameSize;

	private String ip;
	private int port;
	public MyAudioRecord(String ip,int port){
		this.ip = ip;
		this.port = port;
	}
	
	@Override
	public void run() {
		try {
			byte[] bytes_pkg;
			m_in_rec.startRecording();
			int length = 0;
			byte [] src;
			Log.i("wzf","���ͳ�ʼ��");
			while (m_keep_running) {
				
				
				
				int totleByte = 0;
                short[] bufferRead = new short[frameSize];  
                byte[] processedData = new byte[1024];
                short[] rawdata = new short[1024];
                  
                /**
                 * �����ɼ�
                 * pcm����ͨ��speex����
                 */
                int bufferReadResult = m_in_rec.read(bufferRead, 0,frameSize); 
                synchronized (m_in_rec) {
                    System.arraycopy(bufferRead, 0, rawdata, 0, bufferReadResult);
                    totleByte = speex.encode(rawdata, 0, processedData, bufferReadResult);// ���������ֽڳ���
                     
                    byte[] srcs = new byte[totleByte]; 
                    System.arraycopy(processedData, 0, srcs, 0, totleByte);
                    if (totleByte != 0) {
                        Log.i("SPEEX", "����ɹ� �ֽ����鳤�� = " + totleByte
                                + " �� short[] ���� = " + bufferReadResult);
                        
                        if(receiver!=null){
    						receiver.length(totleByte);
    					}
    					client.doCommand(srcs, "192.168.2.213", 2015);
                    } else {
                        System.out.println("speex����ʧ�ܣ�");
                    }
                }
              
                 
//                /**
//                 * ����
//                 */
//                 
//                short[] rcvProcessedData = new short[160];
//                byte[]  rawData= new byte[256];
//
//                System.arraycopy(processedData, 0, rawData, 0, bufferReadResult);
//                int desize;
//                synchronized (audioTrack) {
//                    desize = speex.decode(processedData, rcvProcessedData, 160);
//                }
//                if (desize > 0) {
//                    audioTrack.write(rcvProcessedData, 0, desize);
//                    System.out.println("speex����ɹ���");
//                }
				
				
				
				/*length =m_in_rec.read(m_in_bytes, 0, 160);
				bytes_pkg = m_in_bytes.clone();
				src = new byte[length];
				System.arraycopy(bytes_pkg, 0, src, 0, length);
				int volum = getVolume(length, src);
				*//**
				 * "192.168.2.110"
				 *//*
				if(volum>2500){
					if(receiver!=null){
						receiver.length(length);
					}
					client.doCommand(src, "192.168.2.213", 2015);					
				}*/
			}

			m_in_rec.stop();
			m_in_rec = null;
			m_in_bytes = null;

		} catch (Exception e) {
			e.printStackTrace();
		}
		super.run();
	}
	

	private int getVolume(int r, byte[] bytes_pkg) {
		// way 1
		int v = 0;
		// �� buffer ����ȡ��������ƽ��������
		for (byte aBytes_pkg : bytes_pkg) {
			// ����û����������Ż���Ϊ�˸���������չʾ����
			v += aBytes_pkg * aBytes_pkg;
		}
		// ƽ���ͳ��������ܳ��ȣ��õ�������С�����Ի�ȡ������ֵ��Ȼ���ʵ�ʲ������б�׼����
		// ��������������ֵ���в����������� sendMessage �����׳����� Handler ����д���
		int volume = (int) (v / (float) r);
		return volume;
	}
	
	public void init() {
		client = UDPClient.getInstance();
		m_in_buf_size = AudioRecord.getMinBufferSize(8000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);

		m_in_rec = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, m_in_buf_size);

		m_in_bytes = new byte[160];
		m_keep_running = true;
		m_in_q = new LinkedList<byte[]>();
		
		//speex�����
        speex = new Speex();
        speex.init();
        frameSize = speex.getFrameSize();
		
	}
	
	public void setReceiver(LengthReceiver receiver) {
		this.receiver = receiver;
	}
	
	public void free() {
		m_keep_running = false;
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			Log.d("sleep exceptions...\n", "");
		}
	}
}
