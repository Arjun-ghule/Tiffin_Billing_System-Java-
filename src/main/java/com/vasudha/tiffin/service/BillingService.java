package com.vasudha.tiffin.service;

import com.vasudha.tiffin.model.Bill;

/**
 * Service class handling tiffin billing logic, validations, and calculations.
 */
public class BillingService {

    /**
     * Validates that both pending and advance amounts are not entered at the same time.
     * @param pending pending amount
     * @param advance advance amount
     * @throws IllegalArgumentException if both are greater than zero
     */
    public void validateAdjustments(double pending, double advance) throws IllegalArgumentException {
        if (pending > 0 && advance > 0) {
            throw new IllegalArgumentException(
                "Cannot have both Pending and Advance amounts at the same time. Please enter one or the other."
            );
        }
    }

    /**
     * Calculates the subtotal for the bill.
     */
    public double calculateSubtotal(int morningCount, double morningRate, int nightCount, double nightRate) {
        if (morningCount < 0 || morningRate < 0 || nightCount < 0 || nightRate < 0) {
            throw new IllegalArgumentException("Counts and rates must be non-negative.");
        }
        return (morningCount * morningRate) + (nightCount * nightRate);
    }

    /**
     * Calculates the final total based on subtotal, pending, and advance amounts.
     */
    public double calculateFinalTotal(double subtotal, double pending, double advance) {
        if (pending < 0 || advance < 0) {
            throw new IllegalArgumentException("Pending and Advance amounts must be non-negative.");
        }
        return subtotal + pending - advance;
    }

    /**
     * Determines the billing status based on pending and advance adjustments.
     */
    public String determineInitialStatus(double pending, double advance) {
        if (pending > 0) {
            return "UNPAID";
        } else if (advance > 0) {
            return "PAID (Advance)";
        } else {
            return "UNPAID";
        }
    }

    /**
     * Populates calculations and determines initial status for a Bill.
     */
    public void processBillCalculations(Bill bill) {
        validateAdjustments(bill.getPendingAmount(), bill.getAdvanceAmount());
        
        double subtotal = calculateSubtotal(
                bill.getMorningCount(),
                bill.getMorningRate(),
                bill.getNightCount(),
                bill.getNightRate()
        );
        double finalTotal = calculateFinalTotal(subtotal, bill.getPendingAmount(), bill.getAdvanceAmount());
        String status = determineInitialStatus(bill.getPendingAmount(), bill.getAdvanceAmount());

        bill.setSubtotal(subtotal);
        bill.setFinalTotal(finalTotal);
        bill.setStatus(status);
    }
}
