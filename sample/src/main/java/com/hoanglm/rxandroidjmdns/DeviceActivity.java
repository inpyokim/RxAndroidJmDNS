package com.hoanglm.rxandroidjmdns;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hoanglm.rxandroidjmdns.socket_device.RxSocketDevice;
import com.hoanglm.rxandroidjmdns.socket_device.connection.RxSocketConnection;
import com.hoanglm.rxandroidjmdns.utils.ConnectionSharingAdapter;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.hoanglm.rxandroidjmdns.utils.StringUtil;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.RxActivity;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class DeviceActivity extends RxActivity {
    private static final String IP_ADDRESS = "IP_ADDRESS";
    private static final String PORT = "PORT";

    public static Intent intent(Context context,
                                String ipAddress,
                                int port) {
        Intent intent = new Intent(context, DeviceActivity.class);
        intent.putExtra(IP_ADDRESS, ipAddress);
        intent.putExtra(PORT, port);
        return intent;
    }

    @BindView(R.id.ip_address_txt)
    TextView mAddressTextView;

    @BindView(R.id.connect_btn)
    Button mConnectButton;

    @BindView(R.id.status_connection_txt)
    TextView mStatusConnectionTextView;

    @BindView(R.id.send_edt)
    EditText mSendEditText;

    private String ipAddress;
    private int port;
    private RxSocketDevice mRxSocketDevice;
    private Observable<RxSocketConnection> mRxSocketConnectionObservable;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        ButterKnife.bind(this);

        ipAddress = getIntent().getStringExtra(IP_ADDRESS);
        port = getIntent().getIntExtra(PORT, 0);
        mRxSocketDevice = ((MainApplication) getApplication()).getRxSocketService().getSocketDevice(ipAddress, port);
        mRxSocketConnectionObservable = prepareConnectionObservable();
        mAddressTextView.setText(String.format(Locale.US, "host = %s-%d", ipAddress, port));
        mRxSocketDevice.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(rxSocketConnectionState -> {
            RxJmDNSLog.d("Connection state -> " + rxSocketConnectionState);
            mStatusConnectionTextView.setText(rxSocketConnectionState.toString());
        });
    }

    private Observable<RxSocketConnection> prepareConnectionObservable() {
        return mRxSocketDevice
                .establishConnection(getApplicationContext())
                .takeUntil(disconnectTriggerSubject)
                .compose(new ConnectionSharingAdapter());
    }

    @OnClick(R.id.connect_btn)
    void connectClicked() {
        if (isConnected()) {
            disconnectTriggerSubject.onNext(null);
        } else {
            mRxSocketConnectionObservable
                    .doOnSubscribe(() -> RxJmDNSLog.d("CONNECTING"))
                    .compose(bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(rxSocketConnection ->
                                    RxJmDNSLog.d("Connect success"),
                            throwable -> RxJmDNSLog.e(throwable, "connect failed"),
                            () -> RxJmDNSLog.d("Connect completed")
                    );
        }
    }

    @OnClick(R.id.send_btn)
    void sendClicked() {
        String text = mSendEditText.getText().toString();
        mRxSocketConnectionObservable
                .doOnSubscribe(() -> RxJmDNSLog.d("send " + text))
                .flatMap(rxSocketConnection -> rxSocketConnection.sendMessage(StringUtil.convertStringToByte(text)))
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(data -> {
                    RxJmDNSLog.d("send success " + StringUtil.convertByteToString(data));
                }, throwable -> RxJmDNSLog.e(throwable, "send failed"));
    }

    @OnClick(R.id.setup_receive_message_btn)
    void setupReceiveMessageClicled() {
        mRxSocketConnectionObservable
                .doOnSubscribe(() -> RxJmDNSLog.d("setup receive messsage"))
                .flatMap(rxSocketConnection -> rxSocketConnection.setupReceivedMessage())
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(data -> {
                    RxJmDNSLog.d("receive data: %s", StringUtil.convertByteToString(data));
                }, throwable -> RxJmDNSLog.e(throwable, "receive failed"));
    }

    private boolean isConnected() {
        return mRxSocketDevice.getConnectionState() == RxSocketConnection.RxSocketConnectionState.CONNECTED;
    }
}
