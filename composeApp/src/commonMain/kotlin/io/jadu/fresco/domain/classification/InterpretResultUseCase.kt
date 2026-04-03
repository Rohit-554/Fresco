package io.jadu.fresco.domain.classification

import io.jadu.fresco.platform.ml.ClassificationOutput

class InterpretResultUseCase(private val interpreter: ResultInterpreter) {
    operator fun invoke(output: ClassificationOutput): List<ClassificationResult> =
        interpreter.interpret(output)
}
