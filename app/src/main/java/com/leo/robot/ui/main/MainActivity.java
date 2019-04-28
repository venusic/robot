package com.leo.robot.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.leo.robot.R;
import com.leo.robot.base.NettyActivity;
import com.leo.robot.bean.AllMsg;
import com.leo.robot.bean.ErroMsg;
import com.leo.robot.constant.RobotInit;
import com.leo.robot.ui.wire_stripping.WireStrippingActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cree.mvp.util.data.SPUtils;
import cree.mvp.util.ui.ToastUtils;

/**
 * created by Leo on 2019/4/14 18 : 11
 */


public class MainActivity extends NettyActivity<MainActivityPresenter> {


    @BindView(R.id.tv_date)
    TextView mTvDate;
    @BindView(R.id.tv_signal)
    TextView mTvSignal;
    @BindView(R.id.tv_own_power)
    TextView mTvOwnPower;
    @BindView(R.id.tv_ground_power)
    TextView mTvGroundPower;
    @BindView(R.id.ibtn_wire_stripping)
    ImageButton mIbtnWireStripping;
    @BindView(R.id.ibtn_wirin)
    ImageButton mIbtnWirin;
    @BindView(R.id.ibtn_cut_line)
    ImageButton mIbtnCutLine;
    @BindView(R.id.ibtn_cleaning)
    ImageButton mIbtnCleaning;
    private boolean isShown = false;

    private FragmentManager manager = getSupportFragmentManager();


    @Override
    protected void bindingDagger2(@Nullable Bundle bundle) {
        DaggerMainActivityComponent.create().inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //实时更新时间（1秒更新一次）
        mPresenter.updateTime(mTvDate);
        initBroadcast(mTvGroundPower);
    }

    @Override
    protected void notifyData(String message) {
        SPUtils utils = new SPUtils(RobotInit.PUSH_KEY);
        utils.putString(RobotInit.PUSH_MSG, message);
    }



    @Override
    protected void onPause() {
        super.onPause();
        isShown = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isShown = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void acceptErroMsg(ErroMsg msg) {
        if (isShown) {
            ToastUtils.showShortToast(msg.getMsg());
        }
    }

    public void startActivity(Class<?> clazz) {
        startActivity(new Intent(MainActivity.this, clazz));
    }

    private long firstTime;// 记录点击返回时第一次的时间毫秒值

    /**
     * 重写该方法，判断用户按下返回按键的时候，执行退出应用方法
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {// 点击了返回按键
            if (manager.getBackStackEntryCount() != 0) {
                manager.popBackStack();
            } else {
                exitApp(2000);// 退出应用
            }
            return true;// 返回true，防止该事件继续向下传播
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出应用
     *
     * @param timeInterval 设置第二次点击退出的时间间隔
     */
    private void exitApp(long timeInterval) {
        // 第一次肯定会进入到if判断里面，然后把firstTime重新赋值当前的系统时间
        // 然后点击第二次的时候，当点击间隔时间小于2s，那么退出应用；反之不退出应用
        if (System.currentTimeMillis() - firstTime >= timeInterval) {
            ToastUtils.showShortToast("再按一次退出程序");
//            Intent intent2 = new Intent(MainActivity.this, NettyService.class);
//            stopService(intent2);// 关闭服务
            firstTime = System.currentTimeMillis();
        } else {
            finish();// 销毁当前activity
            System.exit(0);// 完全退出应用
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showMsg(AllMsg msg) {
        if (isShown) {
            ToastUtils.showShortToast(msg.getMsg() + "    " + msg.getCode());
        }
    }

    @OnClick({R.id.ibtn_wire_stripping, R.id.ibtn_wirin, R.id.ibtn_cut_line, R.id.ibtn_cleaning})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ibtn_wire_stripping:
                if (!mPresenter.isFastDoubleClick()) {
                    startActivity(WireStrippingActivity.class);
                }
                break;
            case R.id.ibtn_wirin:
                if (!mPresenter.isFastDoubleClick()) {
//                    startActivity(WiringActivity.class);
                    ToastUtils.showShortToast("接线作业！");

                }
                break;
            case R.id.ibtn_cut_line:
                if (!mPresenter.isFastDoubleClick()) {
//                    startActivity(CutLineActivity.class);
                    ToastUtils.showShortToast("剪线作业作业！");

                }
                break;
            case R.id.ibtn_cleaning:
                ToastUtils.showShortToast("清洗绝缘子作业！");

                break;
        }
    }
}
