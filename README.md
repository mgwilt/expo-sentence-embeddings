# expo-sentence-embeddings

A native Expo module for generating sentence embeddings on-device using MiniLM. This module provides efficient text embedding generation for both Android ~~and iOS~~ platforms.

**I'm still working on the swift implementation**, but the Android implementation is working and tested on Android API 35.

## Installation

```bash
# Clone the repository into your project's modules directory

git clone https://github.com/mgwilt/expo-sentence-embeddings.git modules/expo-sentence-embeddings

# Add the module to your package.json dependencies
cd modules/expo-sentence-embeddings
npm install
```

Make sure to add the module to your app.json/expo.config.js plugins array:

```json
{
  "expo": {
    "plugins": [
      ["./modules/expo-sentence-embeddings"]
    ]
  }
}
```

## Setup

### Download Model

Before using the module, you need to download the required model files. Run the provided Python script:

```bash
cd modules/expo-sentence-embeddings
python scripts/download_model.py
```

This will download the necessary model files and tokenizer to the appropriate locations in the Android ~~and iOS~~ directories.

## Usage

```typescript
import * as SentenceEmbeddings from 'expo-sentence-embeddings';

// Optional: Configure the module
await SentenceEmbeddings.configure({
  maxLength: 256, // Maximum sequence length (default: 256)
  normalize: true // Whether to L2-normalize embeddings (default: true)
});

// Generate embeddings for a single sentence
const text = "Your input text here";
const embeddings = await SentenceEmbeddings.encode(text);

// Generate embeddings for multiple sentences
const texts = ["First sentence", "Second sentence"];
const batchEmbeddings = await SentenceEmbeddings.encode(texts);
```

## API Reference

### `configure(options: ConfigureOptions): Promise<void>`

Configures the module settings.

- **Parameters:**
  - `options`: Configuration object with the following properties:
    - `maxLength`: Maximum sequence length (default: 256)
    - `normalize`: Whether to L2-normalize embeddings (default: true)

### `encode(input: string | string[]): Promise<number[] | number[][]>`

Generates embeddings for text input(s).

- **Parameters:**
  - `input`: A single text string or array of text strings
- **Returns:** A promise that resolves to:
  - For single text: An array of numbers representing the embedding vector
  - For multiple texts: An array of embedding vectors

## Technical Details

This module uses the all-MiniLM-L6-v2 model to generate sentence embeddings. The embeddings are 384-dimensional vectors that capture semantic meaning of the input text. The model runs entirely on-device, ensuring privacy and offline functionality.

The module includes basic word tokenization with special tokens ([CLS], [SEP], [UNK], [PAD], [MASK]) and handles unknown words by using the [UNK] token. Future versions may implement proper WordPiece tokenization for better handling of out-of-vocabulary words.

## License

MIT
