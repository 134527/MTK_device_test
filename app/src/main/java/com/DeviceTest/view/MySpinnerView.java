package com.DeviceTest.view;

import android.content.Context;
import android.widget.TextView;

public class MySpinnerView extends androidx.appcompat.widget.AppCompatTextView {

	public MySpinnerView(Context context) {
		super(context);
	}

	
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		setTextSize((bottom - top) * 3 / 4);
	}

	
}
