package com.DeviceTest;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.DeviceTest.helper.ConfigUtil;
//import com.DeviceTest.helper.SystemUtil;
import com.DeviceTest.helper.TestCase;
import com.DeviceTest.helper.XmlDeal;
import com.DeviceTest.helper.TestCase.RESULT;
import com.DeviceTest.view.MyGridView;
import com.DeviceTest.view.MyItemView;
import com.activity.GpsTestActivity;
import com.activity.RuninTestActivity;


import android.view.LayoutInflater;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.View;
import android.util.Log;
import android.content.res.Resources;

public class DeviceTest extends Activity {

	public static final int DEVICE_TEST_MAX_NUM = 1000;
	public static final int TEST_FAILED_DELAY = 5000;
	public static final String EXTRA_TEST_PROGRESS = "test progress";
	public static final String EXTRA_TEST_RESULT_INFO = "test result info";

	public static final String RESULT_INFO_HEAD = ";";
	public static final String RESULT_INFO_HEAD_JUST_INFO = "just info;";

	public static final String EXTRA_PATH = "/data/";
	private static final String CONFIG_FILE_NAME = "DeviceTestConfig.xml";
	private static final String CONFIG_FILE_NAME_CN = "DeviceTestConfig_CN.xml";
	private static final String EXTRA_CONFIG_FILE_NAME = EXTRA_PATH
			+ CONFIG_FILE_NAME;
	private static final String EXTRA_CONFIG_FILE_NAME_CN = EXTRA_PATH
			+ CONFIG_FILE_NAME_CN;		
	public static final String DATA_PATH = "/data/data/com.DeviceTest/";
	private static final String SAVE_FILE_PATH = EXTRA_PATH
			+ "DeviceTestResult";
	private static final String TAG = "DeviceTest";
	private static final String SAVE_DATA_PATH = DATA_PATH + "DeviceTest.tmp";
	public static final String TEMP_FILE_PATH = DeviceTest.DATA_PATH + "test";
    private static final String[] BLACK_LIST_SPECIAL_EVB = new String[] {
            "VibrationTestActivity",
            "BrightnessTestActivity",
            "GpsLocationTestActivity",
            "CompassTestActivity",
            "MsensorTestActivity",
            "GyroscopeTestActivity",
            "LightsensorTestActivity",
    };
    private static final String[] WHITE_LIST_SPECIAL_EVB = new String[] {
            "PCIETestActivity",
            "SATATestActivity",
            "Ethernet2TestActivity",
    };

	private XmlDeal xmldoc = null;
	private Spinner mGroupTestSpinner;
	private Button mButtonCancel;
	private Button mTestChecked;
	MyGridView myGridView;

	private List<TestCase> mTestCases;
	private List<TestCase> mCurrentCaseGroup;
	private Object[] mTestGroupNames;
    private int mPosition =-1;
	/** Called when the activity is first created. */
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		Environment.getFlashStorageDirectory();
		setContentView(R.layout.main);
		if (!InitTestData()) {
			System.exit(-1);
		}
		this.setTitle("DeviceTest Version:"
				+ getResources().getString(R.string.Version) + "  (for 8.1~12.0)");
        mTestCases = new ArrayList<TestCase>();
        for (TestCase temp : xmldoc.mTestCases) {
            boolean skip = false;
            if (ConfigUtil.isSpecialEvb()) {
                for (String black: BLACK_LIST_SPECIAL_EVB) {
                    if (temp.getClassName().equals(black)) {
                        skip = true;
                        break;
                    }
                }
            } else {
                for (String black: WHITE_LIST_SPECIAL_EVB) {
                    if (temp.getClassName().equals(black)) {
                        skip = true;
                        break;
                    }
                }
            }
            if (!skip) {
                mTestCases.add(temp);
            }
        }
		try {
			loadData();
		} catch (Exception e) {
			Log.e("Jeffy", "load data error.");
			e.printStackTrace();
		}

		myGridView = (MyGridView) findViewById(R.id.myGridView);
		myGridView.setColumnCount(3);

		for (TestCase testCase : mTestCases) {
			MyItemView itemView = new MyItemView(this);
			itemView.setText(testCase.getTestName());
			itemView.setTag(testCase.getTestNo());
			itemView.setCheck(testCase.getneedtest());
			if (testCase.isShowResult()) {
				RESULT result = testCase.getResult();
				itemView.setResult(result);
			}
			System.out.println("================="+testCase.getTestName());
			myGridView.addView(itemView);
		}

		myGridView.setOnItemClickListener(new MyGridView.OnItemClickListener() {
			public void onItemClick(ViewParent parent, View view, int position) {
				if(((MyItemView)view).  getTemcheckclick()){
					showDialog(DIALOG_PASSWORK_ID);
					mPosition =position;
					return;
				}
				if(enableitemclick)
					enableitemclick = false;
				else
					return;
				Intent intent = new Intent();
				try {
					if (mTestCases.get(position) != null) {
						String strClsPath = "com.DeviceTest."
								+ mTestCases.get(position).getClassName();
						intent.setClass(DeviceTest.this,
								Class.forName(strClsPath).newInstance()
										.getClass());
						intent.putExtra(EXTRA_TEST_PROGRESS, "0/1");
						startActivityForResult(intent, position);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mGroupTestSpinner = (Spinner) findViewById(R.id.GroupTestSpinner);

		mTestGroupNames = xmldoc.mCaseGroups.keySet().toArray();
		String[] testGroupTexts = new String[mTestGroupNames.length + 1];
		for (int i = 1; i < testGroupTexts.length; i++) {
			testGroupTexts[i] = "Group: " + mTestGroupNames[i - 1].toString();
		}
		testGroupTexts[0] = "CaseGroups";

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, testGroupTexts);
		mGroupTestSpinner.setAdapter(adapter);
		mGroupTestSpinner.setSelection(0, false);
		mGroupTestSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						if (position == 0) {
							return;
						}
						testGroup(mTestGroupNames[position - 1].toString());
						mGroupTestSpinner.setSelection(0, false);
					}

					public void onNothingSelected(AdapterView<?> parent) {

					}
				});

		mButtonCancel = (Button) findViewById(R.id.btn_cancel);
		mButtonCancel.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				try {
					save(SAVE_FILE_PATH);
				} catch (Exception e) {
					Log.e(TAG, "Failed to save test result!");
					e.printStackTrace();
				}
				finish();
			}

		});
		mTestChecked = (Button) findViewById(R.id.btn_testall);
		mTestChecked.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				testGroup(mTestGroupNames[0].toString());
			}
		});

       Button saveButton = (Button) findViewById(R.id.btn_save);
		saveButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				// zyt ++
				File savefile = new File("/cache/Result.txt");
				if (savefile.exists())
				{
					savefile.delete();
				}
				if (!savefile.exists())
				{
					try
					{
						savefile.createNewFile();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < myGridView.getChildCount(); i++)
				{
					MyItemView myItemView = (MyItemView) myGridView.getChildAt(i);
					RESULT r = myItemView.getResult();
					String testName = mTestCases.get(i).getTestName();
					String result = mAkeEnumToString(r);
					sb.append(testName).append(":").append(result).append("\n");

					/*
					 * myItemView.setResult(RESULT.UNDEF);
					 * myItemView.setCheck(true);
					 * mTestCases.get(i).setShowResult(false);
					 * mTestCases.get(i).setneedtest(true);
					 */
				}
				String sbStirng = sb.toString();
				FileOutputStream fos = null;
				try
				{
					fos = new FileOutputStream(savefile, false);
					fos.write(sbStirng.getBytes());
					fos.close();
				} catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally
				{
					if (fos != null)
					{
						try
						{
							fos.close();
						} catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
				Toast.makeText(getApplicationContext(), getString(R.string.save_isok), Toast.LENGTH_SHORT).show();	
				

			}
		});



		Button clearButton = (Button) findViewById(R.id.btn_clear);
		clearButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				showDialog(DIALOG_CLEAR_ID);
			}
		});

		Button uninstallButton = (Button) findViewById(R.id.btn_uninstall);
		uninstallButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				uninstallPackage("com.DeviceTest");
			}
		});
		createAssetFile("memtester", MEMTESTER_PATH);
		createAssetFile("gps_coldstart", GPS_COLD_START_PATH);
	}
	private boolean enableitemclick = true;
	@Override
	protected void onResume() {
		enableitemclick = true;
		mPosition =-1;
		super.onResume();
	}
	private void createAssetFile(String name, String destPath) {

		InputStream is = null;
		OutputStream os = null;
		try {
			is = getAssets().open(name);
			os = new FileOutputStream(destPath);
			int data = 0;
			while (true) {
				data = is.read();
				if (data < 0) {
					break;
				}
				os.write(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
				}
			}
//			SystemUtil.execRootCmd("chmod 777 " + destPath);
		}
	}

	public final static String MEMTESTER_PATH = DeviceTest.DATA_PATH
			+ "memtester";
	public final static String GPS_COLD_START_PATH = DeviceTest.DATA_PATH
	+ "gps_coldstart";
	private static final int DIALOG_CLEAR_ID = 10;
    private static final int DIALOG_PASSWORK_ID =11; 
	protected Dialog onCreateDialog(int id) {
		switch(id){
		case DIALOG_CLEAR_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			return builder.setMessage("Clear all test status?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									for (int i = 0; i < myGridView.getChildCount(); i++) {
										MyItemView myItemView = (MyItemView) myGridView
												.getChildAt(i);
										myItemView.setResult(RESULT.UNDEF);
										myItemView.setCheck(true);
										mTestCases.get(i).setShowResult(false);
										mTestCases.get(i).setneedtest(true);
									}
									try {
										save(SAVE_FILE_PATH);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					}).create();
		  case DIALOG_PASSWORK_ID:
			  LayoutInflater factory = LayoutInflater.from(this);
	            final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
	            final EditText  text = (EditText)textEntryView.findViewById(R.id.password_edit);
	            return new AlertDialog.Builder(this)
	                .setIconAttribute(android.R.attr.alertDialogIcon)
	                .setTitle(R.string.alert_dialog_warning_title)
	                  .setMessage(R.string.alert_dialog_text_entry)
	                .setView(textEntryView)
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    	String password = text.getText().toString();
	                    	if("123456".equals(password)){
	                    		MyItemView myItemView = (MyItemView) myGridView
										.getChildAt(mPosition);
	                    		if(myItemView.setCheckClick()){
		                    		if(!(myItemView).getischeck()){
		        						mTestCases.get(mPosition).setneedtest(false);
		        					}else{
		        						mTestCases.get(mPosition).setneedtest(true);
		        					}
	                    		}
	                    		
	                    	}else{
	                    		Toast.makeText(DeviceTest.this,
                                        "                  Password error !!!                  ",
                                        Toast.LENGTH_LONG).show();
	                    	}
	                        /* User clicked OK so do some stuff */
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked cancel so do some stuff */
	                    }
	                })
	                .create();
		}
		return null;
	}

	private void loadData() throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				SAVE_DATA_PATH));
		try{
    		List<TestCase> savedData = (List<TestCase>) ois.readObject();
    		for (TestCase savedCase : savedData) {
    			for (TestCase testCase : mTestCases) {
    				if (testCase.getClassName().equals(savedCase.getClassName())) {
    					testCase.setResult(savedCase.getResult());
    					testCase.setDetail(savedCase.getDetail());
    					testCase.setShowResult(savedCase.isShowResult());
    				}
    			}
    		}
		}catch(Exception e){
		    e.printStackTrace();
		}finally{
		    if(ois != null){
		     ois.close();
		    }
		}
		
	}

	public static String formatResult(String testName, RESULT result,
			String detail) {
		if (detail == null) {
			return "[" + testName + "]\n" + result.name();
		}
		if (detail.startsWith(RESULT_INFO_HEAD_JUST_INFO)) {
			return detail.substring(RESULT_INFO_HEAD_JUST_INFO.length());
		}
		return "[" + testName + "]\n" + result.name() + detail;
	}

	synchronized private void save(String saveFilePath) throws IOException {
		FileWriter fw;
		String tempSavePath = DATA_PATH + "save";
		fw = new FileWriter(tempSavePath);
		FileWriter fw2 = null;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			fw2 = new FileWriter("/mnt/sdcard/testResult.txt");
	    }
		for (TestCase testCase : mTestCases) {
			if (testCase.getClassName().equals(
					RuninTestActivity.class.getSimpleName())) {
				if (testCase.getDetail() == null) {
					testCase.setDetail(new RuninTestActivity().getResult());
				}
			} else if(testCase.getClassName().equals(GpsTestActivity.class.getSimpleName())) {
				if (testCase.getDetail() == null) {
					testCase.setDetail(new GpsTestActivity().getResult());
				}
			}
			fw.write(formatResult(testCase.getTestName(), testCase.getResult(),
					testCase.getDetail()) + "\n");
			if(fw2 != null){
				fw2.write(formatResult(testCase.getTestName(), testCase.getResult(),
						testCase.getDetail()) + "\n");
			}
		}
		fw.close();
		if(fw2!=null){
			fw2.close();
		}
//		SystemUtil.execScriptCmd("cat " + tempSavePath + ">" + saveFilePath,
//				TEMP_FILE_PATH, true;

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				SAVE_DATA_PATH));
		oos.writeObject(mTestCases);
		oos.close();
	}

	protected void testGroup(String selectGroup) {
		mCurrentCaseGroup = xmldoc.mCaseGroups.get(selectGroup);
		int  pos = 0;
		  if (pos < mCurrentCaseGroup.size()) {
              while(!mCurrentCaseGroup.get(pos).getneedtest()){
                  pos ++;
                  if(pos >= mCurrentCaseGroup.size()){
                      return;
                  }
              }
         }
		Intent intent = new Intent();
		if (mCurrentCaseGroup != null && mCurrentCaseGroup.get(pos) != null) {
			try {
				String strClsPath = "com.DeviceTest."
						+ mCurrentCaseGroup.get(pos).getClassName();
				intent.setClass(DeviceTest.this, Class.forName(strClsPath)
						.newInstance().getClass());
				intent.putExtra(EXTRA_TEST_PROGRESS,
				        pos+"/" + mCurrentCaseGroup.size());
				// we use nagtiv value to keep the sequence number when
				// do a all test.
				startActivityForResult(intent,pos + DEVICE_TEST_MAX_NUM);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
	}
	

	private void uninstallPackage(String packageName) {
		String cmd = "mount -o remount,rw /system /system\n"
				+ "rm -r /data/data/*DeviceTest*\n"
				+ "rm /data/app/*DeviceTest*\n"
				+ "rm /system/app/*DeviceTest*\n";
//		SystemUtil.execScriptCmd(cmd, TEMP_FILE_PATH, true);

		Uri uninstallUri = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, uninstallUri);
		startActivity(uninstallIntent);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent paramIntent) {
		super.onActivityResult(requestCode, resultCode, paramIntent);
		Log.i("Jeffy", " -------------------- onResult---request:" + requestCode + ",result:"
				+ resultCode);		
		if(resultCode == RESULT_OK) return;
		
		if(mCurrentCaseGroup != null && (requestCode - DEVICE_TEST_MAX_NUM) >= mCurrentCaseGroup.size())
			return;
        
		int pos = requestCode;
		boolean ignore = (resultCode == RESULT.UNDEF.ordinal());
		
		if (requestCode >= DEVICE_TEST_MAX_NUM) {
			if(mCurrentCaseGroup == null){
				Log.d(TAG, " _________________ mCurrentCaseGroup == null~~~~!!!!!");
			}else{
			// test auto judged.
			TestCase tmpTestCase = mCurrentCaseGroup.get(requestCode - DEVICE_TEST_MAX_NUM);
		
			if(tmpTestCase == null){
				Log.d(TAG, " _________________ tmpTestCase == null~~~~!!!!!");
			}else{
			pos = tmpTestCase.getTestNo();
			Log.d(TAG, " _________________ tmpTestCas-----------~~~~!!!!!"+pos);
		}
		}
		}

		if (!ignore && pos < mTestCases.size()) {
			MyItemView itemView = (MyItemView) myGridView.getChildAt(pos);
			RESULT result = RESULT.values()[resultCode];
			itemView.setResult(result);
			mTestCases.get(pos).setResult(result);
			mTestCases.get(pos).setShowResult(true);
			try {
				String detail = paramIntent
						.getStringExtra(EXTRA_TEST_RESULT_INFO);
				mTestCases.get(pos).setDetail(detail);
				Log.d(TAG, " _________________ tmpTestCas---------detail--~~~~!!!!!"+detail);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				save(SAVE_FILE_PATH);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		if (requestCode >= DEVICE_TEST_MAX_NUM) {
			// test next autojuaged.
			pos = requestCode - DEVICE_TEST_MAX_NUM;
			if(!ignore) 
			    pos++;
			else
			    pos --;
			if(pos < 0 || pos >= mCurrentCaseGroup.size()) return;
			Log.d(TAG, " _________________ tmpTestCas---------pos--1~~~~!!!!!"+pos);
			Intent intent = new Intent();
			if ( pos >= 0 && pos < mCurrentCaseGroup.size()) {
				while(!ignore && !mCurrentCaseGroup.get(pos).getneedtest()){
					pos ++;
					if(pos >= mCurrentCaseGroup.size()){
						return;
					}
				}
				while(ignore && !mCurrentCaseGroup.get(pos).getneedtest()){
					pos --;
					if(pos < 0 ){
						return;
					}
				}
			}
				Log.d(TAG, " _________________ tmpTestCas---------pos--2~~~~!!!!!"+pos);
				try {
					String strClsPath = "com.DeviceTest."
							+ mCurrentCaseGroup.get(pos).getClassName();
					intent.setClass(DeviceTest.this, Class.forName(strClsPath)
							.newInstance().getClass());

					intent.putExtra(EXTRA_TEST_PROGRESS, pos + "/"
							+ mCurrentCaseGroup.size());

					// we use nagtiv value to keep the sequence number when
					// do a all test.
					startActivityForResult(intent, pos + DEVICE_TEST_MAX_NUM);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				}
			
		}

	}

	private boolean InitTestData(InputStream is) {
		if (is == null) {
			return false;
		}
		try {
			xmldoc = new XmlDeal(is);
		} catch (Exception e) {
			Log.e(TAG, "parse the xmlfile is fail");
			return false;
		}
		return true;

	}

	private boolean InitTestData() {
		InputStream is = null;
		try {
if(Resources.getSystem().getConfiguration().locale.getCountry().equals("CN")){
			File configFile = new File(EXTRA_CONFIG_FILE_NAME_CN);
			if (configFile.exists()) {
				Log.i("Jeffy", "Use extra config file:"
						+ EXTRA_CONFIG_FILE_NAME_CN);
				if (InitTestData(new FileInputStream(configFile))) {
					return true;
				}
			}

			// is = this.openFileInput(strXmlPath);
			is = getAssets().open(CONFIG_FILE_NAME_CN);
}else{
			File configFile = new File(EXTRA_CONFIG_FILE_NAME);
			if (configFile.exists()) {
				Log.i("Jeffy", "Use extra config file:"
						+ EXTRA_CONFIG_FILE_NAME);
				if (InitTestData(new FileInputStream(configFile))) {
					return true;
				}
			}
			// is = this.openFileInput(strXmlPath);
			is = getAssets().open(CONFIG_FILE_NAME);
}
			try {
				xmldoc = new XmlDeal(is);
			} catch (Exception e) {
				Log.e(TAG, "parse the xmlfile is fail");
				return false;
			}
		} catch (IOException e) {

			e.printStackTrace();
			Log.e(TAG, "read the xmlfile is fail" + e.getMessage());
			// ForwardErrorActive();
			return false;
		}

		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;

	}

		private String mAkeEnumToString(RESULT r)
	{

		if (r == RESULT.OK)
		{
			return "OK";
		} else if (r == RESULT.NG)
		{
			return "NG";
		} else if (r == RESULT.SKIP)
		{
			return "SKIP";
		} else
		{
			return "UNDEF";
		}

	}


}
