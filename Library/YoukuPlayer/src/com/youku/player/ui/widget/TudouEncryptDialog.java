package com.youku.player.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.youku.android.player.R;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.AnalyticsWrapper;

public class TudouEncryptDialog extends Dialog {

	private EditText mPassWord;
	private TextView mConfirmDialog;
	private TextView mCancleDialog;
	private TextView mEncryptTips;
	private OnPositiveClickListener mPositiveClickListener;

	public TudouEncryptDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public TudouEncryptDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.yp_tudou_encrypt_dialog);
		initViews();
		setListeners();
	}

	private void initViews() {
		mPassWord = (EditText) findViewById(R.id.password_edit);
		mConfirmDialog = (TextView) findViewById(R.id.tudou_dialog_confirm);
		mCancleDialog = (TextView) findViewById(R.id.tudou_dialog_cancel);
		mEncryptTips = (TextView) findViewById(R.id.tudou_dialog_encrypt_tips);
	}

	private void setListeners() {
		mConfirmDialog.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPositiveClickListener == null) {
					TudouEncryptDialog.this.dismiss();
					return;
				}
				AnalyticsWrapper.trackCustomEvent(getContext(),
						"加密视频密码输入框确认按钮点击", "视频播放页", null,
						MediaPlayerDelegate.getUserID());
				mPositiveClickListener.onClick(mPassWord.getText().toString());
				TudouEncryptDialog.this.dismiss();
				mPassWord.setText("");
			}
		});

		mCancleDialog.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TudouEncryptDialog.this.dismiss();
				mPassWord.setText("");
			}
		});
	}
	
	public void setEncryptTips(String tips) {
		mEncryptTips.setText(tips);
	}

	public interface OnPositiveClickListener {

		public void onClick(String passWord);

	}

	public void setPositiveClickListener(OnPositiveClickListener listener) {

		this.mPositiveClickListener = listener;
	}
}
