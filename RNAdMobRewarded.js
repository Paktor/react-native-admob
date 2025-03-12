"use strict";

import {
  NativeModules,
  DeviceEventEmitter,
  NativeEventEmitter,
} from "react-native";

const RNAdMobRewarded = NativeModules.RNAdMobRewarded;
const eventEmitter = new NativeEventEmitter(RNAdMobRewarded);

const eventHandlers = {
  rewardedVideoDidRewardUser: new Map(),
  rewardedVideoDidLoad: new Map(),
  rewardedVideoDidFailToLoad: new Map(),
  rewardedVideoDidOpen: new Map(),
  rewardedVideoDidClose: new Map(),
};

const addEventListener = (type, handler) => {
  switch (type) {
    case "rewardedVideoDidRewardUser":
      eventHandlers[type].set(
        handler,
        eventEmitter.addListener(type, (type, amount) => {
          handler(type, amount);
        })
      );
      break;
    case "rewardedVideoDidLoad":
      eventHandlers[type].set(handler, eventEmitter.addListener(type, handler));
      break;
    case "rewardedVideoDidFailToLoad":
      eventHandlers[type].set(
        handler,
        eventEmitter.addListener(type, (error) => {
          handler(error);
        })
      );
      break;
    case "rewardedVideoDidOpen":
      eventHandlers[type].set(handler, eventEmitter.addListener(type, handler));
      break;
    case "rewardedVideoDidClose":
      eventHandlers[type].set(handler, eventEmitter.addListener(type, handler));
      break;
    default:
      console.log(`Event with type ${type} does not exist.`);
  }
};

const removeEventListener = (type, handler) => {
  if (!eventHandlers[type].has(handler)) {
    return;
  }
  eventHandlers[type].get(handler).remove();
  eventHandlers[type].delete(handler);
};

const removeAllListeners = () => {
  DeviceEventEmitter.removeAllListeners("rewardedVideoDidRewardUser");
  DeviceEventEmitter.removeAllListeners("rewardedVideoDidLoad");
  DeviceEventEmitter.removeAllListeners("rewardedVideoDidFailToLoad");
  DeviceEventEmitter.removeAllListeners("rewardedVideoDidOpen");
  DeviceEventEmitter.removeAllListeners("rewardedVideoDidClose");
};

module.exports = {
  ...RNAdMobRewarded,
  requestAd: (cb = () => {}) => RNAdMobRewarded.requestAd(cb), // requestAd callback is optional
  showAd: (cb = () => {}) => RNAdMobRewarded.showAd(cb), // showAd callback is optional
  addEventListener,
  removeEventListener,
  removeAllListeners,
};
