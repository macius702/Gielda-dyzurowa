describe('Rename buttons Test', () => {
    it('Changes state of a duty vacancy', () => {

        doctor_login();
        assertHasButtonWithText('Assign')
        cy.visit('/auth/logout');        


        hotel_login()
        assertHasButtonWithText('Waiting');
        cy.visit('/auth/logout');        

        doctor_login();
        assertHasButtonWithText('Assign', true)
        cy.visit('/auth/logout');        

        hotel_login()
        assertHasButtonWithText('Consent', true);
        cy.visit('/auth/logout');        
        

        doctor_login();
        assertHasButtonWithText('Revoke', true)
        cy.visit('/auth/logout');        

        hotel_login()
        assertHasButtonWithText('Waiting');
        cy.visit('/auth/logout');        

    });
});


function assertHasButtonWithText(btnText, shouldClick = false) {
    cy.get('li.list-group-item')
        .filter((index, el) => {
            const $el = Cypress.$(el);
            return (
                $el.find('div.btn').length > 0 &&
                $el.find('.required-specialty').text().trim() === 'qqq'
            );
        })
        .then(($el) => {
            if ($el.length > 0) {
                cy.log('Element found');
                cy.log('Element HTML:', $el.html());
                cy.log('Element text:', $el.text());
                cy.log('Element ID:', $el.attr('id'));
                cy.log('Element class:', $el.attr('class'));

                const $btn = $el.find('div.btn');
                cy.log('Button HTML:', $btn.html());
                cy.log('Button text:', $btn.text());

                // Assert that the button exists
                cy.wrap($btn).should('exist');
                cy.wrap($btn).should('contain.text', btnText);

                // Click the button if shouldClick is true
                if (shouldClick) {
                    $btn.click();
                }

                return cy.wrap($btn);

            } else {
                // Assert that the element does not exist
                cy.get('li.list-group-item').should('not.exist');
            }
        });
}

function doctor_login() {
    cy.visit('/auth/login');
    cy.get('input[name="username"]').type('Doktor2');
    cy.get('input[name="password"]').type('alamakota');
    cy.get('form').submit();
    cy.visit('duty/slots/rendered');
    cy.wait(1000);  
}

function hotel_login() {
    cy.visit('/auth/login');
    cy.get('input[name="username"]').type('H1');
    cy.get('input[name="password"]').type('alamakota');
    cy.get('form').submit();
    cy.visit('duty/slots/rendered');
    cy.wait(1000);  
}  
