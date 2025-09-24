import { createRouter, createWebHistory } from 'vue-router'
import CalculatorView from '@/views/CalculatorView.vue'
import CompareView from '@/views/CompareView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'calculator',
      component: CalculatorView,
      meta: {
        title: 'Mortgage Calculator'
      }
    },
    {
      path: '/compare',
      name: 'compare',
      component: CompareView,
      meta: {
        title: 'Compare Mortgages'
      }
    }
  ],
})

// Update page title on route change
router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - MortgageCalculator` : 'MortgageCalculator'
})

export default router
