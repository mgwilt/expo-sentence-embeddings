import { requireNativeModule } from 'expo-modules-core';
import { EmbeddingOptions, EmbeddingResult } from './ExpoSentenceEmbeddings.types';

interface ExpoSentenceEmbeddingsModule {
  // Main embedding function
  encode(sentences: string | string[]): Promise<number[][]>;
  
  // Configuration
  configure(options: EmbeddingOptions): Promise<void>;
}

const module = requireNativeModule<ExpoSentenceEmbeddingsModule>('ExpoSentenceEmbeddings');
export default module;
