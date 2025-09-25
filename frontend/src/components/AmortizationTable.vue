<template>
  <div class="space-y-4">
    <div class="flex flex-col space-y-3 sm:flex-row sm:items-center sm:justify-between sm:space-y-0">
      <h2 class="text-lg sm:text-xl font-semibold text-gray-900">Amortization Schedule</h2>
      <div class="flex flex-col space-y-2 sm:flex-row sm:items-center sm:space-y-0 sm:space-x-4">
        <select
          v-model="selectedYear"
          class="text-sm border-gray-300 rounded-md focus:border-primary-500 focus:ring-primary-500 min-h-[44px] px-3"
        >
          <option value="all">All Years</option>
          <option v-for="year in availableYears" :key="year" :value="year">Year {{ year }}</option>
        </select>
        <span class="text-sm text-gray-500 text-center sm:text-left"> {{ filteredSchedule.length }} payments </span>
      </div>
    </div>

    <!-- Legend -->
    <div
      class="flex flex-col space-y-3 sm:flex-row sm:items-center sm:justify-center sm:space-y-0 sm:space-x-6 p-3 sm:p-4 bg-gray-50 rounded-lg border border-gray-200"
    >
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 sm:gap-6">
        <div class="flex items-center space-x-2">
          <div class="w-4 h-2 bg-primary-500 rounded-full flex-shrink-0"></div>
          <span class="text-xs sm:text-sm text-gray-600">Principal Payment</span>
        </div>
        <div class="flex items-center space-x-2">
          <div class="w-4 h-2 bg-green-500 rounded-full flex-shrink-0"></div>
          <span class="text-xs sm:text-sm text-gray-600">Extra Principal</span>
        </div>
        <div class="flex items-center space-x-2">
          <div class="w-4 h-2 bg-orange-500 rounded-full flex-shrink-0"></div>
          <span class="text-xs sm:text-sm text-gray-600">Interest Payment</span>
        </div>
      </div>
      <div class="text-xs text-gray-500 text-center sm:text-left">
        Progress bars show the proportion of each payment type
      </div>
    </div>

    <!-- Summary for selected period -->
    <div
      v-if="selectedYear !== 'all'"
      class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3 sm:gap-4 p-3 sm:p-4 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg border border-blue-200"
    >
      <div class="text-center">
        <div class="text-sm text-gray-600 font-medium">Total Payments</div>
        <div class="text-lg font-bold text-gray-900">
          ${{ yearSummary.totalPayments.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
        </div>
        <div class="text-xs text-gray-500">{{ filteredSchedule.length }} payments</div>
      </div>
      <div class="text-center">
        <div class="text-sm text-gray-600 font-medium">Principal Paid</div>
        <div class="text-lg font-bold text-primary-600">
          ${{ yearSummary.totalPrincipal.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
        </div>
        <div class="text-xs text-primary-500">
          {{ ((yearSummary.totalPrincipal / yearSummary.totalPayments) * 100).toFixed(1) }}% of
          payments
        </div>
      </div>
      <div class="text-center">
        <div class="text-sm text-gray-600 font-medium">Extra Principal</div>
        <div class="text-lg font-bold text-green-600">
          ${{
            yearSummary.totalAdditionalPrincipal.toLocaleString(undefined, {
              minimumFractionDigits: 2,
            })
          }}
        </div>
        <div class="text-xs text-green-500">
          {{
            ((yearSummary.totalAdditionalPrincipal / yearSummary.totalPayments) * 100).toFixed(1)
          }}% of payments
        </div>
      </div>
      <div class="text-center">
        <div class="text-sm text-gray-600 font-medium">Interest Paid</div>
        <div class="text-lg font-bold text-orange-600">
          ${{ yearSummary.totalInterest.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
        </div>
        <div class="text-xs text-orange-500">
          {{ ((yearSummary.totalInterest / yearSummary.totalPayments) * 100).toFixed(1) }}% of
          payments
        </div>
      </div>
      <div class="text-center">
        <div class="text-sm text-gray-600 font-medium">Interest Saved</div>
        <div class="text-lg font-bold text-purple-600">
          ${{ yearSummary.interestSaved.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
        </div>
        <div class="text-xs text-purple-500">Cumulative savings</div>
      </div>
    </div>

    <!-- Overall loan summary -->
    <div
      v-if="selectedYear === 'all'"
      class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-3 sm:gap-4 p-3 sm:p-4 bg-gradient-to-r from-green-50 to-emerald-50 rounded-lg border border-green-200"
    >
      <div class="text-center">
        <div class="text-sm text-gray-600 font-medium">Total Loan Amount</div>
        <div class="text-lg font-bold text-gray-900">
          ${{ totalLoanAmount.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
        </div>
      </div>
      <div class="text-center">
        <div class="text-sm text-gray-600 font-medium">Extra Principal</div>
        <div class="text-lg font-bold text-green-600">
          ${{
            totalAdditionalPrincipalAmount.toLocaleString(undefined, { minimumFractionDigits: 2 })
          }}
        </div>
        <div class="text-xs text-green-500">Total additional payments</div>
      </div>
      <div class="text-center">
        <div class="text-sm text-gray-600 font-medium">Total Interest</div>
        <div class="text-lg font-bold text-orange-600">
          ${{ totalInterestAmount.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
        </div>
        <div class="text-xs text-orange-500">
          {{ ((totalInterestAmount / totalLoanAmount) * 100).toFixed(1) }}% of principal
        </div>
      </div>
      <div class="text-center">
        <div class="text-sm text-gray-600 font-medium">Interest Saved</div>
        <div class="text-lg font-bold text-purple-600">
          ${{ totalInterestSaved.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
        </div>
        <div class="text-xs text-purple-500">From extra payments</div>
      </div>
      <div class="text-center">
        <div class="text-sm text-gray-600 font-medium">Monthly Payment</div>
        <div class="text-lg font-bold text-blue-600">
          ${{ monthlyPayment.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
        </div>
      </div>
      <div class="text-center">
        <div class="text-sm text-gray-600 font-medium">Loan Term</div>
        <div class="text-lg font-bold text-indigo-600">{{ schedule.length }} payments</div>
        <div class="text-xs text-indigo-500">{{ Math.ceil(schedule.length / 12) }} years</div>
      </div>
    </div>

    <!-- Table -->
    <div class="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-300">
          <thead class="bg-gray-50">
            <tr>
              <th
                class="px-3 sm:px-6 py-2 sm:py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Payment #
              </th>
              <th
                class="px-3 sm:px-6 py-2 sm:py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Payment Date
              </th>
              <th
                class="px-3 sm:px-6 py-2 sm:py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Total Payment
              </th>
              <th
                class="px-3 sm:px-6 py-2 sm:py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                <div class="flex items-center justify-end space-x-1">
                  <span class="hidden sm:inline">Principal</span>
                  <span class="sm:hidden">Prin.</span>
                  <div class="w-2 h-2 bg-primary-500 rounded-full"></div>
                </div>
              </th>
              <th
                class="px-3 sm:px-6 py-2 sm:py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                <div class="flex items-center justify-end space-x-1">
                  <span class="hidden sm:inline">Extra Principal</span>
                  <span class="sm:hidden">Extra</span>
                  <div class="w-2 h-2 bg-green-500 rounded-full"></div>
                </div>
              </th>
              <th
                class="px-3 sm:px-6 py-2 sm:py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                <div class="flex items-center justify-end space-x-1">
                  <span>Interest</span>
                  <div class="w-2 h-2 bg-orange-500 rounded-full"></div>
                </div>
              </th>
              <th
                class="px-3 sm:px-6 py-2 sm:py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                <span class="hidden sm:inline">Interest Rate</span>
                <span class="sm:hidden">Rate</span>
              </th>
              <th
                class="px-3 sm:px-6 py-2 sm:py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                <span class="hidden sm:inline">Remaining Balance</span>
                <span class="sm:hidden">Balance</span>
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr
              v-for="(payment, index) in paginatedSchedule"
              :key="payment.paymentNumber"
              :class="index % 2 === 0 ? 'bg-white' : 'bg-gray-50'"
            >
              <td class="px-3 sm:px-6 py-2 sm:py-4 whitespace-nowrap text-xs sm:text-sm font-medium text-gray-900">
                {{ payment.paymentNumber }}
              </td>
              <td class="px-3 sm:px-6 py-2 sm:py-4 whitespace-nowrap text-xs sm:text-sm text-gray-500">
                <span class="hidden sm:inline">{{ formatDate(payment.paymentDate) }}</span>
                <span class="sm:hidden">{{ formatDate(payment.paymentDate).split(' ').slice(0, 2).join(' ') }}</span>
              </td>
              <td class="px-3 sm:px-6 py-2 sm:py-4 whitespace-nowrap text-xs sm:text-sm text-gray-900 text-right font-medium">
                ${{ payment.totalPayment.toLocaleString(undefined, { minimumFractionDigits: 0 }) }}
              </td>
              <td class="px-3 sm:px-6 py-2 sm:py-4 whitespace-nowrap text-xs sm:text-sm text-right font-medium">
                <div class="flex items-center justify-end space-x-1 sm:space-x-2">
                  <div class="hidden sm:block flex-1 bg-gray-200 rounded-full h-2 max-w-[60px]">
                    <div
                      class="bg-primary-500 h-2 rounded-full"
                      :style="{ width: getPrincipalPercentage(payment) + '%' }"
                    ></div>
                  </div>
                  <span class="text-primary-600">
                    ${{
                      payment.principalPayment.toLocaleString(undefined, {
                        minimumFractionDigits: 0,
                      })
                    }}
                  </span>
                </div>
              </td>
              <td class="px-3 sm:px-6 py-2 sm:py-4 whitespace-nowrap text-xs sm:text-sm text-right font-medium">
                <div class="flex items-center justify-end space-x-1 sm:space-x-2">
                  <div
                    v-if="payment.additionalPrincipalPayment > 0"
                    class="hidden sm:block flex-1 bg-gray-200 rounded-full h-2 max-w-[60px]"
                  >
                    <div
                      class="bg-green-500 h-2 rounded-full"
                      :style="{ width: getAdditionalPrincipalPercentage(payment) + '%' }"
                    ></div>
                  </div>
                  <span
                    class="text-green-600"
                    :class="
                      payment.additionalPrincipalPayment > 0 ? 'font-semibold' : 'text-gray-400'
                    "
                  >
                    ${{
                      payment.additionalPrincipalPayment.toLocaleString(undefined, {
                        minimumFractionDigits: 0,
                      })
                    }}
                  </span>
                </div>
              </td>
              <td class="px-3 sm:px-6 py-2 sm:py-4 whitespace-nowrap text-xs sm:text-sm text-right font-medium">
                <div class="flex items-center justify-end space-x-1 sm:space-x-2">
                  <div class="hidden sm:block flex-1 bg-gray-200 rounded-full h-2 max-w-[60px]">
                    <div
                      class="bg-orange-500 h-2 rounded-full"
                      :style="{ width: getInterestPercentage(payment) + '%' }"
                    ></div>
                  </div>
                  <span class="text-orange-600">
                    ${{
                      payment.interestPayment.toLocaleString(undefined, {
                        minimumFractionDigits: 0,
                      })
                    }}
                  </span>
                </div>
              </td>
              <td class="px-3 sm:px-6 py-2 sm:py-4 whitespace-nowrap text-xs sm:text-sm text-gray-900 text-right font-medium">
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :class="getRateChangeClass(payment)"
                >
                  {{ payment.interestRate.toFixed(2) }}%
                </span>
              </td>
              <td class="px-3 sm:px-6 py-2 sm:py-4 whitespace-nowrap text-xs sm:text-sm text-gray-900 text-right font-medium">
                ${{
                  payment.remainingBalance.toLocaleString(undefined, { minimumFractionDigits: 0 })
                }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Pagination -->
    <div
      class="flex items-center justify-between border-t border-gray-200 bg-white px-4 py-3 sm:px-6"
    >
      <div class="flex flex-1 justify-between sm:hidden">
        <button
          @click="previousPage"
          :disabled="currentPage === 1"
          class="relative inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
        >
          Previous
        </button>
        <button
          @click="nextPage"
          :disabled="currentPage === totalPages"
          class="relative ml-3 inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
        >
          Next
        </button>
      </div>
      <div class="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
        <div>
          <p class="text-sm text-gray-700">
            Showing <span class="font-medium">{{ startIndex + 1 }}</span> to
            <span class="font-medium">{{ Math.min(endIndex, filteredSchedule.length) }}</span> of
            <span class="font-medium">{{ filteredSchedule.length }}</span> payments
          </p>
        </div>
        <div>
          <nav class="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
            <button
              @click="previousPage"
              :disabled="currentPage === 1"
              class="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
            >
              <span class="sr-only">Previous</span>
              <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path
                  fill-rule="evenodd"
                  d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z"
                  clip-rule="evenodd"
                />
              </svg>
            </button>

            <span v-for="page in visiblePages" :key="page">
              <button
                v-if="page !== '...'"
                @click="goToPage(page as number)"
                :class="
                  page === currentPage
                    ? 'relative z-10 inline-flex items-center bg-primary-600 px-4 py-2 text-sm font-semibold text-white focus:z-20 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600'
                    : 'relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0'
                "
              >
                {{ page }}
              </button>
              <span
                v-else
                class="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-700 ring-1 ring-inset ring-gray-300 focus:outline-offset-0"
              >
                ...
              </span>
            </span>

            <button
              @click="nextPage"
              :disabled="currentPage === totalPages"
              class="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
            >
              <span class="sr-only">Next</span>
              <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path
                  fill-rule="evenodd"
                  d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z"
                  clip-rule="evenodd"
                />
              </svg>
            </button>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { AmortizationEntry } from '@/types/mortgage'

interface Props {
  schedule: AmortizationEntry[]
}

const props = defineProps<Props>()

const selectedYear = ref<string | number>('all')
const currentPage = ref(1)
const itemsPerPage = 12 // Show one year of monthly payments by default

const availableYears = computed(() => {
  const years = new Set<number>()
  props.schedule.forEach((entry) => {
    const year = Math.ceil(entry.paymentNumber / 12)
    years.add(year)
  })
  return Array.from(years).sort((a, b) => a - b)
})

const filteredSchedule = computed(() => {
  if (selectedYear.value === 'all') {
    return props.schedule
  }

  const year = selectedYear.value as number
  const startPayment = (year - 1) * 12 + 1
  const endPayment = year * 12

  return props.schedule.filter(
    (entry) => entry.paymentNumber >= startPayment && entry.paymentNumber <= endPayment,
  )
})

const totalPages = computed(() => {
  return Math.ceil(filteredSchedule.value.length / itemsPerPage)
})

const startIndex = computed(() => (currentPage.value - 1) * itemsPerPage)
const endIndex = computed(() => startIndex.value + itemsPerPage)

const paginatedSchedule = computed(() => {
  return filteredSchedule.value.slice(startIndex.value, endIndex.value)
})

const visiblePages = computed(() => {
  const pages: (number | string)[] = []
  const total = totalPages.value
  const current = currentPage.value

  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 3) {
      pages.push(1)
      pages.push('...')
      for (let i = total - 4; i <= total; i++) {
        pages.push(i)
      }
    } else {
      pages.push(1)
      pages.push('...')
      for (let i = current - 1; i <= current + 1; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    }
  }

  return pages
})

const yearSummary = computed(() => {
  if (selectedYear.value === 'all') {
    return {
      totalPayments: 0,
      totalPrincipal: 0,
      totalAdditionalPrincipal: 0,
      totalInterest: 0,
      balanceReduction: 0,
      interestSaved: 0,
    }
  }

  const yearPayments = filteredSchedule.value
  const totalPayments = yearPayments.reduce((sum, p) => sum + p.totalPayment, 0)
  const totalPrincipal = yearPayments.reduce((sum, p) => sum + p.principalPayment, 0)
  const totalAdditionalPrincipal = yearPayments.reduce(
    (sum, p) => sum + p.additionalPrincipalPayment,
    0,
  )
  const totalInterest = yearPayments.reduce((sum, p) => sum + p.interestPayment, 0)
  const interestSaved = yearPayments[yearPayments.length - 1]?.interestSaved || 0

  const startBalance =
    yearPayments[0]?.remainingBalance +
      yearPayments[0]?.principalPayment +
      yearPayments[0]?.additionalPrincipalPayment || 0
  const endBalance = yearPayments[yearPayments.length - 1]?.remainingBalance || 0
  const balanceReduction = startBalance - endBalance

  return {
    totalPayments,
    totalPrincipal,
    totalAdditionalPrincipal,
    totalInterest,
    balanceReduction,
    interestSaved,
  }
})

// Overall loan summary calculations
const totalLoanAmount = computed(() => {
  if (props.schedule.length === 0) return 0
  return props.schedule[0].remainingBalance + props.schedule[0].principalPayment
})

const totalAdditionalPrincipalAmount = computed(() => {
  return props.schedule.reduce((sum, payment) => sum + payment.additionalPrincipalPayment, 0)
})

const totalInterestAmount = computed(() => {
  return props.schedule.reduce((sum, payment) => sum + payment.interestPayment, 0)
})

const totalInterestSaved = computed(() => {
  return props.schedule.length > 0 ? props.schedule[props.schedule.length - 1].interestSaved : 0
})

const monthlyPayment = computed(() => {
  return props.schedule.length > 0 ? props.schedule[0].regularPayment : 0
})

const getPrincipalPercentage = (payment: AmortizationEntry): number => {
  return (payment.principalPayment / payment.totalPayment) * 100
}

const getAdditionalPrincipalPercentage = (payment: AmortizationEntry): number => {
  return (payment.additionalPrincipalPayment / payment.totalPayment) * 100
}

const getInterestPercentage = (payment: AmortizationEntry): number => {
  return (payment.interestPayment / payment.totalPayment) * 100
}

const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

const previousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const goToPage = (page: number) => {
  currentPage.value = page
}

// Reset to first page when filter changes
const resetPagination = () => {
  currentPage.value = 1
}

const getRateChangeClass = (payment: AmortizationEntry): string => {
  if (props.schedule.length === 0) return 'bg-gray-100 text-gray-800'

  // Find the baseline rate (rate from the last payment, assuming it's the permanent rate)
  const baselineRate =
    props.schedule[props.schedule.length - 1]?.interestRate || payment.interestRate

  // If this payment has a different rate than baseline, it's likely a buydown period
  if (Math.abs(payment.interestRate - baselineRate) > 0.01) {
    return 'bg-green-100 text-green-800' // Reduced rate (buydown)
  }

  return 'bg-gray-100 text-gray-800' // Standard rate
}

// Watch for filter changes and schedule updates
import { watch } from 'vue'
watch(selectedYear, resetPagination)

// Reset component state when schedule changes
watch(
  () => props.schedule,
  (newSchedule, oldSchedule) => {
    console.log('AmortizationTable: Schedule changed', {
      newLength: newSchedule?.length,
      oldLength: oldSchedule?.length,
      newFirstRate: newSchedule?.[0]?.interestRate,
      oldFirstRate: oldSchedule?.[0]?.interestRate,
    })
    selectedYear.value = 'all'
    currentPage.value = 1
  },
  { deep: true },
)
</script>
