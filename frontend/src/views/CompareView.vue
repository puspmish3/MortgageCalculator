<template>
  <div class="space-y-8">
    <!-- Header -->
    <div class="text-center px-4 sm:px-0">
      <h1 class="text-2xl sm:text-3xl font-bold text-gray-900">Mortgage Comparison</h1>
      <p class="mt-2 text-base sm:text-lg text-gray-600">Compare multiple mortgage options side by side</p>
    </div>

    <!-- Add Mortgage Forms -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-8">
      <div v-for="(mortgage, index) in mortgageInputs" :key="index" class="card">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-semibold text-gray-900">Mortgage Option {{ index + 1 }}</h2>
          <button
            v-if="mortgageInputs.length > 1"
            @click="removeMortgage(index)"
            class="text-red-600 hover:text-red-700 p-1"
            type="button"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        <MortgageForm
          :key="`form-${index}`"
          :initial-values="mortgageInputs[index]"
          @submit="(input) => updateMortgage(index, input)"
          @update="(input) => updateMortgage(index, input)"
          :loading="mortgageStore.isCalculating"
        />
      </div>

      <!-- Add New Mortgage Button -->
      <div v-if="mortgageInputs.length < 4" class="card flex items-center justify-center">
        <button
          @click="addMortgage"
          class="flex flex-col items-center p-8 text-gray-500 hover:text-gray-700 transition-colors"
          type="button"
        >
          <svg class="w-12 h-12 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 6v6m0 0v6m0-6h6m-6 0H6"
            />
          </svg>
          <span class="text-lg font-medium">Add Mortgage Option</span>
          <span class="text-sm">Compare up to 4 mortgages</span>
        </button>
      </div>
    </div>

    <!-- Compare Button -->
    <div v-if="canCompare" class="text-center">
      <button
        @click="compareMortgages"
        :disabled="mortgageStore.isCalculating"
        class="btn-primary px-8 py-3 text-lg disabled:opacity-50 disabled:cursor-not-allowed"
        type="button"
      >
        <span v-if="mortgageStore.isCalculating" class="flex items-center">
          <svg
            class="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              class="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              stroke-width="4"
            ></circle>
            <path
              class="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            ></path>
          </svg>
          Comparing...
        </span>
        <span v-else>Compare Mortgages</span>
      </button>
    </div>

    <!-- Comparison Results -->
    <div v-if="mortgageStore.hasComparison" class="space-y-8">
      <!-- Summary Comparison -->
      <div class="card">
        <h2 class="text-xl font-semibold text-gray-900 mb-6">Comparison Summary</h2>

        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-300">
            <thead>
              <tr>
                <th
                  class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Metric
                </th>
                <th
                  v-for="(mortgage, index) in mortgageStore.comparisonResult!.mortgages"
                  :key="index"
                  class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Option {{ index + 1 }}
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  Monthly Payment
                </td>
                <td
                  v-for="(mortgage, index) in mortgageStore.comparisonResult!.mortgages"
                  :key="index"
                  class="px-6 py-4 whitespace-nowrap text-sm text-right font-medium"
                  :class="getBestValueClass('monthlyPayment', mortgage.monthlyPayment)"
                >
                  ${{
                    mortgage.monthlyPayment.toLocaleString(undefined, { minimumFractionDigits: 2 })
                  }}
                </td>
              </tr>
              <tr class="bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  Total Interest
                </td>
                <td
                  v-for="(mortgage, index) in mortgageStore.comparisonResult!.mortgages"
                  :key="index"
                  class="px-6 py-4 whitespace-nowrap text-sm text-right font-medium"
                  :class="getBestValueClass('totalInterest', mortgage.totalInterest)"
                >
                  ${{
                    mortgage.totalInterest.toLocaleString(undefined, { minimumFractionDigits: 2 })
                  }}
                </td>
              </tr>
              <tr>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  Total Payments
                </td>
                <td
                  v-for="(mortgage, index) in mortgageStore.comparisonResult!.mortgages"
                  :key="index"
                  class="px-6 py-4 whitespace-nowrap text-sm text-right font-medium text-gray-900"
                >
                  {{ mortgage.totalPayments }}
                </td>
              </tr>
              <tr class="bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  Loan Amount
                </td>
                <td
                  v-for="(mortgage, index) in mortgageStore.comparisonResult!.mortgages"
                  :key="index"
                  class="px-6 py-4 whitespace-nowrap text-sm text-right font-medium text-gray-900"
                >
                  ${{
                    mortgage.summary.loanAmount.toLocaleString(undefined, {
                      minimumFractionDigits: 2,
                    })
                  }}
                </td>
              </tr>
              <tr>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  Interest Rate
                </td>
                <td
                  v-for="(mortgage, index) in mortgageStore.comparisonResult!.mortgages"
                  :key="index"
                  class="px-6 py-4 whitespace-nowrap text-sm text-right font-medium text-gray-900"
                >
                  {{ mortgage.summary.interestRate }}%
                </td>
              </tr>
              <tr class="bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  Loan Term
                </td>
                <td
                  v-for="(mortgage, index) in mortgageStore.comparisonResult!.mortgages"
                  :key="index"
                  class="px-6 py-4 whitespace-nowrap text-sm text-right font-medium text-gray-900"
                >
                  {{ mortgage.summary.loanTermYears }} years
                </td>
              </tr>
              <tr>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  Payment Frequency
                </td>
                <td
                  v-for="(mortgage, index) in mortgageStore.comparisonResult!.mortgages"
                  :key="index"
                  class="px-6 py-4 whitespace-nowrap text-sm text-right font-medium text-gray-900"
                >
                  {{ mortgage.summary.paymentFrequency }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Visual Comparison Chart -->
      <div class="card">
        <ComparisonChart :comparison="mortgageStore.comparisonResult!" />
      </div>

      <!-- Amortization Schedules Comparison -->
      <div class="space-y-8">
        <div class="text-center">
          <h2 class="text-2xl font-bold text-gray-900 mb-2">Amortization Schedules</h2>
          <p class="text-gray-600">Compare payment breakdowns for each mortgage option</p>
        </div>

        <div
          v-for="(mortgage, index) in mortgageStore.comparisonResult!.mortgages"
          :key="index"
          class="card"
        >
          <div class="mb-6">
            <h3 class="text-lg font-semibold text-gray-900 mb-2">
              Option {{ index + 1 }} - Amortization Schedule
            </h3>
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 p-3 sm:p-4 bg-gray-50 rounded-lg text-xs sm:text-sm">
              <div>
                <span class="text-gray-600">Loan Amount:</span>
                <span class="font-medium ml-2"
                  >${{
                    mortgage.summary.loanAmount.toLocaleString(undefined, {
                      minimumFractionDigits: 2,
                    })
                  }}</span
                >
              </div>
              <div>
                <span class="text-gray-600">Interest Rate:</span>
                <span class="font-medium ml-2">{{ mortgage.summary.interestRate }}%</span>
              </div>
              <div>
                <span class="text-gray-600">Term:</span>
                <span class="font-medium ml-2">{{ mortgage.summary.loanTermYears }} years</span>
              </div>
              <div>
                <span class="text-gray-600">Monthly Payment:</span>
                <span class="font-medium ml-2"
                  >${{
                    mortgage.monthlyPayment.toLocaleString(undefined, { minimumFractionDigits: 2 })
                  }}</span
                >
              </div>
            </div>
          </div>
          <AmortizationTable
            :key="`amortization-${index}-${comparisonCounter}-${mortgage.amortizationSchedule?.length || 0}`"
            :schedule="mortgage.amortizationSchedule"
          />
        </div>
      </div>

      <!-- Export Comparison -->
      <div class="card">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-medium text-gray-900">Export Comparison</h3>
          <ExportButton :data="mortgageStore.comparisonResult!" type="comparison" />
        </div>
        <p class="text-sm text-gray-600">
          Download your mortgage comparison as PDF or Excel document.
        </p>
      </div>
    </div>

    <!-- Error Display -->
    <div v-if="mortgageStore.error" class="bg-red-50 border border-red-200 rounded-lg p-4">
      <div class="flex">
        <div class="flex-shrink-0">
          <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
            <path
              fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
              clip-rule="evenodd"
            />
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
import { ref, computed, onMounted, watch } from 'vue'
import { useMortgageStore } from '@/stores/mortgage'
import {
  MortgageType,
  PaymentFrequency,
  BuydownType,
  AdditionalPaymentFrequency,
  type MortgageInput,
} from '@/types/mortgage'
import MortgageForm from '@/components/MortgageForm.vue'
import ComparisonChart from '@/components/ComparisonChart.vue'
import AmortizationTable from '@/components/AmortizationTable.vue'
import ExportButton from '@/components/ExportButton.vue'

const mortgageStore = useMortgageStore()

// Force reactivity counter
const comparisonCounter = ref(0)

const mortgageInputs = ref<MortgageInput[]>([
  {
    loanAmount: 400000,
    interestRate: 6.5,
    loanTermYears: 30,
    downPayment: 100000,
    propertyValue: 500000,
    mortgageType: MortgageType.FIXED,
    paymentFrequency: PaymentFrequency.MONTHLY,
    buydownType: BuydownType.NONE,
    additionalPrincipalPayment: 0,
    additionalPaymentFrequency: AdditionalPaymentFrequency.MONTHLY,
  },
  {
    loanAmount: 400000,
    interestRate: 6.0,
    loanTermYears: 15,
    downPayment: 100000,
    propertyValue: 500000,
    mortgageType: MortgageType.FIXED,
    paymentFrequency: PaymentFrequency.MONTHLY,
    buydownType: BuydownType.TWO_ONE,
    additionalPrincipalPayment: 200,
    additionalPaymentFrequency: AdditionalPaymentFrequency.MONTHLY,
  },
])

const canCompare = computed(() => {
  return (
    mortgageInputs.value.length >= 2 &&
    mortgageInputs.value.every(
      (input) => input.loanAmount > 0 && input.interestRate > 0 && input.loanTermYears > 0,
    )
  )
})

const addMortgage = () => {
  if (mortgageInputs.value.length < 4) {
    mortgageInputs.value.push({
      loanAmount: 400000,
      interestRate: 6.5,
      loanTermYears: 30,
      downPayment: 100000,
      propertyValue: 500000,
      mortgageType: MortgageType.FIXED,
      paymentFrequency: PaymentFrequency.MONTHLY,
      buydownType: BuydownType.NONE,
      additionalPrincipalPayment: 0,
      additionalPaymentFrequency: AdditionalPaymentFrequency.MONTHLY,
    })
  }
}

const removeMortgage = (index: number) => {
  if (mortgageInputs.value.length > 1) {
    mortgageInputs.value.splice(index, 1)
  }
}

const updateMortgage = (index: number, input: MortgageInput) => {
  console.log(`Updating mortgage ${index} with:`, input)
  mortgageInputs.value[index] = { ...input }
}

const compareMortgages = async () => {
  console.log('Comparing mortgages with inputs:', mortgageInputs.value)
  await mortgageStore.compareMortgages(mortgageInputs.value)
  comparisonCounter.value++
  console.log('Comparison completed, result:', mortgageStore.comparisonResult)
}

const getBestValueClass = (metric: string, value: number): string => {
  if (!mortgageStore.comparisonResult) return 'text-gray-900'

  const values = mortgageStore.comparisonResult.mortgages.map((m) => {
    switch (metric) {
      case 'monthlyPayment':
        return m.monthlyPayment
      case 'totalInterest':
        return m.totalInterest
      default:
        return value
    }
  })

  const minValue = Math.min(...values)
  const isMinimum = Math.abs(value - minValue) < 0.01

  return isMinimum ? 'text-green-600' : 'text-gray-900'
}

// Ensure forms are initialized properly on mount
onMounted(() => {
  console.log('CompareView mounted with initial mortgageInputs:', mortgageInputs.value)
})

// Watch for changes in comparison results
watch(
  () => mortgageStore.comparisonResult,
  (newResult) => {
    if (newResult) {
      console.log('ComparisonResult updated with new data:', {
        mortgageCount: newResult.mortgages.length,
        firstMortgageScheduleLength: newResult.mortgages[0]?.amortizationSchedule?.length,
        firstMortgageFirstRate: newResult.mortgages[0]?.amortizationSchedule?.[0]?.interestRate,
      })
    }
  },
  { deep: true },
)
</script>
