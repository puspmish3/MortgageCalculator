<template>
  <div class="space-y-8">
    <!-- Header -->
    <div class="text-center">
      <h2 class="section-subheader">Your Mortgage Calculation</h2>
      <p class="text-gray-600">Here's your detailed mortgage breakdown</p>
    </div>

    <!-- Key Metrics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div class="metric-card bg-gradient-to-br from-primary-50 to-primary-100 border-primary-200">
        <div class="metric-label text-primary-700">Monthly Payment</div>
        <div class="metric-value text-primary-900">
          ${{ calculation.monthlyPayment.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
        </div>
      </div>

      <div class="metric-card bg-gradient-to-br from-orange-50 to-orange-100 border-orange-200">
        <div class="metric-label text-orange-700">Total Interest</div>
        <div class="metric-value text-orange-900">
          ${{ calculation.totalInterest.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
        </div>
      </div>

      <div class="metric-card bg-gradient-to-br from-green-50 to-green-100 border-green-200">
        <div class="metric-label text-green-700">Total Amount Paid</div>
        <div class="metric-value text-green-900">
          ${{ calculation.summary.totalAmountPaid.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
        </div>
      </div>
    </div>

    <!-- Detailed Summary -->
    <div class="card">
      <div class="flex justify-between items-center mb-6 pb-4 border-b border-gray-200">
        <h3 class="section-subheader mb-0">Loan Summary</h3>
        <ExportButton :data="calculation" type="mortgage" />
      </div>
        <dl class="grid grid-cols-1 gap-x-4 gap-y-3 sm:grid-cols-2">
          <div>
            <dt class="text-sm font-medium text-gray-500">Loan Amount</dt>
            <dd class="mt-1 text-sm text-gray-900 font-semibold">
              ${{ calculation.summary.loanAmount.toLocaleString() }}
            </dd>
          </div>
          <div>
            <dt class="text-sm font-medium text-gray-500">Interest Rate</dt>
            <dd class="mt-1 text-sm text-gray-900 font-semibold">
              {{ calculation.summary.interestRate }}%
            </dd>
          </div>
          <div>
            <dt class="text-sm font-medium text-gray-500">Loan Term</dt>
            <dd class="mt-1 text-sm text-gray-900 font-semibold">
              {{ calculation.summary.loanTermYears }} years
            </dd>
          </div>
          <div>
            <dt class="text-sm font-medium text-gray-500">Payment Frequency</dt>
            <dd class="mt-1 text-sm text-gray-900 font-semibold">
              {{ formatPaymentFrequency(calculation.summary.paymentFrequency) }}
            </dd>
          </div>
          <div>
            <dt class="text-sm font-medium text-gray-500">Total Amount Paid</dt>
            <dd class="mt-1 text-sm text-gray-900 font-semibold">
              ${{ calculation.summary.totalAmountPaid.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
            </dd>
          </div>
          <div>
            <dt class="text-sm font-medium text-gray-500">Total Payments</dt>
            <dd class="mt-1 text-sm text-gray-900 font-semibold">
              {{ calculation.totalPayments }}
            </dd>
          </div>
        </dl>
    </div>

    <!-- Interest vs Principal Breakdown -->
    <div class="card">
      <h3 class="section-subheader mb-6">Payment Breakdown</h3>
      <div class="space-y-3">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-600">Principal</span>
          <div class="flex items-center">
            <div class="w-32 bg-gray-200 rounded-full h-2 mr-3">
              <div 
                class="bg-primary-600 h-2 rounded-full" 
                :style="{ width: principalPercentage + '%' }"
              ></div>
            </div>
            <span class="text-sm font-medium text-gray-900 w-16 text-right">
              {{ principalPercentage.toFixed(1) }}%
            </span>
          </div>
        </div>
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-600">Interest</span>
          <div class="flex items-center">
            <div class="w-32 bg-gray-200 rounded-full h-2 mr-3">
              <div 
                class="bg-orange-500 h-2 rounded-full" 
                :style="{ width: interestPercentage + '%' }"
              ></div>
            </div>
            <span class="text-sm font-medium text-gray-900 w-16 text-right">
              {{ interestPercentage.toFixed(1) }}%
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Export Buttons -->
    <div class="flex flex-col sm:flex-row gap-3">
      <button
        @click="$emit('exportPdf')"
        class="flex-1 btn-primary flex items-center justify-center"
        type="button"
      >
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        Export PDF
      </button>
      
      <button
        @click="$emit('exportExcel')"
        class="flex-1 btn-secondary flex items-center justify-center"
        type="button"
      >
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        Export Excel
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { MortgageCalculation, PaymentFrequency } from '@/types/mortgage'
import ExportButton from './ExportButton.vue'

interface Props {
  calculation: MortgageCalculation
}

const props = defineProps<Props>()

defineEmits<{
  exportPdf: []
  exportExcel: []
}>()

const principalPercentage = computed(() => {
  const total = props.calculation.summary.totalAmountPaid
  const principal = props.calculation.summary.loanAmount
  return (principal / total) * 100
})

const interestPercentage = computed(() => {
  const total = props.calculation.summary.totalAmountPaid
  const interest = props.calculation.totalInterest
  return (interest / total) * 100
})

const formatPaymentFrequency = (frequency: PaymentFrequency): string => {
  switch (frequency) {
    case 'MONTHLY':
      return 'Monthly'
    case 'BI_WEEKLY':
      return 'Bi-weekly'
    case 'WEEKLY':
      return 'Weekly'
    default:
      return frequency
  }
}
</script>