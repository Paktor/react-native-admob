package com.sbugert.rnadmob;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.reward.RewardedVideoAd;
//import com.google.android.gms.ads.reward.RewardedVideoAdListener;
//import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class RNAdMobRewardedVideoAdModule extends ReactContextBaseJavaModule {
    RewardedAd mRewardedVideoAd;
    String adUnitID;
    String testDeviceID;
    Callback requestAdCallback;
    Callback showAdCallback;

    @Override
    public String getName() {
        return "RNAdMobRewarded";
    }

    public RNAdMobRewardedVideoAdModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    @ReactMethod
    public void setAdUnitID(String adUnitID) {
        this.adUnitID = adUnitID;
    }

    @ReactMethod
    public void setTestDeviceID(String testDeviceID) {
        this.testDeviceID = testDeviceID;
    }

    @ReactMethod
    public void requestAd(final Callback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run () {
                ArrayList<String> urls = new ArrayList<String>();
                urls.add("https://gn-event-page.soundon.fm/Web/admob/call.html");
                urls.add("https://gn-event-page.soundon.fm/Web/admob/match.html");
                AdRequest adRequest = new AdRequest.Builder().setNeighboringContentUrls(urls).build();
                RewardedAd.load(getCurrentActivity(), adUnitID,
                    adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            mRewardedVideoAd = null;

                            WritableMap event = Arguments.createMap();
                            String errorString = null;

                            switch (loadAdError.getCode()) {
                                case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                                    errorString = "ERROR_CODE_INTERNAL_ERROR";
                                    break;
                                case AdRequest.ERROR_CODE_INVALID_REQUEST:
                                    errorString = "ERROR_CODE_INVALID_REQUEST";
                                    break;
                                case AdRequest.ERROR_CODE_NETWORK_ERROR:
                                    errorString = "ERROR_CODE_NETWORK_ERROR";
                                    break;
                                case AdRequest.ERROR_CODE_NO_FILL:
                                    errorString = "ERROR_CODE_NO_FILL";
                                    break;
                            }

                            event.putString("error", errorString);
                            sendEvent("rewardedVideoDidFailToLoad", event);
                            callback.invoke(errorString);
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            mRewardedVideoAd = rewardedAd;
                            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent();
                                    sendEvent("rewardedVideoDidClose", null);
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                    super.onAdFailedToShowFullScreenContent(adError);

                                    WritableMap event = Arguments.createMap();
                                    String errorString = null;
                                    event.putString("error", adError.getMessage());

                                    sendEvent("rewardedVideoDidFailToLoad", event);
                                    callback.invoke(adError.getMessage());
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                    sendEvent("rewardedVideoDidStart", null);
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent();
                                    sendEvent("rewardedVideoDidOpen", null);
                                }
                            });

                            sendEvent("rewardedVideoDidLoad", null);
                            callback.invoke();
                        }
                    });

//                    RNAdMobRewardedVideoAdModule.this.mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getCurrentActivity());
//
//                RNAdMobRewardedVideoAdModule.this.mRewardedVideoAd.setRewardedVideoAdListener(RNAdMobRewardedVideoAdModule.this);
//
//                if (mRewardedVideoAd.isLoaded()) {
//                    callback.invoke("Ad is already loaded."); // TODO: make proper error
//                } else {
//                    requestAdCallback = callback;
//
//                    AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
//
//                    if (testDeviceID != null){
//                        if (testDeviceID.equals("EMULATOR")) {
//                            adRequestBuilder = adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
//                        } else {
//                            adRequestBuilder = adRequestBuilder.addTestDevice(testDeviceID);
//                        }
//                    }
//
//                    AdRequest adRequest = adRequestBuilder.build();
//                    mRewardedVideoAd.loadAd(adUnitID, adRequest);
//                }
            }
        });
    }

    @ReactMethod
    public void showAd(final Callback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run () {
                if (mRewardedVideoAd != null) {
                    showAdCallback = callback;
                    mRewardedVideoAd.show(getCurrentActivity(), new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            WritableMap reward = Arguments.createMap();

                            reward.putInt("amount", rewardItem.getAmount());
                            reward.putString("type", rewardItem.getType());

                            sendEvent("rewardedVideoDidRewardUser", reward);
                        }
                    });
                } else {
                    callback.invoke("Ad is not ready."); // TODO: make proper error
                }
            }
        });
    }

    @ReactMethod
    public void isReady(final Callback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run () {
                callback.invoke(mRewardedVideoAd != null);
            }
        });
    }
}
