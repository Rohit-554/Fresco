package io.jadu.fresco.platform.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import io.jadu.fresco.platform.preprocessing.ImageTensor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer

/**
 * Android classifier backed by ONNX Runtime.
 * Loads `efficientnet_b0.onnx` from the app's assets on first inference.
 */
class AndroidFruitClassifier(private val context: Context) : FruitClassifier {

    private val mutex = Mutex()
    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    override suspend fun classify(tensor: ImageTensor): ClassificationOutput =
        withContext(Dispatchers.IO) {
            val (env, session) = ensureSession()
            val inputName = session.inputNames.first()
            val shape = tensor.shape.map { it.toLong() }.toLongArray()

            val ortTensor = OnnxTensor.createTensor(
                env,
                FloatBuffer.wrap(tensor.data),
                shape
            )

            ortTensor.use { input ->
                session.run(mapOf(inputName to input)).use { result ->
                    val output = result[0].value
                    val probabilities = when (output) {
                        is Array<*> -> {
                            // Shape [1, numClasses] — typical ONNX output
                            @Suppress("UNCHECKED_CAST")
                            (output as Array<FloatArray>)[0]
                        }
                        is FloatArray -> output
                        else -> throw IllegalStateException(
                            "Unexpected ONNX output type: ${output?.javaClass}"
                        )
                    }
                    ClassificationOutput(softmax(probabilities))
                }
            }
        }

    override fun close() {
        ortSession?.close()
        ortSession = null
        ortEnvironment?.close()
        ortEnvironment = null
    }

    private suspend fun ensureSession(): Pair<OrtEnvironment, OrtSession> = mutex.withLock {
        val env = ortEnvironment ?: OrtEnvironment.getEnvironment().also { ortEnvironment = it }
        val session = ortSession ?: run {
            val modelBytes = context.assets.open(MODEL_PATH).use { it.readBytes() }
            env.createSession(modelBytes).also { ortSession = it }
        }
        env to session
    }

    companion object {
        private const val MODEL_PATH = "model/efficientnet_b0.onnx"
    }
}

/**
 * Numerically stable softmax — handles models that output raw logits.
 * If the model already outputs probabilities, this is a no-op in practice.
 */
private fun softmax(logits: FloatArray): FloatArray {
    val max = logits.max()
    val exps = FloatArray(logits.size) { kotlin.math.exp(logits[it] - max) }
    val sum = exps.sum()
    for (i in exps.indices) exps[i] /= sum
    return exps
}
