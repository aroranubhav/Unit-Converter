package com.maxi.unitconverter.ui.viewmodel

import com.maxi.unitconverter.model.ConversionCategory
import com.maxi.unitconverter.model.UnitType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UnitConverterViewModelTest {

    private lateinit var viewModel: UnitConverterViewModel

    @Before
    fun setup() {
        viewModel = UnitConverterViewModel()
    }

    // --- convert(): Temperature ---

    @Test
    fun `celsius to fahrenheit`() {
        val result = viewModel.convert(100.0, UnitType.CELSIUS, UnitType.FAHRENHEIT)
        assertEquals(212.0, result!!, 0.01)
    }

    @Test
    fun `celsius to fahrenheit - negative value`() {
        val result = viewModel.convert(-40.0, UnitType.CELSIUS, UnitType.FAHRENHEIT)
        assertEquals(-40.0, result!!, 0.01)
    }

    @Test
    fun `fahrenheit to celsius`() {
        val result = viewModel.convert(98.6, UnitType.FAHRENHEIT, UnitType.CELSIUS)
        assertEquals(37.0, result!!, 0.01)
    }

    @Test
    fun `fahrenheit to celsius - freezing point`() {
        val result = viewModel.convert(32.0, UnitType.FAHRENHEIT, UnitType.CELSIUS)
        assertEquals(0.0, result!!, 0.01)
    }

    // --- convert(): Weight ---

    @Test
    fun `kilogram to pounds`() = runTest {
        val result = viewModel.convert(1.0, UnitType.KILOGRAM, UnitType.POUNDS)
        assertEquals(2.20462, result!!, 0.001)
    }

    @Test
    fun `kilogram to ounces`() {
        val result = viewModel.convert(1.0, UnitType.KILOGRAM, UnitType.OUNCES)
        assertEquals(35.274, result!!, 0.001)
    }

    @Test
    fun `pounds to kilogram`() {
        val result = viewModel.convert(1.0, UnitType.POUNDS, UnitType.KILOGRAM)
        assertEquals(0.453592, result!!, 0.001)
    }

    @Test
    fun `pounds to ounces`() {
        val result = viewModel.convert(1.0, UnitType.POUNDS, UnitType.OUNCES)
        assertEquals(16.0, result!!, 0.001)
    }

    @Test
    fun `ounces to kilogram`() {
        val result = viewModel.convert(1.0, UnitType.OUNCES, UnitType.KILOGRAM)
        assertEquals(0.0283495, result!!, 0.001)
    }

    @Test
    fun `ounces to pounds`() {
        val result = viewModel.convert(16.0, UnitType.OUNCES, UnitType.POUNDS)
        assertEquals(1.0, result!!, 0.001)
    }

    // --- convert(): Distance ---

    @Test
    fun `kilometer to miles`() = runTest {
        val result = viewModel.convert(1.0, UnitType.KILOMETER, UnitType.MILES)
        assertEquals(0.621371, result!!, 0.001)
    }

    @Test
    fun `miles to kilometer`() {
        val result = viewModel.convert(1.0, UnitType.MILES, UnitType.KILOMETER)
        assertEquals(1.60934, result!!, 0.001)
    }

    // --- convert(): Edge Cases --

    @Test
    fun `same unit conversion returns same value`() {
        val result = viewModel.convert(10.0, UnitType.KILOGRAM, UnitType.KILOGRAM)
        assertEquals(10.0, result!!, 0.0)
    }

    @Test
    fun `cross category conversion returns null`() {
        val result = viewModel.convert(1.0, UnitType.CELSIUS, UnitType.MILES)
        assertNull(result)
    }

    @Test
    fun `zero value conversion`() {
        val result = viewModel.convert(0.0, UnitType.KILOGRAM, UnitType.POUNDS)
        assertEquals(0.0, result!!, 0.0)
    }

    // --- onInputChanged() ---

    @Test
    fun `valid input update result`() {
        viewModel.onInputChanged("100")
        assertNotNull(viewModel.result.value)
        assert(viewModel.result.value.isNotEmpty())
    }

    @Test
    fun `invalid input sets error and clears result`() {
        viewModel.onInputChanged("alpha")
        assertEquals("Invalid input", viewModel.error.value)
        assertEquals("", viewModel.result.value)
    }

    @Test
    fun `blank input clears result and error`() {
        viewModel.onInputChanged("")
        assertEquals("", viewModel.result.value)
        assertNull(viewModel.error.value)
    }

    // --- onCategoryChanged() ---

    @Test
    fun `category change resets input and result`() {
        viewModel.onInputChanged("100")
        viewModel.onCategoryChanged(ConversionCategory.WEIGHT)
        assertEquals("", viewModel.inputValue.value)
        assertEquals("", viewModel.result.value)
    }

    @Test
    fun `category change updates fromUnit and toUnit to category defaults`() {
        viewModel.onCategoryChanged(ConversionCategory.WEIGHT)
        assertEquals(UnitType.KILOGRAM, viewModel.fromUnit.value)
        assertEquals(UnitType.POUNDS, viewModel.toUnit.value)
    }

    @Test
    fun `category change clears error`() {
        viewModel.onInputChanged("abc")
        viewModel.onCategoryChanged(ConversionCategory.DISTANCE)
        assertNull(viewModel.error.value)
    }


    // --- onFromUnitChanged() / onToUnitChanged() ---

    @Test
    fun `changing fromUnit recalculates result`() {
        viewModel.onInputChanged("100")
        val resultBefore = viewModel.result.value
        viewModel.onFromUnitChanged(UnitType.FAHRENHEIT)
        val resultAfter = viewModel.result.value
        assert(resultBefore != resultAfter)
    }

    @Test
    fun `changing toUnit recalculates result`() {
        viewModel.onInputChanged("100")
        val resultBefore = viewModel.result.value
        viewModel.onToUnitChanged(UnitType.CELSIUS)
        val resultAfter = viewModel.result.value
        assert(resultBefore != resultAfter)
    }
}