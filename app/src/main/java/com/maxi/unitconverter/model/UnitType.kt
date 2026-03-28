package com.maxi.unitconverter.model

enum class UnitType(
    val label: String,
    val category: ConversionCategory
) {

    // Temperature
    CELSIUS("Celsius", ConversionCategory.TEMPERATURE),
    FAHRENHEIT("Fahrenheit", ConversionCategory.TEMPERATURE),

    // Weight
    KILOGRAM("Kilogram", ConversionCategory.WEIGHT),
    POUNDS("Pounds", ConversionCategory.WEIGHT),
    OUNCES("Ounces", ConversionCategory.WEIGHT),

    // Distance
    KILOMETER("Kilometer", ConversionCategory.DISTANCE),
    MILES("Miles", ConversionCategory.DISTANCE);

    companion object {
        fun unitsForCategory(category: ConversionCategory): List<UnitType> {
            return entries.filter { it.category == category }
        }
    }
}