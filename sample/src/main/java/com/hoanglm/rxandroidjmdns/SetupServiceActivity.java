package com.hoanglm.rxandroidjmdns;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hoanglm.rxandroidjmdns.jmdns_service.RxSocketService;
import com.hoanglm.rxandroidjmdns.jmdns_service.JmDNSConnector;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.RxActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.jmdns.ServiceInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;

public class SetupServiceActivity extends RxActivity {
    @BindView(R.id.peer_list)
    ListView mPeerListView;

    private RxSocketService mRxSocketService;
    private ArrayAdapter<String> adapter;
    private Observable<JmDNSConnector> mServiceConnectorObservale;
    private List<ServiceInfo> mServiceInfo;

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
        mServiceInfo = new ArrayList<>();

        mPeerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ServiceInfo info = mServiceInfo.get(position);
                String ipAddress = info.getInetAddresses()[0].getHostAddress();
                startActivity(DeviceActivity.intent(SetupServiceActivity.this, ipAddress, info.getPort()));
            }
        });

        mRxSocketService.observeServiceStateChanges()
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
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
        mServiceConnectorObservale = mRxSocketService.setup(true);
        mServiceConnectorObservale.flatMap(serviceConnector -> serviceConnector.getServiceDiscoveredChanged())
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(serviceInfos -> {
                    adapter.clear();
                    mServiceInfo.clear();
                    for (ServiceInfo info : serviceInfos) {
                        RxJmDNSLog.d("found device: %s", info.getName() + " - " + info.getServer());
                        adapter.add(info.getInet4Addresses()[0] + " - " + info.getPort());
                        mServiceInfo.add(info);
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
        mServiceConnectorObservale.flatMap(serviceConnector -> serviceConnector.restartService())
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
