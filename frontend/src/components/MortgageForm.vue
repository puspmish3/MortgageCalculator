<template>
  <form @submit.prevent="handleSubmit" class="space-y-8">
    <!-- Header -->
    <div class="text-center mb-8">
      <h2 class="section-subheader">Mortgage Details</h2>
      <p class="text-gray-600">Enter your mortgage information to calculate payments</p>
    </div>

    <!-- Property Value and Down Payment -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="form-group">
        <label for="propertyValue" class="form-label">Property Value</label>
        <div class="relative">
          <span class="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-500 font-medium">$</span>
          <input
            id="propertyValue"
            v-model.number="form.propertyValue"
            type="number"
            step="1000"
            min="0"
            class="form-input pl-8"
            placeholder="500,000"
          />
        </div>
      </div>

      <div class="form-group">
        <label for="downPayment" class="form-label">Down Payment</label>
        <div class="relative">
          <span class="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-500 font-medium">$</span>
          <input
            id="downPayment"
            v-model.number="form.downPayment"
            type="number"
            step="1000"
            min="0"
            class="form-input pl-8"
            placeholder="100,000"
          />
        </div>
        <p class="mt-2 text-sm text-gray-600 font-medium">
          {{ downPaymentPercentage }}% of property value
        </p>
      </div>
    </div>

    <!-- Loan Amount Display -->
    <div class="form-group">
      <label class="form-label">Loan Amount (Calculated)</label>
      <div class="metric-card">
        <div class="metric-label">Amount to Finance</div>
        <div class="metric-value">${{ loanAmount.toLocaleString() }}</div>
      </div>
    </div>

    <!-- Interest Rate and Term -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="form-group">
        <label for="interestRate" class="form-label">Annual Interest Rate</label>
        <div class="relative">
          <input
            id="interestRate"
            v-model.number="form.interestRate"
            type="number"
            step="0.01"
            min="0"
            max="30"
            class="form-input pr-12"
            placeholder="6.50"
            required
          />
          <span class="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-500 font-medium">%</span>
        </div>
      </div>

      <div class="form-group">
        <label for="loanTermYears" class="form-label">Loan Term</label>
        <div class="relative">
          <input
            id="loanTermYears"
            v-model.number="form.loanTermYears"
            type="number"
            min="1"
            max="50"
            class="form-input pr-20"
            placeholder="30"
            required
          />
          <span class="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-500 font-medium">years</span>
        </div>
      </div>
    </div>

    <!-- Mortgage Type -->
    <div class="form-group">
      <label class="form-label">Mortgage Type</label>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <label 
          v-for="type in mortgageTypes" 
          :key="type.value"
          class="relative flex cursor-pointer rounded-xl border p-5 focus:outline-none transition-all duration-200 hover:shadow-md"
          :class="form.mortgageType === type.value ? 'border-primary-600 bg-primary-50 shadow-md' : 'border-gray-300 hover:border-gray-400'"
        >
          <input
            v-model="form.mortgageType"
            :value="type.value"
            type="radio"
            class="sr-only"
          />
          <div class="flex-1">
            <div class="font-medium text-gray-900">{{ type.label }}</div>
            <div class="text-sm text-gray-500">{{ type.description }}</div>
          </div>
          <div 
            v-if="form.mortgageType === type.value"
            class="flex-shrink-0 text-primary-600"
          >
            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
          </div>
        </label>
      </div>
    </div>

    <!-- Payment Frequency -->
    <div class="form-group">
      <label for="paymentFrequency" class="form-label">Payment Frequency</label>
      <select
        id="paymentFrequency"
        v-model="form.paymentFrequency"
        class="form-select"
        required
      >
        <option value="MONTHLY">Monthly (12 payments/year)</option>
        <option value="BI_WEEKLY">Bi-weekly (26 payments/year)</option>
        <option value="WEEKLY">Weekly (52 payments/year)</option>
      </select>
      <p class="mt-2 text-sm text-gray-600">
        More frequent payments can reduce total interest paid
      </p>
    </div>

    <!-- Buydown Options -->
    <div class="form-group">
      <label class="form-label">Rate Buydown Option</label>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <label 
          v-for="option in buydownOptions" 
          :key="option.value"
          class="relative flex cursor-pointer rounded-xl border p-5 focus:outline-none transition-all duration-200 hover:shadow-md"
          :class="form.buydownType === option.value ? 'border-primary-600 bg-primary-50 shadow-md' : 'border-gray-300 hover:border-gray-400'"
        >
          <input
            v-model="form.buydownType"
            :value="option.value"
            type="radio"
            class="sr-only"
          />
          <div class="flex-1">
            <div class="font-medium text-gray-900">{{ option.label }}</div>
            <div class="text-sm text-gray-500">{{ option.description }}</div>
          </div>
          <div 
            v-if="form.buydownType === option.value"
            class="flex-shrink-0 text-primary-600"
          >
            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
          </div>
        </label>
      </div>
      <p class="mt-2 text-sm text-gray-600">
        Buydowns reduce your interest rate for initial years, saving money upfront
      </p>
    </div>

    <!-- Additional Principal Payment -->
    <div class="form-group">
      <label for="additionalPrincipalPayment" class="form-label">Additional Principal Payment</label>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- Payment Amount -->
        <div class="relative">
          <span class="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-500 font-medium">$</span>
          <input
            id="additionalPrincipalPayment"
            v-model.number="form.additionalPrincipalPayment"
            type="number"
            step="10"
            min="0"
            class="form-input pl-8"
            placeholder="0"
          />
        </div>
        
        <!-- Payment Frequency -->
        <div>
          <select
            id="additionalPaymentFrequency"
            v-model="form.additionalPaymentFrequency"
            class="form-input"
            :disabled="!form.additionalPrincipalPayment || form.additionalPrincipalPayment <= 0"
          >
            <option :value="AdditionalPaymentFrequency.MONTHLY">Monthly</option>
            <option :value="AdditionalPaymentFrequency.BI_WEEKLY">Bi-Weekly</option>
            <option :value="AdditionalPaymentFrequency.QUARTERLY">Quarterly</option>
            <option :value="AdditionalPaymentFrequency.SEMI_ANNUALLY">Semi-Annually</option>
            <option :value="AdditionalPaymentFrequency.ANNUALLY">Annually</option>
            <option :value="AdditionalPaymentFrequency.ONE_TIME">One-Time Payment</option>
          </select>
        </div>
      </div>
      
      <div class="mt-2 grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-3">
          <div class="text-blue-800 font-medium">💡 Impact</div>
          <div class="text-blue-600 text-xs">Extra payments reduce loan term and total interest</div>
        </div>
        <div class="bg-green-50 border border-green-200 rounded-lg p-3">
          <div class="text-green-800 font-medium">📅 Frequency</div>
          <div class="text-green-600 text-xs">Choose how often you make extra payments</div>
        </div>
        <div class="bg-purple-50 border border-purple-200 rounded-lg p-3">
          <div class="text-purple-800 font-medium">💰 Optional</div>
          <div class="text-purple-600 text-xs">Leave blank or 0 for standard payments</div>
        </div>
      </div>
    </div>

    <!-- Submit Button -->
    <div class="pt-6">
      <button
        type="submit"
        :disabled="loading || !isFormValid"
        class="w-full btn-primary text-lg py-4 disabled:opacity-50 disabled:cursor-not-allowed transform hover:scale-[1.02] transition-transform duration-200"
      >
        <span v-if="loading" class="flex items-center justify-center">
          <svg class="animate-spin -ml-1 mr-3 h-6 w-6 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          Calculating...
        </span>
        <span v-else class="flex items-center justify-center">
          <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
          </svg>
          Calculate Mortgage
        </span>
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { reactive, computed, watch } from 'vue'
import { MortgageType, PaymentFrequency, BuydownType, AdditionalPaymentFrequency, type MortgageInput } from '@/types/mortgage'

interface Props {
  loading?: boolean
  initialValues?: MortgageInput
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<{
  submit: [input: MortgageInput]
  update: [input: MortgageInput]
}>()

// Flag to prevent reactivity loops
let isUpdatingFromParent = false

const form = reactive({
  propertyValue: props.initialValues?.propertyValue ?? 500000,
  downPayment: props.initialValues?.downPayment ?? 100000,
  interestRate: props.initialValues?.interestRate ?? 6.5,
  loanTermYears: props.initialValues?.loanTermYears ?? 30,
  mortgageType: props.initialValues?.mortgageType ?? MortgageType.FIXED,
  paymentFrequency: props.initialValues?.paymentFrequency ?? PaymentFrequency.MONTHLY,
  buydownType: props.initialValues?.buydownType ?? BuydownType.NONE,
  additionalPrincipalPayment: props.initialValues?.additionalPrincipalPayment ?? 0,
  additionalPaymentFrequency: props.initialValues?.additionalPaymentFrequency ?? AdditionalPaymentFrequency.MONTHLY
})

// Watch for changes in initial values and update form
watch(() => props.initialValues, (newValues) => {
  if (newValues) {
    isUpdatingFromParent = true
    Object.assign(form, {
      propertyValue: newValues.propertyValue,
      downPayment: newValues.downPayment,
      interestRate: newValues.interestRate,
      loanTermYears: newValues.loanTermYears,
      mortgageType: newValues.mortgageType,
      paymentFrequency: newValues.paymentFrequency,
      buydownType: newValues.buydownType,
      additionalPrincipalPayment: newValues.additionalPrincipalPayment,
      additionalPaymentFrequency: newValues.additionalPaymentFrequency
    })
    isUpdatingFromParent = false
  }
}, { deep: true })

// Debounced update function
let updateTimeout: number | null = null
let lastEmittedInput: MortgageInput | null = null

// Watch for form changes and emit updates with debouncing
watch([
  () => form.propertyValue,
  () => form.downPayment, 
  () => form.interestRate,
  () => form.loanTermYears,
  () => form.mortgageType,
  () => form.paymentFrequency,
  () => form.buydownType,
  () => form.additionalPrincipalPayment,
  () => form.additionalPaymentFrequency
], () => {
  // Don't emit updates when updating from parent to prevent reactivity loops
  if (isUpdatingFromParent) return
  
  if (updateTimeout) {
    clearTimeout(updateTimeout)
  }
  
  updateTimeout = window.setTimeout(() => {
    if (isFormValid.value) {
      const input: MortgageInput = {
        loanAmount: loanAmount.value,
        interestRate: form.interestRate,
        loanTermYears: form.loanTermYears,
        downPayment: form.downPayment,
        propertyValue: form.propertyValue,
        mortgageType: form.mortgageType,
        paymentFrequency: form.paymentFrequency,
        buydownType: form.buydownType,
        additionalPrincipalPayment: form.additionalPrincipalPayment,
        additionalPaymentFrequency: form.additionalPaymentFrequency
      }
      
      // Only emit if the input has actually changed
      if (!lastEmittedInput || JSON.stringify(lastEmittedInput) !== JSON.stringify(input)) {
        lastEmittedInput = input
        emit('update', input)
      }
    }
  }, 500) // 500ms debounce
}, { flush: 'post' })

const mortgageTypes = [
  {
    value: MortgageType.FIXED,
    label: 'Fixed Rate',
    description: 'Interest rate stays the same'
  },
  {
    value: MortgageType.VARIABLE,
    label: 'Variable Rate',
    description: 'Interest rate can change'
  },
  {
    value: MortgageType.INTEREST_ONLY,
    label: 'Interest Only',
    description: 'Pay only interest initially'
  }
]

const buydownOptions = [
  {
    value: BuydownType.NONE,
    label: 'No Buydown',
    description: 'Standard fixed rate for entire term'
  },
  {
    value: BuydownType.TWO_ONE,
    label: '2-1 Buydown',
    description: '2% off year 1, 1% off year 2'
  },
  {
    value: BuydownType.THREE_TWO_ONE,
    label: '3-2-1 Buydown',
    description: '3% off year 1, 2% off year 2, 1% off year 3'
  }
]

const loanAmount = computed(() => {
  return Math.max(0, (form.propertyValue || 0) - (form.downPayment || 0))
})

const downPaymentPercentage = computed(() => {
  if (!form.propertyValue || form.propertyValue === 0) return 0
  return Math.round(((form.downPayment || 0) / form.propertyValue) * 100)
})

const isFormValid = computed(() => {
  return form.interestRate > 0 && 
         form.loanTermYears > 0 && 
         loanAmount.value > 0
})

const handleSubmit = () => {
  if (!isFormValid.value) return

  const input: MortgageInput = {
    loanAmount: loanAmount.value,
    interestRate: form.interestRate,
    loanTermYears: form.loanTermYears,
    downPayment: form.downPayment,
    propertyValue: form.propertyValue,
    mortgageType: form.mortgageType,
    paymentFrequency: form.paymentFrequency,
    buydownType: form.buydownType,
    additionalPrincipalPayment: form.additionalPrincipalPayment
  }

  emit('submit', input)
}
</script>