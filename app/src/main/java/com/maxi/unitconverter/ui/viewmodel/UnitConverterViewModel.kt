package com.maxi.unitconverter.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.maxi.unitconverter.model.ConversionCategory
import com.maxi.unitconverter.model.UnitType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnitConverterViewModel: ViewModel() {

    private val _selectedCategory = MutableStateFlow<ConversionCategory>(ConversionCategory.TEMPERATURE)
    val selectedCategory
        get() = _selectedCategory.asStateFlow()

    private val _inputValue = MutableStateFlow("")
    val inputValue
        get() = _inputValue.asStateFlow()

    private val _fromUnit = MutableStateFlow(UnitType.CELSIUS)
    val fromUnit
        get() = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow(UnitType.FAHRENHEIT)
    val toUnit
        get() = _toUnit.asStateFlow()

    private val _result = MutableStateFlow("")
    val result
        get() = _result.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error
        get() = _error.asStateFlow()

    fun onCategoryChanged(category: ConversionCategory) {
        _selectedCategory.value =  category
        _inputValue.value = ""
        _result.value = ""
        _error.value = null

        val units = UnitType.unitsForCategory(category)
        _fromUnit.value = units[0]
        _toUnit.value = units[1]
    }

    fun onInputChanged(input: String) {
        _inputValue.value = input
        calculate(input, _fromUnit.value, _toUnit.value)
    }

    fun onFromUnitChanged(unit: UnitType) {
        _fromUnit.value = unit
        calculate(_inputValue.value, unit, _toUnit.value)
    }

    fun onToUnitChanged(unit: UnitType) {
        _toUnit.value = unit
        calculate(_inputValue.value, _fromUnit.value, unit)
    }

    private fun calculate(input: String, from: UnitType, to: UnitType) {
        if (input.isBlank()) {
            _result.value = ""
            _error.value = null
            return
        }

        val input = input.toDoubleOrNull()

        if (input == null) {
            _error.value = "Invalid input"
            _result.value = ""
            return
        }

        val convertedValue = convert(input, from, to)
        if (convertedValue == null) {
            _error.value = "Conversion not supported"
            _result.value = ""
            return
        }

        _error.value = null
        _result.value = "%.4f".format(convertedValue)
    }

    fun convert(value: Double, from: UnitType, to: UnitType): Double? {
        if (from == to) return value

        return when {
            // Temperature
            from == UnitType.CELSIUS     && to == UnitType.FAHRENHEIT -> (value * 9 / 5) + 32
            from == UnitType.FAHRENHEIT  && to == UnitType.CELSIUS    -> (value - 32) * 5 / 9

            // Weight
            from == UnitType.KILOGRAM    && to == UnitType.POUNDS     -> value * 2.20462
            from == UnitType.KILOGRAM    && to == UnitType.OUNCES     -> value * 35.274
            from == UnitType.POUNDS      && to == UnitType.KILOGRAM   -> value / 2.20462
            from == UnitType.POUNDS      && to == UnitType.OUNCES     -> value * 16
            from == UnitType.OUNCES      && to == UnitType.KILOGRAM   -> value / 35.274
            from == UnitType.OUNCES      && to == UnitType.POUNDS     -> value / 16

            // Distance
            from == UnitType.KILOMETER   && to == UnitType.MILES      -> value * 0.621371
            from == UnitType.MILES       && to == UnitType.KILOMETER  -> value / 0.621371

            else -> null
        }
    }

}