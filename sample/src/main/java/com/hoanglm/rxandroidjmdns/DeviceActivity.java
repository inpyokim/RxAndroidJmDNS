package com.hoanglm.rxandroidjmdns;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.hoanglm.rxandroidjmdns.socket_device.RxSocketDevice;
import com.hoanglm.rxandroidjmdns.socket_device.connection.RxSocketConnection;
import com.hoanglm.rxandroidjmdns.utils.ConnectionSharingAdapter;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.trello.rxlifecycle.components.RxActivity;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
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
        mRxSocketDevice.observeConnectionStateChanges().subscribe(rxSocketConnectionState -> RxJmDNSLog.d("Connection state -> " +rxSocketConnectionState));

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
                    .subscribe(rxSocketConnection ->
                                    RxJmDNSLog.d("Connect success"),
                            throwable -> RxJmDNSLog.e(throwable, "connect failed"),
                            () -> RxJmDNSLog.d("Connect completed")
                    );
        }
    }

    private boolean isConnected() {
        return mRxSocketDevice.getConnectionState() == RxSocketConnection.RxSocketConnectionState.CONNECTED;
    }
}
