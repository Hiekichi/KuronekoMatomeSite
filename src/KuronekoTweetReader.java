import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;

public class KuronekoTweetReader extends JFrame 
		implements ActionListener {

	Twitter mTwitter;
	
	Container mContentPane;
	JScrollPane mScrollPane;
	JTextArea mTextArea;
	JPanel    mPanel;
	JButton   mButton;

	ArrayList mArrayList;
	String mSepa = "@@hogehiefuga@@";


	public static void main(String[] args) {
		KuronekoTweetReader frame = new KuronekoTweetReader();
		frame.setTitle("Tweet Reader for KuronekoWiki");
		frame.setSize(800, 600);
		frame.setVisible(true);
	}

	public KuronekoTweetReader() {
		ConfigurationBuilder confbuilder = new ConfigurationBuilder();
		TwitterFactory twitterfactory = new TwitterFactory(confbuilder.build());
		mTwitter = twitterfactory.getInstance();

		mContentPane = getContentPane();
		mContentPane.setLayout(new BorderLayout());
		
		mTextArea = new JTextArea();
		mScrollPane = new JScrollPane();
		mScrollPane.setViewportView(mTextArea);
		mContentPane.add(mScrollPane, BorderLayout.CENTER);

		mPanel = new JPanel();
		mPanel.setLayout(new FlowLayout());
		mButton = new JButton("Get");
		mPanel.add(mButton);
		mContentPane.add(mPanel, BorderLayout.SOUTH);
		
		mButton.addActionListener(this);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		mArrayList = new ArrayList<String>();
		mTextArea.setText("");
		ResponseList<Status> list;

		//System.out.println("ActionEvent : " + arg0.toString());
		String screenname;
		
		if (arg0.getSource() == mButton) {
			//System.out.println("Getが押されました。");
			Paging paging = new Paging(1, 100);
			
			try {
				screenname = "kuroneko_daten";
				list = mTwitter.getUserTimeline(screenname, paging);
				getTweet(screenname, list);
				
				screenname = "kirino_kousaka";
				list = mTwitter.getUserTimeline(screenname, paging);
				getTweet(screenname, list);
			}
			catch( Exception e) {
				System.out.println("ERROR : " + e.toString());
				System.exit(1);
			}
			
			Collections.sort(mArrayList);
			String str;
			String[] col;
			String[] rtCol;
			String currentDate = "";
			boolean kaishiFlag = false;
			for (int i = 100; i < mArrayList.size(); ++i) {
				//mTextArea.append( mArrayList.get(i) + "\n");
				str = mArrayList.get(i).toString();
				str = str.replaceAll("@kuroneko_daten ", "");
				str = str.replaceAll("@kirino_kousaka ", "");
				
				rtCol = str.split(" RT @");
				str = rtCol[0];
				
				col = str.split( mSepa );
				if ( ! col[1].equals(currentDate) ) {
					if (! currentDate.equals("")) {
						kaishiFlag = true;
						mTextArea.append("\n<< " + col[1] + " >>\n");
					}
					currentDate = col[1];
				}

				if ( kaishiFlag ) {
					if ( rtCol.length == 2 ) {
						mTextArea.append( "　&color(gray){（RT @" + rtCol[1] + "）}\n" );
					}
					
					if (col[2].equals("kuroneko_daten")) {
						mTextArea.append("#divclass(kuroneko){");
					}
					else {
						mTextArea.append("#divclass(kirino){");
					}
					mTextArea.append(col[2] + ": " + col[3] + "}\n");
				}
			}
		}
	}		

	private void getTweet(String screenname, 	ResponseList<Status> list) {
		for(int i = 0; i < list.size(); i++){
			Status s = list.get(i);
			// 一意なツイートID
			long id = s.getId();
			// ツイートの内容
			String text = s.getText();
			// 投稿日時
			Date created = s.getCreatedAt();
			Calendar cal = Calendar.getInstance();
			cal.setTime(created);
			// 位置情報
			GeoLocation geoloc = s.getGeoLocation();
			Double lat = null;
			Double lng = null;
			if(geoloc != null){
				lat = new Double(geoloc.getLatitude());
				lng = new Double(geoloc.getLongitude());
			}
			// 配列に格納
			StringBuffer sb = new StringBuffer();
			sb.append(id + mSepa);
			//sb.append("text=" + text + ":");
			String thisDate = cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DATE);
			sb.append(thisDate + mSepa);
			sb.append(screenname + mSepa + text);
			//sb.append("created=" + created + ":");
			//sb.append("lat=" + lat + ":");
			//sb.append("lng=" + lng + ":");
			//sb.append("\n");
			//System.out.println(sb.toString());
			
			mArrayList.add(sb.toString());
			//mTextArea.insert(sb.toString(), 0);
		}
	}
}
