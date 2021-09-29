package com.moondroid.billingtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MyBillingImpl myBilling;

    private GoogleBillingImpl googleBilling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBilling = new MyBillingImpl(this);
        googleBilling = new GoogleBillingImpl(this);
        googleBilling.init();
    }

    public void clickProd01(View view) {
        googleBilling.purchase(this, "production_01");
    }

    public void clickProd02(View view) {
        googleBilling.purchase(this, "production_02");
    }

    @Override
    protected void onStop() {
        Toast.makeText(this, "Process Stopped", Toast.LENGTH_SHORT).show();
        super.onStop();
    }
}
