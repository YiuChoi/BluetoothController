package com.doraemon.bluetoothcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.codeaurora.bluetooth.hiddtestapp.HidWrapperService;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements HidWrapperService.HidEventListener{

    private  final  String TAG=FullscreenActivity.class.getSimpleName();
    private HidWrapperService mHidWrapper;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mHidWrapper = ((HidWrapperService.LocalBinder) service).getService();
            mHidWrapper.setEventListener(FullscreenActivity.this);
            Log.e(TAG, "connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mHidWrapper = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        setTitle("等待配对");
        Intent intent = new Intent(this, HidWrapperService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            //配对的时候不要退出这个activity，否则onApplicationState=false，上面绑定的服务会暂停。具体见logcat的输出
            //可以用盒子来配对手机，或者在手机下拉通知栏开启蓝牙
            BluetoothAdapter.getDefaultAdapter().enable();

        }
        View.OnTouchListener touchListener =new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {//方法1、事件触发
                byte keycode=0;
                switch (v.getId()){
                    case R.id.btnDown:keycode=0x51;break;
                    case R.id.btnUp:keycode=0x52;break;
                    case R.id.btnLeft:keycode=0x50;break;
                    case R.id.btnRight:keycode=0x4F;break;
                }
                if (event.getAction()==MotionEvent.ACTION_DOWN){//按下
                    mHidWrapper.keyboardKeyDown( keycode);
                }else if (event.getAction()==MotionEvent.ACTION_UP){//抬起
                    mHidWrapper.keyboardKeyUp( keycode);
                }
                return false;
            }
        };
        findViewById(R.id.btnUp).setOnTouchListener(touchListener);
        findViewById(R.id.btnDown).setOnTouchListener(touchListener);
        findViewById(R.id.btnLeft).setOnTouchListener(touchListener);
        findViewById(R.id.btnRight).setOnTouchListener(touchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.v(TAG, "onDestroy");

        if (mHidWrapper != null) {
            unbindService(mConnection);
        }
    }

    @Override
    public void onApplicationState(boolean registered) {
        Log.e(TAG, "onApplicationState:"+registered);
        if (!registered){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(getApplicationContext(), "registered=false", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onPluggedDeviceChanged(BluetoothDevice device) {
        if (device==null){
            return;
        }
        final String name=device.getName();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(name);
            }
        });

    }

    @Override
    public void onConnectionState(BluetoothDevice device, boolean connected) {
        final String name=device.getName()+(connected?"已连接":"未连接");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(name);
            }
        });

    }

    @Override
    public void onProtocolModeState(boolean bootMode) {

    }

    @Override
    public void onKeyboardLedState(boolean numLock, boolean capsLock, boolean scrollLock) {

    }

//方法2、用软件模拟按下、停顿、抬起这一过程
    public void click(View view) throws InterruptedException {
        int id = view.getId();
        byte keycode=0;
        switch (id){
            case  R.id.btnHome:keycode=0x4a;
                break;
            case  R.id.btnMenu:keycode=0x65;
                break;
            case  R.id.btnPower:keycode=0x66;
                break;
            case  R.id.btnEsc:keycode=0x29;
                break;
            case R.id.btnOK:keycode=0x58;
                break;
            case R.id.btnPtrScr:keycode=0x46;
                break;
            case R.id.btnVolDown:keycode= (byte) 0x86;
                break;
            case R.id.btnVolMute:keycode= (byte) 0x84;
                break;
            case R.id.btnVolUp:keycode= (byte) 0x85;
                break;
        }
        mHidWrapper.keyboardKeyDown(keycode);
        Thread.sleep(5);
        mHidWrapper.keyboardKeyUp( keycode);
    }
}
