package com.youku.player.ui.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.youku.android.player.R;

/**
 * password input dialog
 */
public class PasswordInputDialog extends DialogFragment {
    private PasswordInputDialogInterface mClickListener;
    public static PasswordInputDialog newInstance(int title, PasswordInputDialogInterface clickListener) {
        PasswordInputDialog frag = new PasswordInputDialog();
        frag.setClickLisener(clickListener);
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(
                R.layout.yp_youku_dialog_password_interact, null);
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(textEntryView)
                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                EditText passwordEditText = (EditText) textEntryView
                                        .findViewById(R.id.password_edit);
                                String password = passwordEditText
                                        .getText().toString();
                                if (mClickListener != null) {
                                    mClickListener.onPositiveClick(password);
                                }
                            }
                        })
                .setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                if (mClickListener != null) {
                                    mClickListener.onNegativeClick();
                                }
                            }
                        }).create();
    }

    private void setClickLisener(PasswordInputDialogInterface lisener) {
        mClickListener = lisener;
    }

    public interface PasswordInputDialogInterface {
        public void onPositiveClick(String password);

        public void onNegativeClick();
    }
}