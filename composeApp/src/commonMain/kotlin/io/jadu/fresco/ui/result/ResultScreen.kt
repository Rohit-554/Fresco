package io.jadu.fresco.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.jadu.fresco.domain.classification.ClassificationResult
import io.jadu.fresco.domain.food.FoodInfo

@Composable
fun ResultScreen(
    results: List<ClassificationResult>,
    foodInfo: FoodInfo?,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        ClassificationHeader(results)

        if (foodInfo != null) {
            foodInfo.nutrition?.let { nutrition ->
                Spacer(Modifier.height(20.dp))
                NutritionCard(nutrition)
            }

            if (foodInfo.recipes.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                RecipeSection(foodInfo.recipes)
            }
        }

        if (results.size > 1) {
            Spacer(Modifier.height(20.dp))
            OtherPredictions(results.drop(1))
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetake,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retake")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ClassificationHeader(results: List<ClassificationResult>) {
    if (results.isEmpty()) {
        Text(
            text = "No confident predictions",
            style = MaterialTheme.typography.headlineSmall
        )
        return
    }

    val top = results.first()
    Text(
        text = top.label.replaceFirstChar { it.uppercase() },
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(4.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "${(top.confidence * 100).toInt()}% confidence",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (top.isFruitOrVegetable) {
            Text(
                text = "Fruit / Vegetable",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun OtherPredictions(others: List<ClassificationResult>) {
    Text(
        text = "Other predictions",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(8.dp))
    others.forEach { result ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = result.label.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${(result.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
