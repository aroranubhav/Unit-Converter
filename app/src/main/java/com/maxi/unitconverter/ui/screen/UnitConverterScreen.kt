@file:OptIn(ExperimentalMaterial3Api::class)

package com.maxi.unitconverter.ui.screen


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maxi.unitconverter.model.ConversionCategory
import com.maxi.unitconverter.model.UnitType
import com.maxi.unitconverter.ui.viewmodel.UnitConverterViewModel

@Composable
fun UnitConverterScreen(
    modifier: Modifier,
    viewModel: UnitConverterViewModel = viewModel(),
) {
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val inputValue by viewModel.inputValue.collectAsStateWithLifecycle()
    val fromUnit by viewModel.fromUnit.collectAsStateWithLifecycle()
    val toUnit by viewModel.toUnit.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val availableUnits = UnitType.unitsForCategory(selectedCategory)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {

        // Title
        Text(
            text = "Unit Converter",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 16.dp)
        )

        // Category Selector
        CategorySelector(
            selectedCategory = selectedCategory,
            onCategorySelected = viewModel::onCategoryChanged
        )

        // Input Field
        OutlinedTextField(
            value = inputValue,
            onValueChange = viewModel::onInputChanged,
            label = { Text("Enter value") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = error != null,
            supportingText = {
                if (error != null) Text(error!!)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // From / To Unit Dropdowns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UnitDropdown(
                label = "From",
                selectedUnit = fromUnit,
                units = availableUnits,
                onUnitSelected = viewModel::onFromUnitChanged,
                modifier = Modifier.weight(1f)
            )

            UnitDropdown(
                label = "To",
                selectedUnit = toUnit,
                units = availableUnits,
                onUnitSelected = viewModel::onToUnitChanged,
                modifier = Modifier.weight(1f)
            )
        }

        // Result
        AnimatedVisibility(
            visible = result.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ResultCard(
                result = result,
                toUnit = toUnit
            )
        }
    }
}

@Composable
fun CategorySelector(
    selectedCategory: ConversionCategory,
    onCategorySelected: (ConversionCategory) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ConversionCategory.entries.forEach { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = {
                    onCategorySelected(category)
                },
                label = {
                    Text(
                        text = category.name
                            .lowercase()
                            .replaceFirstChar { it.uppercase() }
                    )
                }
            )
        }
    }
}

@Composable
fun UnitDropdown(
    label: String,
    selectedUnit: UnitType,
    units: List<UnitType>,
    onUnitSelected: (UnitType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedUnit.label,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.label) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ResultCard(result: String, toUnit: UnitType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = result,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = toUnit.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}