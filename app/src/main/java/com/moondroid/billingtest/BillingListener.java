package com.moondroid.billingtest;

import com.android.billingclient.api.Purchase;

public interface BillingListener {
    void onSuccess(int code, Purchase purchase);
    void onError(String err);
    void onFail(int code);
}
