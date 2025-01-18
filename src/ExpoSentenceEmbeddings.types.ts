import type { StyleProp, ViewStyle } from 'react-native';

export type OnLoadEventPayload = {
  url: string;
};

export type ExpoSentenceEmbeddingsModuleEvents = {
  onChange: (params: ChangeEventPayload) => void;
};

export type ChangeEventPayload = {
  value: string;
};

export type ExpoSentenceEmbeddingsViewProps = {
  url: string;
  onLoad: (event: { nativeEvent: OnLoadEventPayload }) => void;
  style?: StyleProp<ViewStyle>;
};

export interface EmbeddingOptions {
  maxLength?: number;  // Default: 256
  normalize?: boolean; // Default: true
}

export interface EmbeddingResult {
  embedding: number[];
  error?: string;
}
