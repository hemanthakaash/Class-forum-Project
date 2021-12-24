import javax.swing.JButton;  
import javax.swing.JFrame;  
import javax.swing.JLabel; 
import javax.swing.JTextField; 
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTabbedPane;
import javax.swing.JFileChooser;
import javax.swing.JComboBox;
import java.awt.FlowLayout;
import java.awt.*;
import java.awt.event.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.RandomAccessFile;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.util.*;
import java.lang.*;
class Frame2 extends JFrame{
	RandomAccessFile fr;
    Frame2(Frame1 connect ,Socket soc){
        Socket sock=soc;
        try{
        	//All the Streams required for data transmission..
            FileWriter fos=new FileWriter("question_log.txt",true);
            PrintWriter pow=new PrintWriter(fos);
            fr=new RandomAccessFile("question_log.txt","r");
            OutputStream os=sock.getOutputStream();
            DataOutputStream dos=new DataOutputStream(os);
            DataInputStream dis=new DataInputStream(sock.getInputStream());

            //Contents of second Frame..
            JTabbedPane window=new JTabbedPane();
            JPanel convo=new JPanel(new FlowLayout(FlowLayout.LEFT));
            JPanel uploadTab=new JPanel(new FlowLayout(FlowLayout.LEFT));
            JPanel downTab=new JPanel();

            //Contents of Discussion Tab
            JLabel convoLabel1=new JLabel("Discussions: ");
            convo.add(convoLabel1);

            JTextArea convotext1=new JTextArea(16,70);
            JScrollPane convoscroll1=new JScrollPane(convotext1);
            convo.add(convoscroll1);

            JLabel convoLabel2=new JLabel("Enter your text here: ");
            convo.add(convoLabel2);

            JTextArea convotext2=new JTextArea(5,70);
            JScrollPane convoscroll2=new JScrollPane(convotext2); 
            convo.add(convoscroll2);

            JButton convoButton1=new JButton("POST");
            convo.add(convoButton1);

            JButton convoButton2=new JButton("REFRESH");
            convo.add(convoButton2);

            String s=writeconvo();	//Getting Contents of log file onto the String
            convotext1.setText(s);	//Displaying the contents of the log file

            convoButton1.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    String s=convotext2.getText();
                    try{
                        pow.println(s);
                        pow.flush();
                        dos.writeInt(1);
                        dos.writeUTF(s);
                    }
                    catch(Exception E){
                        E.printStackTrace();
                    }
                    convotext2.setText("");
                }
            });
            convoButton2.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    convotext2.setText("");
                    String s=writeconvo();	//Similar Action as done before
                    convotext1.setText(s);
                }
            });
            
            //Contents of Upload Tab
            JLabel uploadTablabel1=new JLabel("Choose a file to upload : ",JLabel.CENTER); 
            uploadTab.setLayout(null);
            uploadTab.add(uploadTablabel1);
            uploadTablabel1.setBounds(30,30,150,30);

            JLabel uploadTablabel2=new JLabel("");
            uploadTab.add(uploadTablabel2);
            uploadTablabel2.setBounds(250,300,250,30);

            JTextField uploadTabtext1=new JTextField();
            uploadTabtext1.setBounds(50,90,250,30); 
            uploadTab.add(uploadTabtext1);

            JButton uploadTabButton1=new JButton("Select");
            uploadTabButton1.setBounds(350,90,150,30);
            uploadTab.add(uploadTabButton1);

            JButton uploadTabButton2 = new JButton("Upload");
            uploadTab.add(uploadTabButton2);
            uploadTabButton2.setBounds(250,200,150,30);

            uploadTabButton1.addActionListener(new ActionListener(){  
                public void actionPerformed(ActionEvent e){
                	uploadTablabel2.setText("");
                    JFileChooser chooser = new JFileChooser();
                    chooser.setCurrentDirectory(new java.io.File("D:\\"));//Defualt opeing Address of File chooser
                    chooser.setDialogTitle("Choose File");
                    chooser.setAcceptAllFileFilterUsed(false);
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        String filename=chooser.getSelectedFile().getName();
                        filename=chooser.getCurrentDirectory()+"\\"+filename;
                        uploadTabtext1.setText(filename);
                    }        
                }
            });

            
            uploadTabButton2.addActionListener(new ActionListener(){  
                public void actionPerformed(ActionEvent e){
                    String upl=uploadTabtext1.getText();
                    upl.replace("\\","\\\\");
                    try{
                    	dos.writeInt(2);
                        File file=new File(upl);
                        FileInputStream fis=new FileInputStream(file);
                        BufferedInputStream bis=new BufferedInputStream(fis);
                        String[] arrof=upl.split("\\\\");
                        int l=arrof.length;
                        dos.writeUTF(arrof[l-1]);
                        long fileLength=file.length();
                        System.out.println("Entered"+fileLength);
                        dos.writeLong(fileLength);						//Tell server number to bytes to receive
                        long current=0;
                        byte[] contents;
                        while(current!=fileLength){ 
                            int size = 100000000;
                            if(fileLength - current >= size)
                                current += size;    
                            else{ 
                                size = (int)(fileLength - current); 
                                current = fileLength;
                            } 
                            contents = new byte[size]; 
                            try{
                                bis.read(contents, 0, size);		//Read data from file
                                os.write(contents);					//Push it onto the socket
                            }
                            catch(IOException Exe){
                                Exe.printStackTrace();
                            }
                            String s="Uploading... "+(current*100)/fileLength+"% .";
                        	uploadTablabel2.setText(s);    
                        }
                        uploadTablabel2.setText("Upload Complete");
                        bis.close();
                    }
                    catch(IOException Ex){
                    	System.out.println("Error Occured");
                    }
                }
            });

            //Contents of Download Tab
            JLabel downTablabel1=new JLabel("Files Available on Server: ",JLabel.LEFT);
            downTab.add(downTablabel1);
            downTablabel1.setBounds(30,30,150,30);
            downTab.setLayout(null);

            dos.writeInt(3);									//Request list of files..
            String s1="";										
            ArrayList<String> files=new ArrayList<String>();	//
            s1=dis.readUTF();									//
            while(!s1.equals("end")){							//Add all the filenames to the list
            	files.add(s1);									//
            	s1=dis.readUTF();								//
            }
            String[] file1=files.toArray(new String[0]);

            JComboBox<String> combobox=new JComboBox<String>(file1);	//Add list of file names to ComboBox
            combobox.setBounds(30,90,250,40);							//
            downTab.add(combobox);

            JButton downTabButton1=new JButton("Download");
            downTabButton1.setBounds(550,90,150,30);
            downTab.add(downTabButton1);

            downTabButton1.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent e){
            		try{
	            		String s=(String)combobox.getSelectedItem();
	            		dos.writeInt(4);								//Request the file
	            		dos.writeUTF(s);								//Send file name
	            		long length=dis.readLong(),countfile=0;
	            		int bytesRead=0;
	            		byte[] contents = new byte[100000000];
	            		FileOutputStream fos = new FileOutputStream(new File("Downloads\\"+s));
		       			BufferedOutputStream bos = new BufferedOutputStream(fos);
		        		InputStream is = sock.getInputStream();

		        		while(length>countfile){						//
	   	 					bytesRead=is.read(contents);				//Receive the data from server
			            	bos.write(contents,0,bytesRead);			//write into the file
			            	countfile+=bytesRead;						//
	   	 				}
			        	bos.flush(); 
			        	bos.close();
			        	is.close();
		        	}
		        	catch(IOException exc){
		        		exc.printStackTrace();
		        	}
            	}
            });
            //Adding Tabs to Tabbed Pane and the Tabbed pane to the frame
            window.add("Discussion",convo);
            window.add("Upload",uploadTab);
            window.add("Download",downTab);
            add(window);
        }
    catch(IOException e){
        e.printStackTrace();
    }
        setVisible(true);
        setSize(804,497);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    //Function to load the contents of log file onto the Discussion Area:
    String writeconvo(){
    	String s="",m="";
    	try{
	    	long len=fr.length();
	        if(len>13200)	fr.seek(len-13200);
	        else fr.seek(0);
	        while((m=fr.readLine())!=null){
	        	s+=(m+".\n");
	        }
    	}
    	catch(IOException e){
    		e.printStackTrace();
    	}
        return s;
    }
}
class Frame1 extends JFrame{
    Frame1 thisobj;
    Frame1(){
        thisobj=this;
		setTitle("My Window");

        JLabel label1=new JLabel("Welcome to Great kirikalan Magic show.",JLabel.CENTER); 
        label1.setBounds(120,25,330,40);
        add(label1);

        JLabel label2=new JLabel("Enter the Server's IP Address:");
        label2.setBounds(50,100,200,20);
        add(label2);

        JTextField text1=new JTextField();
        text1.setBounds(50,130,200,25); 
        add(text1);

        JLabel label3=new JLabel();
        label3.setBounds(150,200,250,30);
        add(label3);

        JButton button1=new JButton("Connect");
        button1.setBounds(300,130,100,25);
        add(button1);
        button1.addActionListener(new ActionListener(){  
            public void actionPerformed(ActionEvent e){  
                try{
                    label3.setText("");
                    Socket sock=new Socket(text1.getText(),5000);	//Establish Connection
                    System.out.println("Connection Established");
                    setVisible(false);
                    Frame2 obj1=new Frame2(thisobj,sock);
                }
                catch(UnknownHostException E){
                    label3.setText("Couldn't reach server....");  
                }
                catch(IOException E){
                    label3.setText("Connection disturbed...");
                }
            }  
        });
        
        setSize(804,497);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setVisible(true);
   } 
}
class application_client{  
    public static void main(String[] s) {  
        Frame1 obj=new Frame1();
    }  
}  