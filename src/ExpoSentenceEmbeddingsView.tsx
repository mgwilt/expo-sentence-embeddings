import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoSentenceEmbeddingsViewProps } from './ExpoSentenceEmbeddings.types';

const NativeView: React.ComponentType<ExpoSentenceEmbeddingsViewProps> =
  requireNativeView('ExpoSentenceEmbeddings');

export default function ExpoSentenceEmbeddingsView(props: ExpoSentenceEmbeddingsViewProps) {
  return <NativeView {...props} />;
}
