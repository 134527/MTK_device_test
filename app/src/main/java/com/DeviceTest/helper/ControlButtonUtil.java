package com.DeviceTest.helper;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.DeviceTest.DeviceTest;

import com.DeviceTest.R;
import com.DeviceTest.helper.TestCase.RESULT;


public class ControlButtonUtil {
	static Activity mActivity;
	static ControlButtonUtil mControlButtonView;
	static NotificationManager mNotificationManager;
	static Intent resultIntent = new Intent();
    private static String mAction;
	public ControlButtonUtil(Activity paramActivity) {
		resultIntent.removeExtra(DeviceTest.EXTRA_TEST_RESULT_INFO);
		mActivity = paramActivity;
		
		Button returnButton = (Button) mActivity.findViewById(R.id.btn_return);

		returnButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                ControlButtonUtil.mActivity.setResult(mActivity.RESULT_OK,
                        resultIntent);
                if (ControlButtonUtil.mNotificationManager != null)
                    ControlButtonUtil.mNotificationManager.cancelAll();
                stopService();
                ControlButtonUtil.mActivity.finish();
            }
        });

		Button passButton = (Button) mActivity.findViewById(R.id.btn_Pass);

		passButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ControlButtonUtil.mActivity.setResult(RESULT.OK.ordinal(),
						resultIntent);
				if (ControlButtonUtil.mNotificationManager != null)
					ControlButtonUtil.mNotificationManager.cancelAll();
				stopService();
				ControlButtonUtil.mActivity.finish();
			}
		});

		Button failedButton = (Button) mActivity.findViewById(R.id.btn_Fail);

		failedButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ControlButtonUtil.mActivity.setResult(RESULT.NG.ordinal(),
						resultIntent);
				if (ControlButtonUtil.mNotificationManager != null)
					ControlButtonUtil.mNotificationManager.cancelAll();
				stopService();
				ControlButtonUtil.mActivity.finish();

			}
		});

		Button skipButton = (Button) mActivity.findViewById(R.id.btn_Skip);

		skipButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
//				ControlButtonUtil.mActivity.setResult(RESULT.SKIP.ordinal(),
//						resultIntent);
//				if (ControlButtonUtil.mNotificationManager != null)
//					ControlButtonUtil.mNotificationManager.cancelAll();
//				ControlButtonUtil.mActivity.finish();

				ControlButtonUtil.mActivity.setResult(RESULT.UNDEF.ordinal(),
						resultIntent);
				if (ControlButtonUtil.mNotificationManager != null)
					ControlButtonUtil.mNotificationManager.cancelAll();
				stopService();
				ControlButtonUtil.mActivity.finish();
			}
		});

	}
//
//	public static void back() {
//	}
	private void stopService(){
	    if(mAction != null){
	        Intent intent = new Intent(mAction);
            ControlButtonUtil.mActivity.stopService(intent);
	    }
	}
	public static void setIntent(String result) {
	    mAction = result;
        
    }
	public static void setResult(String result) {
		resultIntent.putExtra(DeviceTest.EXTRA_TEST_RESULT_INFO, result);
	}

	public static void Hide() {
		mActivity.findViewById(R.id.btn_Pass).setVisibility(View.GONE);
		mActivity.findViewById(R.id.btn_Fail).setVisibility(View.GONE);
		mActivity.findViewById(R.id.btn_Skip).setVisibility(View.GONE);
		mActivity.findViewById(R.id.btn_return).setVisibility(View.GONE);
	}

	public static void Show() {
		mActivity.findViewById(R.id.btn_Pass).setVisibility(View.VISIBLE);
		mActivity.findViewById(R.id.btn_Pass).requestFocus();
		mActivity.findViewById(R.id.btn_Fail).setVisibility(View.VISIBLE);
		mActivity.findViewById(R.id.btn_Fail).requestFocus();
		mActivity.findViewById(R.id.btn_Skip).setVisibility(View.VISIBLE);
		mActivity.findViewById(R.id.btn_Skip).requestFocus();
		
		mActivity.findViewById(R.id.btn_return).setVisibility(View.VISIBLE);
		mActivity.findViewById(R.id.btn_return).requestFocus();
	}

	public static void initControlButtonView(Activity paramActivity) {
		mControlButtonView = new ControlButtonUtil(paramActivity);
	}

	static void setNotification(NotificationManager paramNotificationManager) {
		mNotificationManager = paramNotificationManager;
	}
}