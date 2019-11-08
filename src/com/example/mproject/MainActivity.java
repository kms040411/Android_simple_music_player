package com.example.mproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.TabActivity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")

public class MainActivity extends TabActivity {

	String Path = "/sdcard/Download/"; // �뷡 ���� ���
	String sdPath = "/sdcard/Playlists/";
	String nowPlaying = new String(); // ��� ���� �뷡 ����
	String nowPlayingList = new String(); // ��� ���� �뷡 ���
	MediaPlayer mp;
	final SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss"); // �ð� ǥ��
																		// ����

	boolean jacketHide = false;
	boolean playState = false; // ������ ������ �����ΰ�?
	boolean resetState = true; // ������ ������� ���� �����ΰ�?
	//int roopState = 0;// 0 : �Ѱ� �ݺ�, 1 : ��ü �ݺ�, 2 : �ݺ� ����
	int musicId; // ������� ������ ��ġ
	ArrayList<String> TimeListTime = new ArrayList<String>();

	// 1��° ���� ����
	ImageButton btnPrev, btnNext, btnPlay, btnPick, btnStop;
	TextView tvTime, title;
	SeekBar sb;
	ListView timeList;
	SlidingDrawer sd;

	// 2��° ���� ����
	ListView songList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setTitle("MP��");

		// ��ȣ��Ʈ ����
		final TabHost tabHost = getTabHost();
		TabSpec tabSpec1 = tabHost.newTabSpec("SONG").setIndicator("���");
		tabSpec1.setContent(R.id.tabSong);
		tabHost.addTab(tabSpec1);

		TabSpec tabSpec2 = tabHost.newTabSpec("ALL").setIndicator("���뷡");
		tabSpec2.setContent(R.id.tabAllSong);
		tabHost.addTab(tabSpec2);

		// --���� ���
		// �ҷ�����----------------------------------------------------------------------------------------------------

		final ArrayList<String> SongList = new ArrayList<String>();
		File[] FileList = new File(Path).listFiles(); // ��ο� �ִ� ���� ����� �ҷ���
		for (File file : FileList) { // FileList�� ���ҵ��� ���ʴ�� file������ ����
			String name = file.getName(); // File �̸��� �޾ƿ�
			try {
				String extension = name.substring(name.length() - 3); // ������
																		// Ȯ���ڸ�
																		// �޾ƿ�
				if (extension.equals((String) "mp3")) // Ȯ���ڰ� "mp3"�̸�
					SongList.add(name); // ��Ͽ� �߰�
			} catch (Exception e) {
			}
		}

		// --------------------------------------------------------------------------------------------------------------

		// 1��° ���� ����
		title = (TextView) findViewById(R.id.title);
		btnPrev = (ImageButton) findViewById(R.id.prevsong);
		btnNext = (ImageButton) findViewById(R.id.nextsong);
		btnPlay = (ImageButton) findViewById(R.id.play);
		btnPick = (ImageButton) findViewById(R.id.pick);
		btnStop = (ImageButton) findViewById(R.id.stop);
		tvTime = (TextView) findViewById(R.id.time);
		final ImageView ivJacket = (ImageView) findViewById(R.id.albumjacket);
		sb = (SeekBar) findViewById(R.id.seekBar);
		sd = (SlidingDrawer) findViewById(R.id.timetable);
		timeList = (ListView) findViewById(R.id.timelist);

		mp = new MediaPlayer();

//		switch (roopState) {
//		case 0:
//			mp.setLooping(true);
//			break;
//		case 2:
//			mp.setLooping(false);
//			break;
//		}

		ivJacket.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (jacketHide) {
					changeJacket(ivJacket);
					jacketHide = false;
				} else {
					ivJacket.setImageResource(R.drawable.appicon);
					jacketHide = true;
				}
				return false;
			}
		});

		btnPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (playState == false && resetState == true) {
					try {
						playsong(ivJacket);
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(), "����� ������ �������ּ���", 0).show();
					}
				} else if (playState == true && resetState == false) {
					mp.pause();
					playState = false;
					btnPlay.setImageResource(R.drawable.play);
				} else if (playState == false && resetState == false) {
					mp.start();
					moveBar();
					playState = true;
					btnPlay.setImageResource(R.drawable.stop);
				}

			}
		});

		btnStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (resetState == false) {
					mp.stop();
					mp.reset();
					sb.setProgress(0);
					tvTime.setText("00:00" + "/" + timeFormat.format(mp.getDuration()));
					playState = false;
					resetState = true;
					btnPlay.setImageResource(R.drawable.play);
				}
			}
		});

		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int nextId = musicId + 1;
				if (SongList.size() != nextId) {
					musicId = nextId;
					mp.stop();
					mp.reset();
					nowPlaying = SongList.get(nextId);
					try {
						playsong(ivJacket);
					} catch (Exception e) {
					}
				} else {
					Toast.makeText(getApplicationContext(), "������ �뷡 �Դϴ�", 0).show();
				}
			}
		});

		btnPrev.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int prevId = musicId - 1;
				if (prevId != -1) {
					musicId = prevId;
					mp.stop();
					mp.reset();
					nowPlaying = SongList.get(prevId);
					try {
						playsong(ivJacket);
					} catch (Exception e) {
					}
				} else {
					Toast.makeText(getApplicationContext(), "ó�� �뷡 �Դϴ�", 0).show();
				}
			}
		});

		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mp.seekTo(seekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}
		});

		btnPick.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					FileInputStream fis = new FileInputStream(sdPath + nowPlaying + ".txt");
					byte[] time = new byte[fis.available()];
					fis.read(time);
					fis.close();
					String str = new String(time).trim();
					String savestr = str.concat("+" + tvTime.getText().toString().substring(0, 5));
					FileOutputStream fos = new FileOutputStream(sdPath + nowPlaying + ".txt");
					fos.write(savestr.getBytes());
					fos.close();
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "�� ����� �ۼ��մϴ�", 0).show();
					try {
						File newfile = new File(sdPath + nowPlaying + ".txt");
						newfile.createNewFile();
						FileOutputStream fos = new FileOutputStream(sdPath + nowPlaying + ".txt");
						String savestr = tvTime.getText().toString().substring(0, 5);
						fos.write(savestr.getBytes());
						fos.close();
					} catch (Exception e2) {
						Toast.makeText(getApplicationContext(), "����", 0).show();
					}
				}
				refreshTimeList();
				sd.animateOpen();
			}
		});

		timeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String time = TimeListTime.get(position);
				Date dateTime = new Date();
				try {
					dateTime = timeFormat.parse(time);
				} catch (ParseException e) {
				}
				int dateTime2 = (int) dateTime.getTime() + 32400000;
				mp.seekTo(dateTime2);
				sd.animateClose();
			}
		});

		timeList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				TimeListTime.remove(position);
				try {
					if (TimeListTime.size() >= 1) {
						FileOutputStream fos = new FileOutputStream(sdPath + nowPlaying + ".txt");
						String str = new String();
						for (int i = 0; i < TimeListTime.size(); i++) {
							str = str.concat(TimeListTime.get(i) + "+");
						}
						String savestr = str.substring(0, str.length() - 1);
						fos.write(savestr.getBytes());
						fos.close();
						refreshTimeList();
					} else {
						File file = new File(sdPath + nowPlaying + ".txt");
						file.delete();
						Toast.makeText(getApplicationContext(), "����� �ð� ����� �����Ͽ����ϴ�", 0).show();
						refreshTimeList();
					}
				} catch (Exception e) {
				}
				return true;
			}
		});

		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				int nextId = musicId + 1;
				if (playState == true) {
					if (SongList.size() != nextId) {
						musicId = nextId;
						mp.stop();
						mp.reset();
						nowPlaying = SongList.get(nextId);
						try {
							playsong(ivJacket);
						} catch (Exception e) {
						}
					} else {
						Toast.makeText(getApplicationContext(), "������ �뷡 �Դϴ�", 0).show();
					}
				}
			}
		});
		
		// 2��° ���� ����
		songList = (ListView) findViewById(R.id.songlist);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, SongList);
		songList.setAdapter(adapter); // ListView�� ����� ����

		songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mp.stop();
				mp.reset(); // ������ �������� ���� ����
				musicId = position;
				nowPlaying = SongList.get(position); // ����� �뷡�� ����
				try {
					playsong(ivJacket);
				} catch (Exception e) {
				}
				tabHost.setCurrentTab(0); // ��� ������ �̵�
			}
		});

	}

	public void moveBar() {
		new Thread() {
			public void run() {
				if (mp != null) {
					sb.setMax(mp.getDuration());
					while (playState == true) {
						runOnUiThread(new Runnable() {
							public void run() {
								sb.setProgress(mp.getCurrentPosition());
								tvTime.setText(timeFormat.format(mp.getCurrentPosition()) + "/"
										+ timeFormat.format(mp.getDuration()));
							}
						});
						SystemClock.sleep(200);
					}
				}
			}
		}.start();
	}

	public void changeJacket(ImageView ivJacket) {
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		try{
		mmr.setDataSource(Path + nowPlaying);
		} catch (IllegalArgumentException e){
			
		}

		byte[] data = mmr.getEmbeddedPicture();
		if (data != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			ivJacket.setImageBitmap(bitmap);
		} else {
			ivJacket.setImageResource(R.drawable.appicon);
		}
	}

	public void playsong(ImageView ivJacket) {
		try {
			mp.setDataSource(Path + nowPlaying);
			mp.prepare();
			mp.start();
			title.setText(nowPlaying);
			changeJacket(ivJacket);
			moveBar();
			playState = true;
			resetState = false;
			btnPlay.setImageResource(R.drawable.stop);
			refreshTimeList();
		} catch (IOException e) {
		}
	}

	public void refreshTimeList() {
		TimeListTime.clear();
		try {
			FileInputStream fis = new FileInputStream(sdPath + nowPlaying + ".txt");
			byte[] list = new byte[fis.available()];
			fis.read(list);
			String str = new String(list);
			int startidx = 0;
			for (int i = 0; i < str.length(); i++) {
				int endidx = str.length();
				if (str.charAt(i) == '+') {
					try {
						endidx = i;
						TimeListTime.add(str.substring(startidx, endidx));
						startidx = i + 1;
					} catch (Exception e) {
						continue;
					}
				}
			}
			TimeListTime.add(str.substring(startidx, str.length()));
			fis.close();

			ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
					TimeListTime);
			timeList.setAdapter(timeAdapter);
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "�ҷ��� ����� �����ϴ�", 0).show();
			timeList.setAdapter(null);
		}
	}
}
