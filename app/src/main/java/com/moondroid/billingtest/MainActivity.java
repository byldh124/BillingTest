package com.moondroid.billingtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements BillingListener{

    final String TAG = "BillingCheck";

    private GoogleBillingImpl googleBilling;
    private GoogleBillingSubImpl googleBillingSub;
    private BillingClient mBillingClient;

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tv);

        googleBilling = new GoogleBillingImpl(this, this);
        googleBilling.init();
        googleBillingSub = new GoogleBillingSubImpl(this, this);
        googleBillingSub.init();
    }

    public void clickProd01(View view) {
        googleBilling.purchase(this, "production_01");
    }

    public void clickProd02(View view) {
        googleBilling.purchase(this, "production_02");
    }
    public void clickProd03(View view) { googleBillingSub.purchase(this, "re_product_01");}

    @Override
    public void onSuccess(int code, Purchase purchase) {
        Log.e(TAG, "orderId : " + purchase.getOrderId() +
        "\npurchaseState : " + purchase.isAcknowledged());
    }

    @Override
    public void onError(String err) {
        Log.e(TAG, err);
    }

    @Override
    public void onFail(int code) {
        Log.e(TAG, "return code : " + code);
    }


}
