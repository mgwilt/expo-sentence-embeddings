import * as React from 'react';

import { ExpoSentenceEmbeddingsViewProps } from './ExpoSentenceEmbeddings.types';

export default function ExpoSentenceEmbeddingsView(props: ExpoSentenceEmbeddingsViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
