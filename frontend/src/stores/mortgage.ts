import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { 
  MortgageInput, 
  MortgageCalculation, 
  MortgageComparison
} from '@/types/mortgage'
import { MortgageApiService, downloadBlob } from '@/services/mortgageApi'
import { ExportService } from '@/services/exportService'

export const useMortgageStore = defineStore('mortgage', () => {
  // State
  const currentCalculation = ref<MortgageCalculation | null>(null)
  const comparisonResult = ref<MortgageComparison | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const mortgageInputs = ref<MortgageInput[]>([])

  // Getters
  const hasCalculation = computed(() => currentCalculation.value !== null)
  const hasComparison = computed(() => comparisonResult.value !== null)
  const isCalculating = computed(() => isLoading.value)

  // Actions
  const calculateMortgage = async (input: MortgageInput): Promise<void> => {
    try {
      isLoading.value = true
      error.value = null
      
      const result = await MortgageApiService.calculateMortgage(input)
      currentCalculation.value = result
      
      // Store input for potential comparison
      if (!mortgageInputs.value.find(m => JSON.stringify(m) === JSON.stringify(input))) {
        mortgageInputs.value.push(input)
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to calculate mortgage'
      console.error('Mortgage calculation error:', err)
    } finally {
      isLoading.value = false
    }
  }

  const compareMortgages = async (mortgages: MortgageInput[]): Promise<void> => {
    try {
      isLoading.value = true
      error.value = null
      
      const result = await MortgageApiService.compareMortgages(mortgages)
      comparisonResult.value = result
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to compare mortgages'
      console.error('Mortgage comparison error:', err)
    } finally {
      isLoading.value = false
    }
  }

  const exportToPdf = async (calculationId: string, includeChart: boolean = true): Promise<void> => {
    try {
      isLoading.value = true
      if (!currentCalculation.value) {
        throw new Error('No calculation available for export')
      }
      await ExportService.exportMortgagePdf(currentCalculation.value)
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to export PDF'
      console.error('PDF export error:', err)
    } finally {
      isLoading.value = false
    }
  }

  const exportToExcel = async (calculationId: string): Promise<void> => {
    try {
      isLoading.value = true
      if (!currentCalculation.value) {
        throw new Error('No calculation available for export')
      }
      await ExportService.exportMortgageExcel(currentCalculation.value)
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to export Excel'
      console.error('Excel export error:', err)
    } finally {
      isLoading.value = false
    }
  }

  const clearCalculation = (): void => {
    currentCalculation.value = null
    error.value = null
  }

  const clearComparison = (): void => {
    comparisonResult.value = null
    error.value = null
  }

  const clearError = (): void => {
    error.value = null
  }

  const addMortgageInput = (input: MortgageInput): void => {
    if (mortgageInputs.value.length < 5) { // Limit to 5 comparisons
      mortgageInputs.value.push(input)
    }
  }

  const removeMortgageInput = (index: number): void => {
    mortgageInputs.value.splice(index, 1)
  }

  const clearAllInputs = (): void => {
    mortgageInputs.value = []
  }

  return {
    // State
    currentCalculation,
    comparisonResult,
    isLoading,
    error,
    mortgageInputs,
    
    // Getters
    hasCalculation,
    hasComparison,
    isCalculating,
    
    // Actions
    calculateMortgage,
    compareMortgages,
    exportToPdf,
    exportToExcel,
    clearCalculation,
    clearComparison,
    clearError,
    addMortgageInput,
    removeMortgageInput,
    clearAllInputs
  }
})