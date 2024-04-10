describe('My First Test', () => {
  it('Visits Google', () => {
    cy.visit('https://www.google.com');
    cy.title().should('include', 'Google');
  });
});
