package com.hoanglm.rxandroidjmdns;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;

import java.util.concurrent.TimeUnit;

import rx.Observable;

public class RxMergeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_merge);
        Observable firstObservable = Observable.interval(1000, TimeUnit.MILLISECONDS).map(value -> "first " + value).take(3);
        Observable secondObservable = Observable.interval(500, TimeUnit.MILLISECONDS).map(value -> "second " + value).take(3);

        findViewById(R.id.merge_with_button).setOnClickListener(view -> {
            firstObservable.mergeWith(secondObservable).subscribe(value -> RxJmDNSLog.d(value.toString()));
        });

        findViewById(R.id.merge_button).setOnClickListener(view -> {
            Observable.merge(firstObservable, secondObservable).subscribe(value -> RxJmDNSLog.d(value.toString()));
        });

        findViewById(R.id.switch_merge_button).setOnClickListener(view -> {
            firstObservable.switchMap(value -> secondObservable).subscribe(value -> RxJmDNSLog.d(value.toString()));
        });
    }
}
