// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })

Cypress.Commands.add('hospital_login', () => {
    cy.visit('/auth/login');
    cy.get('input[name="username"]').type('H1');
    cy.get('input[name="password"]').type('alamakota');
    cy.get('form').submit();
    cy.visit('duty/slots/rendered');
    cy.wait(1000);  
});


Cypress.Commands.add('assureDutySlotQqqExists', () => {
    // Check if the duty slot exists
    cy.hospital_login();
    cy.request('/duty/find_by_specialty?specialty=Choroby wewnętrzne').then((response) => {
        const dutySlots = response.body.dutySlots;

        // If the duty slot does not exist, create it
        if (dutySlots.length === 0) {
            // Visit the page containing the form
            cy.visit('/duty/publish');

            // Fill out the form
            cy.get('#requiredSpecialty').select('Choroby wewnętrzne');

            // Submit the form
            cy.get('form').submit();
        }
    });
    cy.visit('/auth/logout');
});
