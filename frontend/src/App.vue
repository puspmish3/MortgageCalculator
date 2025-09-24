<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <nav class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <router-link 
              to="/" 
              class="text-xl font-bold text-primary-600 hover:text-primary-700"
            >
              MortgageCalculator
            </router-link>
          </div>
          <div class="flex items-center space-x-4">
            <router-link 
              to="/" 
              class="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
            >
              Calculator
            </router-link>
            <router-link 
              to="/compare" 
              class="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
            >
              Compare
            </router-link>
          </div>
        </div>
      </div>
    </nav>

    <main class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
// App component with modern navigation
</script>

<style scoped>
.router-link-active {
  @apply text-primary-600 bg-primary-50;
}
</style>
