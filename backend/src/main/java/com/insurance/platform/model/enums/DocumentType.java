package com.insurance.platform.model.enums;

/**
 * Classification of a stored policy document.
 */
public enum DocumentType {
    /** Core policy bond / schedule document. */
    POLICY_DOCUMENT,
    /** Identity proof (passport, licence, ...). */
    ID_PROOF,
    /** Address proof. */
    ADDRESS_PROOF,
    /** Claim-related document. */
    CLAIM,
    /** Payment receipt. */
    RECEIPT,
    /** Renewal notice / confirmation. */
    RENEWAL_NOTICE,
    /** Any other supporting document. */
    OTHER
}
