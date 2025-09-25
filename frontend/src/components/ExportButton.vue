<template>
  <div class="flex items-center space-x-2">
    <Menu as="div" class="relative inline-block text-left">
      <div>
        <MenuButton
          class="inline-flex w-full justify-center gap-x-1.5 rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
          :disabled="isExporting"
        >
          <ArrowDownTrayIcon class="-ml-0.5 h-5 w-5" aria-hidden="true" />
          {{ isExporting ? 'Exporting...' : 'Export' }}
          <ChevronDownIcon class="-mr-1 h-5 w-5 text-gray-400" aria-hidden="true" />
        </MenuButton>
      </div>

      <transition
        enter-active-class="transition ease-out duration-100"
        enter-from-class="transform opacity-0 scale-95"
        enter-to-class="transform opacity-100 scale-100"
        leave-active-class="transition ease-in duration-75"
        leave-from-class="transform opacity-100 scale-100"
        leave-to-class="transform opacity-0 scale-95"
      >
        <MenuItems
          class="absolute right-0 z-10 mt-2 w-56 origin-top-right rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none"
        >
          <div class="py-1">
            <MenuItem v-slot="{ active }">
              <button
                @click="exportPdf"
                :class="[
                  active ? 'bg-gray-100 text-gray-900' : 'text-gray-700',
                  'group flex items-center px-4 py-2 text-sm w-full text-left',
                ]"
                :disabled="isExporting"
              >
                <DocumentIcon
                  class="mr-3 h-5 w-5 text-gray-400 group-hover:text-gray-500"
                  aria-hidden="true"
                />
                Export as PDF
              </button>
            </MenuItem>
            <MenuItem v-slot="{ active }">
              <button
                @click="exportExcel"
                :class="[
                  active ? 'bg-gray-100 text-gray-900' : 'text-gray-700',
                  'group flex items-center px-4 py-2 text-sm w-full text-left',
                ]"
                :disabled="isExporting"
              >
                <TableCellsIcon
                  class="mr-3 h-5 w-5 text-gray-400 group-hover:text-gray-500"
                  aria-hidden="true"
                />
                Export as Excel
              </button>
            </MenuItem>
          </div>
        </MenuItems>
      </transition>
    </Menu>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Menu, MenuButton, MenuItems, MenuItem } from '@headlessui/vue'
import {
  ArrowDownTrayIcon,
  ChevronDownIcon,
  DocumentIcon,
  TableCellsIcon,
} from '@heroicons/vue/24/outline'
import { ExportService } from '@/services/exportService'
import type { MortgageCalculation, MortgageComparison } from '@/types/mortgage'

interface Props {
  data: MortgageCalculation | MortgageComparison
  type: 'mortgage' | 'comparison'
}

const props = defineProps<Props>()

const isExporting = ref(false)

const exportPdf = async () => {
  if (isExporting.value) return

  try {
    isExporting.value = true

    if (props.type === 'mortgage') {
      await ExportService.exportMortgagePdf(props.data as MortgageCalculation)
    } else {
      await ExportService.exportComparisonPdf(props.data as MortgageComparison)
    }
  } catch (error) {
    console.error('Export failed:', error)
    // You might want to show a toast notification here
  } finally {
    isExporting.value = false
  }
}

const exportExcel = async () => {
  if (isExporting.value) return

  try {
    isExporting.value = true

    if (props.type === 'mortgage') {
      await ExportService.exportMortgageExcel(props.data as MortgageCalculation)
    } else {
      await ExportService.exportComparisonExcel(props.data as MortgageComparison)
    }
  } catch (error) {
    console.error('Export failed:', error)
    // You might want to show a toast notification here
  } finally {
    isExporting.value = false
  }
}
</script>
