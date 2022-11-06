package ChatRoom;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JToolBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JInternalFrame;
import javax.swing.JDesktopPane;
import javax.swing.border.LineBorder;
import javax.swing.UIManager;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.awt.event.ActionEvent;

public class fw {

	private JFrame frame;
	private JTextArea bt;
	private JList inline;
	private JTextField writeSth;
	private JTextField people;
	private JTextField serverDk;
	private JTextArea message2;
	private JButton clossButton;
	private JButton startButton;
	private JButton tiButton;
	private JButton setButton;
	private int max;
	private int dk;
	private boolean isStart=false;
	private ServerSocket ServerCocket;
	private JList userList;  
	private DefaultListModel listModel;
	private List<User> userlist=new ArrayList<User>();
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					fw window = new fw();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public fw() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("服务器");
		frame.setBounds(100, 100, 1105, 577);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
//		创建人数
		JLabel label = new JLabel("人数上限：");
		label.setBounds(0, 25, 90, 21);
		frame.getContentPane().add(label);
//		创建端口
		JLabel label_2 = new JLabel("端口：");
		label_2.setBounds(290, 25, 54, 21);
		frame.getContentPane().add(label_2);
//	服务器启动	
		startButton = new JButton("启动");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String maxTemp =people.getText();
				String Duanko =serverDk.getText();
				if(maxTemp.equals("")){
					JOptionPane.showMessageDialog(frame,"人数为空","警告",JOptionPane.WARNING_MESSAGE);
					return;
				}
				try{
					max=Integer.parseInt(maxTemp);
				}catch(NumberFormatException e1){
					JOptionPane.showMessageDialog(frame,"人数必须为数字","警告",JOptionPane.WARNING_MESSAGE);
				}
				if(max<=0){
					JOptionPane.showMessageDialog(frame,"人数要大于0","警告",JOptionPane.WARNING_MESSAGE);
					return;
				}
				if(max>1000){
					JOptionPane.showMessageDialog(frame,"人数已满","警告",JOptionPane.WARNING_MESSAGE);
					return;
				}
//				***************************************
				if(Duanko.equals("")){
					JOptionPane.showMessageDialog(frame,"端口为空","警告",JOptionPane.WARNING_MESSAGE);
					return;
				}
				try{
					dk=Integer.parseInt(Duanko);
				}catch(NumberFormatException e1){
					JOptionPane.showMessageDialog(frame,"端口必须为数字","警告",JOptionPane.WARNING_MESSAGE);
				}
				if(dk<=0){
					JOptionPane.showMessageDialog(frame,"端口要大于0","警告",JOptionPane.WARNING_MESSAGE);
					return;
				}
				//启动服务器
				try{
					dk=Integer.parseInt(Duanko);
					ServerCocket = new ServerSocket(dk);
					isStart=true;
				}catch(IOException e1){
					JOptionPane.showMessageDialog(frame,"启动","警告",JOptionPane.WARNING_MESSAGE);
					e1.printStackTrace();
				}
				
				OnlineThread onlineThread = new OnlineThread();
				onlineThread.start();
//				按钮的禁用
				startButton.setEnabled(false);
				clossButton.setEnabled(true);
				setButton.setEnabled(true);
				people.setEditable(false);
				serverDk.setEditable(false);
			}
		});
		startButton.setBounds(483, 21, 118, 29);
		frame.getContentPane().add(startButton);
//	关闭按钮	
		clossButton = new JButton("关闭");
		clossButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(User u:userlist){
					u.getWriter().println("CLOSE");
					u.getWriter().flush();
					
					try {
						u.getReader().close();
						u.getIn().close();
						u.getWriter();
						u.getOut().close();
						u.getSocket().close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				clossButton.setEnabled(false);
				startButton.setEnabled(true);
				setButton.setEnabled(false);
				people.setEditable(true);
				serverDk.setEditable(true);
			}
		});
		clossButton.setBounds(599, 21, 118, 29);
		clossButton.setEnabled(false);
		frame.getContentPane().add(clossButton);
//		踢人操作
		JLabel label_4 = new JLabel("被踢用户：");
		label_4.setBounds(782, 61, 90, 21);
		frame.getContentPane().add(label_4);
		
		bt = new JTextArea();
		bt.setBounds(874, 58, 96, 27);
		frame.getContentPane().add(bt);
		bt.setColumns(10);
		
		tiButton = new JButton("踢");
		tiButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String outName = bt.getText();
				if(!outName.equals("")) {
					listModel.removeElement(outName);//从在线列表中踢出
					OutputStream out;
					PrintWriter write;										
					try {
						for(User users:userlist) {//列遍每一个用户找到被踢用户,提出集合					
							if(users.getName().equals(outName)) {						
								String info_send = "ti@"+users.getName();		
								out =users.getSocket().getOutputStream();
								write = new PrintWriter(out);
								write.println(info_send);////将信息写进字符缓冲区
								write.flush();//刷新该流的缓冲
//								bt.removeAll();
								users.interrupt();	
								for(User u:userlist) {//转发当前踢出消息到其他用户
									u.getWriter().println("out@"+outName);
									u.getWriter().flush();
								}
								message2.append("服务器："+outName+"已被踢出聊天室\n");
								JOptionPane.showMessageDialog(null,outName+"已被踢出群聊", "服务器", JOptionPane.INFORMATION_MESSAGE);																
							}else {
								String info_send = "OFFLINE@"+outName;	
								out = users.getSocket().getOutputStream();
								write = new PrintWriter(out);
								write.println(info_send);////将信息写进字符缓冲区
								write.flush();//刷新该流的缓冲
							}
						}
					} catch (IOException e1) {
								// TODO 自动生成的 catch 块
								e1.printStackTrace();
							}//返回此套接字的输出流。
					
				}else {
					JOptionPane.showMessageDialog(null,"请选择被踢用户", "服务器", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		tiButton.setBounds(978, 57, 51, 29);
		frame.getContentPane().add(tiButton);
		
		JLabel label_5 = new JLabel("未完待续……");
		label_5.setBounds(862, 230, 128, 21);
		frame.getContentPane().add(label_5);
		
		listModel = new DefaultListModel();  
		inline = new JList(listModel);
		inline.setBounds(20, 91, 179, 300);
		frame.getContentPane().add(inline);
		
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(null, "在线用户", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4.setBounds(10, 69, 197, 335);
		frame.getContentPane().add(panel_4);
		
		writeSth = new JTextField();
		writeSth.setBounds(20, 444, 568, 43);
		writeSth.setText(null);
		frame.getContentPane().add(writeSth);
		writeSth.setColumns(10);
//		发送至客户端口
		setButton = new JButton("发送");
		setButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = writeSth.getText();
				for(User u:userlist){
					u.getWriter().println("ALL@SERVER@"+message);
					u.getWriter().flush();
				}
				message2.append("客服："+message+"\n");
				writeSth.setText(null);
				setButton.setEnabled(true);
			}
		});
		setButton.setBounds(594, 444, 123, 43);
		setButton.setEnabled(false);
		frame.getContentPane().add(setButton);
		
		people = new JTextField();
		people.setText("2");
		people.setBounds(84, 22, 96, 27);
		frame.getContentPane().add(people);
		people.setColumns(10);
		
		serverDk = new JTextField();
		serverDk.setText("6666");
		serverDk.setBounds(335, 22, 96, 27);
		frame.getContentPane().add(serverDk);
		serverDk.setColumns(10);
		
		message2 = new JTextArea();
		message2.getText();
		message2.setBounds(268, 99, 433, 284);
		frame.getContentPane().add(message2);
		message2.setEditable(false);
		message2.setColumns(10);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "消息显示框", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(249, 69, 468, 322);
		frame.getContentPane().add(panel_1);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "服务器功能操作", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(762, 25, 306, 481);
		frame.getContentPane().add(panel);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "写消息", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setBounds(0, 419, 740, 76);
		frame.getContentPane().add(panel_2);
	}
	//建立线程处理客户端上线请求
	public class OnlineThread extends Thread{
		public void run(){
			System.out.println("---------处理客户端上线请求----------");
			while(isStart){
			try{
				System.out.println("---------1----------");
				Socket socket=ServerCocket.accept();
				InputStream in = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				OutputStream out = socket.getOutputStream();
				PrintWriter write = new PrintWriter(out);
				System.out.println("---------2----------");
				if(userlist.size()==max){
					System.out.println("---------3----------");
					write.println("MAX@在线人数已满");
					write.flush();
					write.close();
					out.close();
					reader.close();
					in.close();
					socket.close();
				}else{
					System.out.println("---------4----------");
					write.println("SUCCESS@连接成功@");
					write.flush();
					String info = reader.readLine();
					StringTokenizer st = new StringTokenizer(info,"@");
					String ip = st.nextToken();
					String name = st.nextToken();
					message2.append(info+"@上线\n");
					listModel.addElement(name);
					System.out.println("---------5----------"+info);
					//客户端接受线程
					
					User user = new User();
					user.setName(name);
					user.setIp(ip);
					user.setIn(in);
					user.setOut(out);
					user.setReader(reader);
					user.setWriter(write);
					user.setSocket(socket);
					ReceleThred receleThred = new ReceleThred(reader);
					receleThred.start();
					user.setReceiveThread(receleThred);
					
					userlist.add(user);
					//循环集合
					for(User users:userlist){
						if(users.getName().equals(name)){
							continue;
						}
						users.getWriter().println("ADD@"+name);
						System.out.println(name);
						users.getWriter().flush();
					}
					String onlist = "USERLIST@"+userlist.size();
					for(User us:userlist){
						onlist = onlist+"@"+us.getName();
					}
					//将信息发送给客户端
					write.println(onlist);
					write.flush();
				}
			}catch(IOException e){
				JOptionPane.showMessageDialog(frame,"上线失败","警告",JOptionPane.WARNING_MESSAGE);
				e.printStackTrace();
			}
		}
	  }
	}
	public class ReceleThred extends Thread{
		
		private BufferedReader reader;
		private PrintWriter write;
		public ReceleThred(BufferedReader reader){
			this.reader=reader;
		}
		public ReceleThred(BufferedReader reader,PrintWriter write){
			this.reader=reader;
			this.write=write;
		}
		 public void run(){
			 boolean flag = true;
			 while(flag){
				try {
				  String info=reader.readLine();
				  System.out.println("info:"+info);
//				  message2.append(name+"\n");
//				  message2.append(info+"\n");
				  StringTokenizer st = new StringTokenizer(info,"@");
				  String type = st.nextToken();
				  if(type.equals("ALL")){
					  //转发群聊
					  for(User u:userlist){
							u.getWriter().println(info);
							u.getWriter().flush();
							System.out.println(info);
						}
					  message2.append(st.nextToken()+"说："+st.nextToken()+"\n");
				  }else if(type.equals("DELETE")){
					  //DELETE操作
					  for(User u:userlist){
							u.getWriter().println(info);
							u.getWriter().flush();
						}
					  //释放资源
					  String name = st.nextToken();
					  for(User u:userlist){
							if(u.getName().equals(name)){
								u.getReader().close();
								u.getIn().close();
								u.getWriter();
								u.getOut().close();
								u.getSocket().close();
								userlist.remove(u);
								break;
							}
						}
					  flag = false;
					  listModel.removeElement(name);
//					  listModel.removeAllElements();
					  listModel.addElement(name);
					  message2.append(name+"下线\n");
					  write.println("DELETE@"+info);
					  write.flush();
				  }
//				  for(User u1:userlist){
//						if(u1.getName().equals(info)){
//							continue;
//						}
//						u1.getWriter().println("ADD@"+info);
//						System.out.println(info);
//						u1.getWriter().flush();
//					}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame,"发送失败","警告",JOptionPane.WARNING_MESSAGE);
				e.printStackTrace();
			} 
		 }
		}
	}
}

