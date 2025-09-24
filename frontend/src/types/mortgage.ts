export interface MortgageInput {
  loanAmount: number
  interestRate: number
  loanTermYears: number
  downPayment?: number
  propertyValue?: number
  mortgageType: MortgageType
  paymentFrequency: PaymentFrequency
  buydownType?: BuydownType
  additionalPrincipalPayment?: number
  additionalPaymentFrequency?: AdditionalPaymentFrequency
}

export interface AmortizationEntry {
  paymentNumber: number
  paymentDate: string
  principalPayment: number
  interestPayment: number
  additionalPrincipalPayment: number
  regularPayment: number
  totalPayment: number
  remainingBalance: number
  interestRate: number
  interestSaved: number
}

export interface MortgageCalculation {
  monthlyPayment: number
  totalInterest: number
  totalPayments: number
  amortizationSchedule: AmortizationEntry[]
  summary: MortgageSummary
}

export interface MortgageSummary {
  loanAmount: number
  totalInterestPaid: number
  totalAmountPaid: number
  monthlyPayment: number
  interestRate: number
  loanTermYears: number
  paymentFrequency: PaymentFrequency
}

export interface MortgageComparison {
  mortgages: MortgageCalculation[]
  comparisonSummary: {
    bestMonthlyPayment: number
    bestTotalInterest: number
    differences: ComparisonDifference[]
  }
  comparisonId: string
}

export interface ComparisonDifference {
  metric: string
  mortgage1: number
  mortgage2: number
  difference: number
  percentageDifference: number
}

export enum MortgageType {
  FIXED = 'FIXED',
  VARIABLE = 'VARIABLE',
  INTEREST_ONLY = 'INTEREST_ONLY'
}

export enum PaymentFrequency {
  MONTHLY = 'MONTHLY',
  BI_WEEKLY = 'BI_WEEKLY',
  WEEKLY = 'WEEKLY'
}

export enum BuydownType {
  NONE = 'NONE',
  TWO_ONE = 'TWO_ONE',
  THREE_TWO_ONE = 'THREE_TWO_ONE'
}

export enum AdditionalPaymentFrequency {
  MONTHLY = 'MONTHLY',
  BI_WEEKLY = 'BI_WEEKLY',
  QUARTERLY = 'QUARTERLY',
  SEMI_ANNUALLY = 'SEMI_ANNUALLY',
  ANNUALLY = 'ANNUALLY',
  ONE_TIME = 'ONE_TIME'
}

export interface ExportRequest {
  calculationId: string
  format: 'PDF' | 'EXCEL'
  includeChart: boolean
}