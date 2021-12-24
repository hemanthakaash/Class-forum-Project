import java.net.ServerSocket;
import java.net.Socket;
import java.net.*;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.DataInputStream;
import java.io.Serializable;
import java.io.PrintWriter;	
import java.io.FileWriter;
import java.io.*;
class student implements Runnable{
	Socket sock;
	private volatile boolean exit=false;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	DataInputStream dis;
	PrintWriter pow;
	DataOutputStream dos;
	FileWriter fos;
	student(Socket s){
		sock=s;
		try{
			fos=new FileWriter("question_log.txt",true);
			pow=new PrintWriter(fos);
			dis=new DataInputStream(sock.getInputStream());
			dos=new DataOutputStream(sock.getOutputStream());
		}
		catch(IOException E){
			stop();
		}
	}
	public void run(){
		String s="";
		try{
			System.out.println(""+Thread.currentThread().getName());
			while(true){
				System.out.println("Yes");
				int a=dis.readInt();
				System.out.println(a);
				switch(a){
					case 1:	s=dis.readUTF();//
							pow.println(s);	//
							pow.flush();	//Enter the text into log files
							break;			//
					case 2: s=dis.readUTF();						//Receive File from client
							System.out.println(s);
							byte[] contents = new byte[100000000];
	        
	        				FileOutputStream fos = new FileOutputStream(new File("files\\"+s));
	       					BufferedOutputStream bos = new BufferedOutputStream(fos);
	        				InputStream is = sock.getInputStream();
	        				long filelength=dis.readLong(),countfile=0;
	        				int bytesRead = 0; 
	        
	       	 				while(filelength>countfile){		
	       	 					bytesRead=is.read(contents);
				            	bos.write(contents,0,bytesRead);
				            	countfile+=bytesRead;
	       	 				}
				        	bos.flush(); 
				        	bos.close();
				        	is.close();
				        	break;
				    case 3: File direc=new File("files");
				    		File[] files=direc.listFiles();	//Send list of available files to client
				    		for (File fi:files){
				    			System.out.println(fi.getName());
				    			dos.writeUTF(fi.getName());
				    		}
				    		dos.writeUTF("end");
				    		int c;
				    		break;
				    case 4: s=dis.readUTF();
				    		s="files\\"+s;								
				    		System.out.println(s);			
				    		File f=new File(s);				
				    		FileInputStream fis=new FileInputStream(f);
				    		BufferedInputStream bis=new BufferedInputStream(fis);
				    		OutputStream os=sock.getOutputStream();
				    		long fileLength=f.length();								//Send the requested file
				    		dos.writeLong(fileLength);
				    		long current=0;
                        	byte[] con;
                        	while(current!=fileLength){ 
	                            int size = 100000000;
	                            if(fileLength - current >= size)
	                                current += size;    
	                            else{ 
	                                size = (int)(fileLength - current); 
	                                current = fileLength;
	                            } 
	                            con = new byte[size]; 
	                            try{
	                                bis.read(con, 0, size); 
	                                os.write(con);
	                            }
	                            catch(IOException Exe){
	                                Exe.printStackTrace();
	                            }
	                        }
	                        break;
				}
			}
		}
		catch(SocketException e){
			try{
				sock.close();
				System.out.println("Connecion Closed\n");
			}
			catch(IOException hi){
				System.out.println("Hello");
			}
		}
		catch (Exception e){
			
			System.out.println("Up up and away");
			stop();
		}
	}
	public void stop(){
		exit=true;
	}
}
class application_server{
	public static void main(String[] args) throws Exception{
		ServerSocket server=new ServerSocket(5000);
		Socket s=null;
		while(true){
			s=server.accept();
			student st=new student(s);
			Thread t1=new Thread(st);
			t1.start();

		}

	} 
}