// Test endpoint for duty vacancy removal
// Use router.get('/duty/find'
// And use router.post('/duty/remove'
describe('Remove buttons Test', () => {
    
    before(() => {
        // Performing this before all the tests hopefully will make the test more stable
        cy.assureDutySlotQqqExists();
    });

    it('Finds and removes a duty vacancy', () => {

        cy.hospital_login()

        // Invoke router.get('/duty/find_by_specialty') with parameter 'qqq', 
        // then take the resulting ID using cy.request
        cy.request('/duty/find_by_specialty?specialty=qqq').then((response) => {
            cy.log('Response:', response);


            const dutySlots = response.body.dutySlots;
            cy.log('Type of dutySlots:', typeof dutySlots);
            
            cy.log('dutySlots:', dutySlots);

            const dutySlot = dutySlots[0];
            cy.log('dutySlot:', dutySlot);

            const dutySlotId = dutySlot._id;
            cy.log('dutySlotId:', dutySlotId);

            //invoke router.post('/duty/remove' with parameter dutySlotId
            cy.request('POST', '/duty/remove', { dutySlotId }).then((response) => {
                cy.log('response:', response);
                expect(response.status).to.eq(200);
            });
        }
        );

        // Check that the duty slot is no longer in the database    
        cy.request('/duty/find_by_specialty?specialty=qqq').then((response) => {
            const dutySlots = response.body.dutySlots;
            expect(dutySlots).to.have.length(0);
        });


        // publish a new duty slot', function() 
        // Visit the page containing the form
        cy.visit('/duty/publish');

        // Fill out the form
        cy.get('#date').type('2024-12-31');
        cy.get('#dutyHours').type('20:00 - 08:00');
        cy.get('#requiredSpecialty').type('qqq');

        // Submit the form
        cy.get('form').submit();


        // Check that the duty slot appearedlonger in the database  
        cy.request('/duty/find_by_specialty?specialty=qqq').then((response) => {
            const dutySlots = response.body.dutySlots;
            expect(dutySlots).to.have.length(1);
        });

    }
    );
});

