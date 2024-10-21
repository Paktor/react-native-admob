#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTEventDispatcher.h>
#else
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTEventDispatcher.h>
#endif

@import GoogleMobileAds;

@interface RNAdMobInterstitial : RCTEventEmitter <RCTBridgeModule, GADFullScreenContentDelegate>

    @property (nonatomic, strong) GADInterstitialAd *interstitialAd;
    @property (nonatomic, strong) NSString *adUnitID;
    @property (nonatomic, copy) RCTResponseSenderBlock requestAdCallback;
    @property (nonatomic, copy) RCTResponseSenderBlock showAdCallback;

@end
