export interface EmbeddingOptions {
  maxLength?: number;  // Default: 256
  normalize?: boolean; // Default: true
}

export interface EmbeddingResult {
  embedding: number[];
  error?: string;
}

export type ExpoSentenceEmbeddingsModuleEvents = {
  onChange: (params: ChangeEventPayload) => void;
};

export type ChangeEventPayload = {
  value: string;
};
