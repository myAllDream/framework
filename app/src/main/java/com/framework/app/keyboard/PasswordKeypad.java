package com.framework.app.keyboard;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.framework.app.R;


/**
 * Created by chenjiawei on 16/8/30.
 * 防支付宝密码输入界面
 */
public class PasswordKeypad extends DialogFragment implements View.OnClickListener, PasswordKeyboard.OnPasswordInputListener,
        MDProgressBar.OnPasswordCorrectlyListener {

    private TextView errorMsgTv;

    private Callback mCallback;

    private RelativeLayout passwordContainer;

    private MDProgressBar progressBar;
    private TextView notice;

    private PasswordView passwordView;

    private int passwordCount;

    private boolean passwordState = true;

    PasswordKeyboard numberKeyBoard;

    private StringBuffer mPasswordBuffer = new StringBuffer();

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            mCallback = (Callback) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.password_keypad, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        Window window = getDialog().getWindow();
        //去掉边框
        window.setBackgroundDrawable(new ColorDrawable(0xffffffff));
        window.setLayout(dm.widthPixels, window.getAttributes().height);
        window.setWindowAnimations(R.style.exist_menu_animstyle);
        window.setGravity(Gravity.BOTTOM);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        errorMsgTv = (TextView) view.findViewById(R.id.error_msg);
        TextView forgetPasswordTv = (TextView) view.findViewById(R.id.forget_password);
        TextView cancelTv = (TextView) view.findViewById(R.id.cancel_dialog);

        passwordContainer = (RelativeLayout) view.findViewById(R.id.password_content);
        progressBar = (MDProgressBar) view.findViewById(R.id.password_progressBar);
        notice = (TextView) view.findViewById(R.id.notice);
        progressBar.setOnPasswordCorrectlyListener(this);
        passwordView = (PasswordView) view.findViewById(R.id.password_inputBox);
        //设置密码长度
        if (passwordCount > 0) {
            passwordView.setPasswordCount(passwordCount);
        }

        numberKeyBoard = (PasswordKeyboard) view.findViewById(R.id.password_keyboard);
        numberKeyBoard.setOnPasswordInputListener(this);

        cancelTv.setOnClickListener(this);
        forgetPasswordTv.setOnClickListener(this);
    }

    /**
     * 设置密码长度
     */
    public void setPasswordCount(int passwordCount) {
        this.passwordCount = passwordCount;
    }

    @Override
    public void onClick(View v) {
        if (R.id.cancel_dialog == v.getId()) {
            if (mCallback != null) {
                mCallback.onCancel();
            }
            dismiss();
        } else if (R.id.forget_password == v.getId()) {
            if (mCallback != null) {
                mCallback.onForgetPassword();
            }
        }
    }

    public void setCallback(Callback callBack) {
        this.mCallback = callBack;
    }

    public void setPasswordState(boolean correct) {
        setPasswordState(correct, "");
    }

    public void setPasswordState(boolean correct, String msg) {
        passwordState = correct;
        if (correct) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    notice.setText("付款成功");
                    progressBar.setSuccessfullyStatus();
                }
            },1000);
        } else {
            numberKeyBoard.resetKeyboard();
            passwordView.clearPassword();
            progressBar.setVisibility(View.GONE);
            notice.setVisibility(View.GONE);
            passwordContainer.setVisibility(View.VISIBLE);
            errorMsgTv.setText(msg);
        }
    }

    /**
     * 带动画的提醒错误
     * @param message
     * @param context
     */
    public void setPasswordError(final String message, final Context context){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                numberKeyBoard.resetKeyboard();
                passwordView.clearPassword();
                progressBar.setVisibility(View.GONE);
                notice.setVisibility(View.GONE);
                passwordContainer.setVisibility(View.VISIBLE);
                errorMsgTv.setText(message);
                shakeAnimation(context);
            }
        },1000);

    }
    public void shakeAnimation(Context context){
        Animation animation=AnimationUtils.loadAnimation(context,R.anim.shake);
        errorMsgTv.startAnimation(animation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                errorMsgTv.setText("");
            }
        },1000);
    }

    @Override
    public void onPasswordCorrectly() {
        if (mCallback != null) {
            mCallback.onPasswordCorrectly();
        }
    }

    private void startLoading(CharSequence password) {
        passwordContainer.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        notice.setVisibility(View.VISIBLE);
        if (mCallback != null) {
            mCallback.onInputCompleted(password);
        }
    }

    @Override
    public void onInput(String character) {
        if (PasswordKeyboard.DEL.equals(character)) {
            if (mPasswordBuffer.length() > 0) {
                mPasswordBuffer.delete(mPasswordBuffer.length() - 1, mPasswordBuffer.length());
            }
        } else if (PasswordKeyboard.DONE.equals(character)) {
            dismiss();
        } else {
            if (!passwordState) {
                if (!TextUtils.isEmpty(errorMsgTv.getText())) {
                    errorMsgTv.setText("");
                }
            }
            mPasswordBuffer.append(character);
        }
        passwordView.setPassword(mPasswordBuffer);
        if (mPasswordBuffer.length() == passwordView.getPasswordCount()) {
            startLoading(mPasswordBuffer);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mPasswordBuffer.length() > 0) {
            mPasswordBuffer.delete(0, mPasswordBuffer.length());
        }
    }
}
