<template>
  <div class="space-y-8">
    <!-- Header -->
    <div class="text-center">
      <h1 class="text-3xl font-bold text-gray-900">Mortgage Calculator</h1>
      <p class="mt-2 text-lg text-gray-600">
        Calculate your mortgage payments and view detailed amortization schedules
      </p>
    </div>

    <!-- Main Calculator -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
      <!-- Input Form -->
      <div class="card">
        <h2 class="text-xl font-semibold text-gray-900 mb-6">Loan Details</h2>
        
        <MortgageForm 
          @submit="handleCalculation"
          :loading="mortgageStore.isCalculating"
        />
      </div>

      <!-- Results -->
      <div class="card" v-if="mortgageStore.hasCalculation">
        <h2 class="text-xl font-semibold text-gray-900 mb-6">Calculation Results</h2>
        
        <MortgageResults 
          :calculation="mortgageStore.currentCalculation!"
          @export-pdf="handlePdfExport"
          @export-excel="handleExcelExport"
        />
      </div>

      <!-- Placeholder when no calculation -->
      <div class="card flex items-center justify-center" v-else>
        <div class="text-center">
          <div class="w-16 h-16 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
            <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z"></path>
            </svg>
          </div>
          <h3 class="text-lg font-medium text-gray-900">Ready to Calculate</h3>
          <p class="text-gray-500">Enter your loan details to see results</p>
        </div>
      </div>
    </div>

    <!-- Amortization Schedule -->
    <div class="card" v-if="mortgageStore.hasCalculation">
      <AmortizationTable 
        :schedule="mortgageStore.currentCalculation!.amortizationSchedule"
      />
    </div>

    <!-- Payment Chart -->
    <div class="card" v-if="mortgageStore.hasCalculation">
      <PaymentChart 
        :schedule="mortgageStore.currentCalculation!.amortizationSchedule"
      />
    </div>

    <!-- Error Display -->
    <div v-if="mortgageStore.error" class="bg-red-50 border border-red-200 rounded-lg p-4">
      <div class="flex">
        <div class="flex-shrink-0">
          <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
          </svg>
        </div>
        <div class="ml-3">
          <h3 class="text-sm font-medium text-red-800">Error</h3>
          <p class="mt-1 text-sm text-red-700">{{ mortgageStore.error }}</p>
          <button 
            @click="mortgageStore.clearError()"
            class="mt-2 text-sm text-red-600 hover:text-red-500"
          >
            Dismiss
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMortgageStore } from '@/stores/mortgage'
import type { MortgageInput } from '@/types/mortgage'
import MortgageForm from '@/components/MortgageForm.vue'
import MortgageResults from '@/components/MortgageResults.vue'
import AmortizationTable from '@/components/AmortizationTable.vue'
import PaymentChart from '@/components/PaymentChart.vue'

const mortgageStore = useMortgageStore()

const handleCalculation = async (input: MortgageInput) => {
  await mortgageStore.calculateMortgage(input)
}

const handlePdfExport = async () => {
  if (mortgageStore.currentCalculation) {
    // For now, use a simple ID. In real app, this would come from the backend
    const calculationId = 'current'
    await mortgageStore.exportToPdf(calculationId, true)
  }
}

const handleExcelExport = async () => {
  if (mortgageStore.currentCalculation) {
    // For now, use a simple ID. In real app, this would come from the backend
    const calculationId = 'current'
    await mortgageStore.exportToExcel(calculationId)
  }
}
</script>