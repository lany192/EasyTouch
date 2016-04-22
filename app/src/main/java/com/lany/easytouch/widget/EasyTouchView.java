package com.lany.easytouch.widget;

import java.util.Timer;
import java.util.TimerTask;

import com.lany.easytouch.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

public class EasyTouchView extends View {
    private Context mContext;
    private WindowManager mWManager;
    private LayoutParams mWMParams;
    private View mTouchView;
    private ImageView mIconImageView;
    private PopupWindow mPopuWin;
    private ServiceListener mSerLisrener;
    private View mActionWidget;
    private ViewPager mViewPager;

    private int mTag = 0;
    private int midX;
    private int midY;
    private int mOldOffsetX;
    private int mOldOffsetY;

    private Timer mTimer;
    private TimerTask mTask;

    public EasyTouchView(Context context, ServiceListener listener) {
        super(context);
        this.mContext = context;
        this.mSerLisrener = listener;
        initEasyTouchViewEvent();
        initActionView();
    }

    private void initEasyTouchViewEvent() {
        // 设置载入view WindowManager参数
        mWManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        midX = mContext.getResources().getDisplayMetrics().widthPixels / 2 - 25;
        midY = mContext.getResources().getDisplayMetrics().heightPixels / 2 - 44;
        mTouchView = LayoutInflater.from(mContext).inflate(
                R.layout.easytouch_view, null);
        mIconImageView = (ImageView) mTouchView
                .findViewById(R.id.easy_touch_view_imageview);
        mTouchView.setBackgroundColor(Color.TRANSPARENT);
        mTouchView.setOnTouchListener(mTouchListener);
        WindowManager wm = mWManager;
        LayoutParams wmParams = new LayoutParams();
        mWMParams = wmParams;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            wmParams.type = LayoutParams.TYPE_TOAST;//这个不需要SYSTEM_ALERT_WINDOW权限
        } else {
            wmParams.type = LayoutParams.TYPE_PHONE; // 这里的2002表示系统级窗口，你也可以试试2003,这个需要SYSTEM_ALERT_WINDOW权限。
        }
        wmParams.flags = 40; // 设置桌面可控
        wmParams.width = 200;
        wmParams.height = 200;
        wmParams.format = -3; // 透明
        wm.addView(mTouchView, wmParams);
    }

    private void initActionView() {
        mActionWidget = LayoutInflater.from(mContext).inflate(
                R.layout.easytouch_widget_layout, null);
        mViewPager = (ViewPager) mActionWidget.findViewById(R.id.easy_touch_viewpager);

    }
//
//    private OnClickListener mClickListener = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.show_setting_table_item_common_use_button:
//                    hideSettingTable("常用");
//                    break;
//                case R.id.show_setting_table_item_screen_lock_button:
//                    hideSettingTable("锁屏");
//                    break;
//                case R.id.show_setting_table_item_notification_button:
//                    hideSettingTable("通知");
//                    break;
//
//                case R.id.show_setting_table_item_phone_button:
//                    hideSettingTable("电话");
//                    break;
//                case R.id.show_setting_table_item_page_button:
//                    hideSettingTable("1");
//                    break;
//                case R.id.show_setting_table_item_camera_button:
//                    hideSettingTable("相机");
//                    break;
//
//                case R.id.show_setting_table_item_back_button:
//                    hideSettingTable("返回");
//                    break;
//                case R.id.show_setting_table_item_home_button:
//                    hideSettingTable("主页");
//                    break;
//                case R.id.show_setting_table_item_exit_touch_button:
//                    quitTouchView();
//                    break;
//            }
//
//        }
//    };

    private void quitTouchView() {
        hideSettingTable("退出");

        mWManager.removeView(mTouchView);
        mSerLisrener.OnCloseService(true);

        clearTimerThead();
    }

    private OnTouchListener mTouchListener = new OnTouchListener() {
        float lastX, lastY;
        int paramX, paramY;

        public boolean onTouch(View v, MotionEvent event) {
            final int action = event.getAction();
            float x = event.getRawX();
            float y = event.getRawY();
            if (mTag == 0) {
                mOldOffsetX = mWMParams.x; // 偏移量
                mOldOffsetY = mWMParams.y; // 偏移量
            }
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    motionActionDownEvent(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    motionActionMoveEvent(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    motionActionUpEvent(x, y);
                    break;
                default:
                    break;
            }
            return true;
        }

        private void motionActionDownEvent(float x, float y) {
            lastX = x;
            lastY = y;
            paramX = mWMParams.x;
            paramY = mWMParams.y;
        }

        private void motionActionMoveEvent(float x, float y) {
            int dx = (int) (x - lastX);
            int dy = (int) (y - lastY);
            mWMParams.x = paramX + dx;
            mWMParams.y = paramY + dy;
            mTag = 1;

            // 更新悬浮窗位置
            mWManager.updateViewLayout(mTouchView, mWMParams);
        }

        private void motionActionUpEvent(float x, float y) {
            int newOffsetX = mWMParams.x;
            int newOffsetY = mWMParams.y;
            if (mOldOffsetX == newOffsetX && mOldOffsetY == newOffsetY) {
                mPopuWin = new PopupWindow(mActionWidget,
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                mPopuWin.setTouchInterceptor(new OnTouchListener() {

                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                            hideSettingTable();
                            return true;
                        }
                        return false;
                    }
                });

                mPopuWin.setBackgroundDrawable(new BitmapDrawable());
                mPopuWin.setTouchable(true);
                mPopuWin.setFocusable(true);
                mPopuWin.setOutsideTouchable(true);
                mPopuWin.setContentView(mActionWidget);

                if (Math.abs(mOldOffsetX) > midX) {
                    if (mOldOffsetX > 0) {
                        mOldOffsetX = midX;
                    } else {
                        mOldOffsetX = -midX;
                    }
                }

                if (Math.abs(mOldOffsetY) > midY) {
                    if (mOldOffsetY > 0) {
                        mOldOffsetY = midY;
                    } else {
                        mOldOffsetY = -midY;
                    }
                }

                mPopuWin.setAnimationStyle(R.style.AnimationPreview);
                mPopuWin.setFocusable(true);
                mPopuWin.update();
                mPopuWin.showAtLocation(mTouchView, Gravity.CENTER,
                        -mOldOffsetX, -mOldOffsetY);
                mIconImageView.setBackgroundResource(android.R.color.transparent);
                catchSettingTableDismiss();
            } else {
                mTag = 0;
            }
        }
    };

    private void catchSettingTableDismiss() {
        mTimer = new Timer();
        mTask = new TimerTask() {

            @Override
            public void run() {
                if (mPopuWin == null || !mPopuWin.isShowing()) {
                    handler.sendEmptyMessage(0x0);
                }
            }
        };

        mTimer.schedule(mTask, 0, 100);
    }

    private void clearTimerThead() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            mIconImageView.setBackgroundResource(R.drawable.icon_easytouch_pic);
        }
    };

    private void hideSettingTable(String content) {
        hideSettingTable();
        Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
    }

    private void hideSettingTable() {
        if (null != mPopuWin) {
            mPopuWin.dismiss();
        }
    }

    public interface ServiceListener {
        void OnCloseService(boolean isClose);
    }
}
