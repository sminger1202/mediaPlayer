package com.youku.player.unicom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.youku.android.player.R;

/**
 * Created by malijie on 2015/3/12.
 * 联通提示对话框，供播放器与主客户端复用
 */
public class ChinaUnicomAlertDialog extends Dialog{

    /**对话框点击Listener*/
    private View.OnClickListener positiveListener;
    private View.OnClickListener negativeListener;

    /** 对话框消息展示*/
    private  String mMessage = null;
    private  Context mContext = null;

    private Button mButtonPositive = null;
    private Button mButtonNegative = null;
    private TextView mTextViewMessage = null;

    /**对话框按钮文字*/
    private String positiveButtonText = null;
    private String negativeButtonText = null;

    public ChinaUnicomAlertDialog(Context context,String message) {
        super(context,R.style.ChinaUnicomDialog);
        this.mMessage = message;
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unicom_notify_dialog);

        mTextViewMessage = (TextView)findViewById(R.id.unicom_notify_message);
        mButtonNegative = (Button) findViewById(R.id.unicom_btn_cancel);
        mButtonPositive = (Button) findViewById(R.id.unicom_btn_continue);


        mButtonPositive.setOnClickListener(positiveListener);
        mButtonNegative.setOnClickListener(negativeListener);
        mButtonPositive.setText(positiveButtonText);
        mButtonNegative.setText(negativeButtonText);
        mTextViewMessage.setText(mMessage);

        setCanceledOnTouchOutside(false);
    }

    public void setUnicomPositiveBtnListener(View.OnClickListener listener){
        this.positiveListener = listener;
    }

    public void setUnicomNegativeBtnListener(View.OnClickListener listener){
        this.negativeListener = listener;
    }

    public void setUnicomPositiveBtnText(String text){
        this.positiveButtonText = text;
    }

    public void setUnicomNegativeBtnText(String text){
        this.negativeButtonText = text;
    }

    @Override
    public void onBackPressed() {
        return;
    }

}


