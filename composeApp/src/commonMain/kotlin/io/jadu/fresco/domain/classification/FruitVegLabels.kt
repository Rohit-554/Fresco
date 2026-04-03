package io.jadu.fresco.domain.classification

/**
 * 36 fruit and vegetable class labels from the ResNet-50 model
 * fine-tuned on the Fruits & Vegetables Image Recognition dataset.
 *
 * All classes are fruits or vegetables by definition.
 */
object FruitVegLabels {

    private val labels: Array<String> = arrayOf(
        "apple",
        "banana",
        "beetroot",
        "bell pepper",
        "cabbage",
        "capsicum",
        "carrot",
        "cauliflower",
        "chilli pepper",
        "corn",
        "cucumber",
        "eggplant",
        "garlic",
        "ginger",
        "grapes",
        "jalepeno",
        "kiwi",
        "lemon",
        "lettuce",
        "mango",
        "onion",
        "orange",
        "paprika",
        "pear",
        "peas",
        "pineapple",
        "pomegranate",
        "potato",
        "raddish",
        "soy beans",
        "spinach",
        "sweetcorn",
        "sweetpotato",
        "tomato",
        "turnip",
        "watermelon",
    )

    val size: Int get() = labels.size

    fun labelFor(index: Int): String =
        if (index in labels.indices) labels[index] else "unknown"

    /** All 36 classes are fruits or vegetables. */
    fun isFruitOrVegetable(index: Int): Boolean = index in labels.indices
}
