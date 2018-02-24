package com.hoanglm.rxandroidjmdns.socket_device.connection;

import android.support.annotation.NonNull;

import com.hoanglm.rxandroidjmdns.dagger.ConnectionScope;
import com.hoanglm.rxandroidjmdns.network.TCPClient;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.hoanglm.rxandroidjmdns.utils.RxUtil;
import com.hoanglm.rxandroidjmdns.utils.SchedulerProvider;
import com.hoanglm.rxandroidjmdns.utils.StringUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;

import static com.hoanglm.rxandroidjmdns.dagger.RxSocketDeviceComponent.RxSocketDeviceModule.IP_ADDRESS;
import static com.hoanglm.rxandroidjmdns.dagger.RxSocketDeviceComponent.RxSocketDeviceModule.PORT;

@ConnectionScope
public class RxSocketConnectionImpl implements RxSocketConnection {
    private static final long WRITE_TIMEOUT = 5000;

    private final SchedulerProvider mSchedulerProvider;
    private final String ipAddress;
    private final int port;

    @Inject
    public RxSocketConnectionImpl(@Named(IP_ADDRESS) String ipAddress,
                            @Named(PORT) int port,
                            SchedulerProvider schedulerProvider) {
        mSchedulerProvider = schedulerProvider;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public Observable<byte[]> sendMessage(@NonNull byte[] data) {
        return Observable.defer(() -> {
            RxJmDNSLog.d("start ping connection ip address = %s, port = %d", ipAddress, port);
            final String response;
            try {
                response = TCPClient.sendTo(data,
                        ipAddress,
                        port);
                return Observable.just(StringUtil.convertStringToByte(response));
            } catch (IOException e) {
                e.printStackTrace();
                RxJmDNSLog.e(e, "pingConnection");
                return Observable.error(e);
            }
        })
                .compose(RxUtil.timeoutJustFirstEmit(WRITE_TIMEOUT, TimeUnit.MILLISECONDS))
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui());
    }
}
