import { requireNativeModule } from 'expo-modules-core';

/**
 * The native ExpoAlarmEngine module.
 * Exposes Android alarm scheduling via AlarmManager with
 * foreground service, boot persistence, and lock screen support.
 */
const ExpoAlarmEngineModule = requireNativeModule('ExpoAlarmEngine');

export default ExpoAlarmEngineModule;
