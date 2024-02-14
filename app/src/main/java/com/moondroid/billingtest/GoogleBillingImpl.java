package com.moondroid.billingtest;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.SkuDetails;

import java.util.ArrayList;
import java.util.List;

public class GoogleBillingImpl implements PurchasesUpdatedListener {
    private static final String TAG = "GoogleBillingImpl";

    //결제 client
    private final BillingClient mBillingClient;

    //상품 목록 list
    private List<ProductDetails> productDetailsList = new ArrayList<>();
    private Context mContext;
    //결제 완료시 Callback
    private BillingListener mBillingListener;


    public GoogleBillingImpl(@NonNull final Context context, BillingListener billingListener) {
        mContext = context;
        mBillingListener = billingListener;

        //BillingClient Instance
        mBillingClient = BillingClient.newBuilder(mContext)
                .enablePendingPurchases()
                .setListener(this)
                .build();
    }

    /**
     * BillingClient를 세팅하는 작업
     * - 구글 플레이 연결
     * - 상품목록 생성
     */
    public void init() {
        try {
            mBillingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        // BillingClient가 구글플레이와 연결이 되면 상품목록을 넣어넣음.
                        List<String> strList = new ArrayList<>();
                        strList.add("inapp_dokkabi");
                        strList.add("inapp_peterpan");
                        //strList.add("re_product_01");

                        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                                .setProductList(List.of(
                                        QueryProductDetailsParams.Product.newBuilder().setProductId("inapp_dokkabi").setProductType(BillingClient.ProductType.INAPP).build(),
                                        QueryProductDetailsParams.Product.newBuilder().setProductId("inapp_peterpan").setProductType(BillingClient.ProductType.INAPP).build())
                                ).build();

                        //SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                        //params.setSkusList(strList).setType(BillingClient.SkuType.INAPP);  //인앱상품 (정기결제 = SUBS)
                        //params.setSkusList(strList).setType(BillingClient.SkuType.SUBS);  //인앱상품 (정기결제 = SUBS)

                        mBillingClient.queryProductDetailsAsync(
                                queryProductDetailsParams,
                                new ProductDetailsResponseListener() {
                                    @Override
                                    public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                                        // Process the result.
                                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                            if (list.isEmpty()) {
                                                Log.d(TAG, "list is zero");
                                            } else {
                                                Log.e(TAG, list.toString());
                                                productDetailsList = list;
                                            }
                                        } else {
                                            mBillingListener.onFail(GlobalKey.PURCHASE_FAIL_CODE.PURCHASE_FAIL_SKU_LIST);
                                        }
                                    }
                                }
                        );

                        /*mBillingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                             @Nullable List<SkuDetails> list) {
                                // Process the result.
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                                        && list != null) {
                                    if (list.isEmpty()) {
                                        Log.d(TAG, "list is zero");
                                    } else {
                                        skuDetailsList = list;
                                    }
                                } else {
                                    mBillingListener.onFail(GlobalKey.PURCHASE_FAIL_CODE.PURCHASE_FAIL_SKU_LIST);
                                }
                            }
                        });*/
                    } else {
                        mBillingListener.onFail(GlobalKey.PURCHASE_FAIL_CODE.PURCHASE_FAIL_BILLING_SETUP);
                        Log.d(TAG, "google purchase error");
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.

                    // 구글 플레이와 연결이 끊어졌을 경우 다시 연결하는 프로세스를 진행한다.
                    Log.d(TAG, "onBillingServiceDisconnected");
                }
            });
        } catch (Exception e) {
            mBillingListener.onError(e.toString());
        }

    }

    /**
     * 실제 버튼(상품)을 눌렀을때 구글플레이에 구매를 요청하는 함수
     */
    public void purchase(Activity activity, String productId) {
        try {
            // 구매자가 요청한 상품의 productId를 가져와 BillingFlowParams에 저장하고
            // BillingClient에게 구매 프로세스 시작을 요청한다.

            ProductDetails productDetails = getProductDetails(productId);
            if (productDetails != null) {
                List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = List.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails).build()
                );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

                BillingResult billingResult = mBillingClient.launchBillingFlow(activity, billingFlowParams);
            } else {
                Log.d(TAG, "sku is null");
                mBillingListener.onFail(GlobalKey.PURCHASE_FAIL_CODE.PURCHASE_FAIL_NO_SKU);
            }
        } catch (Exception e) {
            mBillingListener.onError(e.toString());
        }
    }

    /**
     * 인앱결제에서 구매가 이루어 졌는지 확인하는 Listener Method (PurchaseUpdatedListener)
     */
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        try {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && list != null) {
                for (Purchase purchase : list) {
                    // 사용자가 구매 버튼을 누르면 구매 완료 프로세스 호출
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                Log.d(TAG, "user purchase cancel");
                mBillingListener.onFail(GlobalKey.PURCHASE_FAIL_CODE.PURCHASE_FAIL_USER_CANCEL);
            } else {
                // Handle any other error codes.
                Log.d(TAG, "google purchase error");
                mBillingListener.onFail(GlobalKey.PURCHASE_FAIL_CODE.PURCHASE_FAIL_PURCHASE_FAIL);
            }
        } catch (Exception e) {
            mBillingListener.onError(e.toString());
        }
    }

    /**
     * 소비성 구매 정보 확인
     */
    private void handlePurchase(final Purchase purchase) {
        try {
            String purchaseToken, payLoad;
            purchaseToken = purchase.getPurchaseToken();
            payLoad = purchase.getDeveloperPayload();

            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                ConsumeParams consumeParams =
                        ConsumeParams.newBuilder()
                                .setPurchaseToken(purchaseToken)
                                .build();

                mBillingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                        int resultCode = billingResult.getResponseCode();
                        if (resultCode == BillingClient.BillingResponseCode.OK) {
                            //구매과 완료되고 onSuccess 콜백 메소드 호출
                            Log.d(TAG, "google purchase success");
                            Log.e(TAG, purchase.getOriginalJson());
                            mBillingListener.onSuccess(resultCode, purchase);
                        } else {
                            Log.d(TAG, "google purchase consume error 01 : " + resultCode);
                            mBillingListener.onFail(GlobalKey.PURCHASE_FAIL_CODE.PURCHASE_FAIL_NO_CONSUME_INFO);
                        }
                    }
                });
            }
        } catch (Exception e) {
            mBillingListener.onError(e.toString());
        }
    }

    /**
     * 정기 결제 상품 구매 확정
     */
    private void confirmPurchase(final Purchase purchase) {
        try {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                        @Override
                        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                            mBillingListener.onSuccess(billingResult.getResponseCode(), purchase);
                        }
                    });
                }
            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                //구매유예
            } else {
                //구매확정 취소됨(기타 다양한 사유)
                mBillingListener.onFail(GlobalKey.PURCHASE_FAIL_CODE.PURCHASE_FAIL_IN_ACKNOWLEDGE);
            }
        } catch (Exception e) {
            mBillingListener.onError(e.toString());
        }
    }

    private ProductDetails getProductDetails(String productId) {
        try {
            for (ProductDetails item : productDetailsList) {
                if (item.getProductId().equals(productId)) {
                    return item;
                }
            }
        } catch (Exception e) {
            mBillingListener.onError(e.toString());
        }
        return null;
    }
}
