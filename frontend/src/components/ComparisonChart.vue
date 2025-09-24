<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-semibold text-gray-900">Visual Comparison</h2>
      <select 
        v-model="chartType" 
        class="text-sm border-gray-300 rounded-md focus:border-primary-500 focus:ring-primary-500 px-3 py-2"
      >
        <option value="monthly">Monthly Payment</option>
        <option value="totalCost">Total Cost</option>
        <option value="breakdown">Cost Breakdown</option>
        <option value="paymentOverTime">Payment Over Time</option>
      </select>
    </div>

    <!-- Chart Container -->
    <div class="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
      <div class="relative" style="height: 450px;">
        <canvas 
          ref="chartCanvas" 
          class="w-full h-full"
          v-show="!isLoading && chartInstance"
        ></canvas>
        <div 
          v-if="isLoading" 
          class="absolute inset-0 flex items-center justify-center"
        >
          <div class="text-gray-500">Loading chart...</div>
        </div>
        
        <!-- Fallback CSS Chart -->
        <div 
          v-if="!isLoading && !chartInstance" 
          class="absolute inset-0 p-4"
        >
          <div class="mb-4 text-center">
            <h3 class="text-lg font-medium text-gray-900">{{ getChartTitle() }}</h3>
            <p class="text-sm text-gray-600">CSS Fallback Chart</p>
          </div>
          
          <div class="space-y-3 max-h-96 overflow-auto">
            <div v-for="(mortgage, index) in comparison.mortgages" :key="index" class="flex items-center space-x-4">
              <div class="w-20 text-sm font-medium text-gray-700">
                Option {{ index + 1 }}
              </div>
              <div class="flex-1 bg-gray-200 rounded-full h-8 relative">
                <div 
                  :class="getBarColor(index)"
                  class="h-8 rounded-full flex items-center justify-end pr-3 text-white text-sm font-medium"
                  :style="{ width: getBarWidth(mortgage) }"
                >
                  {{ getDisplayValue(mortgage) }}
                </div>
              </div>
            </div>
          </div>
          
          <div class="mt-4 text-center">
            <button @click="createChart" class="text-blue-600 hover:text-blue-800 text-sm">
              Retry Chart.js
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Key Insights -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-green-50 border border-green-200 rounded-lg p-4">
        <div class="text-sm font-medium text-green-800">Best Monthly Payment</div>
        <div class="text-lg font-semibold text-green-900">
          Option {{ bestMonthlyPaymentIndex + 1 }}
        </div>
        <div class="text-sm text-green-600">
          ${{ bestMonthlyPayment.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}/month
        </div>
      </div>

      <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div class="text-sm font-medium text-blue-800">Lowest Total Interest</div>
        <div class="text-lg font-semibold text-blue-900">
          Option {{ bestTotalInterestIndex + 1 }}
        </div>
        <div class="text-sm text-blue-600">
          ${{ bestTotalInterest.toLocaleString(undefined, { minimumFractionDigits: 2 }) }} interest
        </div>
      </div>

      <div class="bg-purple-50 border border-purple-200 rounded-lg p-4">
        <div class="text-sm font-medium text-purple-800">Savings Opportunity</div>
        <div class="text-lg font-semibold text-purple-900">
          ${{ maxSavings.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
        </div>
        <div class="text-sm text-purple-600">
          vs highest cost option
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import {
  Chart,
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  Title,
  Tooltip,
  Legend,
  type ChartConfiguration
} from 'chart.js'
import type { MortgageComparison } from '@/types/mortgage'

// Register Chart.js components
Chart.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  Title,
  Tooltip,
  Legend
)

interface Props {
  comparison: MortgageComparison
}

const props = defineProps<Props>()

const chartCanvas = ref<HTMLCanvasElement | null>(null)
const chartType = ref<'monthly' | 'totalCost' | 'breakdown' | 'paymentOverTime'>('monthly')
const isLoading = ref(true)
let chartInstance: Chart | null = null

const mortgageLabels = computed(() => {
  return props.comparison.mortgages.map((_, index) => `Option ${index + 1}`)
})

const bestMonthlyPayment = computed(() => {
  return Math.min(...props.comparison.mortgages.map(m => m.monthlyPayment))
})

const bestMonthlyPaymentIndex = computed(() => {
  return props.comparison.mortgages.findIndex(m => m.monthlyPayment === bestMonthlyPayment.value)
})

const bestTotalInterest = computed(() => {
  return Math.min(...props.comparison.mortgages.map(m => m.totalInterest))
})

const bestTotalInterestIndex = computed(() => {
  return props.comparison.mortgages.findIndex(m => m.totalInterest === bestTotalInterest.value)
})

const maxSavings = computed(() => {
  const totalCosts = props.comparison.mortgages.map(m => m.summary.totalAmountPaid)
  const minCost = Math.min(...totalCosts)
  const maxCost = Math.max(...totalCosts)
  return maxCost - minCost
})

const colors = [
  { bg: 'rgba(59, 130, 246, 0.8)', border: 'rgb(59, 130, 246)' },
  { bg: 'rgba(16, 185, 129, 0.8)', border: 'rgb(16, 185, 129)' },
  { bg: 'rgba(245, 158, 11, 0.8)', border: 'rgb(245, 158, 11)' },
  { bg: 'rgba(239, 68, 68, 0.8)', border: 'rgb(239, 68, 68)' }
]

const chartData = computed(() => {
  const labels = mortgageLabels.value

  switch (chartType.value) {
    case 'monthly':
      return {
        labels,
        datasets: [
          {
            label: 'Monthly Payment',
            data: props.comparison.mortgages.map(m => m.monthlyPayment),
            backgroundColor: colors.slice(0, props.comparison.mortgages.length).map(c => c.bg),
            borderColor: colors.slice(0, props.comparison.mortgages.length).map(c => c.border),
            borderWidth: 2,
            borderRadius: 4
          }
        ]
      }

    case 'totalCost':
      return {
        labels,
        datasets: [
          {
            label: 'Total Amount Paid',
            data: props.comparison.mortgages.map(m => m.summary.totalAmountPaid),
            backgroundColor: colors.slice(0, props.comparison.mortgages.length).map(c => c.bg),
            borderColor: colors.slice(0, props.comparison.mortgages.length).map(c => c.border),
            borderWidth: 2,
            borderRadius: 4
          }
        ]
      }

    case 'breakdown':
      return {
        labels,
        datasets: [
          {
            label: 'Principal',
            data: props.comparison.mortgages.map(m => m.summary.loanAmount),
            backgroundColor: 'rgba(59, 130, 246, 0.8)',
            borderColor: 'rgb(59, 130, 246)',
            borderWidth: 2,
            borderRadius: 4,
            stack: 'cost'
          },
          {
            label: 'Interest',
            data: props.comparison.mortgages.map(m => m.totalInterest),
            backgroundColor: 'rgba(249, 115, 22, 0.8)',
            borderColor: 'rgb(249, 115, 22)',
            borderWidth: 2,
            borderRadius: 4,
            stack: 'cost'
          }
        ]
      }

    case 'paymentOverTime':
      // Create a line chart showing payment breakdown over years
      const maxYears = Math.max(...props.comparison.mortgages.map(m => m.summary.loanTermYears))
      const yearLabels = Array.from({ length: maxYears }, (_, i) => `Year ${i + 1}`)
      
      return {
        labels: yearLabels,
        datasets: props.comparison.mortgages.map((mortgage, index) => ({
          label: `Option ${index + 1} - Remaining Balance`,
          data: generateRemainingBalanceData(mortgage, maxYears),
          borderColor: colors[index % colors.length].border,
          backgroundColor: colors[index % colors.length].bg,
          borderWidth: 3,
          fill: false,
          tension: 0.1,
          pointRadius: 2,
          pointHoverRadius: 6
        }))
      }

    default:
      return { labels: [], datasets: [] }
  }
})

// Helper function to generate remaining balance data for line chart
const generateRemainingBalanceData = (mortgage: MortgageComparison['mortgages'][0], maxYears: number) => {
  const data: number[] = []
  const schedule = mortgage.amortizationSchedule || []
  
  for (let year = 1; year <= maxYears; year++) {
    // Find the last payment of each year (payment 12, 24, 36, etc.)
    const paymentIndex = year * 12 - 1
    
    if (paymentIndex < schedule.length) {
      data.push(schedule[paymentIndex].remainingBalance || 0)
    } else {
      // Loan is paid off
      data.push(0)
    }
  }
  
  return data
}

const chartConfig = computed((): ChartConfiguration => {
  const isLineChart = chartType.value === 'paymentOverTime'
  
  return {
    type: isLineChart ? 'line' : 'bar',
    data: chartData.value,
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        mode: 'index',
        intersect: false,
      },
      plugins: {
        legend: {
          position: 'top',
          labels: {
            usePointStyle: isLineChart,
            padding: 20
          }
        },
        title: {
          display: true,
          text: getChartTitle(),
          font: {
            size: 16,
            weight: 'bold'
          },
          padding: 20
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          titleColor: 'white',
          bodyColor: 'white',
          borderColor: 'rgba(255, 255, 255, 0.1)',
          borderWidth: 1,
          callbacks: {
            label: (context) => {
              const value = context.parsed.y
              if (chartType.value === 'paymentOverTime') {
                return `${context.dataset.label}: $${value.toLocaleString(undefined, { minimumFractionDigits: 2 })}`
              }
              return `${context.dataset.label}: $${value.toLocaleString(undefined, { minimumFractionDigits: 2 })}`
            }
          }
        }
      },
      scales: {
        x: {
          grid: {
            display: chartType.value === 'paymentOverTime',
            color: 'rgba(0, 0, 0, 0.1)'
          },
          title: {
            display: true,
            text: isLineChart ? 'Years' : 'Mortgage Options',
            font: {
              weight: 'bold'
            }
          }
        },
        y: {
          beginAtZero: true,
          stacked: chartType.value === 'breakdown',
          grid: {
            color: 'rgba(0, 0, 0, 0.1)'
          },
          title: {
            display: true,
            text: getYAxisTitle(),
            font: {
              weight: 'bold'
            }
          },
          ticks: {
            callback: function(value) {
              return '$' + Number(value).toLocaleString()
            }
          }
        }
      },
      elements: {
        bar: {
          borderRadius: 4
        },
        line: {
          borderWidth: 3
        },
        point: {
          radius: 4,
          hoverRadius: 8
        }
      }
    }
  }
})

const getChartTitle = () => {
  switch (chartType.value) {
    case 'monthly':
      return 'Monthly Payment Comparison'
    case 'totalCost':
      return 'Total Cost Comparison'
    case 'breakdown':
      return 'Principal vs Interest Breakdown'
    case 'paymentOverTime':
      return 'Remaining Balance Over Time'
    default:
      return 'Mortgage Comparison'
  }
}

const getYAxisTitle = () => {
  switch (chartType.value) {
    case 'monthly':
      return 'Monthly Payment ($)'
    case 'totalCost':
      return 'Total Amount Paid ($)'
    case 'breakdown':
      return 'Amount ($)'
    case 'paymentOverTime':
      return 'Remaining Balance ($)'
    default:
      return 'Amount ($)'
  }
}

const createChart = () => {
  if (!chartCanvas.value) {
    console.error('Chart canvas not found')
    return
  }

  isLoading.value = true

  try {
    console.log('Creating chart with data:', chartData.value)
    console.log('Chart config:', chartConfig.value)
    
    // Destroy existing chart
    if (chartInstance) {
      chartInstance.destroy()
      chartInstance = null
    }

    // Create new chart
    chartInstance = new Chart(chartCanvas.value, chartConfig.value)
    
    console.log('Chart created successfully:', chartInstance)
    
    // Chart is ready
    setTimeout(() => {
      isLoading.value = false
    }, 100)
  } catch (error) {
    console.error('Error creating chart:', error)
    console.error('Chart data was:', chartData.value)
    console.error('Comparison data:', props.comparison)
    isLoading.value = false
  }
}

const updateChart = () => {
  if (!chartInstance) {
    createChart()
    return
  }

  try {
    isLoading.value = true
    
    // Check if chart type changed, if so, recreate the chart
    const newChartType = chartType.value === 'paymentOverTime' ? 'line' : 'bar'
    const currentChartType = chartInstance.config.type
    
    if (newChartType !== currentChartType) {
      createChart()
      return
    }

    // Update existing chart
    chartInstance.data = chartData.value
    chartInstance.options = chartConfig.value.options || {}
    chartInstance.update('active')
    
    setTimeout(() => {
      isLoading.value = false
    }, 100)
  } catch (error) {
    console.error('Error updating chart:', error)
    createChart() // Fallback to recreating chart
  }
}

onMounted(async () => {
  await nextTick()
  console.log('Component mounted, canvas element:', chartCanvas.value)
  console.log('Comparison data:', props.comparison)
  console.log('Canvas dimensions:', chartCanvas.value?.getBoundingClientRect())
  createChart()
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.destroy()
  }
})

// CSS Fallback Chart Helpers
const getBarColor = (index: number) => {
  const colorClasses = [
    'bg-blue-500',
    'bg-green-500', 
    'bg-orange-500',
    'bg-purple-500',
    'bg-red-500',
    'bg-indigo-500'
  ]
  return colorClasses[index % colorClasses.length]
}

const getBarWidth = (mortgage: MortgageComparison['mortgages'][0]) => {
  let value: number
  let maxValue: number
  
  switch (chartType.value) {
    case 'monthly':
      value = mortgage.monthlyPayment
      maxValue = Math.max(...props.comparison.mortgages.map(m => m.monthlyPayment))
      break
    case 'totalCost':
      value = mortgage.summary.totalAmountPaid
      maxValue = Math.max(...props.comparison.mortgages.map(m => m.summary.totalAmountPaid))
      break
    case 'breakdown':
      value = mortgage.totalInterest
      maxValue = Math.max(...props.comparison.mortgages.map(m => m.totalInterest))
      break
    default:
      value = mortgage.monthlyPayment
      maxValue = Math.max(...props.comparison.mortgages.map(m => m.monthlyPayment))
  }
  
  const percentage = (value / maxValue) * 100
  return `${Math.max(percentage, 10)}%` // Minimum 10% for visibility
}

const getDisplayValue = (mortgage: MortgageComparison['mortgages'][0]) => {
  switch (chartType.value) {
    case 'monthly':
      return '$' + mortgage.monthlyPayment.toLocaleString(undefined, { maximumFractionDigits: 0 })
    case 'totalCost':
      return '$' + mortgage.summary.totalAmountPaid.toLocaleString(undefined, { maximumFractionDigits: 0 })
    case 'breakdown':
      return '$' + mortgage.totalInterest.toLocaleString(undefined, { maximumFractionDigits: 0 })
    default:
      return '$' + mortgage.monthlyPayment.toLocaleString(undefined, { maximumFractionDigits: 0 })
  }
}

watch([chartType, () => props.comparison], () => {
  if (chartInstance) {
    updateChart()
  } else {
    createChart()
  }
}, { deep: true })
</script>