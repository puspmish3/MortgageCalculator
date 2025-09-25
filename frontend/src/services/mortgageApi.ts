import axios from 'axios'
import type { 
  MortgageInput, 
  MortgageCalculation, 
  MortgageComparison,
  ExportRequest 
} from '@/types/mortgage'

// Runtime configuration for Azure Container Apps
const getApiBaseUrl = (): string => {
  // Check if we're in Azure Container Apps environment
  if (window.location.hostname.includes('azurecontainerapps.io')) {
    // In Azure, derive backend URL from frontend URL
    const frontendUrl = window.location.origin
    const backendUrl = frontendUrl.replace('frontend.', 'backend.')
    return `${backendUrl}/api`
  }
  
  // For local development, use environment variable or fallback
  return import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
}

const API_BASE_URL = getApiBaseUrl()

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 30000
})

// Request interceptor for logging
apiClient.interceptors.request.use(
  (config) => {
    console.log(`Making ${config.method?.toUpperCase()} request to ${config.url}`)
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

export class MortgageApiService {
  /**
   * Calculate mortgage details and amortization schedule
   */
  static async calculateMortgage(input: MortgageInput): Promise<MortgageCalculation> {
    const response = await apiClient.post<MortgageCalculation>('/mortgage/calculate', input)
    return response.data
  }

  /**
   * Compare multiple mortgages
   */
  static async compareMortgages(mortgages: MortgageInput[]): Promise<MortgageComparison> {
    const response = await apiClient.post<MortgageComparison>('/mortgage/compare', { mortgages })
    return response.data
  }

  /**
   * Export mortgage calculation to PDF
   */
  static async exportToPdf(calculationId: string, includeChart: boolean = true): Promise<Blob> {
    const response = await apiClient.post(
      '/v1/export/mortgage/pdf',
      { calculationId, format: 'PDF', includeChart } as ExportRequest,
      { responseType: 'blob' }
    )
    return response.data
  }

  /**
   * Export mortgage calculation to Excel
   */
  static async exportToExcel(calculationId: string): Promise<Blob> {
    const response = await apiClient.post(
      '/v1/export/mortgage/excel',
      { calculationId, format: 'EXCEL', includeChart: false } as ExportRequest,
      { responseType: 'blob' }
    )
    return response.data
  }

  /**
   * Health check endpoint
   */
  static async healthCheck(): Promise<{ status: string }> {
    const response = await apiClient.get('/health')
    return response.data
  }
}

// Utility functions for file downloads
export const downloadBlob = (blob: Blob, filename: string): void => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

export default MortgageApiService