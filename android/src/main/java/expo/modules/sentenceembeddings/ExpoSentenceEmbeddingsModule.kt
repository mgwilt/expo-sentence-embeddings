package expo.modules.sentenceembeddings

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import org.pytorch.IValue
import org.pytorch.Module as TorchModule
import org.pytorch.Tensor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.CodedException
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Expo module for generating sentence embeddings using a MiniLM model.
 * This module provides functionality to encode text into dense vector representations
 * that can be used for semantic similarity comparisons and other NLP tasks.
 */
class ExpoSentenceEmbeddingsModule : Module() {
  // Maximum sequence length for input text (including special tokens)
  private var maxLength: Int = 256
  
  // Whether to L2-normalize the output embeddings
  private var normalize: Boolean = true
  
  // PyTorch model instance
  private var model: TorchModule? = null
  
  // Mapping of words to token IDs for the tokenizer
  private var tokenizerVocab: Map<String, Int>? = null
  
  // Special tokens used by the model with their corresponding IDs
  private var specialTokens: Map<String, Int> = mapOf(
    "[PAD]" to 0,   // Padding token
    "[UNK]" to 100, // Unknown token for words not in vocabulary
    "[CLS]" to 101, // Classification token added at start
    "[SEP]" to 102, // Separator token added at end
    "[MASK]" to 103 // Mask token (unused in inference)
  )

  // Coroutine scope for async operations
  private val moduleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  override fun definition() = ModuleDefinition {
    Name("ExpoSentenceEmbeddings")

    // Configure module settings like max length and normalization
    AsyncFunction("configure") { options: Map<String, Any>, promise: Promise ->
      try {
        maxLength = options["maxLength"] as? Int ?: 256
        normalize = options["normalize"] as? Boolean ?: true
        promise.resolve(null)
      } catch (e: Exception) {
        promise.reject("CONFIG_ERROR", e.message, e)
      }
    }

    // Main function to generate embeddings from text input
    // Accepts either a single string or array of strings
    AsyncFunction("encode") { input: Any, promise: Promise ->
      moduleScope.launch(Dispatchers.Default) {
        try {
          val result = when (input) {
            is String -> encodeText(listOf(input))
            is List<*> -> encodeText(input.filterIsInstance<String>())
            else -> throw IllegalArgumentException("Input must be a string or array of strings")
          }
          promise.resolve(result)
        } catch (e: Exception) {
          promise.reject("ENCODING_ERROR", e.message, e)
        }
      }
    }
  }

  /**
   * Generates embeddings for a list of input texts.
   * The process involves:
   * 1. Tokenizing each text
   * 2. Converting tokens to model input format
   * 3. Running the model inference
   * 4. Post-processing embeddings (mean pooling and optional normalization)
   */
  private suspend fun encodeText(texts: List<String>): List<FloatArray> = withContext(Dispatchers.Default) {
    ensureModelLoaded()
    
    texts.map { text ->
      // Convert text to token IDs, adding special tokens for model input
      val tokens = tokenize(text)
      val inputIds = intArrayOf(specialTokens["[CLS]"]!!) + tokens + intArrayOf(specialTokens["[SEP]"]!!)
      
      // Create PyTorch tensor from token IDs
      val inputTensor = Tensor.fromBlob(
        inputIds,
        longArrayOf(1, inputIds.size.toLong())
      )

      // Run model inference
      val output = model?.forward(IValue.from(inputTensor))?.toTensor()
        ?: throw Exception("Failed to get embeddings")
      
      // Average the token embeddings, excluding special tokens
      val embeddings = meanPool(output)
      
      // Optionally normalize the embedding vector
      if (normalize) {
        normalizeVector(embeddings)
      } else {
        embeddings
      }
    }
  }

  /**
   * Converts input text to token IDs using basic word tokenization.
   * For production use, consider implementing proper WordPiece tokenization.
   */
  private fun tokenize(text: String): IntArray {
    return text.lowercase()
      .split(Regex("\\s+"))
      .take(maxLength - 2) // Reserve space for [CLS] and [SEP]
      .flatMap { word -> 
        if (tokenizerVocab?.containsKey(word) == true) {
          listOf(tokenizerVocab!![word]!!)
        } else {
          // For unknown words, use the [UNK] token
          // TODO: Implement proper subword tokenization
          listOf(specialTokens["[UNK]"]!!)
        }
      }
      .toIntArray()
  }

  /**
   * Computes mean pooling over token embeddings, excluding [CLS] and [SEP] tokens.
   * This creates a fixed-size sentence embedding from variable-length input.
   */
  private fun meanPool(tensor: Tensor): FloatArray {
    val shape = tensor.shape()
    val data = tensor.dataAsFloatArray
    val seqLen = shape[1].toInt()
    val hiddenSize = shape[2].toInt()
    val result = FloatArray(hiddenSize)

    for (i in 0 until hiddenSize) {
      var sum = 0f
      // Average embeddings from tokens 1 to n-1, skipping [CLS] and [SEP]
      for (j in 1 until seqLen - 1) {
        sum += data[j * hiddenSize + i]
      }
      result[i] = sum / (seqLen - 2)
    }

    return result
  }

  /**
   * L2-normalizes a vector by dividing by its magnitude.
   * This makes cosine similarity calculations simpler.
   */
  private fun normalizeVector(vector: FloatArray): FloatArray {
    val magnitude = sqrt(vector.map { it * it }.sum())
    return if (magnitude > 0) {
      vector.map { it / magnitude }.toFloatArray()
    } else {
      vector
    }
  }

  /**
   * Ensures model and tokenizer are loaded before first use.
   * Loads from app assets if not already loaded.
   */
  private suspend fun ensureModelLoaded() = withContext(Dispatchers.IO) {
    if (model == null) {
      try {
        val modelFile = loadFile("all-minilm-l6-v2.pt")
        model = TorchModule.load(modelFile.absolutePath)
        loadTokenizerVocab()
      } catch (e: Exception) {
        throw Exception("Failed to load model or tokenizer: ${e.message}")
      }
    }
  }

  /**
   * Loads the tokenizer vocabulary from assets.
   * The vocab file should be a text file with one token per line.
   */
  private fun loadTokenizerVocab() {
    val vocabFile = appContext.reactContext?.assets?.open("tokenizer/vocab.txt")?.bufferedReader()
      ?: throw Exception("Could not open tokenizer vocabulary file")
    tokenizerVocab = vocabFile.readLines()
      .mapIndexed { index, token -> token to index }
      .toMap()
  }

  /**
   * Copies a file from assets to the app's private storage.
   * This is needed because PyTorch requires a file path to load models.
   */
  private fun loadFile(assetName: String): File {
    val context = appContext.reactContext ?: throw Exception("React context is null")
    val file = File(context.filesDir, assetName)
    if (!file.exists()) {
      context.assets.open(assetName).use { input ->
        FileOutputStream(file).use { output ->
          input.copyTo(output)
        }
      }
    }
    return file
  }

  private fun sqrt(x: Float): Float = kotlin.math.sqrt(x)
}
