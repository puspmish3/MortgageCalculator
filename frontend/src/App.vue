<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <!-- Mobile-friendly navigation -->
    <nav class="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <router-link to="/" class="text-lg sm:text-xl font-bold text-primary-600 hover:text-primary-700 truncate">
              MortgageCalculator
            </router-link>
          </div>
          
          <!-- Mobile menu button -->
          <div class="md:hidden flex items-center">
            <button
              @click="mobileMenuOpen = !mobileMenuOpen"
              class="inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500"
            >
              <span class="sr-only">Open main menu</span>
              <svg :class="mobileMenuOpen ? 'hidden' : 'block'" class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
              <svg :class="mobileMenuOpen ? 'block' : 'hidden'" class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <!-- Desktop navigation -->
          <div class="hidden md:flex items-center space-x-4">
            <router-link
              to="/"
              class="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              Calculator
            </router-link>
            <router-link
              to="/compare"
              class="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              Compare
            </router-link>
          </div>
        </div>

        <!-- Mobile menu -->
        <div :class="mobileMenuOpen ? 'block' : 'hidden'" class="md:hidden">
          <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3 border-t border-gray-200">
            <router-link
              to="/"
              @click="mobileMenuOpen = false"
              class="text-gray-600 hover:text-gray-900 block px-3 py-2 rounded-md text-base font-medium transition-colors"
            >
              Calculator
            </router-link>
            <router-link
              to="/compare"
              @click="mobileMenuOpen = false"
              class="text-gray-600 hover:text-gray-900 block px-3 py-2 rounded-md text-base font-medium transition-colors"
            >
              Compare
            </router-link>
          </div>
        </div>
      </div>
    </nav>

    <main class="max-w-7xl mx-auto py-4 px-4 sm:py-6 sm:px-6 lg:px-8">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// Mobile menu state
const mobileMenuOpen = ref(false)
</script>

<style scoped>
.router-link-active {
  @apply text-primary-600 bg-primary-50;
}
</style>
