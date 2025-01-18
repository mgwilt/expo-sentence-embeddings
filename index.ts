// Reexport the native module. On web, it will be resolved to ExpoSentenceEmbeddingsModule.web.ts
// and on native platforms to ExpoSentenceEmbeddingsModule.ts
export { default } from './src/ExpoSentenceEmbeddingsModule';
export { default as ExpoSentenceEmbeddingsView } from './src/ExpoSentenceEmbeddingsView';
export * from  './src/ExpoSentenceEmbeddings.types';
