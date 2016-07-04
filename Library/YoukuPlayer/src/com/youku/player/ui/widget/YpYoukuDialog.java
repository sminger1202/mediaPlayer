package com.youku.player.ui.widget;

import com.youku.android.player.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class YpYoukuDialog extends AlertDialog {

	private TextView positiveBtn;
	private TextView negtiveBtn;
	private View positive_btn_layout;
	private View negtive_btn_layout;
	private int negtive_btn_layout_resId = 0;
	private int negtive_btn_color_resId = 0;
	private TextView contentTV;

	private View.OnClickListener normalPositiveBtnListener;
	private View.OnClickListener normalNegtiveBtnListener;

	private String content;
	private String positiveText;
	private String negitiveText;
	private Context mContext;
	public YpYoukuDialog(Context context) {
        super(context, R.style.ypYoukuDialog);
		mContext = context;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			setContentView(R.layout.yp_player_youku_dialog_normal);
			positive_btn_layout = findViewById(R.id.positive_btn_layout);
			negtive_btn_layout = findViewById(R.id.negtive_btn_layout);
			positiveBtn = (TextView) findViewById(R.id.positive_btn);
			negtiveBtn = (TextView) findViewById(R.id.negtive_btn);
			if (negtive_btn_layout_resId != 0) {
				negtive_btn_layout.setBackgroundResource(negtive_btn_layout_resId);
			}
			if (negtive_btn_color_resId != 0) {
				negtiveBtn.setTextColor(negtive_btn_color_resId);
			}
			contentTV = (TextView) findViewById(R.id.content);
			if (content != null && content.length() != 0)
				contentTV.setText(content);
			if (positiveText != null && positiveText.length() != 0)
				positiveBtn.setText(positiveText);
			if (negitiveText != null && negitiveText.length() != 0)
				negtiveBtn.setText(negitiveText);
			setNormalListener();
		}

	private void setNormalListener() {

		positive_btn_layout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				normalPositiveBtnListener.onClick(v);
			}
		});

		negtive_btn_layout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				normalNegtiveBtnListener.onClick(v);
			}
		});

		// closeBtn.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// YoukuDialog.this.dismiss();
		// }
		// });

	}

	public void setNormalPositiveBtn(int textId, View.OnClickListener listener) {
		this.normalPositiveBtnListener = listener;
		positiveText = (String) mContext.getResources().getText(textId);
	}

	public void setNormalNegtiveBtn(int textId, View.OnClickListener listener) {
		this.normalNegtiveBtnListener = listener;
		negitiveText = (String) mContext.getResources().getText(textId);
	}

	public void setNormalPositiveBtn(String text, View.OnClickListener listener) {
		this.normalPositiveBtnListener = listener;
		positiveText = text;
	}

	public void setNormalNegtiveBtn(String text, View.OnClickListener listener) {
		this.normalNegtiveBtnListener = listener;
		negitiveText = text;
	}

	public void setNormalNegtiveBackGround(int resid) {
		this.negtive_btn_layout_resId = resid;
	}

	public void setNormalNegtiveTextColor(int color) {
		this.negtive_btn_color_resId = color;
	}

	public void setMessage(int content) {

		this.content = (String) mContext.getResources().getText(content);
	}
	public void setMessage(String content) {

		this.content = content;
	}

}
