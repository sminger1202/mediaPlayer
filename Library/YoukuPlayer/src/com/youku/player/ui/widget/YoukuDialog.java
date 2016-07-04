package com.youku.player.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.youku.android.player.R;
import com.youku.player.goplay.Profile;
import com.youku.player.util.DetailUtil;

/**
 * 
 * @Description: TODO
 * @author lelouch
 * @date 2012-11-16 下午7:58:46
 * @version V1.0
 */
public class YoukuDialog extends Dialog {

	private static int TYPE_NORMAL_DIALOG = 1;
	private static int TYPE_PIKER_DIALOG = 2;
	private TYPE currentType;
	private OnClickListener listener;
	private View.OnClickListener positiveBtnListener;

	private View.OnClickListener normalPositiveBtnListener;
	private View.OnClickListener normalNegtiveBtnListener;
	private View.OnClickListener closeBtnListener;

	private RelativeLayout item1;
	private RelativeLayout item2;
	private RelativeLayout item3;
	private RelativeLayout item4;

	// private ImageSwitcher item1Switcher;
	// private ImageSwitcher item2Switcher;
	// private ImageSwitcher item3Switcher;

	private RadioButton normalQualityBtn;
	private RadioButton highQualityBtn;
	private RadioButton superQualityBtn;

	private TextView title;

	private Button positiveBtn;
	private Button negtiveBtn;
	private TextView contentTV;

	private String content;
	private String titleText;
	private String positiveText;
	private String negitiveText;
	private Context mContext;
	private int checkedItem;

	public enum TYPE {

		normal, picker

	}

	public YoukuDialog(Context context) {
		super(context, R.style.YoukuDialog);
		this.mContext = context;
	}

	public YoukuDialog(Context context, TYPE type) {
		this(context);
		currentType = type;
		int selectedFormat = DetailUtil.readCachedFormat(context);
		if (selectedFormat == Profile.FORMAT_FLV_HD)
			checkedItem = 0;
		else if (selectedFormat == Profile.FORMAT_MP4)
			checkedItem = 1;
		else if (selectedFormat == Profile.FORMAT_HD2)
			checkedItem = 2;
	}

	public YoukuDialog(Context context, int checkedItem) {
		this(context);
		currentType = TYPE.picker;
		this.checkedItem = checkedItem;
	}

	private String selectedLanguage;// 视频语言
	private String selectedLanguageName;// 视频语言

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (currentType == TYPE.picker) {
			setContentView(R.layout.yp_youku_dialog_picker);
			positiveBtn = (Button) findViewById(R.id.positive_btn);
			negtiveBtn = (Button) findViewById(R.id.negtive_btn);
			title = (TextView) findViewById(R.id.edit_title);
			if (titleText != null && !titleText.equals(""))
				title.setText(titleText);
			item1 = (RelativeLayout) findViewById(R.id.item1);
			item2 = (RelativeLayout) findViewById(R.id.item2);
			item3 = (RelativeLayout) findViewById(R.id.item3);
			item4 = (RelativeLayout) findViewById(R.id.item4);

			selectedLanguage = "中文";
			selectedLanguageName = "国语";
			TextView languageTextView = (TextView) findViewById(R.id.item4_tv);
			languageTextView.setText(selectedLanguageName);
			// item1Switcher = (ImageSwitcher)
			// findViewById(R.id.site_btn_switcher);
			// item2Switcher = (ImageSwitcher)
			// findViewById(R.id.site_btn_switcher2);
			// item3Switcher = (ImageSwitcher)
			// findViewById(R.id.site_btn_switcher3);

			normalQualityBtn = (RadioButton) findViewById(R.id.normal_quality);
			highQualityBtn = (RadioButton) findViewById(R.id.high_quality);
			;
			superQualityBtn = (RadioButton) findViewById(R.id.super_quality);
			;

			item1.setTag(0);
			item2.setTag(1);
			item3.setTag(2);
			normalQualityBtn.setTag(0);
			highQualityBtn.setTag(1);
			superQualityBtn.setTag(2);
			item4.setTag(3);

			// item1Switcher.setTag(0);
			// item2Switcher.setTag(1);
			// item3Switcher.setTag(2);

			setListeners();
			if (checkedItem == 0)
				item1.performClick();
			else if (checkedItem == 1)
				item2.performClick();
			else if (checkedItem == 2)
				item3.performClick();
			else if (checkedItem == 3)
				item4.performClick();

		} else if (currentType == TYPE.normal) {
			setContentView(R.layout.yp_youku_dialog_normal);
			// dialogWrapper = findViewById(R.id.dialog_wrapper);
			// dialogWrapper.getBackground().setAlpha(204);
			positiveBtn = (Button) findViewById(R.id.positive_btn);
			negtiveBtn = (Button) findViewById(R.id.negtive_btn);
			contentTV = (TextView) findViewById(R.id.content);
			// closeBtn = (ImageButton) findViewById(R.id.close_btn);
			// title = (TextView) findViewById(R.id.edit_title);
			if (content != null && !content.equals(""))
				contentTV.setText(content);
			if (positiveText != null && !positiveText.equals(""))
				positiveBtn.setText(positiveText);
			if (negitiveText != null && !negitiveText.equals(""))
				negtiveBtn.setText(negitiveText);
			setNormalListener();
		}

	}

	public void setItemClickListener(OnClickListener listener) {

		this.listener = listener;
	}

	public interface OnClickListener {

		public void onClick(int which);

	}

	private void resetSelectedSwitcher(int index) {

		if (index == 0) {
			// if (item2Switcher.getDisplayedChild() == 1)
			// item2Switcher.showNext();
			// if (item3Switcher.getDisplayedChild() == 1)
			// item3Switcher.showNext();
			highQualityBtn.setChecked(false);
			superQualityBtn.setChecked(false);
		} else if (index == 1) {
			// if (item1Switcher.getDisplayedChild() == 1)
			// item1Switcher.showNext();
			// if (item3Switcher.getDisplayedChild() == 1)
			// item3Switcher.showNext();
			normalQualityBtn.setChecked(false);
			superQualityBtn.setChecked(false);
		} else {
			// if (item1Switcher.getDisplayedChild() == 1)
			// item1Switcher.showNext();
			// if (item2Switcher.getDisplayedChild() == 1)
			// item2Switcher.showNext();
			normalQualityBtn.setChecked(false);
			highQualityBtn.setChecked(false);
		}

	}

	public void setPositiveBtnClickListener(
			View.OnClickListener positiveBtnListener) {
		this.positiveBtnListener = positiveBtnListener;
	}

	private void setListeners() {

		item1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int index = (Integer) v.getTag();
				resetSelectedSwitcher(index);
				normalQualityBtn.setChecked(true);
				if (listener == null)
					return;
				listener.onClick(index);
			}
		});

		normalQualityBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int index = (Integer) v.getTag();
				resetSelectedSwitcher((Integer) v.getTag());
				normalQualityBtn.setChecked(true);
				if (listener == null)
					return;
				listener.onClick(index);
			}
		});

		item2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int index = (Integer) v.getTag();
				resetSelectedSwitcher((Integer) v.getTag());
				highQualityBtn.setChecked(true);
				if (listener == null)
					return;
				listener.onClick(index);
			}
		});

		highQualityBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int index = (Integer) v.getTag();
				resetSelectedSwitcher((Integer) v.getTag());
				highQualityBtn.setChecked(true);
				if (listener == null)
					return;
				listener.onClick(index);
			}
		});

		item3.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int index = (Integer) v.getTag();
				resetSelectedSwitcher((Integer) v.getTag());
				superQualityBtn.setChecked(true);
				if (listener == null)
					return;
				listener.onClick(index);
			}
		});

		superQualityBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int index = (Integer) v.getTag();
				resetSelectedSwitcher((Integer) v.getTag());
				superQualityBtn.setChecked(true);
				if (listener == null)
					return;
				listener.onClick(index);
			}
		});

		// item1.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // 改变UI
		// int index = (Integer) v.getTag();
		// if (item1Switcher.getDisplayedChild() != 1)
		// item1Switcher.showNext();
		// resetSelectedSwitcher(index);
		// // item1Switcher.getDisplayedChild()
		// if (listener == null)
		// return;
		// listener.onClick(index);
		//
		// }
		// });

		// item1Switcher.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // 改变UI
		// int index = (Integer) v.getTag();
		// if (item1Switcher.getDisplayedChild() != 1)
		// item1Switcher.showNext();
		// resetSelectedSwitcher(index);
		// // item1Switcher.getDisplayedChild()
		// if (listener == null)
		// return;
		// listener.onClick(index);
		// }
		// });

		// item2.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // 改变UI
		// int index = (Integer) v.getTag();
		// if (item2Switcher.getDisplayedChild() != 1)
		// item2Switcher.showNext();
		// resetSelectedSwitcher(index);
		// if (listener == null)
		// return;
		// listener.onClick(index);
		//
		// }
		// });

		// item2Switcher.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // 改变UI
		// int index = (Integer) v.getTag();
		// if (item2Switcher.getDisplayedChild() != 1)
		// item2Switcher.showNext();
		// resetSelectedSwitcher(index);
		// // item1Switcher.getDisplayedChild()
		// if (listener == null)
		// return;
		// listener.onClick(index);
		// }
		// });

		// item3.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // 改变UI
		// int index = (Integer) v.getTag();
		// if (item3Switcher.getDisplayedChild() != 1)
		// item3Switcher.showNext();
		// resetSelectedSwitcher(index);
		// if (listener == null)
		// return;
		// listener.onClick(index);
		//
		// }
		// });

		// item3Switcher.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // 改变UI
		// int index = (Integer) v.getTag();
		// if (item3Switcher.getDisplayedChild() != 1)
		// item3Switcher.showNext();
		// resetSelectedSwitcher(index);
		// // item1Switcher.getDisplayedChild()
		// if (listener == null)
		// return;
		// listener.onClick(index);
		// }
		// });

		item4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int index = (Integer) v.getTag();
				if (listener == null)
					return;
				listener.onClick(index);
			}
		});
		// closeBtn.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// YoukuDialog.this.dismiss();
		// }
		// });

		positiveBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (positiveBtnListener == null) {
					YoukuDialog.this.dismiss();
					return;
				}
				positiveBtnListener.onClick(v);
				YoukuDialog.this.dismiss();
			}
		});

		negtiveBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				YoukuDialog.this.dismiss();
			}
		});

	}

	private void setNormalListener() {

		positiveBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				normalPositiveBtnListener.onClick(v);
			}
		});

		negtiveBtn.setOnClickListener(new View.OnClickListener() {

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

	public void setMessage(String content) {

		this.content = content;
	}

	public void setTitle(String title) {
		titleText = title;
	}

	public void setOnCloseBtn(View.OnClickListener listener) {
		closeBtnListener = listener;
	}

}
