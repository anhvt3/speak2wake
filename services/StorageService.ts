import AsyncStorage from '@react-native-async-storage/async-storage';

const SCHEMA_VERSION = 1;
const KEYS = {
  ALARMS: '@speak2wake/alarms',
  SETTINGS: '@speak2wake/settings',
  SCHEMA: '@speak2wake/schema_version',
} as const;

export const StorageService = {
  async get<T>(key: string): Promise<T | null> {
    const raw = await AsyncStorage.getItem(key);
    if (raw === null) return null;
    return JSON.parse(raw) as T;
  },

  async set<T>(key: string, value: T): Promise<void> {
    await AsyncStorage.setItem(key, JSON.stringify(value));
  },

  async remove(key: string): Promise<void> {
    await AsyncStorage.removeItem(key);
  },

  async getSchemaVersion(): Promise<number> {
    const version = await this.get<number>(KEYS.SCHEMA);
    return version ?? 0;
  },

  async setSchemaVersion(version: number): Promise<void> {
    await this.set(KEYS.SCHEMA, version);
  },

  async initialize(): Promise<void> {
    const currentVersion = await this.getSchemaVersion();
    if (currentVersion < SCHEMA_VERSION) {
      // Future: run migrations here
      await this.setSchemaVersion(SCHEMA_VERSION);
    }
  },

  KEYS,
};
