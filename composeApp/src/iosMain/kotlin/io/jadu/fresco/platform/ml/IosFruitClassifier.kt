package io.jadu.fresco.platform.ml

import coremlhelper.CoreMLHelper_createFloat32MultiArray
import coremlhelper.CoreMLHelper_extractFloats
import io.jadu.fresco.platform.preprocessing.ImageTensor
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.CoreML.MLDictionaryFeatureProvider
import platform.CoreML.MLFeatureValue
import platform.CoreML.MLModel
import platform.CoreML.MLModelConfiguration
import platform.CoreML.MLMultiArray
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.NSURL

/**
 * iOS classifier backed by Core ML.
 * Loads a compiled `.mlmodelc` bundle from the app's main bundle on first inference.
 *
 * Uses a cinterop helper for MLMultiArray creation since K2 Kotlin/Native
 * doesn't expose `initWithShape:dataType:error:` as a Kotlin constructor.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosFruitClassifier : FruitClassifier {

    private val mutex = Mutex()
    private var model: MLModel? = null

    override suspend fun classify(tensor: ImageTensor): ClassificationOutput =
        withContext(Dispatchers.IO) {
            val mlModel = ensureModel()

            val multiArray = createMultiArray(tensor)
            val featureValue = MLFeatureValue.featureValueWithMultiArray(multiArray)

            val inputProvider = MLDictionaryFeatureProvider(
                dictionary = mapOf(INPUT_NAME to featureValue),
                error = null
            )

            val prediction = mlModel.predictionFromFeatures(inputProvider, error = null)
                ?: throw RuntimeException("Inference failed")

            val outputFeature = prediction.featureValueForName(OUTPUT_NAME)
                ?: throw RuntimeException("Output '$OUTPUT_NAME' not found in model prediction")

            val outputArray = outputFeature.multiArrayValue
                ?: throw RuntimeException("Output is not a multi-array")

            val probabilities = extractFloatArray(outputArray)
            ClassificationOutput(softmax(probabilities))
        }

    override fun close() {
        model = null
    }

    private suspend fun ensureModel(): MLModel = mutex.withLock {
        model ?: run {
            val url = NSBundle.mainBundle.URLForResource(MODEL_NAME, withExtension = "mlmodelc")
                ?: throw RuntimeException("Model '$MODEL_NAME.mlmodelc' not found in app bundle")
            val loaded = loadCompiledModel(url)
            model = loaded
            loaded
        }
    }

    private fun loadCompiledModel(url: NSURL): MLModel {
        val config = MLModelConfiguration()
        return MLModel.modelWithContentsOfURL(url, configuration = config, error = null)
            ?: throw RuntimeException("Failed to load Core ML model")
    }

    private fun createMultiArray(tensor: ImageTensor): MLMultiArray {
        val shape = tensor.shape.map { NSNumber(int = it) }
        return tensor.data.usePinned { pinned ->
            CoreMLHelper_createFloat32MultiArray(
                shape = shape,
                floatData = pinned.addressOf(0),
                floatCount = tensor.data.size.toLong()
            ) ?: throw RuntimeException("Failed to create MLMultiArray")
        }
    }

    private fun extractFloatArray(array: MLMultiArray): FloatArray {
        val count = array.count().toInt()
        val result = FloatArray(count)
        result.usePinned { pinned ->
            CoreMLHelper_extractFloats(
                array = array,
                outBuffer = pinned.addressOf(0),
                bufferSize = count.toLong()
            )
        }
        return result
    }

    companion object {
        private const val MODEL_NAME = "EfficientNetB0"
        private const val INPUT_NAME = "input"
        private const val OUTPUT_NAME = "output"
    }
}

private fun softmax(logits: FloatArray): FloatArray {
    val max = logits.max()
    val exps = FloatArray(logits.size) { kotlin.math.exp(logits[it] - max) }
    val sum = exps.sum()
    for (i in exps.indices) exps[i] /= sum
    return exps
}
