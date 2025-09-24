<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-semibold text-gray-900">Payment Breakdown Chart</h2>
      <div class="flex items-center space-x-4">
        <select 
          v-model="chartType" 
          class="text-sm border-gray-300 rounded-md focus:border-primary-500 focus:ring-primary-500"
        >
          <option value="monthly">Monthly Breakdown</option>
          <option value="cumulative">Cumulative Total</option>
          <option value="balance">Remaining Balance</option>
        </select>
      </div>
    </div>

    <!-- Chart Container -->
    <div class="bg-white border border-gray-200 rounded-lg p-6">
      <div style="height: 400px;">
        <canvas ref="chartCanvas"></canvas>
      </div>
    </div>

    <!-- Chart Legend -->
    <div class="flex flex-wrap justify-center gap-4 text-sm">
      <div v-if="chartType === 'monthly' || chartType === 'cumulative'" class="flex items-center">
        <div class="w-4 h-4 bg-primary-600 rounded mr-2"></div>
        <span>Principal</span>
      </div>
      <div v-if="chartType === 'monthly' || chartType === 'cumulative'" class="flex items-center">
        <div class="w-4 h-4 bg-orange-500 rounded mr-2"></div>
        <span>Interest</span>
      </div>
      <div v-if="chartType === 'balance'" class="flex items-center">
        <div class="w-4 h-4 bg-red-500 rounded mr-2"></div>
        <span>Remaining Balance</span>
      </div>
    </div>

    <!-- Chart Summary -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-primary-50 border border-primary-200 rounded-lg p-4 text-center">
        <div class="text-lg font-semibold text-primary-900">
          ${{ totalPrincipal.toLocaleString(undefined, { minimumFractionDigits: 0 }) }}
        </div>
        <div class="text-sm text-primary-600">Total Principal</div>
      </div>
      <div class="bg-orange-50 border border-orange-200 rounded-lg p-4 text-center">
        <div class="text-lg font-semibold text-orange-900">
          ${{ totalInterest.toLocaleString(undefined, { minimumFractionDigits: 0 }) }}
        </div>
        <div class="text-sm text-orange-600">Total Interest</div>
      </div>
      <div class="bg-gray-50 border border-gray-200 rounded-lg p-4 text-center">
        <div class="text-lg font-semibold text-gray-900">
          {{ schedule.length }}
        </div>
        <div class="text-sm text-gray-600">Total Payments</div>
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
import type { AmortizationEntry } from '@/types/mortgage'

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
  schedule: AmortizationEntry[]
}

const props = defineProps<Props>()

const chartCanvas = ref<HTMLCanvasElement | null>(null)
const chartType = ref<'monthly' | 'cumulative' | 'balance'>('monthly')
let chartInstance: Chart | null = null

const totalPrincipal = computed(() => {
  return props.schedule.reduce((sum, entry) => sum + entry.principalPayment, 0)
})

const totalInterest = computed(() => {
  return props.schedule.reduce((sum, entry) => sum + entry.interestPayment, 0)
})

const chartData = computed(() => {
  const maxDataPoints = 360 // Show max 30 years of monthly data
  let dataPoints = props.schedule
  
  // Sample data if there are too many points
  if (dataPoints.length > maxDataPoints) {
    const step = Math.ceil(dataPoints.length / maxDataPoints)
    dataPoints = dataPoints.filter((_, index) => index % step === 0)
  }

  const labels = dataPoints.map((entry, index) => {
    if (dataPoints.length <= 60) {
      // Show payment numbers for smaller datasets
      return `Payment ${entry.paymentNumber}`
    } else {
      // Show years for larger datasets
      const year = Math.ceil(entry.paymentNumber / 12)
      return index % 12 === 0 ? `Year ${year}` : ''
    }
  })

  switch (chartType.value) {
    case 'monthly':
      return {
        labels,
        datasets: [
          {
            label: 'Principal',
            data: dataPoints.map(entry => entry.principalPayment),
            backgroundColor: 'rgb(59, 130, 246)',
            borderColor: 'rgb(59, 130, 246)',
            stack: 'payment'
          },
          {
            label: 'Interest',
            data: dataPoints.map(entry => entry.interestPayment),
            backgroundColor: 'rgb(249, 115, 22)',
            borderColor: 'rgb(249, 115, 22)',
            stack: 'payment'
          }
        ]
      }

    case 'cumulative':
      let cumulativePrincipal = 0
      let cumulativeInterest = 0
      
      return {
        labels,
        datasets: [
          {
            label: 'Cumulative Principal',
            data: dataPoints.map(entry => {
              cumulativePrincipal += entry.principalPayment
              return cumulativePrincipal
            }),
            backgroundColor: 'rgba(59, 130, 246, 0.7)',
            borderColor: 'rgb(59, 130, 246)',
            fill: true
          },
          {
            label: 'Cumulative Interest',
            data: dataPoints.map(entry => {
              cumulativeInterest += entry.interestPayment
              return cumulativeInterest
            }),
            backgroundColor: 'rgba(249, 115, 22, 0.7)',
            borderColor: 'rgb(249, 115, 22)',
            fill: true
          }
        ]
      }

    case 'balance':
      return {
        labels,
        datasets: [
          {
            label: 'Remaining Balance',
            data: dataPoints.map(entry => entry.remainingBalance),
            backgroundColor: 'rgba(239, 68, 68, 0.1)',
            borderColor: 'rgb(239, 68, 68)',
            borderWidth: 2,
            fill: true,
            tension: 0.4
          }
        ]
      }

    default:
      return { labels: [], datasets: [] }
  }
})

const chartConfig = computed((): ChartConfiguration => {
  const baseConfig: ChartConfiguration = {
    type: chartType.value === 'balance' ? 'line' : 'bar',
    data: chartData.value,
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        intersect: false,
        mode: 'index'
      },
      plugins: {
        legend: {
          position: 'top'
        },
        tooltip: {
          callbacks: {
            label: (context) => {
              const value = context.parsed.y
              return `${context.dataset.label}: $${value.toLocaleString(undefined, { minimumFractionDigits: 2 })}`
            }
          }
        }
      },
      scales: {
        x: {
          grid: {
            display: false
          },
          ticks: {
            maxTicksLimit: 12
          }
        },
        y: {
          beginAtZero: true,
          ticks: {
            callback: function(value) {
              return '$' + Number(value).toLocaleString()
            }
          }
        }
      }
    }
  }

  if (chartType.value === 'monthly') {
    baseConfig.options!.scales!.x!.stacked = true
    baseConfig.options!.scales!.y!.stacked = true
  }

  return baseConfig
})

const createChart = () => {
  if (!chartCanvas.value) return

  // Destroy existing chart
  if (chartInstance) {
    chartInstance.destroy()
  }

  // Create new chart
  chartInstance = new Chart(chartCanvas.value, chartConfig.value)
}

const updateChart = () => {
  if (!chartInstance) return

  chartInstance.data = chartData.value
  chartInstance.config.type = chartType.value === 'balance' ? 'line' : 'bar'
  chartInstance.update('none')
}

onMounted(async () => {
  await nextTick()
  createChart()
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.destroy()
  }
})

watch([chartType, () => props.schedule], () => {
  if (chartInstance) {
    updateChart()
  } else {
    createChart()
  }
}, { deep: true })
</script>