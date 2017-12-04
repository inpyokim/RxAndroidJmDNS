package com.hoanglm.rxandroidjmdns;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hoanglm.rxandroidjmdns.connection.RxSocketService;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;

import java.util.LinkedList;

import javax.jmdns.ServiceInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FrontPageActivity extends Activity {
    @BindView(R.id.peer_list)
    ListView mPeerListView;

    private RxSocketService mRxSocketService;
    private ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);

        mRxSocketService = ((MainApplication) getApplication()).getRxSocketService();

        adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1,
                new LinkedList<>());
        mPeerListView.setAdapter(adapter);

        mRxSocketService.observeServiceStateChanges()
                .subscribe(rxSocketServiceState -> {
                    RxJmDNSLog.d("socket service state %s", rxSocketServiceState.toString());
            switch (rxSocketServiceState) {
                case READY:
                    break;
                case SETUP_SUCCESS:
                case RESTART_SUCCESS:
                    break;
                case STOP_SUCCESS:
                    break;
            }
        });
        mRxSocketService.setup(true)
                .subscribe(success -> {
                    RxJmDNSLog.d("setup service is success %b", success);
                }, throwable -> RxJmDNSLog.e(throwable, "setup service is failed"));
        mRxSocketService.getOnServiceInfoDiscovery()
                .subscribe(serviceInfos -> {
                    adapter.clear();
                    for (ServiceInfo info : serviceInfos) {
                        adapter.add(info.getName());
                    }
                    adapter.notifyDataSetChanged();
                }, throwable -> RxJmDNSLog.e(throwable, "getOnServiceInfoDiscovery>>"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRxSocketService.stop();
    }

    @OnClick(R.id.ok_button)
    public void okClicked() {
        mRxSocketService.restart()
                .doOnSubscribe(() -> findViewById(R.id.ok_button).setEnabled(false))
                .doOnNext(success -> findViewById(R.id.ok_button).setEnabled(true))
                .doOnUnsubscribe(() -> findViewById(R.id.ok_button).setEnabled(true))
                .subscribe(success -> {
                    RxJmDNSLog.d("restart service is success %b", success);
                }, throwable -> RxJmDNSLog.e(throwable, "restart service is failed"));
    }

    @OnClick(R.id.ok_cancel)
    public void cancelClicked() {
        mRxSocketService.stop();
    }
}
