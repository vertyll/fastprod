package com.vertyll.fastprod.base.ui.component;

/**
 * The signed-in person as the layout's user menu shows them. The layout owns this contract and the module that keeps
 * accounts implements it, so the layout depends on no feature module.
 */
@FunctionalInterface
public interface SignedInPerson {

    /**
     * The person signed in now.
     *
     * @throws com.vertyll.fastprod.shared.exception.ApiException when the back-end cannot answer
     */
    Profile current();

    record Profile(String fullName, String email) {
    }
}
