import axios from 'axios';
import type { MortgageCalculation, MortgageComparison } from '@/types/mortgage';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 30000
});

export class ExportService {
  /**
   * Export mortgage calculation as PDF
   */
  static async exportMortgagePdf(calculation: MortgageCalculation): Promise<void> {
    try {
      const response = await api.post('/v1/export/mortgage/pdf', calculation, {
        responseType: 'blob',
        headers: {
          'Accept': 'application/pdf'
        }
      });

      this.downloadFile(response.data, `mortgage_calculation_${new Date().toISOString().slice(0, 19)}.pdf`);
    } catch (error) {
      console.error('Error exporting mortgage PDF:', error);
      throw new Error('Failed to export PDF');
    }
  }

  /**
   * Export mortgage calculation as Excel
   */
  static async exportMortgageExcel(calculation: MortgageCalculation): Promise<void> {
    try {
      const response = await api.post('/v1/export/mortgage/excel', calculation, {
        responseType: 'blob',
        headers: {
          'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        }
      });

      this.downloadFile(response.data, `mortgage_calculation_${new Date().toISOString().slice(0, 19)}.xlsx`);
    } catch (error) {
      console.error('Error exporting mortgage Excel:', error);
      throw new Error('Failed to export Excel');
    }
  }

  /**
   * Export mortgage comparison as PDF
   */
  static async exportComparisonPdf(comparison: MortgageComparison): Promise<void> {
    try {
      const response = await api.post('/v1/export/comparison/pdf', comparison, {
        responseType: 'blob',
        headers: {
          'Accept': 'application/pdf'
        }
      });

      this.downloadFile(response.data, `mortgage_comparison_${new Date().toISOString().slice(0, 19)}.pdf`);
    } catch (error) {
      console.error('Error exporting comparison PDF:', error);
      throw new Error('Failed to export PDF');
    }
  }

  /**
   * Export mortgage comparison as Excel
   */
  static async exportComparisonExcel(comparison: MortgageComparison): Promise<void> {
    try {
      const response = await api.post('/v1/export/comparison/excel', comparison, {
        responseType: 'blob',
        headers: {
          'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        }
      });

      this.downloadFile(response.data, `mortgage_comparison_${new Date().toISOString().slice(0, 19)}.xlsx`);
    } catch (error) {
      console.error('Error exporting comparison Excel:', error);
      throw new Error('Failed to export Excel');
    }
  }

  /**
   * Download file blob
   */
  private static downloadFile(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }
}