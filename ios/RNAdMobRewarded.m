#import "RNAdMobRewarded.h"

@implementation RNAdMobRewarded {
  NSString *_adUnitID;
    GADRewardedAd *_rewardedAd;
  RCTResponseSenderBlock _requestAdCallback;
  RCTResponseSenderBlock _showAdCallback;
}

@synthesize bridge = _bridge;

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE();

#pragma mark exported methods

RCT_EXPORT_METHOD(setAdUnitID:(NSString *)adUnitID)
{
  _adUnitID = adUnitID;
}

RCT_EXPORT_METHOD(requestAd:(RCTResponseSenderBlock)callback)
{
  _requestAdCallback = callback;
    
    GADRequest *request = [GADRequest request];

    [GADRewardedAd loadWithAdUnitID:_adUnitID request:request completionHandler:^(GADRewardedAd * _Nullable rewardedAd, NSError * _Nullable error) {
       if (error) {
         _requestAdCallback(@[[error localizedDescription]]);
       } else {
           _rewardedAd = rewardedAd;
         [self sendEventWithName:@"rewardedVideoDidLoad" body:nil];
         _requestAdCallback(@[[NSNull null]]);
       }
     }];
}

RCT_EXPORT_METHOD(showAd:(RCTResponseSenderBlock)callback)
{
  if (_rewardedAd) {
    _showAdCallback = callback;
      [_rewardedAd presentFromRootViewController:[UIApplication sharedApplication].delegate.window.rootViewController
                        userDidEarnRewardHandler:^{
        GADAdReward *reward = _rewardedAd.adReward;
        [self sendEventWithName:@"rewardedVideoDidRewardUser"
                                                        body:@{@"type": reward.type, @"amount": reward.amount}];
      }];

  }
  else {
    callback(@[@"Ad is not ready."]); // TODO: make proper error via RCTUtils.h
  }
}

RCT_EXPORT_METHOD(isReady:(RCTResponseSenderBlock)callback)
{
  callback(@[[NSNumber numberWithBool:_rewardedAd != nil]]);
}


#pragma mark delegate events

- (void)rewardedAdDidPresent:(GADRewardedAd *)rewardedAd {
  [self sendEventWithName:@"rewardedVideoDidOpen" body:nil];
  _showAdCallback(@[[NSNull null]]);
}

- (void)rewardedAdDidDismiss:(GADRewardedAd *)rewardedAd {
  [self sendEventWithName:@"rewardedVideoDidClose" body:nil];
}

- (void)rewardedAd:(GADRewardedAd *)rewardedAd userDidEarnReward:(GADAdReward *)reward {
  [self sendEventWithName:@"rewardedVideoDidRewardUser"
                                                  body:@{@"type": reward.type, @"amount": reward.amount}];
}

- (void)rewardedAd:(GADRewardedAd *)rewardedAd didFailToPresentWithError:(NSError *)error {
  [self sendEventWithName:@"rewardedVideoDidFailToLoad"
                                                  body:@{@"error": [error localizedDescription]}];
}


@end
