/*
 * (C) Copyright IBM Corp. 2017, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.persistence.jdbc.dto;

import java.math.BigDecimal;

import net.sovrinhealth.fhir.persistence.exception.FHIRPersistenceException;

/**
 * This class defines the Data Transfer Object representing a row in the X_NUMBER_VALUES tables.
 */
public class NumberParmVal extends ExtractedParameterValue {

    private BigDecimal valueNumber;
    private BigDecimal valueNumberLow;
    private BigDecimal valueNumberHigh;

    /**
     * Public constructor
     */
    public NumberParmVal() {
        super();
    }

    public BigDecimal getValueNumber() {
        return valueNumber;
    }

    public void setValueNumber(BigDecimal valueNumber) {
        this.valueNumber = valueNumber;
    }

    public BigDecimal getValueNumberLow() {
        return valueNumberLow;
    }

    public void setValueNumberLow(BigDecimal valueNumberLow) {
        this.valueNumberLow = valueNumberLow;
    }

    public BigDecimal getValueNumberHigh() {
        return valueNumberHigh;
    }

    public void setValueNumberHigh(BigDecimal valueNumberHigh) {
        this.valueNumberHigh = valueNumberHigh;
    }

    /**
     * We know our type, so we can call the correct method on the visitor
     */
    @Override
    public void accept(ExtractedParameterValueVisitor visitor) throws FHIRPersistenceException {
        visitor.visit(this);
    }

    @Override
    protected int compareToInner(ExtractedParameterValue o) {
        NumberParmVal other = (NumberParmVal) o;
        int retVal;

        BigDecimal thisValueNumber = this.getValueNumber();
        BigDecimal otherValueNumber = other.getValueNumber();
        if (thisValueNumber != null || otherValueNumber != null) {
            if (thisValueNumber == null) {
                return -1;
            } else if (otherValueNumber == null) {
                return 1;
            }
            retVal = thisValueNumber.compareTo(otherValueNumber);
            if (retVal != 0) {
                return retVal;
            }
        }

        BigDecimal thisValueNumberLow = this.getValueNumberLow();
        BigDecimal otherValueNumberLow = other.getValueNumberLow();
        if (thisValueNumberLow != null || otherValueNumberLow != null) {
            if (thisValueNumberLow == null) {
                return -1;
            } else if (otherValueNumberLow == null) {
                return 1;
            }
            retVal = thisValueNumberLow.compareTo(otherValueNumberLow);
            if (retVal != 0) {
                return retVal;
            }
        }

        BigDecimal thisValueNumberHigh = this.getValueNumberHigh();
        BigDecimal otherValueNumberHigh = other.getValueNumberHigh();
        if (thisValueNumberHigh != null || otherValueNumberHigh != null) {
            if (thisValueNumberHigh == null) {
                return -1;
            } else if (otherValueNumberHigh == null) {
                return 1;
            }
            retVal = thisValueNumberHigh.compareTo(otherValueNumberHigh);
            if (retVal != 0) {
                return retVal;
            }
        }

        return 0;
    }
}