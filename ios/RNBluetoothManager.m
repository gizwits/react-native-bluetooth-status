//
//  RNBluetoothManager.m
//  taskuparkkiReactNativeWorkShop
//
//  Created by Juha Linnanen on 20/03/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "UIKit/UIKit.h"
#import "RNBluetoothManager.h"

#define SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(v)  ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)


@implementation RNBluetoothManager
{
    bool hasListeners;
    NSString *stateName;
}

#pragma mark Initialization

//- (instancetype)init
//{
//    if (self = [super init]) {
//        self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:dispatch_get_main_queue() options:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:0] forKey:CBCentralManagerOptionShowPowerAlertKey]];
//    }
//
//    return self;
//}

- (CBCentralManager *)centralManager
{
  if (!_centralManager) {
    _centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:dispatch_get_main_queue() options:@{CBCentralManagerOptionShowPowerAlertKey: @(NO)}];
  }
  return _centralManager;
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(initialize) {
    [self centralManagerDidUpdateState:self.centralManager];
}

- (NSString *) centralManagerStateToString: (int)state
{
    switch (state) {
        case CBCentralManagerStateUnknown:
            return @"unknown";
        case CBCentralManagerStateResetting:
            return @"resetting";
        case CBCentralManagerStateUnsupported:
            return @"unsupported";
        case CBCentralManagerStateUnauthorized:
            return @"unauthorized";
        case CBCentralManagerStatePoweredOff:
            return @"off";
        case CBCentralManagerStatePoweredOn:
            return @"on";
        default:
            return @"unknown";
    }

    return @"unknown";
}

-(void)startObserving {
    hasListeners = YES;
    [self sendEventWithName:@"bluetoothStatus" body:stateName];
}

-(void)stopObserving {
    hasListeners = NO;
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central
{
    stateName = [self centralManagerStateToString:central.state];
    if (hasListeners) {
        [self sendEventWithName:@"bluetoothStatus" body:stateName];
    }
}

- (NSArray<NSString *> *)supportedEvents { return @[@"bluetoothStatus"]; }

- (NSString *)getConnectionState
{
    return [self centralManagerStateToString:self.centralManager.state];
}

RCT_EXPORT_METHOD(getCurrentState:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
  if (!self.centralManager) {
    NSError * error;
    reject(@"no_bluetooth_init", @"Bluetooth manager cannot be initialized", error);
  } else {
    resolve(@{@"state": [self getConnectionState]});
  }

}
@end


