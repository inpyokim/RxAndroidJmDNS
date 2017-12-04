package com.hoanglm.rxandroidjmdns;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;

public class RxSubjectActivity extends AppCompatActivity {
    private BehaviorSubject<String> mBehaviorSubject = BehaviorSubject.create();
    private ReplaySubject<String> mReplaySubject = ReplaySubject.create();
    private Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_subject);
        requestGetValue();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSubscription = mBehaviorSubject.subscribe(value -> {
                    RxJmDNSLog.d("Value = %s", value); mBehaviorSubject.onCompleted();},
                ex -> RxJmDNSLog.e(ex, "onResume"),
                () -> RxJmDNSLog.d("Completed"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSubscription.unsubscribe();
    }

    private void requestGetValue() {
        RxJmDNSLog.d("Request get value");
        Observable.timer(2000, TimeUnit.MILLISECONDS)
                .subscribe(
                        value -> {
                            mBehaviorSubject.onNext(String.valueOf(System.currentTimeMillis()));
                            mReplaySubject.onNext(String.valueOf(System.currentTimeMillis()));
                        },
                        ex -> RxJmDNSLog.e(ex, "requestGetValue"));
    }
}
