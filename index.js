// @flow

import { Platform } from 'react-native'
import { NativeModules, DeviceEventEmitter, NativeEventEmitter } from 'react-native'
import waitUntil from '@cs125/wait-until'

const { RNBluetoothManager } = NativeModules;

class BluetoothManager {

  subscription: mixed;
  bluetoothState: 'unknown' | 'resetting' | 'unsupported' | 'unauthorized' | 'off' | 'on' | 'unknown';

  constructor() {
    const bluetoothEvent = new NativeEventEmitter(RNBluetoothManager);
    this.bluetoothEvent = bluetoothEvent;
    // this.subscription = bluetoothEvent.addListener('bluetoothStatus', (state) => {
    //   this.bluetoothState = state;
    // });
  }

  addEventListener(key, callback) {
    this.bluetoothEvent.addListener(key, callback);
  }

  removeEventListener(key, callback) {
    if (this.bluetoothEvent.removeListener) {
      this.bluetoothEvent.removeListener(key, callback);
    } else if (this.bluetoothEvent.removeListeners) {
      this.bluetoothEvent.removeListeners(key, callback);
    }
  }

  async state() {
    if (Platform.OS === 'ios') {
      await RNBluetoothManager.getCurrentState(); //iOS，在蓝牙管理类单例当初始化完成时查询的状态一定是unknown，查询第二次可以获取到最新的
      return RNBluetoothManager.getCurrentState();
    } else {
      return new Promise((resolve, reject) => {
        RNBluetoothManager.getBluetoothState((error, status) => {
          if (error) {
            reject(error);
            return;
          }
          resolve({ state: status ? 'on' : 'off' });
        });
      });
    }

    return new Promise((resolve, reject) => {
      if (Platform.OS === 'android') {
        RNBluetoothManager.getBluetoothState((error, status) => {
          if (error) {
            reject(error);
            return;
          }
          resolve(status);
        });
      } else if (Platform.OS === 'ios') {
        waitUntil()
          .interval(100)
          .times(10)
          .condition(() => {
            return this.bluetoothState !== undefined
          })
          .done(() => {
            resolve(this.bluetoothState === 'on');
          })
      }
    });
  };

  async enable(enabled: boolean = true) {
    return new Promise((resolve, reject) => {
      if (Platform.OS === 'android') {
        if (enabled) {
          RNBluetoothManager.setBluetoothOn((error, done) => {
            if (error) {
              reject(error);
              return;
            }
            resolve(done);
          });
        } else {
          RNBluetoothManager.setBluetoothOff((error, done) => {
            if (error) {
              reject(error);
              return;
            }
            resolve(done);
          });
        }
      } else {
        reject('Unsupported platform');
      }
    });
  }

  async disable() {
    return this.enable(false);
  };
}

export let BluetoothStatus = new BluetoothManager();
