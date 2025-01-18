import { registerWebModule, NativeModule } from 'expo';

import { ChangeEventPayload } from './ExpoSentenceEmbeddings.types';

type ExpoSentenceEmbeddingsModuleEvents = {
  onChange: (params: ChangeEventPayload) => void;
}

class ExpoSentenceEmbeddingsModule extends NativeModule<ExpoSentenceEmbeddingsModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
};

export default registerWebModule(ExpoSentenceEmbeddingsModule);
