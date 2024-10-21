#import "RNAdMobInterstitial.h"
#import <GoogleMobileAds/GoogleMobileAds.h>
#import <React/RCTEventEmitter.h>

@implementation RNAdMobInterstitial

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
    return @[@"interstitialDidLoad", @"interstitialDidFailToLoad", @"interstitialDidOpen", @"interstitialDidClose", @"interstitialWillLeaveApplication"];
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(setAdUnitID:(NSString *)adUnitID)
{
  _adUnitID = adUnitID;
}

RCT_EXPORT_METHOD(requestAd:(RCTResponseSenderBlock)callback)
{
    _requestAdCallback = callback;
    GADRequest *request = [GADRequest request];
    [GADInterstitialAd loadWithAdUnitID:_adUnitID request:request completionHandler:^(GADInterstitialAd * _Nullable interstitialAd, NSError * _Nullable error) {
    if (error) {
      callback(@[[error localizedDescription]]);
      return;
    }
    _interstitialAd = interstitialAd;
    _interstitialAd.fullScreenContentDelegate = self;
    callback(@[[NSNull null]]);
  }];
}

RCT_EXPORT_METHOD(showAd:(RCTResponseSenderBlock)callback)
{
  if (_interstitialAd) {
    _showAdCallback = callback;
    [_interstitialAd presentFromRootViewController:[UIApplication sharedApplication].delegate.window.rootViewController];
  }
  else {
    callback(@[@"Ad is not ready."]); // TODO: make proper error via RCTUtils.h
  }
}

RCT_EXPORT_METHOD(isReady:(RCTResponseSenderBlock)callback)
{
  callback(@[[NSNumber numberWithBool:_interstitialAd != nil]]);
}


#pragma mark - GADFullScreenContentDelegate

- (void)adDidRecordImpression:(id<GADFullScreenPresentingAd>)ad {
  [self sendEventWithName:@"interstitialDidLoad" body:nil];
}

- (void)ad:(id<GADFullScreenPresentingAd>)ad didFailToPresentFullScreenContentWithError:(NSError *)error {
  [self sendEventWithName:@"interstitialDidFailToLoad" body:@{@"name": [error localizedDescription]}];
  _requestAdCallback(@[[error localizedDescription]]);
}

- (void)adWillPresentFullScreenContent:(id<GADFullScreenPresentingAd>)ad {
  [self sendEventWithName:@"interstitialDidOpen" body:nil];
  _showAdCallback(@[[NSNull null]]);
}

- (void)adDidDismissFullScreenContent:(id<GADFullScreenPresentingAd>)ad {
  [self sendEventWithName:@"interstitialDidClose" body:nil];
}

- (void)adWillLeaveApplication:(id<GADFullScreenPresentingAd>)ad {
  [self sendEventWithName:@"interstitialWillLeaveApplication" body:nil];
}

@end
