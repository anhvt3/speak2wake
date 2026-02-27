import React, { useState, useCallback } from 'react';
import {
    View,
    Text,
    StyleSheet,
    Pressable,
    Platform,
    Alert,
} from 'react-native';
import { useRouter } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { LinearGradient } from 'expo-linear-gradient';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Colors } from '../theme/colors';

const ONBOARDED_KEY = '@speak2wake/has_onboarded';

/** Check for Speech Recognition (mic) permissions */
async function requestMicPermission(): Promise<boolean> {
    try {
        if (Platform.OS === 'web') return true;
        const SpeechRecognition = require('expo-speech-recognition');
        const result = await SpeechRecognition.requestPermissionsAsync();
        return result.granted === true || result.status === 'granted';
    } catch (e) {
        console.warn('SpeechRecognition permission error:', e);
        return false;
    }
}

/** Check TTS availability */
async function checkTTS(): Promise<boolean> {
    try {
        const Speech = require('expo-speech');
        const voices = await Speech.getAvailableVoicesAsync();
        return voices && voices.length > 0;
    } catch {
        return false;
    }
}

type Step = 'welcome' | 'mic' | 'tts' | 'done';

export default function OnboardingScreen() {
    const router = useRouter();
    const [step, setStep] = useState<Step>('welcome');
    const [micGranted, setMicGranted] = useState(false);
    const [ttsAvailable, setTtsAvailable] = useState(false);

    const handleMicPermission = useCallback(async () => {
        const granted = await requestMicPermission();
        setMicGranted(granted);
        if (!granted) {
            Alert.alert(
                'Microphone Required',
                'Speak2Wake needs microphone access for voice challenges. You can enable it later in Settings.',
                [{ text: 'Continue anyway', onPress: () => setStep('tts') }]
            );
        } else {
            setStep('tts');
        }
    }, []);

    const handleTTSCheck = useCallback(async () => {
        const available = await checkTTS();
        setTtsAvailable(available);
        setStep('done');
    }, []);

    const handleFinish = useCallback(async () => {
        await AsyncStorage.setItem(ONBOARDED_KEY, 'true');
        router.replace('/');
    }, [router]);

    return (
        <View style={styles.container}>
            <StatusBar style="light" />
            <LinearGradient
                colors={[Colors.dark.background, '#1E1224', Colors.dark.background]}
                style={StyleSheet.absoluteFill}
            />

            {step === 'welcome' && (
                <View style={styles.content}>
                    <Text style={styles.emoji}>🌅</Text>
                    <Text style={styles.title}>Welcome to{'\n'}Speak2Wake</Text>
                    <Text style={styles.subtitle}>
                        The alarm clock that wakes you up by making you speak German words.
                    </Text>
                    <Text style={styles.body}>
                        We need a few permissions to get started. Let's set things up!
                    </Text>
                    <Pressable style={styles.button} onPress={() => setStep('mic')}>
                        <LinearGradient
                            colors={[Colors.shared.gradientStart, Colors.shared.gradientEnd]}
                            start={{ x: 0, y: 0 }}
                            end={{ x: 1, y: 0 }}
                            style={styles.buttonGradient}
                        >
                            <Text style={styles.buttonText}>Get Started</Text>
                        </LinearGradient>
                    </Pressable>
                </View>
            )}

            {step === 'mic' && (
                <View style={styles.content}>
                    <Text style={styles.emoji}>🎙️</Text>
                    <Text style={styles.title}>Microphone Access</Text>
                    <Text style={styles.subtitle}>
                        We need microphone access to listen to your voice during alarm challenges.
                    </Text>
                    <Pressable style={styles.button} onPress={handleMicPermission}>
                        <LinearGradient
                            colors={[Colors.shared.gradientStart, Colors.shared.gradientEnd]}
                            start={{ x: 0, y: 0 }}
                            end={{ x: 1, y: 0 }}
                            style={styles.buttonGradient}
                        >
                            <Text style={styles.buttonText}>Grant Microphone</Text>
                        </LinearGradient>
                    </Pressable>
                </View>
            )}

            {step === 'tts' && (
                <View style={styles.content}>
                    <Text style={styles.emoji}>🔊</Text>
                    <Text style={styles.title}>Text-to-Speech</Text>
                    <Text style={styles.subtitle}>
                        Speak2Wake uses TTS to pronounce German words for you to repeat.
                    </Text>
                    <Pressable style={styles.button} onPress={handleTTSCheck}>
                        <LinearGradient
                            colors={[Colors.shared.gradientStart, Colors.shared.gradientEnd]}
                            start={{ x: 0, y: 0 }}
                            end={{ x: 1, y: 0 }}
                            style={styles.buttonGradient}
                        >
                            <Text style={styles.buttonText}>Check TTS</Text>
                        </LinearGradient>
                    </Pressable>
                </View>
            )}

            {step === 'done' && (
                <View style={styles.content}>
                    <Text style={styles.emoji}>✅</Text>
                    <Text style={styles.title}>All Set!</Text>
                    <View style={styles.statusList}>
                        <Text style={styles.statusItem}>
                            {micGranted ? '✅' : '⚠️'} Microphone: {micGranted ? 'Granted' : 'Not granted'}
                        </Text>
                        <Text style={styles.statusItem}>
                            {ttsAvailable ? '✅' : '⚠️'} Text-to-Speech: {ttsAvailable ? 'Available' : 'Limited'}
                        </Text>
                    </View>
                    <Pressable style={styles.button} onPress={handleFinish}>
                        <LinearGradient
                            colors={[Colors.shared.gradientStart, Colors.shared.gradientEnd]}
                            start={{ x: 0, y: 0 }}
                            end={{ x: 1, y: 0 }}
                            style={styles.buttonGradient}
                        >
                            <Text style={styles.buttonText}>Start Using Speak2Wake</Text>
                        </LinearGradient>
                    </Pressable>
                </View>
            )}

            {/* Step indicators */}
            <View style={styles.dots}>
                {(['welcome', 'mic', 'tts', 'done'] as Step[]).map((s) => (
                    <View
                        key={s}
                        style={[styles.dot, s === step && styles.dotActive]}
                    />
                ))}
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 32,
    },
    content: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        width: '100%',
    },
    emoji: {
        fontSize: 64,
        marginBottom: 24,
    },
    title: {
        fontSize: 32,
        fontFamily: 'Jost_600SemiBold',
        color: Colors.dark.textPrimary,
        textAlign: 'center',
        marginBottom: 16,
    },
    subtitle: {
        fontSize: 16,
        fontFamily: 'Jost_400Regular',
        color: Colors.dark.textSecondary,
        textAlign: 'center',
        lineHeight: 24,
        marginBottom: 12,
        paddingHorizontal: 16,
    },
    body: {
        fontSize: 14,
        fontFamily: 'Jost_400Regular',
        color: Colors.dark.textMuted,
        textAlign: 'center',
        lineHeight: 22,
        marginBottom: 32,
        paddingHorizontal: 24,
    },
    statusList: {
        marginBottom: 32,
        alignSelf: 'stretch',
        paddingHorizontal: 24,
    },
    statusItem: {
        fontSize: 16,
        fontFamily: 'Jost_400Regular',
        color: Colors.dark.textPrimary,
        paddingVertical: 8,
    },
    button: {
        width: '100%',
        maxWidth: 300,
        borderRadius: 16,
        overflow: 'hidden',
    },
    buttonGradient: {
        paddingVertical: 16,
        paddingHorizontal: 32,
        alignItems: 'center',
    },
    buttonText: {
        fontSize: 16,
        fontFamily: 'Jost_600SemiBold',
        color: '#FFFFFF',
    },
    dots: {
        flexDirection: 'row',
        gap: 8,
        paddingBottom: 48,
    },
    dot: {
        width: 8,
        height: 8,
        borderRadius: 4,
        backgroundColor: 'rgba(255,255,255,0.2)',
    },
    dotActive: {
        backgroundColor: Colors.shared.primary,
        width: 24,
    },
});
